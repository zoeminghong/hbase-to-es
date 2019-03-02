package com.zerostech.csp.hte;

import com.zerostech.csp.hte.phoenix.PhoenixUtils;
import com.zerostech.csp.hte.phoenix.TableSchema;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessor;
import org.apache.hadoop.hbase.coprocessor.RegionObserver;
import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created on 2019/1/10.
 *
 * @author 迹_Jason
 */
@InterfaceAudience.Private
public abstract class HBaseDataSyncESBaseObserver implements RegionObserver, RegionCoprocessor {

    private static final Logger logger = LoggerFactory.getLogger(HBaseDataSyncESBaseObserver.class);
    protected static Map<String, TableSchema> tableSchema = new HashMap<>();

    private static void readConfiguration(CoprocessorEnvironment env) {
        Configuration conf = env.getConfiguration();
        logger.info("****init params " + conf.toString());
        logger.info("****init params " + conf.get("es_cluster", "elasticsearch"));
        logger.info("****init params " + conf.get("es_host", "localhost"));
        ESClient.clusterName = conf.get("es_cluster", "elasticsearch");
        ESClient.nodeHost = conf.get("es_host", "localhost");
        ESClient.nodePort = conf.getInt("es_port", 9300);
        ESClient.indexName = conf.get("es_index");
    }

    public abstract String getTableSchema();

    public Optional<RegionObserver> getRegionObserver() {
        return Optional.of(this);
    }

    @Override
    public void start(CoprocessorEnvironment env) throws IOException {
        logger.info("****init start first*****");
        readConfiguration(env);
        // init ES client
        ESClient.initEsClient();
        logger.info("****init start*****");
        tableSchema = PhoenixUtils.getTableSchema(getTableSchema());
    }

    @Override
    public void stop(CoprocessorEnvironment env) {
        ESClient.closeEsClient();
        // shutdown time task
        ElasticSearchBulkOperator.shutdownScheduleEx();
        logger.info("****end*****");
    }
}
