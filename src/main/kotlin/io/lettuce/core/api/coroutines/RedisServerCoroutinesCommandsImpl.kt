/*
 * Copyright 2020-2021 the original author or authors.
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

package io.lettuce.core.api.coroutines

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.KillArgs
import io.lettuce.core.TrackingArgs
import io.lettuce.core.UnblockType
import io.lettuce.core.api.reactive.RedisServerReactiveCommands
import io.lettuce.core.protocol.CommandType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.util.*


/**
 * Coroutine executed commands (based on reactive commands) for Server Control.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mikhael Sokolov
 * @since 6.0
 *
 * @generated by io.lettuce.apigenerator.CreateKotlinCoroutinesReactiveImplementation
 */
@ExperimentalLettuceCoroutinesApi
internal class RedisServerCoroutinesCommandsImpl<K : Any, V : Any>(internal val ops: RedisServerReactiveCommands<K, V>) : RedisServerCoroutinesCommands<K, V> {

    override suspend fun bgrewriteaof(): String? = ops.bgrewriteaof().awaitFirstOrNull()

    override suspend fun bgsave(): String? = ops.bgsave().awaitFirstOrNull()

    override suspend fun clientCaching(enabled: Boolean): String? = ops.clientCaching(enabled).awaitFirstOrNull()

    override suspend fun clientGetname(): K? = ops.clientGetname().awaitFirstOrNull()

    override suspend fun clientGetredir(): Long? = ops.clientGetredir().awaitFirstOrNull()

    override suspend fun clientId(): Long? = ops.clientId().awaitFirstOrNull()

    override suspend fun clientKill(addr: String): String? = ops.clientKill(addr).awaitFirstOrNull()

    override suspend fun clientKill(killArgs: KillArgs): Long? = ops.clientKill(killArgs).awaitFirstOrNull()

    override suspend fun clientList(): String? = ops.clientList().awaitFirstOrNull()

    override suspend fun clientPause(timeout: Long): String? = ops.clientPause(timeout).awaitFirstOrNull()

    override suspend fun clientSetname(name: K): String? = ops.clientSetname(name).awaitFirstOrNull()

    override suspend fun clientTracking(args: TrackingArgs): String? = ops.clientTracking(args).awaitFirstOrNull()

    override suspend fun clientUnblock(id: Long, type: UnblockType): Long? = ops.clientUnblock(id, type).awaitFirstOrNull()

    override suspend fun command(): List<Any> = ops.command().asFlow().toList()

    override suspend fun commandCount(): Long? = ops.commandCount().awaitFirstOrNull()

    override suspend fun commandInfo(vararg commands: String): List<Any> = ops.commandInfo(*commands).asFlow().toList()

    override suspend fun commandInfo(vararg commands: CommandType): List<Any> = ops.commandInfo(*commands).asFlow().toList()

    override suspend fun configGet(parameter: String): Map<String, String>? = ops.configGet(parameter).awaitFirstOrNull()

    override suspend fun configResetstat(): String? = ops.configResetstat().awaitFirstOrNull()

    override suspend fun configRewrite(): String? = ops.configRewrite().awaitFirstOrNull()

    override suspend fun configSet(parameter: String, value: String): String? = ops.configSet(parameter, value).awaitFirstOrNull()

    override suspend fun dbsize(): Long? = ops.dbsize().awaitFirstOrNull()

    override suspend fun debugCrashAndRecover(delay: Long): String? = ops.debugCrashAndRecover(delay).awaitFirstOrNull()

    override suspend fun debugHtstats(db: Int): String? = ops.debugHtstats(db).awaitFirstOrNull()

    override suspend fun debugObject(key: K): String? = ops.debugObject(key).awaitFirstOrNull()

    override suspend fun debugOom() = ops.debugOom().awaitFirstOrNull().let { Unit }

    override suspend fun debugReload(): String? = ops.debugReload().awaitFirstOrNull()

    override suspend fun debugRestart(delay: Long): String? = ops.debugRestart(delay).awaitFirstOrNull()

    override suspend fun debugSdslen(key: K): String? = ops.debugSdslen(key).awaitFirstOrNull()

    override suspend fun debugSegfault() = ops.debugSegfault().awaitFirstOrNull().let { Unit }

    override suspend fun flushall(): String? = ops.flushall().awaitFirstOrNull()

    override suspend fun flushallAsync(): String? = ops.flushallAsync().awaitFirstOrNull()

    override suspend fun flushdb(): String? = ops.flushdb().awaitFirstOrNull()

    override suspend fun flushdbAsync(): String? = ops.flushdbAsync().awaitFirstOrNull()

    override suspend fun info(): String? = ops.info().awaitFirstOrNull()

    override suspend fun info(section: String): String? = ops.info(section).awaitFirstOrNull()

    override suspend fun lastsave(): Date? = ops.lastsave().awaitFirstOrNull()

    override suspend fun memoryUsage(key: K): Long? = ops.memoryUsage(key).awaitFirstOrNull()

    override suspend fun save(): String? = ops.save().awaitFirstOrNull()

    override suspend fun shutdown(save: Boolean) = ops.shutdown(save).awaitFirstOrNull().let { Unit }

    override suspend fun slaveof(host: String, port: Int): String? = ops.slaveof(host, port).awaitFirstOrNull()

    override suspend fun slaveofNoOne(): String? = ops.slaveofNoOne().awaitFirstOrNull()

    override suspend fun slowlogGet(): List<Any> = ops.slowlogGet().asFlow().toList()

    override suspend fun slowlogGet(count: Int): List<Any> = ops.slowlogGet(count).asFlow().toList()

    override suspend fun slowlogLen(): Long? = ops.slowlogLen().awaitFirstOrNull()

    override suspend fun slowlogReset(): String? = ops.slowlogReset().awaitFirstOrNull()

    override suspend fun time(): List<V> = ops.time().asFlow().toList()

}

