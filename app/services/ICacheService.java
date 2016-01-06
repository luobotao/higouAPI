package services;

import java.util.List;

public interface ICacheService {
	/*
	 * cache operator;
	 */

	// 设置一个数据像 key为键值，o为对象，timeout为超时设置（秒）
	public boolean setWithOutTime(String key, String value, int timeout);

	// 设置一个数据像 key为键值，o为对象，永不超时
	public boolean set(String key, String value);

	// 根据键值 获取数据
	public String get(String key);

	// 清除数据
	public String clear(String key);

	/**
	 * 将一个list写入缓存
	 * @param key
	 * @param pushIDs
	 */
	/**
	 * 获取list
	 * @param key
	 * @return
	 */
	public  List<String> getDataByMap(String key) ;

	/**
	 * 将对象存入缓存 若timeout为零则永不超时
	 * @param bytes
	 * @param serialize
	 * @param timeout
	 */
	public boolean setObject(String key, Object value, int timeout);
	/**
	 * 通过key 获取对象（不刷新过期时间）
	 * @param key
	 * @return
	 */
	public Object getObject(String key);
	/**
	 * 通过key 获取对象（刷新过期时间）
	 * @param key
	 * @return
	 */
	public Object getObjectAndRefreshTimeOut(String key, int timeout);

}
