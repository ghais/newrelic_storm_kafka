/**
 * (C)2014 Digi-Net Technologies, Inc.
 * 5887 Glenridge Drive
 * Suite 350
 * Atlanta, GA 30328 USA
 * All rights reserved.
 */
package com.rekko.newrelic.storm.kafka;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

/**
 * Main class for storm-kafka monitoring agent.
 *
 * @author ghais
 */
public class Main {

  public static void main(String[] args) {
    try {
      Runner runner = new Runner();
      runner.add(new StormKafkaAgentFactory());
      runner.setupAndRun(); // Never returns
    } catch (ConfigurationException e) {
      System.err.println("ERROR: " + e.getMessage());
      System.exit(1);
    }
  }
}
