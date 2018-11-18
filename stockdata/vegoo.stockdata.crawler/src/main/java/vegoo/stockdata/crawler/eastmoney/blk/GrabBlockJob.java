package vegoo.stockdata.crawler.eastmoney.blk;


import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.karaf.scheduler.Job;
import org.apache.karaf.scheduler.JobContext;
import org.apache.karaf.scheduler.Scheduler;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import vegoo.stockcommon.bo.BlockService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.dao.BlockDao;
import vegoo.stockcommon.dao.StockDao;
import vegoo.stockdata.crawler.core.BaseGrabJob;


/*
 * 这些星号由左到右按顺序代表 ：     *    *    *    *    *    *   *     
                         格式： [秒] [分] [小时] [日] [月] [周] [年]
 * 时间大小由小到大排列，从秒开始，顺序为 秒，分，时，天，月，年    *为任意 ？为无限制。 由此上面所配置的内容就是，在每天的16点26分启动buildSendHtml() 方法
	
	具体时间设定可参考
	"0/10 * * * * ?" 每10秒触发
	"0 0 12 * * ?" 每天中午12点触发 
	"0 15 10 ? * *" 每天上午10:15触发 
	"0 15 10 * * ?" 每天上午10:15触发 
	"0 15 10 * * ? *" 每天上午10:15触发 
	"0 15 10 * * ? 2005" 2005年的每天上午10:15触发 
	"0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发 
	"0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发 
	"0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发 
	"0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发 
	"0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发 
	"0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发 
	"0 15 10 15 * ?" 每月15日上午10:15触发 
	"0 15 10 L * ?" 每月最后一日的上午10:15触发 
	"0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发 
	"0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发 
	"0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发
	"0 0 06,18 * * ?"  在每天上午6点和下午6点触发 
	"0 30 5 * * ? *"   在每天上午5:30触发
	"0 0/3 * * * ?"    每3分钟触发
 */

@Component (
	immediate = true, 
	configurationPid = "stockdata.grab.block",
	service = { Job.class,  ManagedService.class}, 
	property = {
	    Scheduler.PROPERTY_SCHEDULER_EXPRESSION + "= 0 * 1,8,12,18 * * ?", //  静态信息，每天7，8，18抓三次
	    // Scheduler.PROPERTY_SCHEDULER_CONCURRENT + "= false"
	} 
)
public class GrabBlockJob extends BaseGrabJob implements Job, ManagedService {
	/*！！！ Job,ManagedService接口必须在此申明，不能一道父类中，否则，karaf无法辨认，Job无法执行  ！！！*/
	private static final Logger logger = LoggerFactory.getLogger(GrabBlockJob.class);
	
	static final String PN_URL_BLKINFO   = "url-blockinfo";
	static final String PN_URL_STKBLK   = "url-stocks-of-block";
    static final String PN_BLOCK_TYPES   = "block-types";
	
	private static final String TAG_BLOCK_TYPE   = "<BLOCK_TYPE>";
	private static final String TAG_BLOCK_CODE = "<BLOCK_CODE>";
	// private static final String TAG_PAGENO     = "<PAGE_NO>";

	private static String[] blockTypes = {"BKHY", //  行业
			"BKGN",  // 概念
			"BKDY"};  // 地域

	
	// 东方财富定义的板别
	private static String[] BOARD_IDS = {
							"C._A",  // 沪深A股
							"C.2",   // 上证A股
							"C._SZAME",  // 深证A股
							"C.13",   // 中小板
							"C.80",   // 创业板
							"C.81 ",  // 新三板
							"C._B "  // B股
							// "C._AB_FXJS",  // 风险警示板
							// "C._AB"  //  全部股票
							};
	
	private static String[] BLOCK_BOARDS = {"BK_HSAG","BK_SHAG","BK_SZAG","BK_ZXB","BK_CYB","BK_XSB","BK_BG"};
	private static String[] BLOCK_BOARD_NAMES = {"沪深A股","上证A股","深证A股","中小板","创业板","新三板","B股"};

	
	static final String PN_URL_STOCK   = "url-stock";
	static final String TAG_BOARD_ID   = "<BOARD_ID>";

	
	private String urlStock;
	private String urlBlockinfo;
	private String urlStocksOfBlk;
	
