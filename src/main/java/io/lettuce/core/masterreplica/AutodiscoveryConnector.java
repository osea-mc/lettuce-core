/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lettuce.core.masterreplica;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.event.jfr.EventRecorder;
import io.lettuce.core.models.role.RedisNodeDescription;

/**
 * {@link MasterReplicaConnector} to connect unmanaged Redis Master/Replica with auto-discovering master and replica nodes from
 * a single {@link RedisURI}.
 *
 * @author Mark Paluch
 * @author Jim Brunner
 * @since 5.1
 */
class AutodiscoveryConnector<K, V> implements MasterReplicaConnector<K, V> {

    private final RedisClient redisClient;

    private final RedisCodec<K, V> codec;

    private final RedisURI redisURI;

    private final Map<RedisURI, StatefulRedisConnection<?, ?>> initialConnections = new ConcurrentHashMap<>();

    AutodiscoveryConnector(RedisClient redisClient, RedisCodec<K, V> codec, RedisURI redisURI) {
        this.redisClient = redisClient;
        this.codec = codec;
        this.redisURI = redisURI;
    }

    @Override
    public CompletableFuture<StatefulRedisMasterReplicaConnection<K, V>> connectAsync() {

        ConnectionFuture<StatefulRedisConnection<K, V>> initialConnection = redisClient.connectAsync(codec, redisURI);
        Mono<StatefulRedisMasterReplicaConnection<K, V>> connect = Mono.fromCompletionStage(initialConnection)
                .flatMap(nodeConnection -> {

                    initialConnections.put(redisURI, nodeConnection);

                    TopologyProvider topologyProvider = new ReplicaTopologyProvider(nodeConnection, redisURI);

                    return Mono.fromCompletionStage(topologyProvider.getNodesAsync())
                            .flatMap(nodes -> getMasterConnectionAndUri(nodes, Tuples.of(redisURI, nodeConnection), codec));
                }).flatMap(connectionAndUri -> {
                    return initializeConnection(codec, connectionAndUri);
                });

        return connect.onErrorResume(t -> {

            Mono<Void> close = Mono.empty();

            for (StatefulRedisConnection<?, ?> connection : initialConnections.values()) {
                close = close.then(Mono.fromFuture(connection.closeAsync()));
            }

            return close.then(Mono.error(t));
        }).onErrorMap(ExecutionException.class, Throwable::getCause).toFuture();

    }

    private Mono<Tuple2<RedisURI, StatefulRedisConnection<K, V>>> getMasterConnectionAndUri(List<RedisNodeDescription> nodes,
            Tuple2<RedisURI, StatefulRedisConnection<K, V>> connectionTuple, RedisCodec<K, V> codec) {

        RedisNodeDescription node = getConnectedNode(redisURI, nodes);

        if (!node.getRole().isUpstream()) {

            RedisNodeDescription master = lookupMaster(nodes);
            ConnectionFuture<StatefulRedisConnection<K, V>> masterConnection = redisClient.connectAsync(codec, master.getUri());

            return Mono.just(master.getUri()).zipWith(Mono.fromCompletionStage(masterConnection)) //
                    .doOnNext(it -> {
                        initialConnections.put(it.getT1(), it.getT2());
                    });
        }

        return Mono.just(connectionTuple);
    }

    @SuppressWarnings("unchecked")
    private Mono<StatefulRedisMasterReplicaConnection<K, V>> initializeConnection(RedisCodec<K, V> codec,
            Tuple2<RedisURI, StatefulRedisConnection<K, V>> connectionAndUri) {

        ReplicaTopologyProvider topologyProvider = new ReplicaTopologyProvider(connectionAndUri.getT2(),
                connectionAndUri.getT1());

        MasterReplicaTopologyRefresh refresh = new MasterReplicaTopologyRefresh(redisClient, topologyProvider);
        MasterReplicaConnectionProvider<K, V> connectionProvider = new MasterReplicaConnectionProvider<>(redisClient, codec,
                redisURI, (Map) initialConnections);

        Mono<List<RedisNodeDescription>> refreshFuture = refresh.getNodes(redisURI);

        return refreshFuture.map(nodes -> {

            EventRecorder.getInstance().record(new MasterReplicaTopologyChangedEvent(redisURI, nodes));

            connectionProvider.setKnownNodes(nodes);

            MasterReplicaChannelWriter channelWriter = new MasterReplicaChannelWriter(connectionProvider, redisClient.getResources(), redisClient.getOptions());

            StatefulRedisMasterReplicaConnectionImpl<K, V> connection = new StatefulRedisMasterReplicaConnectionImpl<>(
                    channelWriter, codec, redisURI.getTimeout());

            connection.setOptions(redisClient.getOptions());

            return connection;
        });
    }

    private static RedisNodeDescription lookupMaster(List<RedisNodeDescription> nodes) {

        Optional<RedisNodeDescription> first = findFirst(nodes, n -> n.getRole().isUpstream());
        return first.orElseThrow(() -> new IllegalStateException("Cannot lookup master from " + nodes));
    }

    private static RedisNodeDescription getConnectedNode(RedisURI redisURI, List<RedisNodeDescription> nodes) {

        Optional<RedisNodeDescription> first = findFirst(nodes, n -> equals(redisURI, n));
        return first.orElseThrow(
                () -> new IllegalStateException("Cannot lookup node descriptor for connected node at " + redisURI));
    }

    private static Optional<RedisNodeDescription> findFirst(List<RedisNodeDescription> nodes,
            Predicate<? super RedisNodeDescription> predicate) {
        return nodes.stream().filter(predicate).findFirst();
    }

    private static boolean equals(RedisURI redisURI, RedisNodeDescription node) {
        return node.getUri().getHost().equals(redisURI.getHost()) && node.getUri().getPort() == redisURI.getPort();
    }

}
