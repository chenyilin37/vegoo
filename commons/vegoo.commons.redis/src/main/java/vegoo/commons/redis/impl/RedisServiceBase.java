package vegoo.commons.redis.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

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
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import vegoo.commons.DateUtil;
import vegoo.commons.redis.RedisService;

public class RedisServiceBase  implements RedisService {
	private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

	private JedisPool pool;

	
	private String host;
	private String password;
	private int port;
	
	public void initJedis(String host, int port, String password) {
		this.host = host;
		this.password = password;
		this.port = port;

        JedisPoolConfig config = new JedisPoolConfig();
        
		//设置最大连接数（100个足够用了，没必要设置太大）
		config.setMaxTotal(100);
		//最大空闲连接数
		config.setMaxIdle(10);
		//获取Jedis连接的最大等待时间（50秒） 
		config.setMaxWaitMillis(50 * 1000);
		//在获取Jedis连接时，自动检验连接是否可用
		config.setTestOnBorrow(true);
		//在将连接放回池中前，自动检验连接是否有效
		config.setTestOnReturn(true);
		//自动测试池中的空闲连接是否都是可用连接
		config.setTestWhileIdle(true);
		
		//创建连接池
		pool = new JedisPool(config, host, port, 0, password, 2);
	}	
	
	public void destroyJedis() {
		pool.destroy();
	}

    static interface JedisCallBack<T> {
    	T doInRedis(Jedis jedis);
    }

	protected <T> T excute(JedisCallBack<T> action){
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
		return excute((jedis) -> jedis.set(key, value));
	}

	@Override
	public String set(String key, String value, String nxxx, String expx, long time) {
		return excute((jedis) -> jedis.set(key, value, nxxx, expx, time));
	}

	@Override
	public String set(String key, String value, String nxxx) {
		return excute((jedis) -> jedis.set(key, value, nxxx));
	}

	@Override
	public String get(String key) {

		return excute((jedis) -> jedis.get(key));
	}

	@Override
	public Boolean exists(String key) {
		return excute((jedis) -> jedis.exists(key));
	}

	@Override
	public Long persist(String key) {
		
		return excute((jedis) -> jedis.persist(key));
	}

	@Override
	public String type(String key) {
		return excute((jedis) -> jedis.type(key));
	}

	@Override
	public Long expire(String key, int seconds) {
		return excute((jedis) -> jedis.expire(key, seconds));
	}

	@Override
	public Long pexpire(String key, long milliseconds) {
		
		return excute((jedis) -> jedis.pexpire(key, milliseconds));
	}

	@Override
	public Long expireAt(String key, long unixTime) {
		
		return excute((jedis) -> jedis.expireAt(key, unixTime));
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		
		return excute((jedis) -> jedis.pexpireAt(key, millisecondsTimestamp));
	}

	@Override
	public Long ttl(String key) {
		
		return excute((jedis) -> jedis.ttl(key));
	}

	@Override
	public Long pttl(String key) {
		
		return excute((jedis) -> jedis.pttl(key));
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		
		return excute((jedis) -> jedis.setbit(key, offset, value));
	}

	@Override
	public Boolean setbit(String key, long offset, String value) {
		
		return excute((jedis) -> jedis.setbit(key, offset, value));
	}

	@Override
	public Boolean getbit(String key, long offset) {
		
		return excute((jedis) -> jedis.getbit(key, offset));
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		
		return excute((jedis) -> jedis.setrange(key, offset, value));
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		
		return excute((jedis) -> jedis.getrange(key, startOffset, endOffset));
	}

	@Override
	public String getSet(String key, String value) {
		
		return excute((jedis) -> jedis.getSet(key, value));
	}

	@Override
	public Long setnx(String key, String value) {
		
		return excute((jedis) -> jedis.setnx(key, value));
	}

	@Override
	public String setex(String key, int seconds, String value) {
		
		return excute((jedis) -> jedis.setex(key, seconds, value));
	}

