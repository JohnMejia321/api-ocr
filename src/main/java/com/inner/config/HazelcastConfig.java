package com.inner.config;

import com.hazelcast.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    // private String members;

    // @Bean
    // public Config hazelcastConfig() {
    // return new Config()
    // .setProperty("hazelcast.members", members)
    // // Agrega otras configuraciones necesarias
    // .setInstanceName("hazelcast-instance");
    // }
}
