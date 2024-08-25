package gs2vs.core.redis;

import gs2vs.core.exception.CheckedException;
import gs2vs.core.exception.RedisException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.concurrent.ExecutionException;

public class RedisStringCommand {
    RedisClient redisClient;
    RedisAsyncCommands<String, String> asyncCommands;

    public RedisStringCommand(RedisClient redisClient) {
        this.redisClient = redisClient;
        StatefulRedisConnection<String, String> connect = this.redisClient.connect();
        this.asyncCommands = connect.async();
    }

    public String get(String key) {
        String result;
        try {
            result = asyncCommands.get(key).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CheckedException(e);
        }

        return result;
    }

    public String set(String key, String value) {
        String result;
        try {
            result = asyncCommands.set(key, value).get();
            if (!result.equals("OK")) {
                throw new RedisException("get unexpected result. result: " + result);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new CheckedException(e);
        }

        return result;
    }

}