	@Override
	public String psetex(String key, long milliseconds, String value) {
		
		return excute((jedis) -> jedis.psetex(key, milliseconds, value));
	}

	@Override
	public Long decrBy(String key, long integer) {
		
		return excute((jedis) -> jedis.decrBy(key, integer));
	}

	@Override
	public Long decr(String key) {
		
		return excute((jedis) -> jedis.decr(key));
	}

	@Override
	public Long incrBy(String key, long integer) {
		
		return excute((jedis) -> jedis.incrBy(key, integer));
	}

	@Override
	public Double incrByFloat(String key, double value) {
		
		return excute((jedis) -> jedis.incrByFloat(key, value));
	}

	@Override
	public Long incr(String key) {
		
		return excute((jedis) -> jedis.incr(key));
	}

	@Override
	public Long append(String key, String value) {
		
		return excute((jedis) -> jedis.append(key, value));
	}

	@Override
	public String substr(String key, int start, int end) {
		
		return excute((jedis) -> jedis.substr(key, start, end));
	}

	@Override
	public Long hset(String key, String field, String value) {
		
		return excute((jedis) -> jedis.hset(key, field, value));
	}

	@Override
	public String hget(String key, String field) {
		
		return excute((jedis) -> jedis.hget(key, field));
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		
		return excute((jedis) -> jedis.hsetnx(key, field, value));
	}

	@Override
	public String hmset(String key, Map<String, String> hash) {
		
		return excute((jedis) -> jedis.hmset(key, hash));
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		
		return excute((jedis) -> jedis.hmget(key, fields));
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		
		return excute((jedis) -> jedis.hincrBy(key, field, value));
	}

	@Override
	public Double hincrByFloat(String key, String field, double value) {
		
		return excute((jedis) -> jedis.hincrByFloat(key, field, value));
	}

	@Override
	public Boolean hexists(String key, String field) {
		
		return excute((jedis) -> jedis.hexists(key, field));
	}

	@Override
	public Long hdel(String key, String... field) {
		
		return excute((jedis) -> jedis.hdel(key, field));
	}

	@Override
	public Long hlen(String key) {
		
		return excute((jedis) -> jedis.hlen(key));
	}

	@Override
	public Set<String> hkeys(String key) {
		
		return excute((jedis) -> jedis.hkeys(key));
	}

	@Override
	public List<String> hvals(String key) {
		
		return excute((jedis) -> jedis.hvals(key));
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		
		return excute((jedis) -> jedis.hgetAll(key));
	}

	@Override
	public Long rpush(String key, String... strings) {
		
		return excute((jedis) -> jedis.rpush(key, strings));
	}

	@Override
	public Long lpush(String key, String... strings) {
		
		return excute((jedis) -> jedis.lpush(key, strings));
	}

	@Override
	public Long llen(String key) {
		
		return excute((jedis) -> jedis.llen(key));
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		
		return excute((jedis) -> jedis.lrange(key, start, end));
	}

	@Override
	public String ltrim(String key, long start, long end) {
		return excute((jedis) -> jedis.ltrim(key, start, end));
	}

	@Override
	public String lindex(String key, long index) {
		
		return excute((jedis) -> jedis.lindex(key, index));
	}

	@Override
	public String lset(String key, long index, String value) {
		return excute((jedis) -> jedis.lset(key, index, value));
	}

	@Override
	public Long lrem(String key, long count, String value) {
		return excute((jedis) -> jedis.lrem(key, count, value));
	}

	@Override
	public String lpop(String key) {
		
		return excute((jedis) -> jedis.lpop(key));
	}

	@Override
	public String rpop(String key) {
		return excute((jedis) -> jedis.rpop(key));
	}

	@Override
	public Long sadd(String key, String... members) {
		return excute((jedis) -> jedis.sadd(key, members));
	}

	@Override
	public Set<String> smembers(String key) {
		
		return excute((jedis) -> jedis.smembers(key));
	}

	@Override
	public Long srem(String key, String... member) {
		
		return excute((jedis) -> jedis.srem(key, member));
		
	}