    @Reference private StockService dbStock;	
    @Reference private BlockService dbBlock;
    
    
    
    private Future<?> futureGrabbing;

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		/* ！！！本函数内不要做需要长时间才能完成的工作，否则，会影响其他BUNDLE的初始化！！！  */
		this.urlBlockinfo = (String) properties.get(PN_URL_BLKINFO);
		this.urlStocksOfBlk = (String) properties.get(PN_URL_STKBLK);
		this.urlStock = (String) properties.get(PN_URL_STOCK);
		
		logger.info("{} = {}", PN_URL_BLKINFO, urlBlockinfo);
		logger.info("{} = {}", PN_URL_STKBLK, urlStocksOfBlk);
		logger.info("{} = {}", PN_URL_STOCK, urlStock);
		
		// 优先抓取
		new Thread(new Runnable() {
			@Override
			public void run() {
				grabStocks();
				grabBlocks();
			}}).start();
	}

	@Override
	protected void executeJob(JobContext context) {
		if(Strings.isNullOrEmpty(urlBlockinfo)) {
			logger.error("没有在配置文件中设置{}参数！", PN_URL_BLKINFO);
			return;
		}
		
		if(Strings.isNullOrEmpty(urlStocksOfBlk)) {
			logger.error("没有在配置文件中设置{}参数！", PN_URL_STKBLK);
			return;
		}
		
		if(blockTypes == null) {
			logger.error("没有在配置文件中设置{}参数！", PN_BLOCK_TYPES);
			return;
		}
		
		if(futureGrabbing != null) {
			if(futureGrabbing.isDone() || futureGrabbing.isCancelled()) {
			  futureGrabbing = null;
			}else {
			  return;
			}
		}
		grabStocks();
		grabBlocks();
	}
	
	private void grabBlocks() {
		for(int i=0;i<blockTypes.length;++i) {
			grabBlkInfo(i);		
		}
	}

	private void grabBlkInfo(int i) {
		String url = urlBlockinfo.replaceAll(TAG_BLOCK_TYPE, blockTypes[i]);

		BlockInfoDto listDto = requestData(url, BlockInfoDto.class, "获取板块基本信息");
		
		if(listDto==null) {
			return ;
		}
		
		List<String> blkCodes = processBlockData(i+1,listDto.getData());
		
		grabStockOfBlock(blkCodes);
	}
	
    private List<String> processBlockData(int typeId, List<String> blocks) {
    	List<String> blkCodes = new ArrayList<>();
		for(String blkInfo : blocks) {
			
			String[] fields = split(blkInfo, ","); 
			
			if(fields.length != 4) {
				// 正确的格式: 1,BK0730,农药兽药,BK07301
				logger.error("板块的股票列表数据格式错误，应该类似“1,BK0730,农药兽药,BK07301”，接收到的是: {}", blkInfo);
				break;
			}
			 
			String marketid = fields[0];
			String blkCode  = fields[1];
			String blkname  = fields[2];
			String blkUCode = fields[3];
			
			saveBlockData(typeId, Integer.parseInt(marketid), blkCode, blkname, blkUCode);
			
			blkCodes.add(blkUCode);
		}
		
		return blkCodes;
	}
    
    private void saveBlockData(int typeId, int marketid, String blkCode, String blkname, String blkUCode) {
		if(!dbBlock.existBlock(blkUCode)) {
			BlockDao dao = new BlockDao();
			
			dao.setUCode(blkUCode);
			dao.setCode(blkCode);		
			dao.setName(blkname);
			dao.setMarketId(marketid);
			dao.setTypeId(typeId);
			
			dbBlock.insertBlock(dao);
		}
	}

	private void grabStockOfBlock(List<String> blkCodes) {
		for(String blkCode : blkCodes) {
			String url = urlStocksOfBlk.replaceAll(TAG_BLOCK_CODE, blkCode);
			
			grabStockOfBlock(blkCode, url);
		}
	}
    
	private void grabStockOfBlock(String blkUcode, String urlPattern) {
		List<String> stksOfBlk = new ArrayList<>();
		
		int page = 0;
		while(grabStockOfBlock(blkUcode, ++page, urlPattern, stksOfBlk) > page) ;
		
		saveStocksOfBlock(blkUcode, stksOfBlk);
	}
    
	private int grabStockOfBlock(String blkUcode, int page, String urlPattern, List<String> stksOfBlk) {
		String url = urlPattern.replaceAll(TAG_PAGENO, String.valueOf(page));
		
		StocksOfBlockDto listDto = requestData(url, StocksOfBlockDto.class, "获取板块的股票列表");

		if(listDto==null) {
			return 0;
		}
		
		stksOfBlk.addAll(listDto.getRank());
		
		return listDto.getPages();
	}

	private void saveStocksOfBlock(String blkUcode, List<String> stockInfos) {
		Set<String> stockCodes = new HashSet<>();
		for(String stkInfo: stockInfos) {
			//logger.info("{} stk-blk: {}", ++stkofblkcounter, stkInfo);
			
			String[] fields = split(stkInfo,","); 
			if(fields.length != 4) {
				// 正确的格式: 2,300589,江龙船艇,3005892
				logger.error("板块的股票列表数据格式错误，应该类似“2,300589,江龙船艇,3005892”，接收到的是: {}", stkInfo);
				break;
			}
			
			String marketid = fields[0];
			String stkCode  = fields[1];
			
			stockCodes.add(stkCode);
		}
		dbBlock.updateStocksOfBlock(blkUcode, stockCodes) ;
	}
	
	////////////////////////////////
	private void grabStocks() {
		if(Strings.isNullOrEmpty(urlStock)) {
			logger.error("没有在配置文件中设置{}参数！", PN_URL_STOCK);
			return;
		}

		for(int i=0;i<BOARD_IDS.length;++i) {
			String boardId = BOARD_IDS[i];
			String blkCode = BLOCK_BOARDS[i];
			String blkName = BLOCK_BOARD_NAMES[i];

			String url = urlStock.replaceAll(TAG_BOARD_ID, boardId);
			
			grabStockInfoData(url, blkCode, blkName);
		}
	}

	private void grabStockInfoData(String url, String blkCode, String blkName) {
		StockInfoDto listDto = requestData(url, StockInfoDto.class, "获取股票基本信息");
		
		if(listDto==null) {
			return ;
		}
		
		processStockInfoData(listDto.getData(), blkCode, blkName);
	}

	private void processStockInfoData(List<String> data, String blkCode, String blkName) {
		Set<String> stockCodes = new HashSet<>();
		for(String stkInfo : data) {
			String[] fields = split(stkInfo, ",");
			
			if(fields.length != 4) {
				// 正确的格式: 1,601606,N军工,6016061
				logger.error("股票数据格式错误，应该类似“1,601606,N军工,6016061”，接收到的是: {}", stkInfo);
				continue;
			}
			
			String marketid = fields[0];
			String stkCode  = fields[1];
			String stkName  = fields[2];
			String stkUCode = fields[3];
			
			if(Strings.isNullOrEmpty(stkCode)) {
				continue;
			}
			
			stkCode = stkCode.trim();
			if(stkCode.length() !=6 ) {
				logger.info("股票代码错误：{} // {}", stkCode, stkInfo);
				continue;
			}
			
			saveStockData(marketid, stkCode, stkName, stkUCode);
			stockCodes.add(stkCode);
		}
		
		saveBlockData(BlockService.BLOCKTYPE_BOARD, 1, blkCode, blkName, blkCode);
		dbBlock.updateStocksOfBlock(blkCode, stockCodes) ;
	}

	private void saveStockData(String marketid, String stkCode, String stkName, String stkUCode) {
		if(dbStock.existStock(stkCode)) {
			dbStock.updateStock(stkCode, stkName);
			return;
		}
		
		StockDao dao = new StockDao();
		dao.setCode(stkCode);
		// dao.setUCode(stkUCode);
		dao.setName(stkName);
		dao.setMarketId(Integer.parseInt(marketid));
		// dao.setTypeId();
		// dao.setPublicDate(publicDate);
		dbStock.insertStock(dao);
	}
	

}