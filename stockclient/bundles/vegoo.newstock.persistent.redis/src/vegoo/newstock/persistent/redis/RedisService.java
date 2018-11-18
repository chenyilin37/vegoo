package vegoo.newstock.persistent.redis;

import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;

public interface RedisService extends JedisCommands,MultiKeyCommands {

}