	@Override
	public String spop(String key) {
		
		return excute((jedis) -> jedis.spop(key));
	}

	@Override
	public Set<String> spop(String key, long count) {
		
		return excute((jedis) -> jedis.spop(key, count));
	}

	@Override
	public Long scard(String key) {
		
		return excute((jedis) -> jedis.scard(key));
	}

	@Override
	public Boolean sismember(String key, String member) {
		
		return excute((jedis) -> jedis.sismember(key, member));
	}

	@Override
	public String srandmember(String key) {
		
		return excute((jedis) -> jedis.srandmember(key));
	}

	@Override
	public List<String> srandmember(String key, int count) {
		return excute((jedis) -> jedis.srandmember(key, count));
	}

	@Override
	public Long strlen(String key) {
		
		return excute((jedis) -> jedis.strlen(key));
	}

	@Override
	public Long zadd(String key, double score, String member) {
		
		return excute((jedis) -> jedis.zadd(key, score, member));
	}

	@Override
	public Long zadd(String key, double score, String member, ZAddParams params) {
		
		return excute((jedis) -> jedis.zadd(key, score, member, params));
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		
		return excute((jedis) -> jedis.zadd(key, scoreMembers));
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		
		return excute((jedis) -> jedis.zadd(key, scoreMembers, params));
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {
		
		return excute((jedis) -> jedis.zrange(key, start, end));
	}

	@Override
	public Long zrem(String key, String... members) {
		
		return excute((jedis) -> jedis.zrem(key, members));
	}

	@Override
	public Double zincrby(String key, double score, String member) {
		
		return excute((jedis) -> jedis.zincrby(key, score, member));
	}

	@Override
	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		
		return excute((jedis) -> jedis.zincrby(key, score, member, params));
	}

	@Override
	public Long zrank(String key, String member) {
		
		return excute((jedis) -> jedis.zrank(key, member));
	}

	@Override
	public Long zrevrank(String key, String member) {
		
		return excute((jedis) -> jedis.zrevrank(key, member));
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		
		return excute((jedis) -> jedis.zrevrange(key, start, end));
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		
		return excute((jedis) -> jedis.zrangeWithScores(key, start, end));
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		
		return excute((jedis) -> jedis.zrevrangeWithScores(key, start, end));
	}

	@Override
	public Long zcard(String key) {
		
		return excute((jedis) -> jedis.zcard(key));
	}

	@Override
	public Double zscore(String key, String member) {
		
		return excute((jedis) -> jedis.zscore(key, member));
	}

