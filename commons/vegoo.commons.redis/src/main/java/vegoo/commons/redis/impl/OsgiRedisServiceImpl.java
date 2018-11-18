package vegoo.commons.redis.impl;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


import vegoo.commons.redis.RedisService;

@Component (
		immediate = true, 
		configurationPid = "vegoo.redis",
		service = {RedisService.class,  ManagedService.class}
		//property = {
		//    "host=127.0.0.1",
		//    "port=6398",
		//} 
	)
public class OsgiRedisServiceImpl extends RedisServiceBase implements ManagedService {
	private static final Logger logger = LoggerFactory.getLogger(OsgiRedisServiceImpl.class);

	static final String PN_HOST   = "host";
	static final String PN_PORT   = "port";
	static final String PN_PASSWORD   = "password";

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		String host = (String) properties.get(PN_HOST);
		String port = (String) properties.get(PN_PORT);
		String password = (String) properties.get(PN_PASSWORD);
		
		if(Strings.isNullOrEmpty(host)) {
			logger.error("没有在vegoo.redis.cfg文件中，配置Redis参数");
		}
		
		int iport = 6379;
		try {
			iport = Integer.parseInt(port);
		}catch(Exception e) {
			
		}
		initJedis(host, iport, password);
	}
	
	@Deactivate
	public void deactivate() {
		super.destroyJedis();
	}

}
