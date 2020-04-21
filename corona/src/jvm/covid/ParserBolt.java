package covid;

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

import java.util.Map;
import covid.tweetObject;
import java.util.Arrays;
import java.util.Date;

public class ParserBolt extends BaseRichBolt{

  final static Logger logger = Logger.getLogger(ParserBolt.class);

  static String[] l = {"SARSCoV2","Coronavirus","COVID19", "COVID", "COVIDfoam", "Distancing", "PhysicalDistancing", "SocialDistancing", "COVIDpain","COVIDfoamed", "Virus", "Covid", "covid19", "medical", "jihad"};

  OutputCollector collector;
  Date date =  new Date();
  long cuTime = 0;

  // timeMilli = date.sgetTime();

  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {
    collector = outputCollector;
    cuTime = date.getTime();
  }

  @Override
  public void execute(Tuple tuple)
  {
    tweetObject tweet = (tweetObject) tuple.getValue(0);

    long userID = tweet.userID;
    String t = tweet.tweet;

    String delims = "[ .,?!]+";
    String tokens[] = t.split(delims);

    // long timeMilli = tuple.getLong(1);
    for( String token : tokens ){
      if( Arrays.asList(l).contains(token)  ){

        collector.emit(new Values(userID, cuTime));
      }
    }
    // collector.emit(new Values(userID, ));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("userID", "time"));
  }
}
