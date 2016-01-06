package utils;

import java.util.Map;

import play.mvc.Http.Request;

/**
 * @author luo
 *
 */
public class AjaxHellper {

	/**
	 * JavaScript或超链接请求 ajax数据请求过来
	 * Request里面得到参数是FormUrlEncoded方式
	 * 方法获取Request的参数
	 * @param req
	 * @param key
	 * @return
	 */
	public static String getHttpParamOfFormUrlEncoded(Request req,String key)
	{
		Map<String,String[]> all =req.body().asFormUrlEncoded();
		if( all==null )
			return null;
		String[] values = all.get(key);
		if(values!=null&&values.length>0){
			return values[0];
		}
		return null;
	}
	
	/**
	 * JavaScript或超链接请求 - ajax数据请求过来
	 * Request里面得到参数是 :url?param1=value1&param2=value2...
	 * 方法获取Request的参数
	 * @param req
	 * @param key
	 * @return
	 */
	public static String getHttpParam(Request req,String key)
	{
		Map<String,String[]> all = req.queryString();
		if( all==null )
			return null;
		String[] values = all.get(key);
		if(values!=null&&values.length>0){
			return values[0];
		}
		return null;
	}
}
