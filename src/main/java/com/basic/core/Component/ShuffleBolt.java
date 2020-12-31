package com.basic.core.Component;

import com.basic.core.Utils.GeoHash;
import java.util.Map;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;

import static com.basic.core.Utils.Config.SCHEMA;
import static com.basic.core.Utils.CastUtils.getLong;
import static org.slf4j.LoggerFactory.getLogger;

import com.basic.core.Utils.FileWriter;

public class ShuffleBolt extends BaseBasicBolt {

  private static final Logger LOG = getLogger(ShuffleBolt.class);
  private long numRTuples;
  private long numSTuples;
  private long numTTuples;
  private long numRTTuples;

  private String streamR;
  private String streamS;
  private String streamT;
  private Long tupleRate = 0l;
  private Long tuples = 0l;
  private Long last = System.nanoTime();

  private FileWriter output;

  public ShuffleBolt(int rate) {
    super();
    this.tupleRate = rate * 2l;
  }

  @Override
  public void prepare(Map stormConf, TopologyContext context) {
    super.prepare(stormConf, context);
    numRTuples = 0;
    numSTuples = 0;
    numTTuples = 0;
    streamR = "Orders2";
    streamS = "Gps2";
    streamT = "Gps3";
  }

  @Override
  public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {

    String topic = tuple.getStringByField("topic");
    String value = tuple.getStringByField("value");
    Long ts =  System.currentTimeMillis();
    String[] values = value.split(",");

    if (topic.equals(streamR)) {
      String rel = "R";
      String key = values[2];
      String key2 = values[3];
      numRTuples++;
      basicOutputCollector.emit(new Values(rel, ts, key, key2, value));
    } else if (topic.equals(streamS)) {
      String rel = "S";
      String key = values[2];
      String key2 = values[3];
      numSTuples++;
      basicOutputCollector.emit(new Values(rel, ts, key, key2, value));
    } else if (topic.equals(streamT)) {
      String rel = "T";
      String key = values[2];
      String key2 = values[3];
      numTTuples++;
      basicOutputCollector.emit(new Values(rel, ts, key, key2, value));
    }

  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    outputFieldsDeclarer.declare(new Fields(SCHEMA));
  }

  private void output(String msg) {
    if (output != null)
      output.write(msg);
  }
}