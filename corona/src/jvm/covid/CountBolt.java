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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Date;
/**
 * A bolt that counts the words that it receives
 */
public class CountBolt extends BaseRichBolt
{
  final static Logger logger = Logger.getLogger(ReportBolt.class);

  // To output tuples from this bolt to the next stage bolts, if any
  private OutputCollector collector;

  // Map to store the count of the words
  private Map<Long, Integer> countMap;

  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {

    // save the collector for emitting tuples
    collector = outputCollector;
  // create and initialize the map
    countMap = new HashMap<Long, Integer>();
  }

  public int calculateSurpriseNumber(){

    // 10 random points
    Random r = new Random();
		// return r.nextInt((max - min) + 1) + min;
    int sum = 0;
    int n = countMap.size();
    for ( int i = 0 ; i < 10; i ++ )
    {
      int count  = 0;
      int point = r.nextInt(n);
      Object[] values = countMap.values().toArray();


      // int count = 0;
      int pointValue = (int) values[point];

      for ( int j = point+1 ; j < n; j ++ )
      {
        int c = (int)values[j];
        if( c == pointValue ){
          count ++;
        }
      }
      sum = sum + n*( 2*count -1 );
    }
    return sum/10;
  }


  @Override
  public void execute(Tuple tuple)
  {
    // get the word from the 1st column of incoming tuple
    Long word = tuple.getLong(0);
    long timeMilli = tuple.getLong(1);

    int surpriseNumber = 0;

    // check if the word is present in the map
    if (countMap.get(word) == null) {

      int n = countMap.size();
      // logger.info("size of hash map " + n);
      if( n >= 50 ){
        // remove a entry and add a new userID
        Object[] crunchifyKeys = countMap.keySet().toArray();
        Object key = crunchifyKeys[new Random().nextInt(crunchifyKeys.length)];
        countMap.remove(key);
        // logger.info("removing user" + key);
        countMap.put(word, 1);

      }
      else{
        // not present, add the word with a count of 1
        countMap.put(word, 1);
      }

    } else {

      // already there, hence get the count
      Integer val = countMap.get(word);
      // increment the count and save it to the map
      countMap.put(word, ++val);
    }

    Date date =  new Date();
    long cuTime =  date.getTime();

    logger.info("cureentTime"  + cuTime + "starting Time" + timeMilli);
    if(
          (cuTime - timeMilli >= 600000) && (cuTime - timeMilli <= 601000)
        | (cuTime - timeMilli >= 1200000) && (cuTime - timeMilli >= 1201000)
        | (cuTime - timeMilli >= 2400000) && (cuTime - timeMilli >= 1201000)
        | (cuTime - timeMilli >= 4800000) && (cuTime - timeMilli >= 1201000)

     )
    {
      surpriseNumber = this.calculateSurpriseNumber();
      collector.emit(new Values(word, countMap.get(word), surpriseNumber));
      return;
    }

    // emit the word and count
    collector.emit(new Values(word, countMap.get(word), 0));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
  {
    // tell storm the schema of the output tuple for this spout
    // tuple consists of a two columns called 'word' and 'count'

    // declare the first column 'word', second column 'count'
    outputFieldsDeclarer.declare(new Fields("word","count", "surpriseNumber"));
  }
}
