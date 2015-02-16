newrelic_storm_kafka
====================
This is a [Newrelic Plugin](https://newrelic.com/platform) to monitor the health of an [Apache Storm](storm.incubator.apache.org) topology consumig from a [Kafka Spout](https://github.com/apache/incubator-storm/tree/master/external/storm-kafka) using  

Installation
------------
You can use Newrelic NPI 

```
Linux – Debian/Ubuntu
x86:  
LICENSE_KEY=YOUR_KEY_HERE bash -c "$(curl -sSL https://download.newrelic.com/npi/release/install-npi-linux-debian-x86.sh)"
x64:  
LICENSE_KEY=YOUR_KEY_HERE bash -c "$(curl -sSL https://download.newrelic.com/npi/release/install-npi-linux-debian-x64.sh)"

Linux – Red Hat/CentOS
x86:  
LICENSE_KEY=YOUR_KEY_HERE bash -c "$(curl -sSL https://download.newrelic.com/npi/release/install-npi-linux-redhat-x86.sh)"
x64:  
LICENSE_KEY=YOUR_KEY_HERE bash -c "$(curl -sSL https://download.newrelic.com/npi/release/install-npi-linux-redhat-x64.sh)"

Linux – Generic
x86: 
LICENSE_KEY=YOUR_KEY_HERE bash -c "$(curl -sSL https://download.newrelic.com/npi/release/install-npi-linux-x86.sh)"
x64:  
LICENSE_KEY=YOUR_KEY_HERE bash -c "$(curl -sSL https://download.newrelic.com/npi/release/install-npi-linux-x64.sh)"
[/quote]

```
and then ```./npi install com.rekko.newrelic.storm.kafka```

Configuration
-------------
```javascript
{
    "agents": [
      {
          "name" : "a descriptive name",                 //A name to identify the topology by.
          "kafka.topic" : "topic",                       //The topic you want to monitor
          "kafka.broker": "kafka.example.com:9012",      //The host:port of one of the kafka brokers
          "storm.zk.host": "zookeeper.example.com:2181", //The host:port of zookeeper
          "storm.zk.path": "/kafka_consumers/topology"   //The path storm-kafak uses to store the kafa offset for this topic
      }
    ]
}
```

Status:
------
This project is still under development, however as far as I can tell it works with no problems.
