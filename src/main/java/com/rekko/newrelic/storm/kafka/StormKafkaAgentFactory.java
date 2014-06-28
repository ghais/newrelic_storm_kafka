package com.rekko.newrelic.storm.kafka;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

import java.util.Map;

/**
 * AgentFactory for Wikipedia Example. The name of the Agent and host for Wikipedia. E.g.,
 * 'en.wikipedia.org'.
 *
 * @author jstenhouse
 */
public class StormKafkaAgentFactory extends AgentFactory {

  private static final int DEFAULT_KAFKA_PORT = 9092;

  @Override
  public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
    String broker = (String) properties.get("kafka.broker");
    String topic = (String) properties.get("kafka.topic");
    String zkHost = (String) properties.get("storm.zk.host");
    String zkPath = (String) properties.get("storm.zk.path");
    String name = (String) properties.get("name");
    return new StormKafkaAgent(name, getHost(broker), getPort(broker), topic, zkHost, zkPath);
  }

  private int getPort(String broker) {
    int index = broker.indexOf(':');
    if (-1 == index) {
      return DEFAULT_KAFKA_PORT;
    }
    return Integer.valueOf(broker.substring(index, broker.length()));
  }

  private String getHost(String broker) {
    int index = broker.indexOf(':');
    if (-1 == index) {
      return broker;
    }
    return broker.substring(0, index);
  }
}
