package org.example.dynamodb;

import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.AbstractCompareAndSwapBasedProxyManager;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.AsyncCompareAndSwapOperation;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.CompareAndSwapOperation;
import io.github.bucket4j.distributed.remote.RemoteBucketState;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES;

public class DynamoDBProxyManager extends AbstractCompareAndSwapBasedProxyManager<String> {

    private final DynamoDbClient client;

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<DynamoDBCacheEntry> table;

    protected DynamoDBProxyManager(ClientSideConfig clientSideConfig) {
        super(clientSideConfig);

        AwsCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create("key", "secret"));

        client = DynamoDbClient.builder()
                               .credentialsProvider(credentials)
                               .region(Region.EU_WEST_1)
                               .endpointOverride(URI.create("https://localhost:57754"))
                               .httpClient(ApacheHttpClient.builder().buildWithDefaults(
                                       AttributeMap.builder().put(TRUST_ALL_CERTIFICATES, Boolean.TRUE).build()))
                               .build();

        if (client.listTables().tableNames().stream().anyMatch(tn -> tn.equals("bucket4j"))) {
            client.deleteTable(DeleteTableRequest.builder()
                          .tableName("bucket4j")
                                                 .build());
        }

        client.createTable(CreateTableRequest.builder()
                                             .tableName("bucket4j")
                                             .keySchema(KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("id").build())
                                             .attributeDefinitions(List.of(
                                                     AttributeDefinition
                                                             .builder()
                                                             .attributeName("id")
                                                             .attributeType(ScalarAttributeType.S)
                                                             .build()
                                             ))
                                             .billingMode(BillingMode.PAY_PER_REQUEST)
                                             .build());

        enhancedClient = DynamoDbEnhancedClient.builder()
                                               .dynamoDbClient(client)
                                               .build();

        table = enhancedClient.table("bucket4j", TableSchema.fromBean(DynamoDBCacheEntry.class));
    }

    @Override
    protected CompareAndSwapOperation beginCompareAndSwapOperation(String key) {
        return new CompareAndSwapOperation() {
            @Override
            public Optional<byte[]> getStateData(Optional<Long> timeoutNanos) {
                return Optional.ofNullable(table.getItem(createKey(key)))
                               .map(DynamoDBCacheEntry::getBucket4jState);
            }

            @Override
            public boolean compareAndSwap(byte[] originalData, byte[] newData, RemoteBucketState newState, Optional<Long> timeoutNanos) {
                DynamoDBCacheEntry entry = new DynamoDBCacheEntry(key, newData);
                PutItemEnhancedRequest.Builder<DynamoDBCacheEntry> builder = PutItemEnhancedRequest
                        .builder(DynamoDBCacheEntry.class)
                        .item(entry);

                if (originalData != null) {
                    builder.conditionExpression(Expression.builder()
                                                          .expression("bucket4jState = :originalState")
                                                          .expressionValues(Map.of(":originalState",
                                                                  AttributeValue.fromB(SdkBytes.fromByteArray(originalData))))
                                                          .build()
                    );
                }

                PutItemEnhancedRequest<DynamoDBCacheEntry> request = builder.build();

                try {
                    table.putItem(request);

                    return true;
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    return false;
                }
            }
        };
    }

    @Override
    protected AsyncCompareAndSwapOperation beginAsyncCompareAndSwapOperation(String key) {
        return null;
    }

    @Override
    protected CompletableFuture<Void> removeAsync(String key) {
        return CompletableFuture.runAsync(() ->
                deleteItem(key)
        );
    }

    @Override
    public void removeProxy(String key) {
        deleteItem(key);
    }

    @Override
    public boolean isAsyncModeSupported() {
        return false;
    }

    private void deleteItem(String key) {
        table.deleteItem(createKey(key));
    }

    private static Key createKey(String key) {
        return Key.builder().partitionValue(key).build();
    }

}
