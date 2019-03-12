package com.zerostech.csp.hte;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessor;
import org.apache.hadoop.hbase.coprocessor.RegionObserver;
import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Optional;

/**
 * Created on 2019/1/10.
 *
 * @author 迹_Jason
 */
@InterfaceAudience.Private
public abstract class HBaseDataSyncESBaseObserver implements RegionObserver, RegionCoprocessor {

    private static final Logger logger = LoggerFactory.getLogger(HBaseDataSyncESBaseObserver.class);

    private static void readConfiguration(CoprocessorEnvironment env) {
        Configuration conf = env.getConfiguration();
        ESClient.clusterName = conf.get("es_cluster", "elasticsearch");
        ESClient.nodeHost = conf.get("es_host", "localhost");
        ESClient.nodePort = conf.getInt("es_port", 9300);
        ESClient.indexName = conf.get("es_index");
        ESClient.transportEnabled = conf.getBoolean("es_transport_enabled", true);
        ESClient.truststorePath = conf.get("es_truststore_path");
        ESClient.keystorePath = conf.get("es_keystore_path");
        logger.debug("Init Parameter: [clusterName: {}, nodeHost: {}, nodePort: {}, indexName: {}, transportEnabled: {}, truststorePath:{}, truststorePath:{}, keystorePath:{}.]",
                ESClient.clusterName,
                ESClient.nodeHost,
                ESClient.nodePort,
                ESClient.indexName,
                ESClient.transportEnabled,
                ESClient.truststorePath,
                ESClient.keystorePath);
    }

    public Optional<RegionObserver> getRegionObserver() {
        return Optional.of(this);
    }

    @Override
    public void start(CoprocessorEnvironment env) throws IOException {
        readConfiguration(env);
        // init ES client
        ESClient.initEsClient();
        logger.info("Coprocessor Environment Initialized");
    }

    @Override
    public void stop(CoprocessorEnvironment env) {
        ESClient.closeEsClient();
        // shutdown time task
        ElasticSearchBulkOperator.shutdownScheduleEx();
        logger.info("HBase Data Sync Es Observer Finished");
    }
}

