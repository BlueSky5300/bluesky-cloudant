package org.bluesky.cloudant.utils;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.CouchDbException;
import com.google.gson.*;
import org.bluesky.cloudant.utils.model.CloudantConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CloudantDBManager {

    private ConcurrentHashMap<String, Database> databases = new ConcurrentHashMap<>();
    private CloudantClient cloudantClient = null;

    public CloudantDBManager() {
        Yaml yaml = new Yaml();
        String DEFAULT_CONFIG_PATH = "cloudant.yml";
        CloudantConfig config = yaml.loadAs(this.getClass().getClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG_PATH), CloudantConfig.class);
        cloudantClient = createClient(config);
    }

    public CloudantDBManager(String configPath) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        File file = new File(configPath);
        CloudantConfig config = yaml.loadAs(new FileInputStream(file), CloudantConfig.class);
        cloudantClient = createClient(config);
    }

    public CloudantDBManager(CloudantConfig config) {
        cloudantClient = createClient(config);
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

    private CloudantClient createClient(CloudantConfig config) {
        String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
        if (null != VCAP_SERVICES) {
            JsonObject obj = (JsonObject) new JsonParser().parse(VCAP_SERVICES);
            Entry<String, JsonElement> dbEntry = null;
            Set<Entry<String, JsonElement>> entries = obj.entrySet();
            for (Entry<String, JsonElement> entry : entries) {
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
            config.username = obj.get("username").toString();
            config.password = obj.get("password").toString();
        }
        try {
            ClientBuilder clientBuilder = ClientBuilder.account(config.username)
                    .username(config.username).password(config.password);
            if (null != config.proxyUrl) {
                clientBuilder = clientBuilder.proxyURL(new URL(config.proxyUrl));
            }
            if (null != config.proxyUser) {
                clientBuilder = clientBuilder.proxyUser(config.proxyUser);
            }
            if (null != config.proxyPassword) {
                clientBuilder = clientBuilder.proxyPassword(config.proxyPassword);
            }
            if (config.connectTimeout > 0) {
                clientBuilder = clientBuilder.connectTimeout(config.connectTimeout, TimeUnit.SECONDS);
            }
            if (config.readTimeout > 0) {
                clientBuilder = clientBuilder.readTimeout(config.readTimeout, TimeUnit.SECONDS);
            }
            if (config.maxConnections > 0) {
                clientBuilder = clientBuilder.maxConnections(config.maxConnections);
            }
            if (config.disableSSLAuthentication) {
                clientBuilder = clientBuilder.disableSSLAuthentication();
            }
            return clientBuilder.build();
        } catch (CouchDbException | MalformedURLException e) {
            throw new RuntimeException("unable to build client", e);
        }
    }

}
