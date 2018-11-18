package vegoo.newstock.persistent.redis.core;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import vegoo.newstock.persistent.redis.RedisService;

public class RedisServiceImpl implements RedisService {

	private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);
	//static final String PN_HOST   = "host";
	//static final String PN_PORT   = "port";
	//static final String PN_PASSWORD   = "password";

	private JedisPool pool;
	
	public void initJedis(String host, int port, String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        
        config.setMaxTotal(200);
        config.setMaxIdle(50);
        config.setMinIdle(8);//设置最小空闲数
        config.setMaxWaitMillis(10000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        //Idle时进行连接扫描
        config.setTestWhileIdle(true);
        //表示idle object evitor两次扫描之间要sleep的毫秒数
        config.setTimeBetweenEvictionRunsMillis(30000);
        //表示idle object evitor每次扫描的最多的对象数
        config.setNumTestsPerEvictionRun(10);
        //表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
        config.setMinEvictableIdleTimeMillis(60000);

		//创建连接池
		pool = new JedisPool(config, host, port, 0, password, 2);
	}	
	
	public void destroyJedis() {
		pool.destroy();
	}

    static interface VoidCallBack {
    	void doInRedis(Jedis jedis);
    }
    
	public void execute(VoidCallBack action){
		Jedis jedis = pool.getResource();
		try {
			 action.doInRedis(jedis);
		}finally {
			if (jedis != null) {
				jedis.close();
			 }
		}
	}
	
    static interface ResultCallBack<T> {
    	T doInRedis(Jedis jedis);
    }

	private <T> T exec(ResultCallBack<T> action){
		Jedis jedis = pool.getResource();
		try {
			return action.doInRedis(jedis);
		}finally {
			if (jedis != null) {
				jedis.close();
			 }
		}
	}
	

	@Override
	public String set(String key, String value) {
		 return exec((jedis) -> jedis.set(key, value));
	}

	@Override
	public String set(String key, String value, String nxxx, String expx, long time) {
		return exec((jedis) -> jedis.set(key, value, nxxx, expx, time));
	}

	@Override
	public String set(String key, String value, String nxxx) {
		return exec((jedis) -> jedis.set(key, value, nxxx));
	}

	@Override
	public String get(String key) {
		return exec((jedis) -> jedis.get(key));
	}

	@Override
	public Boolean exists(String key) {
		return exec((jedis) -> jedis.exists(key));
	}

	@Override
	public Long persist(String key) {
		return exec((jedis) -> jedis.persist(key));
	}

	@Override
	public String type(String key) {
		return exec((jedis) -> jedis.type(key));
	}

	@Override
	public Long expire(String key, int seconds) {
		return exec((jedis) -> jedis.expire(key, seconds));
	}

	@Override
	public Long pexpire(String key, long milliseconds) {
		
		return exec((jedis) -> jedis.pexpire(key, milliseconds));
	}

	@Override
	public Long expireAt(String key, long unixTime) {
		
		return exec((jedis) -> jedis.expireAt(key, unixTime));
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		
		return exec((jedis) -> jedis.pexpireAt(key, millisecondsTimestamp));
	}

	@Override
	public Long ttl(String key) {
		
		return exec((jedis) -> jedis.ttl(key));
	}

	@Override
	public Long pttl(String key) {
		
		return exec((jedis) -> jedis.pttl(key));
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		
		return exec((jedis) -> jedis.setbit(key, offset, value));
	}

	@Override
	public Boolean setbit(String key, long offset, String value) {
		
		return exec((jedis) -> jedis.setbit(key, offset, value));
	}

	@Override
	public Boolean getbit(String key, long offset) {
		
		return exec((jedis) -> jedis.getbit(key, offset));
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		
		return exec((jedis) -> jedis.setrange(key, offset, value));
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		
		return exec((jedis) -> jedis.getrange(key, startOffset, endOffset));
	}

	@Override
	public String getSet(String key, String value) {
		
		return exec((jedis) -> jedis.getSet(key, value));
	}

	@Override
	public Long setnx(String key, String value) {
		
		return exec((jedis) -> jedis.setnx(key, value));
	}

	@Override
	public String setex(String key, int seconds, String value) {
		
		return exec((jedis) -> jedis.setex(key, seconds, value));
	}

	@Override
	public String psetex(String key, long milliseconds, String value) {
		
		return exec((jedis) -> jedis.psetex(key, milliseconds, value));
	}

	@Override
	public Long decrBy(String key, long integer) {
		
		return exec((jedis) -> jedis.decrBy(key, integer));
	}

	@Override
	public Long decr(String key) {
		
		return exec((jedis) -> jedis.decr(key));
	}

	@Override
	public Long incrBy(String key, long integer) {
		
		return exec((jedis) -> jedis.incrBy(key, integer));
	}

	@Override
	public Double incrByFloat(String key, double value) {
		
		return exec((jedis) -> jedis.incrByFloat(key, value));
	}

	@Override
	public Long incr(String key) {
		
		return exec((jedis) -> jedis.incr(key));
	}

	@Override
	public Long append(String key, String value) {
		
		return exec((jedis) -> jedis.append(key, value));
	}

	@Override
	public String substr(String key, int start, int end) {
		
		return exec((jedis) -> jedis.substr(key, start, end));
	}

	@Override
	public Long hset(String key, String field, String value) {
		
		return exec((jedis) -> jedis.hset(key, field, value));
	}

	@Override
	public String hget(String key, String field) {
		
		return exec((jedis) -> jedis.hget(key, field));
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		
		return exec((jedis) -> jedis.hsetnx(key, field, value));
	}

	@Override
	public String hmset(String key, Map<String, String> hash) {
		
		return exec((jedis) -> jedis.hmset(key, hash));
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		
		return exec((jedis) -> jedis.hmget(key, fields));
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		
		return exec((jedis) -> jedis.hincrBy(key, field, value));
	}

	@Override
	public Double hincrByFloat(String key, String field, double value) {
		
		return exec((jedis) -> jedis.hincrByFloat(key, field, value));
	}

	@Override
	public Boolean hexists(String key, String field) {
		
		return exec((jedis) -> jedis.hexists(key, field));
	}

	@Override
	public Long hdel(String key, String... field) {
		
		return exec((jedis) -> jedis.hdel(key, field));
	}

	@Override
	public Long hlen(String key) {
		
		return exec((jedis) -> jedis.hlen(key));
	}

	@Override
	public Set<String> hkeys(String key) {
		
		return exec((jedis) -> jedis.hkeys(key));
	}

	@Override
	public List<String> hvals(String key) {
		
		return exec((jedis) -> jedis.hvals(key));
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		
		return exec((jedis) -> jedis.hgetAll(key));
	}

	@Override
	public Long rpush(String key, String... strings) {
		
		return exec((jedis) -> jedis.rpush(key, strings));
	}

	@Override
	public Long lpush(String key, String... strings) {
		
		return exec((jedis) -> jedis.lpush(key, strings));
	}

	@Override
	public Long llen(String key) {
		
		return exec((jedis) -> jedis.llen(key));
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		
		return exec((jedis) -> jedis.lrange(key, start, end));
	}

	@Override
	public String ltrim(String key, long start, long end) {
		return exec((jedis) -> jedis.ltrim(key, start, end));
	}

	@Override
	public String lindex(String key, long index) {
		
		return exec((jedis) -> jedis.lindex(key, index));
	}

	@Override
	public String lset(String key, long index, String value) {
		return exec((jedis) -> jedis.lset(key, index, value));
	}

	@Override
	public Long lrem(String key, long count, String value) {
		return exec((jedis) -> jedis.lrem(key, count, value));
	}

	@Override
	public String lpop(String key) {
		
		return exec((jedis) -> jedis.lpop(key));
	}

	@Override
	public String rpop(String key) {
		return exec((jedis) -> jedis.rpop(key));
	}

	@Override
	public Long sadd(String key, String... members) {
		return exec((jedis) -> jedis.sadd(key, members));
	}

	@Override
	public Set<String> smembers(String key) {
		
		return exec((jedis) -> jedis.smembers(key));
	}

	@Override
	public Long srem(String key, String... member) {
		
		return exec((jedis) -> jedis.srem(key, member));
		
	}

	@Override
	public String spop(String key) {
		
		return exec((jedis) -> jedis.spop(key));
	}

	@Override
	public Set<String> spop(String key, long count) {
		
		return exec((jedis) -> jedis.spop(key, count));
	}

	@Override
	public Long scard(String key) {
		
		return exec((jedis) -> jedis.scard(key));
	}

	@Override
	public Boolean sismember(String key, String member) {
		
		return exec((jedis) -> jedis.sismember(key, member));
	}

	@Override
	public String srandmember(String key) {
		
		return exec((jedis) -> jedis.srandmember(key));
	}

	@Override
	public List<String> srandmember(String key, int count) {
		return exec((jedis) -> jedis.srandmember(key, count));
	}

	@Override
	public Long strlen(String key) {
		
		return exec((jedis) -> jedis.strlen(key));
	}

	@Override
	public Long zadd(String key, double score, String member) {
		
		return exec((jedis) -> jedis.zadd(key, score, member));
	}

	@Override
	public Long zadd(String key, double score, String member, ZAddParams params) {
		
		return exec((jedis) -> jedis.zadd(key, score, member, params));
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		
		return exec((jedis) -> jedis.zadd(key, scoreMembers));
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		
		return exec((jedis) -> jedis.zadd(key, scoreMembers, params));
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {
		
		return exec((jedis) -> jedis.zrange(key, start, end));
	}

	@Override
	public Long zrem(String key, String... members) {
		
		return exec((jedis) -> jedis.zrem(key, members));
	}

	@Override
	public Double zincrby(String key, double score, String member) {
		
		return exec((jedis) -> jedis.zincrby(key, score, member));
	}

	@Override
	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		
		return exec((jedis) -> jedis.zincrby(key, score, member, params));
	}

	@Override
	public Long zrank(String key, String member) {
		
		return exec((jedis) -> jedis.zrank(key, member));
	}

	@Override
	public Long zrevrank(String key, String member) {
		
		return exec((jedis) -> jedis.zrevrank(key, member));
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		
		return exec((jedis) -> jedis.zrevrange(key, start, end));
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		
		return exec((jedis) -> jedis.zrangeWithScores(key, start, end));
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		
		return exec((jedis) -> jedis.zrevrangeWithScores(key, start, end));
	}

	@Override
	public Long zcard(String key) {
		
		return exec((jedis) -> jedis.zcard(key));
	}

	@Override
	public Double zscore(String key, String member) {
		
		return exec((jedis) -> jedis.zscore(key, member));
	}

	@Override
	public List<String> sort(String key) {
		
		return exec((jedis) -> jedis.sort(key));
	}

	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		
		return exec((jedis) -> jedis.sort(key, sortingParameters));
	}

	@Override
	public Long zcount(String key, double min, double max) {
		
		return exec((jedis) -> jedis.zcount(key, min, max));
	}

	@Override
	public Long zcount(String key, String min, String max) {
		
		return exec((jedis) -> jedis.zcount(key, min, max));
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		
		return exec((jedis) -> jedis.zrangeByScore(key, min, max));
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		return exec((jedis) -> jedis.zrangeByScore(key, min, max));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return exec((jedis) -> jedis.zrevrangeByScore(key, max, min));
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return exec((jedis) -> jedis.zrangeByScore(key, min, max, offset, count));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return exec((jedis) -> jedis.zrevrangeByScore(key, max, min));
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return exec((jedis) -> jedis.zrangeByScore(key, max, min, offset, count));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return exec((jedis) -> jedis.zrevrangeByScore(key, max, min, offset, count));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return exec((jedis) -> jedis.zrangeByScoreWithScores(key, min, max));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		return exec((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return exec((jedis) -> jedis.zrangeByScoreWithScores(key, min, max,offset,count));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return exec((jedis) -> jedis.zrevrangeByScore(key, max, min, offset, count));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return exec((jedis) -> jedis.zrangeByScoreWithScores(key, min, max));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		return exec((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return exec((jedis) -> jedis.zrangeByScoreWithScores(key, min, max,offset,count));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		return exec((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		return exec((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		return exec((jedis) -> jedis.zremrangeByRank(key, start, end));
	}

	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		
		return exec((jedis) -> jedis.zremrangeByScore(key, start, end));
	}

	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		
		return exec((jedis) -> jedis.zremrangeByScore(key, start, end));
	}

	@Override
	public Long zlexcount(String key, String min, String max) {
		
		return exec((jedis) -> jedis.zlexcount(key, min, max));
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max) {
		
		return exec((jedis) -> jedis.zrangeByLex(key, min, max));
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		
		return exec((jedis) -> jedis.zrangeByLex(key, min, max, offset,count));
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		
		return exec((jedis) -> jedis.zrevrangeByLex(key, max, min));
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		
		return exec((jedis) -> jedis.zrevrangeByLex(key, min, max, offset, count));
	}

	@Override
	public Long zremrangeByLex(String key, String min, String max) {
		
		return exec((jedis) -> jedis.zremrangeByLex(key, min, max));
	}

	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		
		return exec((jedis) -> jedis.linsert(key, where, pivot, value));
	}

	@Override
	public Long lpushx(String key, String... string) {
		
		return exec((jedis) -> jedis.lpushx(key, string));
	}

	@Override
	public Long rpushx(String key, String... string) {
		
		return exec((jedis) -> jedis.rpushx(key, string));
	}

	@Override
	public List<String> blpop(String arg) {
		
		return exec((jedis) -> jedis.blpop(arg));
	}

	@Override
	public List<String> blpop(int timeout, String key) {
		
		return exec((jedis) -> jedis.blpop(timeout, key));
	}

	@Override
	public List<String> brpop(String arg) {
		
		return exec((jedis) -> jedis.brpop(arg));
	}

	@Override
	public List<String> brpop(int timeout, String key) {
		
		return exec((jedis) -> jedis.brpop(timeout, key));
	}

	@Override
	public Long del(String key) {
		
		return exec((jedis) -> jedis.del(key));
	}

	@Override
	public String echo(String string) {
		
		return exec((jedis) -> jedis.echo(string));
	}

	@Override
	public Long move(String key, int dbIndex) {
		
		return exec((jedis) -> jedis.move(key, dbIndex));
	}

	@Override
	public Long bitcount(String key) {
		
		return exec((jedis) -> jedis.bitcount(key));
	}

	@Override
	public Long bitcount(String key, long start, long end) {
		
		return exec((jedis) -> jedis.bitcount(key, start, end));
	}

	@Override
	public Long bitpos(String key, boolean value) {
		
		return exec((jedis) -> jedis.bitpos(key, value));
	}

	@Override
	public Long bitpos(String key, boolean value, BitPosParams params) {
		
		return exec((jedis) -> jedis.bitpos(key, value, params));
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		
		return exec((jedis) -> jedis.hscan(key, cursor));
	}

	@Override
	public ScanResult<String> sscan(String key, int cursor) {
		
		return exec((jedis) -> jedis.sscan(key, cursor));
	}

	@Override
	public ScanResult<Tuple> zscan(String key, int cursor) {
		
		return exec((jedis) -> jedis.zscan(key, cursor));
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		
		return exec((jedis) -> jedis.hscan(key, cursor));
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		
		return exec((jedis) -> jedis.hscan(key, cursor, params));
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor) {
		
		return exec((jedis) -> jedis.sscan(key, cursor));
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		
		return exec((jedis) -> jedis.sscan(key, cursor, params));
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor) {
		
		return exec((jedis) -> jedis.zscan(key, cursor));
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		
		return exec((jedis) -> jedis.zscan(key, cursor, params));
	}

	@Override
	public Long pfadd(String key, String... elements) {
		
		return exec((jedis) -> jedis.pfadd(key, elements));
	}

	@Override
	public long pfcount(String key) {
		
		return exec((jedis) -> jedis.pfcount(key));
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, String member) {
		
		return exec((jedis) -> jedis.geoadd(key, longitude, latitude, member));
	}

	@Override
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		
		return exec((jedis) -> jedis.geoadd(key, memberCoordinateMap));
	}

	@Override
	public Double geodist(String key, String member1, String member2) {
		
		return exec((jedis) -> jedis.geodist(key, member1, member2));
	}

	@Override
	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		
		return exec((jedis) -> jedis.geodist(key, member1, member2, unit));
	}

	@Override
	public List<String> geohash(String key, String... members) {
		
		return exec((jedis) -> jedis.geohash(key, members));
	}

	@Override
	public List<GeoCoordinate> geopos(String key, String... members) {
		
		return exec((jedis) -> jedis.geopos(key, members));
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		
		return exec((jedis) -> jedis.georadius(key, longitude, latitude, radius, unit));
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		
		return exec((jedis) -> jedis.georadius(key, longitude, latitude, radius, unit, param));
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		
		return exec((jedis) -> jedis.georadiusByMember(key, member, radius, unit));
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		
		return exec((jedis) -> jedis.georadiusByMember(key, member, radius, unit, param));
	}

	@Override
	public List<Long> bitfield(String key, String... arguments) {
		
		return exec((jedis) -> jedis.bitfield(key, arguments));
	}



	@Override
	public Long del(String... keys) {
		return exec((jedis) -> jedis.del(keys));
	}



	@Override
	public Long exists(String... keys) {
		return exec((jedis) -> jedis.exists(keys));
	}



	@Override
	public List<String> blpop(int timeout, String... keys) {
		
		return exec((jedis) -> jedis.blpop(timeout, keys));
	}



	@Override
	public List<String> brpop(int timeout, String... keys) {
		
		return exec((jedis) -> jedis.brpop(timeout, keys));
	}



	@Override
	public List<String> blpop(String... args) {
		
		return exec((jedis) -> jedis.blpop(args));
	}



	@Override
	public List<String> brpop(String... args) {
		return exec((jedis) -> jedis.brpop(args));
	}



	@Override
	public Set<String> keys(String pattern) {
		
		return exec((jedis) -> jedis.keys(pattern));
	}



	@Override
	public List<String> mget(String... keys) {
		
		return exec((jedis) -> jedis.mget(keys));
	}



	@Override
	public String mset(String... keysvalues) {
		
		return exec((jedis) -> jedis.mset(keysvalues));
	}



	@Override
	public Long msetnx(String... keysvalues) {
		
		return exec((jedis) -> jedis.msetnx(keysvalues));
	}



	@Override
	public String rename(String oldkey, String newkey) {
		
		return exec((jedis) -> jedis.rename(oldkey, newkey));
	}



	@Override
	public Long renamenx(String oldkey, String newkey) {
		
		return exec((jedis) -> jedis.renamenx(oldkey, newkey));
	}



	@Override
	public String rpoplpush(String srckey, String dstkey) {
		
		return exec((jedis) -> jedis.rpoplpush(srckey, dstkey));
	}



	@Override
	public Set<String> sdiff(String... keys) {
		
		return exec((jedis) -> jedis.sdiff(keys));
	}



	@Override
	public Long sdiffstore(String dstkey, String... keys) {
		
		return exec((jedis) -> jedis.sdiffstore(dstkey, keys));
	}



	@Override
	public Set<String> sinter(String... keys) {
		
		return exec((jedis) -> jedis.sinter(keys));
	}



	@Override
	public Long sinterstore(String dstkey, String... keys) {
		
		return exec((jedis) -> jedis.sinterstore(dstkey, keys));
	}



	@Override
	public Long smove(String srckey, String dstkey, String member) {
		
		return exec((jedis) -> jedis.smove(srckey, dstkey, member));
	}



	@Override
	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		
		return exec((jedis) -> jedis.sort(key, sortingParameters, dstkey));
	}



	@Override
	public Long sort(String key, String dstkey) {
		
		return exec((jedis) -> jedis.sort(key, dstkey));
	}



	@Override
	public Set<String> sunion(String... keys) {
		
		return exec((jedis) -> jedis.sunion(keys));
	}



	@Override
	public Long sunionstore(String dstkey, String... keys) {
		
		return exec((jedis) -> jedis.sunionstore(dstkey, keys));
	}



	@Override
	public String watch(String... keys) {
		
		return exec((jedis) -> jedis.watch(keys));
	}



	@Override
	public String unwatch() {
		
		return exec((jedis) -> jedis.unwatch());
	}



	@Override
	public Long zinterstore(String dstkey, String... sets) {
		
		return exec((jedis) -> jedis.zinterstore(dstkey, sets));
	}



	@Override
	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		
		return exec((jedis) -> jedis.zinterstore(dstkey, sets));
	}



	@Override
	public Long zunionstore(String dstkey, String... sets) {
		
		return exec((jedis) -> jedis.zunionstore(dstkey, sets));
	}



	@Override
	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		
		return exec((jedis) -> jedis.zunionstore(dstkey, sets));
	}



	@Override
	public String brpoplpush(String source, String destination, int timeout) {
		
		return exec((jedis) -> jedis.brpoplpush(source, destination, timeout));
	}



	@Override
	public Long publish(String channel, String message) {
		
		return exec((jedis) -> jedis.publish(channel, message));
	}



	@Override
	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
		Jedis jedis = pool.getResource();
		try {
			jedis.subscribe(jedisPubSub, channels);
		}finally {
			if (jedis != null) {
			    jedis.close();
			 }
		}
	}

	@Override
	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
		Jedis jedis = pool.getResource();
		try {
			jedis.psubscribe(jedisPubSub, patterns);
		}finally {
			if (jedis != null) {
			    jedis.close();
			 }
		}
	}



	@Override
	public String randomKey() {
		return exec((jedis) -> jedis.randomKey());
	}

	@Override
	public Long bitop(BitOP op, String destKey, String... srcKeys) {
		return exec((jedis) -> jedis.bitop(op, destKey, srcKeys));
	}

	@Override
	public ScanResult<String> scan(int cursor) {
		return exec((jedis) -> jedis.scan(cursor));
	}

	@Override
	public ScanResult<String> scan(String cursor) {
		return exec((jedis) -> jedis.scan(cursor));
	}

	@Override
	public ScanResult<String> scan(String cursor, ScanParams params) {
		return exec((jedis) -> jedis.scan(cursor, params));
	}

	@Override
	public String pfmerge(String destkey, String... sourcekeys) {
		return exec((jedis) -> jedis.pfmerge(destkey, sourcekeys));
	}

	@Override
	public long pfcount(String... keys) {
		return exec((jedis) -> jedis.pfcount(keys));
	}

	public String getNearest(String key, double score, boolean reverse) {
		Set<String> results = reverse?zrevrangeByScore(key, score, 0, 0, 1)
				                     :zrangeByScore(key, score, Double.MAX_VALUE, 0, 1);
		
		for(String s : results) {
			return s;
		}

		return null;
	}
	
	public Set<String> getTopN(String key,int count, boolean reverse){
		return reverse?zrevrange(key, 0, count-1)
                      :zrange(key, 0, count-1);
		
	}
	
	/*
	public static void main(String[] args) {
		RedisServiceImpl redis = new RedisServiceImpl();
		redis.initJedis("127.0.0.1" ,6379, null);
		
		Set<String> stocks = redis.zrange("STOCKS", 0, 10);
		
		testWrapper2(stocks, redis);
		testWrapper1(stocks, redis);
	}
	
	private static void testWrapper1(Set<String> stocks, RedisServiceImpl redis) {
		double result = 0;
		int items = 0;
		
		long starttime = System.currentTimeMillis();
		
		for(String stockCode : stocks) {
			Set<String> dates = redis.zrange("KD:"+stockCode, 0, -1);
		    result += testWrapper1(stockCode, dates, redis);
		    items += dates.size();
		}
		
		long millis = System.currentTimeMillis()-starttime;
		
		System.out.println(String.format("TestWRP: read %d items,value=%f used %dms, avg:%f", items, result, millis, (double)millis/items));
	}

	private static double testWrapper1(String stockCode,Set<String> dates, RedisServiceImpl redis) {
		double result = 0;
		for(String d:dates) {
			String vol = redis.hget("KD:"+stockCode+":"+d, "V");
			result += Double.parseDouble(vol);
		}
		return result;
	}

	private static void testWrapper2(Set<String> stocks, RedisServiceImpl redis) {

		redis.execute((jedis)->{
			double result = 0;
			int items = 0;
			
			long starttime = System.currentTimeMillis();
			for(String stockCode : stocks) {
				Set<String> dates = redis.zrange("KD:"+stockCode, 0, -1);
			    result += testWrapper2(stockCode, dates, jedis);
			    items += dates.size();
			}
			long millis = System.currentTimeMillis()-starttime;
			
			System.out.println(String.format("TestRAW: read %d items,value=%f used %dms, avg:%f", items, result, millis, (double)millis/items));
			
		});
	}

	private static double testWrapper2(String stockCode,Set<String> dates, Jedis jedis) {
		double result = 0;
		for(String d:dates) {
			String vol = jedis.hget("KD:"+stockCode+":"+d, "V");
			result += Double.parseDouble(vol);
		}
		return result;
	}
	*/
}
