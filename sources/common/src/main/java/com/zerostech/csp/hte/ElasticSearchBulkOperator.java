package com.zerostech.csp.hte;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2019/1/11.
 *
 * @author 迹_Jason
 */
public class ElasticSearchBulkOperator {

    private static final Log LOG = LogFactory.getLog(ElasticSearchBulkOperator.class);

    private static BulkProcessor bulkRequestBuilder;

    static {
        // init es bulkRequestBuilder
        bulkRequestBuilder = BulkProcessor.builder(
                ESClient.client,  //增加elasticsearch客户端
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId,
                                           BulkRequest request) {
                        //调用bulk之前执行 ，例如你可以通过request.numberOfActions()方法知道numberOfActions
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          BulkResponse response) {
                        //调用bulk之后执行 ，例如你可以通过request.hasFailures()方法知道是否执行失败
                        if (response.hasFailures()) {
                            LOG.error("ES request existed " + response.buildFailureMessage());
                        }
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request,
                                          Throwable failure) {
                    } //调用失败抛 Throwable
                })
                .setBulkActions(1000) //每次10000请求
                .setBulkSize(new ByteSizeValue(2, ByteSizeUnit.MB)) //拆成5mb一块
                .setFlushInterval(TimeValue.timeValueSeconds(5)) //无论请求数量多少，每5秒钟请求一次。
                .setConcurrentRequests(10) //设置并发请求的数量。值为0意味着只允许执行一个请求。值为1意味着允许1并发请求。
                .setBackoffPolicy(
                        //设置自定义重复请求机制，最开始等待100毫秒，之后成倍更加，重试3次，当一次或多次重复请求失败后因为计算资源不够抛出 EsRejectedExecutionException 异常，可以通过BackoffPolicy.noBackoff()方法关闭重试机制
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();

    }


    public static void addBuilderToBulk(DocWriteRequest builder){
        bulkRequestBuilder.add(builder);
    }

    public static void shutdownScheduleEx() {
        bulkRequestBuilder.flush();
        try {
            bulkRequestBuilder.awaitClose(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("ES bulk request has error " + e.getMessage());
        }
    }
}
