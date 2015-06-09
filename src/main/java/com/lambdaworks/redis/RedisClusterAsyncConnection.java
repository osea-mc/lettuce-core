package com.lambdaworks.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.BaseRedisAsyncConnection;
import com.lambdaworks.redis.api.async.RedisHLLAsyncConnection;
import com.lambdaworks.redis.api.async.RedisHashesAsyncConnection;
import com.lambdaworks.redis.api.async.RedisKeysAsyncConnection;
import com.lambdaworks.redis.api.async.RedisListsAsyncConnection;
import com.lambdaworks.redis.api.async.RedisScriptingAsyncConnection;
import com.lambdaworks.redis.api.async.RedisServerAsyncConnection;
import com.lambdaworks.redis.api.async.RedisSetsAsyncConnection;
import com.lambdaworks.redis.api.async.RedisSortedSetsAsyncConnection;
import com.lambdaworks.redis.api.async.RedisStringsAsyncConnection;

/**
 * Complete asynchronous cluster Redis API with 400+ Methods..
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 3.0
 */
public interface RedisClusterAsyncConnection<K, V> extends RedisHashesAsyncConnection<K, V>, RedisKeysAsyncConnection<K, V>,
        RedisStringsAsyncConnection<K, V>, RedisListsAsyncConnection<K, V>, RedisSetsAsyncConnection<K, V>,
        RedisSortedSetsAsyncConnection<K, V>, RedisScriptingAsyncConnection<K, V>, RedisServerAsyncConnection<K, V>,
        RedisHLLAsyncConnection<K, V>, BaseRedisAsyncConnection<K, V> {

    /**
     * Set the default timeout for operations.
     * 
     * @param timeout the timeout value
     * @param unit the unit of the timeout value
     */
    void setTimeout(long timeout, TimeUnit unit);

    /**
     * Authenticate to the server.
     * 
     * @param password the password
     * @return String simple-string-reply
     */
    String auth(String password);

    RedisFuture<String> clusterMeet(String ip, int port);

    RedisFuture<String> clusterForget(String nodeId);

    RedisFuture<String> clusterAddSlots(int... slots);

    RedisFuture<String> clusterDelSlots(int... slots);

    RedisFuture<String> clusterInfo();

    RedisFuture<String> clusterMyId();

    RedisFuture<String> clusterNodes();

    RedisFuture<List<K>> clusterGetKeysInSlot(int slot, int count);

    /**
     * Get array of Cluster slot to node mappings.
     * 
     * @return RedisFuture&lt;List&lt;Object&gt;&gt; array-reply nested list of slot ranges with IP/Port mappings.
     */
    RedisFuture<List<Object>> clusterSlots();

    RedisFuture<String> clusterSetSlotNode(int slot, String nodeId);

    RedisFuture<String> clusterSetSlotMigrating(int slot, String nodeId);

    RedisFuture<String> clusterSetSlotImporting(int slot, String nodeId);

    RedisFuture<String> asking();

    RedisFuture<String> clusterReplicate(String nodeId);

    RedisFuture<String> clusterFailover(boolean force);

    RedisFuture<String> clusterReset(boolean hard);

    RedisFuture<String> clusterFlushslots();

    RedisFuture<List<String>> clusterSlaves(String nodeId);

    /**
     * Tells a Redis cluster slave node that the client is ok reading possibly stale data and is not interested in running write
     * queries.
     *
     * @return String simple-string-reply
     */
    RedisFuture<String> readOnly();

    /**
     * Resets readOnly flag.
     *
     * @return String simple-string-reply
     */
    RedisFuture<String> readWrite();

}
