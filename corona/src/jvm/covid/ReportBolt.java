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

public class ReportBolt extends BaseRichBolt
{
  final static Logger logger = Logger.getLogger(ReportBolt.class);

  @Override
  public void prepare( Map map, TopologyContext topologyContext, OutputCollector outputCollector)
  {
  }

  @Override
  public void execute(Tuple tuple)
  {
    Long userID = tuple.getLongByField("userID");
    int surpriseNumber = tuple.getIntegerByField("surpriseNumber");
    Integer count = tuple.getIntegerByField("count");
    logger.info("userID : " +  Long.toString(userID) + " count : " + Long.toString(count) );

    if( surpriseNumber != 0 ){
      logger.info("Surpries Number : " + surpriseNumber);
    }
  }

  public void declareOutputFields(OutputFieldsDeclarer declarer)
  {
  }
}
