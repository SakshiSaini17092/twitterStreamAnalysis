package covid;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;

import java.util.Map;
import covid.tweetObject;
import java.util.Arrays;
import java.util.Date;

public class ParserBolt extends BaseRichBolt{

  final static Logger logger = Logger.getLogger(ParserBolt.class);

  static String[] l = {
                        "SARSCoV2","Coronavirus","COVID19", "COVID",
                        "COVIDfoam", "Distancing", "PhysicalDistancing",
                        "SocialDistancing", "COVIDpain","COVIDfoamed",
                        "Virus", "Covid", "covid19", "medical", "jihad"
                      };

  OutputCollector collector;
  Date date =  new Date();
  long cuTime = 0;
  int counter ;

  @Override
  public void prepare( Map map, TopologyContext topologyContext, OutputCollector outputCollector)
  {
    collector = outputCollector;
    cuTime = date.getMinutes();
    // cuTime = System.currentTimeMillis();
    counter = 0;
  }

  @Override
  public void execute(Tuple tuple)
  {
    tweetObject tweet = (tweetObject) tuple.getValue(0);
    long userID = tweet.userID;
    String t = tweet.tweet;

    counter ++;

    String delims = "[ .,?!]+";
    String tokens[] = t.split(delims);
    for( String token : tokens ){

      for (int i =0; i< l.length; i ++)
      {
        if ( token.equalsIgnoreCase(l[i]) )
          collector.emit(new Values(userID, cuTime, counter));
      }
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("userID", "time", "number-of-tweets"));
  }
}
