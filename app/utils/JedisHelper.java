package utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import services.ICacheService;

/**
 * 读取redis缓存内容信息内容
 * 
 * @author luo
 * 
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class JedisHelper implements ICacheService{
	private static final String DEFAULT_INCR_VALUE = "1";
	private static final int DEFAULT_EXPIRE_TIME = 3600;
	private static final int hotResultsNum = 10;
	private int hashCodeNum = 120;

	/**
	 * 非切片客户端链接
	 */
	private Jedis jedis;

	/**
	 * 非切片链接池
	 */
	private JedisPool jedisPool;

	/**
	 * 切片客户端链接
	 */
	private ShardedJedis shardedJedis;

	/**
	 * 切片链接池
	 */
	private ShardedJedisPool shardedJedisPool;
	
	
	/* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */  
    private static JedisHelper instance = null;  
  
    /* 静态工程方法，创建实例 */  
    public static JedisHelper getInstance() {  
    	if (instance == null) {  
            syncInit();  
        }  
        return instance;   
    }  
  
    private static synchronized void syncInit() {  
        if (instance == null) {  
            instance = new JedisHelper();  
        }  
    } 
    /* 私有构造方法，防止被实例化 */  
    private JedisHelper(){
		shardedJedisPool = getShardedPool();
		jedisPool = getPool();
		jedis = jedisPool.getResource();
		shardedJedis = shardedJedisPool.getResource();
	}
	
	/**
	 * 获取切片连接池.
	 * 
	 * @return 切片连接池实例
	 */
	public static ShardedJedisPool getShardedPool() {
		String host = play.Configuration.root().getString("redis.host");
		int port = play.Configuration.root().getInt("redis.port");
		String host2 = play.Configuration.root().getString("redis.host2");
		int port2 = play.Configuration.root().getInt("redis.port2");
		ShardedJedisPool pool = null;
		JedisPoolConfig config = new JedisPoolConfig();
		int maxIdle = play.Configuration.root().getInt("redis.jedisPoolConfig.maxIdle");
		boolean testOnBorrow = play.Configuration.root().getBoolean("redis.jedisPoolConfig.testOnBorrow");
		boolean testOnReturn = play.Configuration.root().getBoolean("redis.jedisPoolConfig.testOnReturn");
		config.setMaxIdle(maxIdle);
		config.setTestOnBorrow(testOnBorrow);//当调用borrow Object方法时，是否进行有效性检查
		config.setTestOnReturn(testOnReturn);//当调用return Object方法时，是否进行有效性检查 
		try {
			/**
			 * 如果你遇到 java.net.SocketTimeoutException: Read timed out
			 * exception的异常信息 请尝试在构造JedisPool的时候设置自己的超时值.
			 * JedisPool默认的超时时间是2秒(单位毫秒)
			 * 此处连接方式为附带密码
			 */
			String name=play.Configuration.root().getString("redis.name");
			JedisShardInfo jedisShardInfo1 = new JedisShardInfo(host, port);
			jedisShardInfo1.setPassword(name);// 密码
			JedisShardInfo jedisShardInfo2 = new JedisShardInfo( host2,port2);
			jedisShardInfo2.setPassword(name);//密码
			/*无密码验证
			 * JedisShardInfo jedisShardInfo1 = new JedisShardInfo(host, port);
			JedisShardInfo jedisShardInfo2 = new JedisShardInfo( host2,port2);*/
			List<JedisShardInfo> list = new LinkedList<JedisShardInfo>();
			list.add(jedisShardInfo1);
			list.add(jedisShardInfo2);
			pool = new ShardedJedisPool(config, list); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pool;
	}
	
	/**
	 * 获取非切片连接池.
	 * 
	 * @return 非切片连接池实例
	 */
	public static JedisPool getPool() {
		String host = play.Configuration.root().getString("redis.host");
		int port = play.Configuration.root().getInt("redis.port");
		JedisPool pool = null;
		JedisPoolConfig config = new JedisPoolConfig();
		int maxIdle = play.Configuration.root().getInt("redis.jedisPoolConfig.maxIdle");
		boolean testOnBorrow = play.Configuration.root().getBoolean("redis.jedisPoolConfig.testOnBorrow");
		boolean testOnReturn = play.Configuration.root().getBoolean("redis.jedisPoolConfig.testOnReturn");
		config.setMaxIdle(maxIdle);
		config.setTestOnBorrow(testOnBorrow);//当调用borrow Object方法时，是否进行有效性检查
		config.setTestOnReturn(testOnReturn);//当调用return Object方法时，是否进行有效性检查 
		try {
			/**
			 * 如果你遇到 java.net.SocketTimeoutException: Read timed out
			 * exception的异常信息 请尝试在构造JedisPool的时候设置自己的超时值.
			 * JedisPool默认的超时时间是2秒(单位毫秒)
			 */
			String name=play.Configuration.root().getString("redis.name");
			pool = new JedisPool(config, host, port, 200, name); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pool;
	}
	/**
	 * 手动释放redis实例到连接池.
	 * 
	 * @param jedis
	 *            redis实例
	 */
	public void closeJedis(Jedis jedis) {
		if (jedis != null) {
			getPool().returnResource(jedis);
		}
	}
	
	public boolean setIfNotExists(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			Long result = jedis.setnx(key, DEFAULT_INCR_VALUE);
			jedis.expire(key, DEFAULT_EXPIRE_TIME);//设置过期时间
			if (result == 0) {
				return false;
			}
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
		return true;
	}

	/**
	 * 获得redis连接 如果获得则返回true，反之false
	 * 
	 * @return
	 */
	public boolean getConnect() {
		ShardedJedis jedis = null;
		try {
			jedis = shardedJedisPool.getResource();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	public int incr(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.incr(key).intValue();
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	public int incr(String key, int second) {
		int index = -1;
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			index = jedis.incr(key).intValue();
			jedis.expire(key, second);//设置过期时间
			return index;
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	public boolean exists(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.exists(key);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	public String get(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.get(key);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	public String clear(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.del(key)+"";
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * @param monitorItemId
	 * @return
	 */
	public List<String> getResultsFromMemory(String monitorItemId) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.lrange(monitorItemId, 0, -1);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 
	 * @param monitorItemId
	 * @param resultJson
	 */
	public void setResultToMemory(String monitorItemId, String resultJson) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.lpush(monitorItemId, resultJson);
			jedis.ltrim(monitorItemId, 0, hotResultsNum);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * @param key
	 * @param detailKey
	 * @param snapshotJson
	 */
	public void setSnapshotToHash(String key, String detailKey,
			String snapshotJson) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.hset(key, detailKey, snapshotJson);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public long getLength(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.hlen(key);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public Long getLengthByKey(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.llen(key);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public Map<String, String> getSnapShots(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.hgetAll(key);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 取出指定list數據
	 * 
	 * @param key
	 * @param start
	 * @param stop
	 * @return
	 */
	public List<String> lrange(String key, int start, int stop) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {

			return jedis.lrange(key, start, stop);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
		return null;
	}

	/**
	 * 把内容信息存放在list内容信息
	 * 
	 * @param key
	 *            key?
	 * @param list
	 *            存放redirs一组内容信息
	 * 
	 */
	public void setIpush(String key, List<String> list) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			for (String str : list) {
				jedis.rpush(key, str);
			}
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 从map中得到List值内容信息
	 * 
	 * @param key
	 * @return
	 */
	public List<String> getDataByMap(String key) {

		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.hvals(key);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> getlrange(String key, long start, long end) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.lrange(key, start, end);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 
	 * 更新缓存中的内容信息
	 * 
	 * @param monitorItemId
	 * @param resultJson
	 */
	public void updateIpush(String key, long index, String resultJson) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.lset(key, index, resultJson);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 通过tokenidhash值求hashCode模值信息
	 * 
	 * @param tokenId
	 */
	public int getHashCodeKey(String tokenId) {

		int key = Math.abs(tokenId.hashCode()) % hashCodeNum;
		return key;
	}

	/**
	 * 
	 * @return
	 */
	public void setByMap(String key, Map map) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.hmset(key, map);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	public void setByMapWithTime(String key, Map map, int second) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.hmset(key, map);
			jedis.expire(key, second);//过期时间
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 
	 * @return
	 */
	public List getByMap(String key, String fields) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.hmget(key, fields);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * key:string value:map<String,String>
	 * 
	 * @return
	 */
	public String getStrFromMap(String key, String fields) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			return jedis.hget(key, fields);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 
	 * @return
	 */
	public Map getByMapGroup(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {

			return jedis.hgetAll(key);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean set(String key, String value) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.set(key, value);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
		return true;
	}

	/**
	 * 带有有效期的redis缓存
	 * 若为0则也是不失效
	 * @return
	 */
	public boolean setWithOutTime(String key, String value, int second) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.set(key, value);
			jedis.expire(key, second);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
		return true;
	}

	
	@Override
	public boolean setObject(String key, Object value, int second) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.set(key.getBytes(),SerializeUtil.serialize(value));
			if(second!=0){
				jedis.expire(key.getBytes(), second);
			}
		}catch(Exception e){
			e.printStackTrace();
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
		return true;
		
	}
	@Override
	public Object getObject(String key) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			byte[] result=jedis.get(key.getBytes());
			return SerializeUtil.unserialize(result);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}
	
	@Override
	public Object getObjectAndRefreshTimeOut(String key, int timeout) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		 int set = Configuration.root().getInt("web.timeout",3000);
		try {
			byte[] result=jedis.get(key.getBytes());
			if(result!=null){//如果存在则刷新存活时间
				jedis.set(key.getBytes(), result);
				jedis.expire(key, set);
			}
			return SerializeUtil.unserialize(result);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
	}

	/**
	 * 
	 * @param group
	 *            redis中一组
	 * @param key
	 *            reids中Map的key
	 * @param value
	 *            reids中Map中的value
	 * @return
	 */
	public boolean setValueByKeyToCacheMap(Object group, Object key,
			Object value) {
		ShardedJedis jedis = (ShardedJedis) this.shardedJedisPool.getResource();
		boolean bool = false;
		try {
			boolean bool1;
			if (jedis.hset((String) group, (String) key, (String) value)
					.longValue() > 0L) {
				bool1 = true;
				return bool1;
			}
			return bool;
		} finally {
			this.shardedJedisPool.returnResource(jedis);
		}
	}


	public int getHotresultsnum() {
		return hotResultsNum;
	}

	public int getHashCodeNum() {
		return hashCodeNum;
	}

	/**
	 * 设置hashCode内容信息，以此设置数字内容
	 * 
	 * @param hashCodeNum
	 */
	public void setHashCodeNum(int hashCodeNum) {
		this.hashCodeNum = hashCodeNum;
	}
	/**
	 * 追加
	 * @param key
	 * @param value
	 */
	public void append(String key, String value) {
		ShardedJedis jedis = shardedJedisPool.getResource();
		try {
			jedis.append(key, value);
		} finally {
			shardedJedisPool.returnResource(jedis);
		}
		
	}

	
	
	
	
	public Jedis getJedis() {
		return jedis;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public ShardedJedis getShardedJedis() {
		return shardedJedis;
	}

	public void setShardedJedis(ShardedJedis shardedJedis) {
		this.shardedJedis = shardedJedis;
	}

	public ShardedJedisPool getShardedJedisPool() {
		return shardedJedisPool;
	}

	public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
		this.shardedJedisPool = shardedJedisPool;
	}

}
