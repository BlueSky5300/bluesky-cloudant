package org.bluesky.cloudant.model;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.CouchDbException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CloudantDBManager {

    private ConcurrentHashMap<String, Database> databases = new ConcurrentHashMap<>();
    private CloudantClient cloudantClient = null;

    public CloudantDBManager(CloudantProperties cloudantProperties) {
        cloudantClient = createClient(cloudantProperties);
    }

    public Database getDatabase(String name, boolean autoCreate) {
        if (null == name) {
            return null;
        }
        Database database = databases.get(name);
        if (null == database) {
            synchronized (this) {
                if (!databases.contains(name)) {
                    try {
                        boolean isPresent = cloudantClient.getAllDbs()
                                .parallelStream().anyMatch(db -> db.equals(name));
                        if (isPresent || autoCreate) {
                            database = cloudantClient.database(name, autoCreate);
                            databases.put(name, database);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return database;
    }

    public void deleteDatabase(String name) {
        cloudantClient.deleteDB(name);
        databases.remove(name);
    }

    public List<String> getDatabases() {
        return cloudantClient.getAllDbs();
    }

    public void createDatabase(String dbName) {
        cloudantClient.createDB(dbName);
    }

    public void createPartitionedDatabase(String dbName) {
        cloudantClient.createPartitionedDB(dbName);
    }

    private CloudantClient createClient(CloudantProperties config) {
        String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
        if (config.isBindService()) {
            if (null == VCAP_SERVICES) {
                throw new RuntimeException("VCAP_SERVICES not found, please set bindService as false in application.yml.");
            }
            JsonObject obj = (JsonObject) new JsonParser().parse(VCAP_SERVICES);
            Map.Entry<String, JsonElement> dbEntry = null;
            Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                if (entry.getKey().toLowerCase().contains("cloudant")) {
                    dbEntry = entry;
                    break;
                }
            }
            if (null == dbEntry) {
                throw new RuntimeException("Could not find cloudant nosql db key in VCAP_SERVICES");
            }
            obj = (JsonObject) ((JsonArray) dbEntry.getValue()).get(0);
            obj = (JsonObject) obj.get("credentials");
            config.setUsername(obj.get("username").toString());
            config.setPassword(obj.get("password").toString());
            config.setUrl(obj.get("url").toString());
        }
        try {
            URL url = new URL(config.getUrl());
            System.out.println(config.getUsername());
            System.out.println(config.getPassword());
            System.out.println(config.getUrl());
//            ClientBuilder clientBuilder = ClientBuilder.url(url)
//                    .username(config.getUsername()).password(config.getPassword());
            ClientBuilder clientBuilder = ClientBuilder.account(config.getUsername())
                    .username(config.getUsername()).password(config.getPassword());
            if (null != config.getProxyUrl()) {
                clientBuilder = clientBuilder.proxyURL(new URL(config.getProxyUrl()));
            }
            if (null != config.getProxyUser()) {
                clientBuilder = clientBuilder.proxyUser(config.getProxyUser());
            }
            if (null != config.getProxyPassword()) {
                clientBuilder = clientBuilder.proxyPassword(config.getProxyPassword());
            }
            if (config.getConnectTimeout() > 0) {
                clientBuilder = clientBuilder.connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS);
            }
            if (config.getReadTimeout() > 0) {
                clientBuilder = clientBuilder.readTimeout(config.getReadTimeout(), TimeUnit.SECONDS);
            }
            if (config.getMaxConnections() > 0) {
                clientBuilder = clientBuilder.maxConnections(config.getMaxConnections());
            }
            if (config.isDisableSSLAuthentication()) {
                clientBuilder = clientBuilder.disableSSLAuthentication();
            }
            return clientBuilder.build();
        } catch (CouchDbException | MalformedURLException e) {
            throw new RuntimeException("unable to build client", e);
        }
    }

}