	@Override
	public List<String> sort(String key) {
		
		return excute((jedis) -> jedis.sort(key));
	}

	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		
		return excute((jedis) -> jedis.sort(key, sortingParameters));
	}

	@Override
	public Long zcount(String key, double min, double max) {
		
		return excute((jedis) -> jedis.zcount(key, min, max));
	}

	@Override
	public Long zcount(String key, String min, String max) {
		
		return excute((jedis) -> jedis.zcount(key, min, max));
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		
		return excute((jedis) -> jedis.zrangeByScore(key, min, max));
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		return excute((jedis) -> jedis.zrangeByScore(key, min, max));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return excute((jedis) -> jedis.zrevrangeByScore(key, max, min));
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return excute((jedis) -> jedis.zrangeByScore(key, min, max, offset, count));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return excute((jedis) -> jedis.zrevrangeByScore(key, max, min));
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return excute((jedis) -> jedis.zrangeByScore(key, max, min, offset, count));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return excute((jedis) -> jedis.zrevrangeByScore(key, max, min, offset, count));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return excute((jedis) -> jedis.zrangeByScoreWithScores(key, min, max));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		return excute((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return excute((jedis) -> jedis.zrangeByScoreWithScores(key, min, max,offset,count));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return excute((jedis) -> jedis.zrevrangeByScore(key, max, min, offset, count));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return excute((jedis) -> jedis.zrangeByScoreWithScores(key, min, max));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		return excute((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return excute((jedis) -> jedis.zrangeByScoreWithScores(key, min, max,offset,count));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		return excute((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		return excute((jedis) -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		return excute((jedis) -> jedis.zremrangeByRank(key, start, end));
	}

	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		
		return excute((jedis) -> jedis.zremrangeByScore(key, start, end));
	}

	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		
		return excute((jedis) -> jedis.zremrangeByScore(key, start, end));
	}

	@Override
	public Long zlexcount(String key, String min, String max) {
		
		return excute((jedis) -> jedis.zlexcount(key, min, max));
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max) {
		
		return excute((jedis) -> jedis.zrangeByLex(key, min, max));
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		
		return excute((jedis) -> jedis.zrangeByLex(key, min, max, offset,count));
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		
		return excute((jedis) -> jedis.zrevrangeByLex(key, max, min));
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		
		return excute((jedis) -> jedis.zrevrangeByLex(key, min, max, offset, count));
	}

	@Override
	public Long zremrangeByLex(String key, String min, String max) {
		
		return excute((jedis) -> jedis.zremrangeByLex(key, min, max));
	}

	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		
		return excute((jedis) -> jedis.linsert(key, where, pivot, value));
	}

	@Override
	public Long lpushx(String key, String... string) {
		
		return excute((jedis) -> jedis.lpushx(key, string));
	}

	@Override
	public Long rpushx(String key, String... string) {
		
		return excute((jedis) -> jedis.rpushx(key, string));
	}

	@Override
	public List<String> blpop(String arg) {
		
		return excute((jedis) -> jedis.blpop(arg));
	}

	@Override
	public List<String> blpop(int timeout, String key) {
		
		return excute((jedis) -> jedis.blpop(timeout, key));
	}

	@Override
	public List<String> brpop(String arg) {
		
		return excute((jedis) -> jedis.brpop(arg));
	}

	@Override
	public List<String> brpop(int timeout, String key) {
		
		return excute((jedis) -> jedis.brpop(timeout, key));
	}

	@Override
	public Long del(String key) {
		
		return excute((jedis) -> jedis.del(key));
	}

	@Override
	public String echo(String string) {
		
		return excute((jedis) -> jedis.echo(string));
	}

	@Override
	public Long move(String key, int dbIndex) {
		
		return excute((jedis) -> jedis.move(key, dbIndex));
	}

	@Override
	public Long bitcount(String key) {
		
		return excute((jedis) -> jedis.bitcount(key));
	}

	@Override
	public Long bitcount(String key, long start, long end) {
		
		return excute((jedis) -> jedis.bitcount(key, start, end));
	}

	@Override
	public Long bitpos(String key, boolean value) {
		
		return excute((jedis) -> jedis.bitpos(key, value));
	}

	@Override
	public Long bitpos(String key, boolean value, BitPosParams params) {
		
		return excute((jedis) -> jedis.bitpos(key, value, params));
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		
		return excute((jedis) -> jedis.hscan(key, cursor));
	}

	@Override
	public ScanResult<String> sscan(String key, int cursor) {
		
		return excute((jedis) -> jedis.sscan(key, cursor));
	}

	@Override
	public ScanResult<Tuple> zscan(String key, int cursor) {
		
		return excute((jedis) -> jedis.zscan(key, cursor));
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		
		return excute((jedis) -> jedis.hscan(key, cursor));
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		
		return excute((jedis) -> jedis.hscan(key, cursor, params));
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor) {
		
		return excute((jedis) -> jedis.sscan(key, cursor));
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		
		return excute((jedis) -> jedis.sscan(key, cursor, params));
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor) {
		
		return excute((jedis) -> jedis.zscan(key, cursor));
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		
		return excute((jedis) -> jedis.zscan(key, cursor, params));
	}

	@Override
	public Long pfadd(String key, String... elements) {
		
		return excute((jedis) -> jedis.pfadd(key, elements));
	}

	@Override
	public long pfcount(String key) {
		
		return excute((jedis) -> jedis.pfcount(key));
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, String member) {
		
		return excute((jedis) -> jedis.geoadd(key, longitude, latitude, member));
	}

	@Override
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		
		return excute((jedis) -> jedis.geoadd(key, memberCoordinateMap));
	}

	@Override
	public Double geodist(String key, String member1, String member2) {
		
		return excute((jedis) -> jedis.geodist(key, member1, member2));
	}

	@Override
	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		
		return excute((jedis) -> jedis.geodist(key, member1, member2, unit));
	}

	@Override
	public List<String> geohash(String key, String... members) {
		
		return excute((jedis) -> jedis.geohash(key, members));
	}

	@Override
	public List<GeoCoordinate> geopos(String key, String... members) {
		
		return excute((jedis) -> jedis.geopos(key, members));
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		
		return excute((jedis) -> jedis.georadius(key, longitude, latitude, radius, unit));
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		
		return excute((jedis) -> jedis.georadius(key, longitude, latitude, radius, unit, param));
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		
		return excute((jedis) -> jedis.georadiusByMember(key, member, radius, unit));
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		
		return excute((jedis) -> jedis.georadiusByMember(key, member, radius, unit, param));
	}

	@Override
	public List<Long> bitfield(String key, String... arguments) {
		
		return excute((jedis) -> jedis.bitfield(key, arguments));
	}



	@Override
	public Long del(String... keys) {
		return excute((jedis) -> jedis.del(keys));
	}



	@Override
	public Long exists(String... keys) {
		return excute((jedis) -> jedis.exists(keys));
	}



	@Override
	public List<String> blpop(int timeout, String... keys) {
		
		return excute((jedis) -> jedis.blpop(timeout, keys));
	}



	@Override
	public List<String> brpop(int timeout, String... keys) {
		
		return excute((jedis) -> jedis.brpop(timeout, keys));
	}



	@Override
	public List<String> blpop(String... args) {
		
		return excute((jedis) -> jedis.blpop(args));
	}



	@Override
	public List<String> brpop(String... args) {
		return excute((jedis) -> jedis.brpop(args));
	}



	@Override
	public Set<String> keys(String pattern) {
		
		return excute((jedis) -> jedis.keys(pattern));
	}



	@Override
	public List<String> mget(String... keys) {
		
		return excute((jedis) -> jedis.mget(keys));
	}



	@Override
	public String mset(String... keysvalues) {
		
		return excute((jedis) -> jedis.mset(keysvalues));
	}



	@Override
	public Long msetnx(String... keysvalues) {
		
		return excute((jedis) -> jedis.msetnx(keysvalues));
	}



	@Override
	public String rename(String oldkey, String newkey) {
		
		return excute((jedis) -> jedis.rename(oldkey, newkey));
	}



	@Override
	public Long renamenx(String oldkey, String newkey) {
		
		return excute((jedis) -> jedis.renamenx(oldkey, newkey));
	}



	@Override
	public String rpoplpush(String srckey, String dstkey) {
		
		return excute((jedis) -> jedis.rpoplpush(srckey, dstkey));
	}



	@Override
	public Set<String> sdiff(String... keys) {
		
		return excute((jedis) -> jedis.sdiff(keys));
	}



	@Override
	public Long sdiffstore(String dstkey, String... keys) {
		
		return excute((jedis) -> jedis.sdiffstore(dstkey, keys));
	}



	@Override
	public Set<String> sinter(String... keys) {
		
		return excute((jedis) -> jedis.sinter(keys));
	}



	@Override
	public Long sinterstore(String dstkey, String... keys) {
		
		return excute((jedis) -> jedis.sinterstore(dstkey, keys));
	}



	@Override
	public Long smove(String srckey, String dstkey, String member) {
		
		return excute((jedis) -> jedis.smove(srckey, dstkey, member));
	}



	@Override
	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		
		return excute((jedis) -> jedis.sort(key, sortingParameters, dstkey));
	}



	@Override
	public Long sort(String key, String dstkey) {
		
		return excute((jedis) -> jedis.sort(key, dstkey));
	}



	@Override
	public Set<String> sunion(String... keys) {
		
		return excute((jedis) -> jedis.sunion(keys));
	}



	@Override
	public Long sunionstore(String dstkey, String... keys) {
		
		return excute((jedis) -> jedis.sunionstore(dstkey, keys));
	}



	@Override
	public String watch(String... keys) {
		
		return excute((jedis) -> jedis.watch(keys));
	}



	@Override
	public String unwatch() {
		
		return excute((jedis) -> jedis.unwatch());
	}



	@Override
	public Long zinterstore(String dstkey, String... sets) {
		
		return excute((jedis) -> jedis.zinterstore(dstkey, sets));
	}



	@Override
	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		
		return excute((jedis) -> jedis.zinterstore(dstkey, sets));
	}



	@Override
	public Long zunionstore(String dstkey, String... sets) {
		
		return excute((jedis) -> jedis.zunionstore(dstkey, sets));
	}



	@Override
	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		
		return excute((jedis) -> jedis.zunionstore(dstkey, sets));
	}



	@Override
	public String brpoplpush(String source, String destination, int timeout) {
		
		return excute((jedis) -> jedis.brpoplpush(source, destination, timeout));
	}



	@Override
	public Long publish(String channel, String message) {
		
		return excute((jedis) -> jedis.publish(channel, message));
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
		return excute((jedis) -> jedis.randomKey());
	}



	@Override
	public Long bitop(BitOP op, String destKey, String... srcKeys) {
		return excute((jedis) -> jedis.bitop(op, destKey, srcKeys));
	}



	@Override
	public ScanResult<String> scan(int cursor) {
		return excute((jedis) -> jedis.scan(cursor));
	}



	@Override
	public ScanResult<String> scan(String cursor) {
		return excute((jedis) -> jedis.scan(cursor));
	}



	@Override
	public ScanResult<String> scan(String cursor, ScanParams params) {
		return excute((jedis) -> jedis.scan(cursor, params));
	}

	@Override
	public String pfmerge(String destkey, String... sourcekeys) {
		
		return excute((jedis) -> jedis.pfmerge(destkey, sourcekeys));
	}

	@Override
	public long pfcount(String... keys) {
		
		return excute((jedis) -> jedis.pfcount(keys));
	}

	//////////////////
	@Override
	public String getStringValue(String key) {
		return get(key);
	}

	
	@Override
	public Date getDateValue(String key) throws ParseException {
		String s = get(key);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDate(s);
	}
	
	@Override
	public Date getDateTimeValue(String key) throws ParseException {
		String s = get(key);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDateTime(s);
	}

	@Override
	public double getDoubleValue(String key) {
		String s = get(key);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Double.parseDouble(s);
	}

	@Override
	public int getIntValue(String key) {
		String s = get(key);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Integer.parseInt(s);
	}
	
	@Override
	public long getLongValue(String key) {
		String s = get(key);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Long.parseLong(s);
	}

	@Override
	public boolean getBoolValue(String key) {
		String s = get(key);
		if(Strings.isNullOrEmpty(s)) {
			return false;
		}
		return Boolean.parseBoolean(s);
	}

	@Override
	public String getStringValue(String key, String field) {
		return hget(key,field);
	}

	@Override
	public Date getDateValue(String key, String field) throws ParseException {
		String s = hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDate(s);
	}
	
	@Override
	public Date getDateTimeValue(String key, String field) throws ParseException {
		String s = hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDateTime(s);
	}

	@Override
	public double getDoubleValue(String key, String field) {
		String s = hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Double.parseDouble(s);
	}

	@Override
	public int getIntValue(String key, String field) {
		String s = hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Integer.parseInt(s);
	}
	
	@Override
	public long getLongValue(String key, String field) {
		String s = hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Long.parseLong(s);
	}

	@Override
	public boolean getBoolValue(String key, String field) {
		String s = hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return false;
		}
		return Boolean.parseBoolean(s);
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
	

}