package vegoo.stockcommon.bo;

import java.util.Dictionary;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vegoo.commons.MyThreadPoolExecutor;

public abstract class BoServiceImpl implements BoService{
	private static final Logger logger = LoggerFactory.getLogger(BoServiceImpl.class);
	
	// 核心线程数量
    private static int corePoolSize = 1;
    // 最大线程数量
    private static int maxPoolSize = 10;
    // 线程存活时间：当线程数量超过corePoolSize时，10秒钟空闲即关闭线程
    private static int keepAliveTime = 10*1000;
    // 缓冲队列
    // private static BlockingQueue<Runnable> workQueue = null;
    // 线程池
    private static ThreadPoolExecutor threadPoolExecutor =  new MyThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS);
    
    public static Future<?> asyncExecute(Runnable runnable) {
		return threadPoolExecutor.submit(runnable);
	}
    
    
}
