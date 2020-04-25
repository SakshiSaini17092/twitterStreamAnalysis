package covid;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Date;

public class CountBolt extends BaseRichBolt
{
  final static Logger logger = Logger.getLogger(ReportBolt.class);
  private OutputCollector collector;
  private Map<Long, Integer> countMap;

  @Override
  public void prepare( Map map, TopologyContext topologyContext, OutputCollector outputCollector)
  {
    collector = outputCollector;
    countMap = new HashMap<Long, Integer>();

    for (int i =0; i< 10000; i++)
    {
      Random r = new Random();
      long point = r.nextInt(90000);
      countMap.put(point, 1);
    }
  }

  public int calculateSurpriseNumber(){

    // 10 random points
    Random r = new Random();
    int sum = 0;
    int n = countMap.size();
    for ( int i = 0 ; i < 100; i ++ )
    {
      int count  = 0;
      int point = r.nextInt(n);
      Object[] values = countMap.values().toArray();
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
    return sum/100;
  }

  @Override
  public void execute(Tuple tuple)
  {
    Long userID = tuple.getLong(0);
    long timeMilli = tuple.getLong(1);
    int counter = tuple.getInteger(2);
    int surpriseNumber = 0;

    if (countMap.get(userID) == null) {

      int n = countMap.size();
      int sample = new Random().nextInt(counter);

      if( sample < n && n >= 10000 ){
        // remove a entry and add a new userID
        Object[] crunchifyKeys = countMap.keySet().toArray();
        Object key = crunchifyKeys[new Random().nextInt(crunchifyKeys.length)];
        countMap.remove(key);
        countMap.put(userID, 1);
      }
      else if( n <= 10000 ){
        countMap.put(userID, 1);
      }

    } else {
      Integer val = countMap.get(userID);
      countMap.put(userID, ++val);
    }

    Date date =  new Date();
    int currentTime = date.getMinutes();

    logger.info("cureentTime "  + currentTime + "starting Time " + timeMilli);
    if( (currentTime - timeMilli == 10) |
        (currentTime - timeMilli == 20) |
        (currentTime - timeMilli == 30) |
        (currentTime - timeMilli == 40)
        )
    {
      surpriseNumber = this.calculateSurpriseNumber();
      collector.emit(new Values(userID, countMap.get(userID), surpriseNumber));
      return;
    }
    surpriseNumber = this.calculateSurpriseNumber();
    collector.emit(new Values(userID, countMap.get(userID), 0));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
  {
    outputFieldsDeclarer.declare(new Fields("userID","count", "surpriseNumber"));
  }
}
