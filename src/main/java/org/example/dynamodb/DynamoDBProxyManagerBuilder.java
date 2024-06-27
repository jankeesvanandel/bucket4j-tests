package org.example.dynamodb;

import io.github.bucket4j.distributed.proxy.AbstractProxyManagerBuilder;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;

public class DynamoDBProxyManagerBuilder extends
        AbstractProxyManagerBuilder<String, DynamoDBProxyManager, DynamoDBProxyManagerBuilder> {

    @Override
    public DynamoDBProxyManager build() {
        return new DynamoDBProxyManager(ClientSideConfig.getDefault());
    }
}
