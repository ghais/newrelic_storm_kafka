/**
 * (C)2014 Digi-Net Technologies, Inc.
 * 5887 Glenridge Drive
 * Suite 350
 * Atlanta, GA 30328 USA
 * All rights reserved.
 */
package com.rekko.newrelic.storm.kafka;

import com.google.common.collect.Lists;

import com.newrelic.metrics.publish.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetRequest;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ghais
 */
public class Kafka {

  private static final int SO_TIMEOUT = 100000;
  private static final int BUFFER_SIZE = 64 * 1024;
  private static Logger LOG = Logger.getLogger(Kafka.class);

  private final String _host;
  private final int _port;
  private final String _topic;

  public Kafka(String host, int port, String topic) {
    _host = checkNotNull(host, "host cannot be null");
    _topic = checkNotNull(topic, "topic can't be null");
    _port = port;
  }

  public Map<Integer, Long> getOffsets() {
    SimpleConsumer consumer =
        new SimpleConsumer(_host, _port, SO_TIMEOUT, BUFFER_SIZE, "newrelic_storm_kafka");
    try {
      List<PartitionMetadata> metaData = getParitionsMetaData(consumer, _topic);
      Map<Integer, Long> offsets = new HashMap<Integer, Long>(metaData.size());
      for (PartitionMetadata partitionMetadata : metaData) {
        OffsetInfo offset = getOffset(_topic, partitionMetadata);
        offsets.put(partitionMetadata.partitionId(), offset.offset);
      }
      return offsets;
    } finally {
      consumer.close();
    }
  }

  private static OffsetInfo getOffset(String topic, PartitionMetadata partition) {
    Broker broker = partition.leader();

    SimpleConsumer consumer = new SimpleConsumer(broker.host(), broker.port(), 10000, 1000000,
                                                 "com.rekko.newrelic.storm.kafka");
    try {
      TopicAndPartition
          topicAndPartition =
          new TopicAndPartition(topic, partition.partitionId());
      PartitionOffsetRequestInfo rquest = new PartitionOffsetRequestInfo(-1, 1);
      Map<TopicAndPartition, PartitionOffsetRequestInfo>
          map =
          new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
      map.put(topicAndPartition, rquest);
      OffsetRequest req = new OffsetRequest(map, (short) 0, "com.rekko.newrelic.storm.kafka");
      OffsetResponse resp = consumer.getOffsetsBefore(req);
      OffsetInfo offset = new OffsetInfo();
      offset.offset = resp.offsets(topic, partition.partitionId())[0];
      return offset;
    } finally {
      consumer.close();
    }
  }


  private static TopicMetadata getTopicMetaData(SimpleConsumer consumer, String topic) {
    TopicMetadataRequest req = new TopicMetadataRequest(Lists.newArrayList(topic));

    List<TopicMetadata> metadatas = consumer.send(req).topicsMetadata();
    if (metadatas.size() != 1 || !metadatas.get(0).topic().equals(topic)) {
      LOG.fatal("no valid topic metadata for topic:", topic, ". run kafka-list-topic.sh to verify");
      System.exit(1);
    }
    return metadatas.get(0);
  }

  private static List<PartitionMetadata> getParitionsMetaData(SimpleConsumer consumer,
                                                              String topic) {
    return getTopicMetaData(consumer, topic).partitionsMetadata();
  }
}
