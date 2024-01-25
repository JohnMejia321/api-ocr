// package com.inner.config;

// import org.apache.kafka.clients.producer.Producer;
// import org.apache.kafka.clients.producer.ProducerConfig;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.kafka.core.DefaultKafkaProducerFactory;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.kafka.core.ProducerFactory;

// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// public class KafkaConfig {

// @Value("${kafka.bootstrapServers}")
// private String bootstrapServers;

// @Bean
// public Producer<String, String> producerFactory() {
// Map<String, Object> configs = new HashMap<>();
// configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
// // Agrega otras configuraciones necesarias

// return new DefaultKafkaProducerFactory<>(configs);
// }

// @Bean
// public KafkaTemplate<String, String> kafkaTemplate() {
// return new KafkaTemplate<>(producerFactory());
// }
// }
