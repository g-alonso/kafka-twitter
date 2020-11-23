package com.galonso;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import static java.lang.Runtime.getRuntime;

@Slf4j
public class KafkaTwitterConsumer {
    private static final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private static final List<String> topics = List.of("my-topic-word-count");
    private static final int noOfWorkerThreads = 3;

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(noOfWorkerThreads);
        IntStream.range(0, noOfWorkerThreads)
                .forEach(i -> service.execute(getRunnableTask()));

        getRuntime().addShutdownHook(new Thread(() -> {
            shutdownRequested.set(true);

            try {
                service.shutdown();
                service.awaitTermination(30L, TimeUnit.SECONDS);
            } catch (InterruptedException var5) {
                log.error("Termination interrupted when shutting down an executor", var5);
            } finally {
                if (!service.isTerminated()) {
                    log.error("Force killing non-finished executor tasks.");
                }
                service.shutdownNow();
            }
        }));
    }

    static Runnable getRunnableTask() {
        return () -> {
            while (true) {
                try {
                    if (shutdownRequested.get()) {
                        log.info("Shutdown requested for {}. Exiting...");
                        return;
                    }
                    startConsumer();
                } catch (Exception e) {
                    log.error("Error occurred: ", e);
                }
            }
        };
    }

    static void startConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "test-group");

        try (KafkaConsumer consumer = new KafkaConsumer(props)) {
            consumer.subscribe(topics);
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(10);
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Thread: {}, Topic: {}, Partition: {}, Offset: {}, key: {}, value: {}", Thread.currentThread().getName(), record.topic(), record.partition(), record.offset(), record.key(), record.value().toUpperCase());
                }
            }
        } catch (Exception e) {
            log.error("Consumer error", e);
        }
    }
}