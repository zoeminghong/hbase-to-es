package com.zerostech.csp.hte;

/**
 * Created on 2019/1/10.
 *
 * @author 迹_Jason
 */

import com.floragunn.searchguard.ssl.util.SSLConfigConstants;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ES Cleint class
 */
public class ESClient {

    public final static String NODE_HOST_SEPARATOR = "&";
    private static final Logger LOG = LoggerFactory.getLogger(ESClient.class);
    // ElasticSearch的集群名称
    static String clusterName;
    // ElasticSearch的host
    static String nodeHost;
    // ElasticSearch的端口（Java API用的是Transport端口，也就是TCP）
    static int nodePort;
    // ElasticSearch的索引名称
    static String indexName;
    // ElasticSearch Client
    static Client client;
    static String keystorePath;
    static String truststorePath;
    static Boolean transportEnabled = true;

    /**
     * init ES client
     */
    static void initEsClient() throws UnknownHostException {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Settings.Builder builder = Settings.builder()
                .put("cluster.name", clusterName)
                .put("client.transport.sniff", "true")
                .put("cluster.routing.allocation.enable", "all")
                .put("cluster.routing.allocation.allow_rebalance", "always");
        if (transportEnabled) {
            builder.put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_ENABLED, true)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_KEYSTORE_FILEPATH, keystorePath)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_TRUSTSTORE_FILEPATH, truststorePath)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_ENABLED, true)
                    .put(SSLConfigConstants.SEARCHGUARD_SSL_TRANSPORT_ENFORCE_HOSTNAME_VERIFICATION, false);
        }
        Settings esSettings = builder.build();
        LOG.info("Receive ES Host: {}", nodeHost);
        PreBuiltTransportClient c = new PreBuiltTransportClient(esSettings);
        String[] nodeHosts = nodeHost.split(NODE_HOST_SEPARATOR);
        for (String host : nodeHosts) {
            c.addTransportAddress(new TransportAddress(InetAddress.getByName(host), nodePort));
        }
        client = c;
    }

    /**
     * Close ES client
     */
    static void closeEsClient() {
        client.close();
    }
}

