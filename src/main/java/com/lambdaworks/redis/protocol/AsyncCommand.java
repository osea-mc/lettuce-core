package com.lambdaworks.redis.protocol;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.RedisCommandInterruptedException;
import com.lambdaworks.redis.RedisFuture;
import io.netty.buffer.ByteBuf;

/**
 * An asynchronous redis command and its result. All successfully executed commands will eventually return a
 * {@link CommandOutput} object.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @param <T> Command output type.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class AsyncCommand<K, V, T> extends CompletableFuture<T> implements RedisCommand<K, V, T>, RedisFuture<T> {

    protected RedisCommand<K, V, T> command;
    protected CountDownLatch latch = new CountDownLatch(1);

    public AsyncCommand(RedisCommand<K, V, T> command) {
        this.command = command;
    }

    /**
     * Wait up to the specified time for the command output to become available.
     *
     * @param timeout Maximum time to wait for a result.
     * @param unit Unit of time for the timeout.
     *
     * @return true if the output became available.
     */
    @Override
    public boolean await(long timeout, TimeUnit unit) {
        try {
            return latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisCommandInterruptedException(e);
        }
    }

    /**
     * Get the object that holds this command's output.
     * 
     * @return The command output object.
     */
    @Override
    public CommandOutput<K, V, T> getOutput() {
        return command.getOutput();
    }

    /**
     * Mark this command complete and notify all waiting threads.
     */
    @Override
    public void complete() {
        latch.countDown();
        if (latch.getCount() == 0) {
            command.complete();
            completeResult();
        }
    }

    protected void completeResult() {
        if (command.getOutput() == null) {
            complete(null);
        } else if (command.getOutput().hasError()) {
            completeExceptionally(new RedisCommandExecutionException(command.getOutput().getError()));
        } else {
            complete(command.getOutput().get());
        }
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        command.completeExceptionally(ex);
        return super.completeExceptionally(ex);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        command.cancel();
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public String getError() {
        return command.getOutput().getError();
    }

    @Override
    public CommandArgs<K, V> getArgs() {
        return command.getArgs();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [type=").append(getType());
        sb.append(", output=").append(getOutput());
        sb.append(']');
        return sb.toString();
    }

    @Override
    public ProtocolKeyword getType() {
        return command.getType();
    }

    @Override
    public void cancel() {
        cancel(true);
    }

    @Override
    public void encode(ByteBuf buf) {
        command.encode(buf);
    }

    @Override
    public void setOutput(CommandOutput<K, V, T> output) {
        command.setOutput(output);
    }
}
