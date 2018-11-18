package vegoo.stockcommon.db.redis.stock;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import redis.clients.jedis.Tuple;
import vegoo.commons.DateUtil;
import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.BlockService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.dao.StockCapitalDao;
import vegoo.stockcommon.dao.StockDao;
import vegoo.stockcommon.db.redis.block.BlockStockServiceImpl;

@Component(immediate = true, service = { StockService.class,
		ManagedService.class }, configurationPid = "stockdata.db.stock")
public class StockServiceImpl extends BlockStockServiceImpl
		implements StockService, ManagedService {
	static final String KEY_STOCKS = "STOCKS";

	@Reference
	protected RedisService redis;

	static String getKeyOfStock(String stockCode) {
		return getKey(TBL_STOCK, stockCode);
	}

	static String getKeyOfCapital(String stockCode) {
		return getKey(TBL_CAPITAL, stockCode);
	}

	static String getKeyOfCapital(String stockCode, Date date) {
		return getKey(TBL_CAPITAL, stockCode, date);
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		

	}

	@Override
	protected RedisService getRedis() {
		return redis;
	}

	@Override
	public boolean existStock(String stkCode) {
		String key = getKeyOfStock(stkCode);
		return redis.exists(key);
	}

	@Override
	public void insertStock(StockDao dao) {
		Map<String, String> props = new HashMap<>();

		props.put("CODE", dao.getCode());
		props.put("NAME", dao.getName());
		props.put("MARKET", String.valueOf(dao.getMarketId()));

		Date pdate = dao.getPublicDate();
		if (pdate != null) {
			props.put("PDATE", DateUtil.formatDate(pdate));
		}

		redis.hmset(getKeyOfStock(dao.getCode()), props);
		redis.zadd(KEY_STOCKS, dao.getMarketId(), dao.getCode());
	}

	@Override
	public void updateStock(String stkCode, String name) {
		redis.hset(getKeyOfStock(stkCode), "NAME", name);
	}

	public void deleteStock(String stockCode) {
		deleteAllBlocksOfStock(stockCode);
		redis.del(getKeyOfStock(stockCode));
		redis.zrem(KEY_STOCKS, stockCode);
	}

	@Override
	public Set<String> getStockCodes() {
		return redis.zrange(KEY_STOCKS, 0, -1);
	}

	@Override
	public Set<String> getStockCodes(int marketId) {
		return redis.zrangeByScore(KEY_STOCKS, marketId, marketId);
	}
	
	@Override
	public Set<String> getStockUCodes() {
		Set<String> result = new HashSet<>();
		Set<Tuple> tuples = redis.zrangeWithScores(KEY_STOCKS, 0, -1);
		for (Tuple tuple : tuples) {
			long marketId = Math.round(tuple.getScore());
			result.add(String.format("%s%d", tuple.getElement(), marketId));
		}
		return result;
	}

	@Override
	public boolean existStockCapital(String stkcode, Date rDate) {
		return redis.exists(getKeyOfCapital(stkcode, rDate));
	}

	@Override
	public void insertStockCapital(StockCapitalDao dao) {
		Date rDate = dao.getTransDate();

		Map<String, String> props = new HashMap<>();
		props.put("LTG", dao.getLtg() + "");
		// props.put("ZGB", dao.getLtg()+"");

		redis.hmset(getKeyOfCapital(dao.getStockCode(), rDate), props);
		redis.zadd(getKeyOfCapital(dao.getStockCode()), rDate.getTime(), DateUtil.formatDate(rDate));
	}


}
