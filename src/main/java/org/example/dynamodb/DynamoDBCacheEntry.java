package org.example.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class DynamoDBCacheEntry {
    private String id;
    private byte[] bucket4jState;

    public DynamoDBCacheEntry() {
    }

    public DynamoDBCacheEntry(String id, byte[] bucket4jState) {
        this.id = id;
        this.bucket4jState = bucket4jState;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getBucket4jState() {
        return bucket4jState;
    }

    public void setBucket4jState(byte[] bucket4jState) {
        this.bucket4jState = bucket4jState;
    }
}
