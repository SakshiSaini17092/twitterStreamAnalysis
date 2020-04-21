package covid;

import covid.TweetSpout;
import covid.ParserBolt;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.apache.log4j.Logger;

import covid.ReportBolt;
import covid.CountBolt;

public class twitterTopology{

  final static Logger logger = Logger.getLogger(twitterTopology.class);

  public static void main(String[] args) {


    // TopologyBuilder
    TopologyBuilder builder = new TopologyBuilder();

    // Create Spout
    TweetSpout tweetSpout = new TweetSpout(
    "nHHZzvxr9odYdpgA2O86wsqpJ",
    "YbxXyo37uh48mgd4VWRHaEOo6qtHFufzfQ40a91N1m1P2HxCoy",
    "1151096317243883520-jKW2RCe5YssHBBT7QMx2OPq8Lwi0fA",
    "xfWOCvrWACwlN8JYAAUGuQevTdNbHZHxfmwSLqNtP13Lk"
    );

    builder.setSpout("tweet-spout", tweetSpout, 1);
    builder.setBolt("parser-bolt", new ParserBolt(), 1).shuffleGrouping("tweet-spout");

    builder.setBolt("count-bolt", new CountBolt(), 1).fieldsGrouping("parser-bolt", new Fields("userID"));

    builder.setBolt("reporter-bolt", new ReportBolt(), 1).globalGrouping("count-bolt");
    Config conf = new Config();
    // conf.setDebug(true);

    conf.setMaxTaskParallelism(3);
    LocalCluster cluster = new LocalCluster();

    cluster.submitTopology("tweet-word-count", conf, builder.createTopology());
    Utils.sleep(2500000);

    cluster.killTopology("tweet-word-count");
    cluster.shutdown();

  }

}
