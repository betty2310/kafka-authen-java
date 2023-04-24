package com.ftl;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class App {
    public static final String TOPIC = "topic-test";
    public static final String BOOTSTRAP_URL = "localhost:9092";
    public static final String USER = "admin";
    public static final String PASSWORD = "admin";
    public static final String GROUP_ID = "test";
    public static final String SECURITY_PROTOCOL = "SASL_PLAINTEXT";
    public static final String MECHANISM = "PLAIN";

    public static Properties makeProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_URL);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("security.protocol", SECURITY_PROTOCOL);
        props.put("sasl.mechanism", MECHANISM);
        props.put("sasl.jaas.config", String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                USER, PASSWORD));
        return props;
    }

    public static KafkaConsumer<String, String> createConsumer() {
        Properties props = makeProps();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Collections.singleton(TOPIC));
        return consumer;
    }

    public static KafkaProducer<String, String> createProducer() {
        Properties props = makeProps();
        return new KafkaProducer<>(props);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IOException("add argument producer or consumer!");
        }
        if (args[0].equals("consumer")) {
            KafkaConsumer<String, String> consumer = createConsumer();
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.printf("%s\n", record.value());
                    }
                }
            } finally {
                consumer.close();
            }
        } else if (args[0].equals("producer")) {
            KafkaProducer<String, String> producer = createProducer();
            Scanner scanner = new Scanner(System.in);
            String message = "";
            while (!message.equals("exit")) {
                System.out.print(">");
                message = scanner.nextLine();
                if (!message.equals("exit")) {
                    ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);
                    producer.send(record);
                }
            }
            scanner.close();
            producer.close();
        } else {
            throw new IOException("add argument producer or consumer!");
        }
    }
}
