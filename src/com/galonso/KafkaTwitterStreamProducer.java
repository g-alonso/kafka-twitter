
package com.galonso;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.util.Properties;

@Slf4j
public class KafkaTwitterStreamProducer {

    /**
     * @param args String[]
     */
    public static void main(String[] args) {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("X")
                .setOAuthConsumerSecret("X")
                .setOAuthAccessToken("X")
                .setOAuthAccessTokenSecret("X");

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer(props);

        StatusListener listener = new StatusListener(){
            public void onStatus(Status status) {
                System.out.println(status.getUser().getName() + " : " + status.getText());

                ProducerRecord<String, String> message1 = new ProducerRecord<>("my-topic", status.getText());
                producer.send(message1);
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            @Override
            public void onScrubGeo(long l, long l1) { }
            @Override
            public void onStallWarning(StallWarning stallWarning) { }
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);
        FilterQuery query = new FilterQuery();
        query.track(new String[]{ "Argentina"});
        twitterStream.filter(query);
    }
}