/**
 * (C)2014 Digi-Net Technologies, Inc.
 * 5887 Glenridge Drive
 * Suite 350
 * Atlanta, GA 30328 USA
 * All rights reserved.
 */
package com.rekko.newrelic.storm.kafka;

import com.google.gson.Gson;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
  * @author ghais
 */
public class StormKafkaAgent extends Agent {

  private static Logger LOG = Logger.getLogger(StormKafkaAgent.class);
  private static final Gson GSON = new Gson();
  public static final String GUID = "com.rekko.newrelic.storm.kafka";
  private static final String VERSION = "1.0.0";

  private final String _broker;
  private final String _topic;
  private final String _zkHost;
  private final String _zkPath;
  private final int _port;
  private final String _name;


  public StormKafkaAgent(String name, String broker, int port, String topic, String zkHost,
                         String zkPath) {
    super(GUID, VERSION);
    _name = name;
    _broker = broker;
    _topic = topic;
    _zkHost = zkHost;
    _zkPath = zkPath;
    _port = port;
  }

  @Override
  public String getComponentHumanLabel() {
    return _name;
  }

  @Override
  public void pollCycle() {
    try {
      long lag = getLag();
      LOG.info("lag for ", _name, ":", lag);
      reportMetric("Count", "messages", lag);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(e.getMessage(), e);
    }
  }


  private long getLag() throws Exception {
    Kafka kafka = new Kafka(_broker, _port, _topic);
    Map<Integer, Long> kafkaOffsets = kafka.getOffsets();
    LOG.info("current kafka offsets for ", _name, ":", kafkaOffsets);
    Map<Integer, Long> stormOffsets = getStormOffsets(kafkaOffsets.keySet());
    LOG.info("current storm offsets for ", _name, ":", stormOffsets);
    int lag = 0;
    for (Map.Entry<Integer, Long> entry : kafkaOffsets.entrySet()) {
      Long offset = stormOffsets.get(entry.getKey());
      if (offset == null) {
        LOG.warn("storm offset was null. This is likely because storm hasn't started processing this topic");
        lag += entry.getValue();
        continue;
      }
      //It is possible that storm has updated the offset since we fetched the kafka offsets
      //so ignore this case.
      if (offset > entry.getValue()) {
        LOG.info("storm offset=", offset, " is ahead of kafka offset=", entry.getValue(),
                 "by ", offset - entry.getValue(), ". for partition", entry.getKey());
        continue;
      }
      lag += (entry.getValue() - offset);
    }
    return lag;
  }

  private Map<Integer, Long> getStormOffsets(Collection<Integer> partitionIds)
      throws Exception {
    CuratorFramework client = CuratorFrameworkFactory.newClient(_zkHost, RETRY);
    client.start();
    try {
      Map<Integer, Long> offsets = new HashMap<Integer, Long>(partitionIds.size());
      for (Integer partitionId : partitionIds) {
        String partitionPath = getPartitionPath(_zkPath, partitionId);
        byte[] data = client.getData().forPath(partitionPath);
        OffsetInfo offset = GSON.fromJson(new String(data), OffsetInfo.class);
        offsets.put(partitionId, offset.offset);
      }
      return offsets;
    } finally {
      client.close();
    }
  }


  private static String getPartitionPath(String zkRoot, Integer partition) {
    if (zkRoot.endsWith("/")) {
      zkRoot = zkRoot.substring(0, zkRoot.length() - 1);
    }
    return String.format("%s/partition_%d", zkRoot, partition);
  }

  RetryPolicy RETRY = new ExponentialBackoffRetry(1000, 3);


}
