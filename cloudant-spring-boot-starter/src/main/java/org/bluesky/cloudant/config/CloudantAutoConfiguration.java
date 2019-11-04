package org.bluesky.cloudant.config;

import org.bluesky.cloudant.model.CloudantDBManager;
import org.bluesky.cloudant.model.CloudantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CloudantProperties.class)
public class CloudantAutoConfiguration {

    @Autowired
    private CloudantProperties cloudantProperties;

    @Bean
    @ConditionalOnMissingBean
    public CloudantDBManager getCloudantDBManager() {
        return new CloudantDBManager(cloudantProperties);
    }

}
