import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import utils.AjaxHellper;
import utils.Constants;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import utils.wxpay.TenpayHttpClient;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import forms.DateBetween;

/**
 * Application wide behaviour. We establish a Spring application context for the dependency injection system and
 * configure Spring Data.
 */
public class Global extends GlobalSettings {

    private static Logger.ALogger logger = Logger.of(Global.class);
  
    /**
     * Declare the application context to be used - a Java annotation based application context requiring no XML.
     */
    final private AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStart(final Application app) {
        super.onStart(app);

        configJson();

        // AnnotationConfigApplicationContext can only be refreshed once, but we do it here even though this method
        // can be called multiple times. The reason for doing during startup is so that the Play configuration is
        // entirely available to this application context.
        logger.info("spring register {}", SpringDataJpaConfiguration.class);
        ctx.register(SpringDataJpaConfiguration.class);
        logger.info("spring scan controllers,models,services,utils");
        ctx.scan("controllers", "models", "services", "utils");
        ctx.refresh();
        // This will construct the beans and call any construction lifecycle methods e.g. @PostConstruct
        ctx.start();
        boolean sync=Play.application().configuration().getBoolean("bbtsync", false);
        if(sync)
        	this.timer();
        //DateBetween 注册
        logger.info("register DateBetween");
        DateBetween.register();
    }

    @Override
    public Action<?> onRequest(final Http.Request request, Method actionMethod) {
    	final String uri = request.uri();
    	if(uri.indexOf("/wx/")>=0 || (uri.indexOf("/sheSaid/")>=0)){
    		return new Action.Simple() {
				public Promise<Result> call(Context arg0) throws Throwable {
					return F.Promise.pure(Action.redirect("http://h5.higegou.com"+uri));
				}
			};
    	}
        String ip = getIpAddr(request);
        if(checkIpBlackFlag(ip)){
        	logger.info("非法IP访问,and ip is {}",ip);
        	return new Action.Simple() {
				public Promise<Result> call(Context arg0) throws Throwable {
					return null;
				}
			};
        }
		String checkArray[] ={"appversion","devid","marketCode"};
		boolean flag = true;
		for(String str:checkArray){
			if(!checkUrl(request, str)){
				flag = false;
				break;
			}
		}
		/*if(!flag){
			return new Action.Simple() {
				@Override
				public Promise<Result> call(Context paramContext)
						throws Throwable {
					return null;
				}
			};
		}*/
        return super.onRequest(request, actionMethod);
    }
    
    public static boolean checkUrl(Request request,String str){
    	String temp = AjaxHellper.getHttpParamOfFormUrlEncoded(request, str)==null?AjaxHellper.getHttpParam(request, str):AjaxHellper.getHttpParamOfFormUrlEncoded(request, str);
    	if(StringUtils.isBlank(temp)){
    		return false;
    	}else{
    		return true;
    	}
    }

    private boolean checkUserAgent(String userAgent) {
        for (String s : Constants.USER_AGENTS) {
            if (userAgent.toLowerCase().contains(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean needCheckReferer(String path) {
        for (Pattern p : Constants.NEED_CHECK_REFERER_URL_PATTERN) {
            if (p.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkReferer(Http.Request request) {
        if (!needCheckReferer(request.path())) {
            return true;
        }
        String referer = request.getHeader("Referer");
        if (StringUtils.isNotBlank(referer) && referer.contains(request.host())) {
            return true;
        }
        return false;
    }

    /**
     * Sync the context lifecycle with Play's.
     */
    @Override
    public void onStop(final Application app) {
        // This will call any destruction lifecycle methods and then release the beans e.g. @PreDestroy
        ctx.close();
        super.onStop(app);
    }

    /**
     * Controllers must be resolved through the application context. There is a special method of GlobalSettings
     * that we can override to resolve a given controller. This resolution is required by the Play router.
     */
    @Override
    public <A> A getControllerInstance(Class<A> aClass) {
        return ctx.getBean(aClass);
    }

    
    
    
    @Override
	public Promise<Result> onBadRequest(RequestHeader paramRequestHeader,
			String paramString) {
		// TODO Auto-generated method stub
		return super.onBadRequest(paramRequestHeader, paramString);
	}

/*	@Override
	public Promise<Result> onError(Http.RequestHeader request, Throwable t) {
		LOGGER.error("error request " + request.uri(), t);
		return Promise.<Result>pure(controllers.Application.index());
	}*/

	@Override
	public Promise<Result> onHandlerNotFound(RequestHeader paramRequestHeader) {
		// TODO Auto-generated method stub
		return super.onHandlerNotFound(paramRequestHeader);
	}

	private void configJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.writerWithDefaultPrettyPrinter();
        Json.setObjectMapper(objectMapper);
    }

    /**
     * This configuration establishes Spring Data concerns including those of JPA.
     */
    @Configuration
    @EnableJpaRepositories("repositories")
    @EnableTransactionManagement
    public static class SpringDataJpaConfiguration {

        @Bean
        public EntityManagerFactory entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean managerFactory = new LocalContainerEntityManagerFactoryBean();
            managerFactory.setDataSource(dataSource());
            managerFactory.setPackagesToScan("models");

            HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
            managerFactory.setJpaVendorAdapter(adapter);

            Properties properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "update");
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            properties.setProperty("hibernate.show_sql", "true");
            managerFactory.setJpaProperties(properties);

            managerFactory.afterPropertiesSet();
            return managerFactory.getObject();
        }

        @Bean
        public HibernateExceptionTranslator hibernateExceptionTranslator() {
            return new HibernateExceptionTranslator();
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
            jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
            return jpaTransactionManager;
        }

        @Bean(initMethod = "init", destroyMethod = "close")
        public DataSource dataSource() {
            DruidDataSource druidDataSource = new DruidDataSource();
            String dataUrl = Play.application().configuration().getString("druid.url.dev");
            String password = Play.application().configuration().getString("druid.password.dev");
            if(Play.application().configuration().getBoolean("production", false)){
            	dataUrl = Play.application().configuration().getString("druid.url.product");
            	password = Play.application().configuration().getString("druid.password.product");
            }
            druidDataSource.setUrl(dataUrl);
            druidDataSource.setUsername(Play.application().configuration().getString("druid.username"));
            druidDataSource.setPassword(password);
            druidDataSource.setMaxActive(Play.application().configuration().getInt("druid.maxActive"));
            druidDataSource.setInitialSize(Play.application().configuration().getInt("druid.initialSize"));
            druidDataSource.setMaxWait(Play.application().configuration().getLong("druid.maxWait"));
            druidDataSource.setMinIdle(Play.application().configuration().getInt("druid.minIdle"));
            druidDataSource.setTimeBetweenEvictionRunsMillis(Play.application().configuration().getLong("druid.timeBetweenEvictionRunsMillis"));
            druidDataSource.setMinEvictableIdleTimeMillis(Play.application().configuration().getLong("druid.minEvictableIdleTimeMillis"));
            druidDataSource.setValidationQuery(Play.application().configuration().getString("druid.validationQuery"));
            druidDataSource.setTestWhileIdle(Play.application().configuration().getBoolean("druid.testWhileIdle"));
            druidDataSource.setTestOnBorrow(Play.application().configuration().getBoolean("druid.testOnBorrow"));
            druidDataSource.setTestOnReturn(Play.application().configuration().getBoolean("druid.testOnReturn"));

            try {
                druidDataSource.setFilters("stat");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

//            druidDataSource.setConnectionProperties("config.decrypt=true");

            return druidDataSource;
        }
    }
    
    public String getIpAddr(Request request) {  
        String ip = request.getHeader("x-forwarded-for");  
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.remoteAddress();  
        }  
        logger.info("host is {} and request is {} and request method is {}", ip, request.uri(),request);
        return ip;
    }

	public boolean checkIpBlackFlag(String ip) {
		Properties properties = new Properties();
//		try (InputStreamReader isr = new InputStreamReader(new FileInputStream("/data/higouapi/ipBlack.properties"), "UTF-8")) {
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream("C:\\ipBlack.properties"), "UTF-8")) {
			properties.load(isr);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String ipTemp = properties.getProperty(ip);
		if ("false".equals(ipTemp)) {
			return true;
		} else {
			return false;
		}
		/*
		 * String sql =
		 * "select count(id) as cnt from blackIP where blackIP='"+ip+"'";
		 * logger.info(sql); int count=0; JdbcOper db =
		 * JdbcOper.getInstance();// 创建DBHelper对象 try {
		 * db.getPrepareStateDao(sql); ResultSet rs = db.pst.executeQuery();//
		 * 执行语句，得到结果集 while(rs.next()){ count = rs.getInt("cnt"); } } catch
		 * (Exception e) { e.printStackTrace(); } finally { db.close(); }
		 * logger.info(count+"==========="); if(count>0){ return true; }else
		 * return false;
		 */
	}
	/*
	 * 取棒棒糖接口物流
	 */
	public void getbill(Long billid,String billnum,String ordercode){
		SimpleDateFormat CHINESE_DATE_TIME_FORMAT_NORMAL = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String timstr= CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date());
		String signstr="mail_num="+billnum+"&out_trade_no="+ordercode+"&time_stamp="+timstr+"&token=aeb13b6a5cb82d43ce66b7f34f44c175";
		try{
			//String tm="mail_num=1234567890123&time_stamp=2015-08-27 16:41:58&token=993aecabe55ca20db52f186b6b5b726a";
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(signstr.getBytes("utf-8"));
            byte[] digest = messageDigest.digest(); 

            signstr=org.apache.commons.codec.binary.Base64.encodeBase64String(digest);
			logger.info(signstr);
		}
		catch(Exception e){}
		//测试环境：api.neolix.cn
		//生产环境：api.ibbt.com
		String gettokenurl="http://api.neolix.cn/haigou/o2o/order/get";
		boolean isproduct=Play.application().configuration().getBoolean("production", false);
		if(isproduct)
			gettokenurl="http://api.ibbt.com/haigou/o2o/order/get";
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(gettokenurl);
		post.setHeader("Content-Type", "text/json; charset=UTF-8");
		com.fasterxml.jackson.databind.node.ObjectNode re=Json.newObject();
		re.put("sign", signstr);
		re.put("time_stamp", timstr);
		re.put("out_trade_no", ordercode);
		re.put("mail_num",billnum);
		try {
			Logger.info("发送查询订单数据到棒棒糖："+re.toString());
			post.setEntity(new StringEntity(re.toString(),"UTF-8"));
			HttpResponse res = client.execute(post);
			String strResult = EntityUtils.toString(res.getEntity(), "UTF-8");
			Logger.info("接收到的棒棒糖订单查询数据："+strResult);
			JdbcOper db =JdbcOper.getInstance();// 创建DBHelper对象 
			try{
				JSONObject json=JSONObject.fromObject(strResult);
				if(json.getString("result").equals("ok")){
					Integer status=Numbers.parseInt(json.getString("status"), 0);
					String postman_phone="";
					String postman_name="";
					try{
						postman_phone=StringUtils.isBlank(json.getString("postman_phone"))?"":json.getString("postman_phone");
						postman_name=StringUtils.isBlank(json.getString("postman_name"))?"":json.getString("postman_name");
					}catch(Exception e){}
					
					/*
					 * 订单状态，1-未分配配送员 2-门店取消 3-已分配 4-配送员接单 5-配送员拒绝 6-已领单 7-待配送 8-已签收 9-已拒收 10-已过期
					 */
					//变更物流日志
					try {
						String sql="update shopping_order_ex set status="+status+",updateTime='"+CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date())+"' where id="+billid;
						if(!StringUtils.isBlank(postman_name))
							sql="update shopping_order_ex set postman_name='"+postman_name+"',postman_phone='"+postman_phone+"',status="+status+",updateTime='"+CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date())+"' where id="+billid;
						
			    		 db.getPrepareStateDao(sql); 
			    		 db.pst.execute();
			    		 sql="{call sp_pardels_Waybill_bbt('"+ordercode+"','"+status+"','"+postman_phone+"')}";
			    		 
			    		 db.getPrepareStateDao(sql);
			    		 db.pst.execute();
			    		 }catch(Exception dbe){}
					
					//如果派送完成，变更订单表
					if(status>7){
						String sql="update shopping_order_ex set syncstatus=1 where id="+billid;						
				    	 try {
				    		 db.getPrepareStateDao(sql); 
				    		 db.pst.execute();
				    		 }catch(Exception dbe){}
					}
				}
			}catch(Exception e){}
			finally{
				db.close();
			}
		}
		catch(Exception e){}
	}
	
	/*
	 * 同步棒棒糖补售数据
	 */
	public void bbtsync(){
		String sql="SELECT p.pid,o.orderCode,o.mcode,pp.title,pp.subtitle,o.name,o.phone,o.province,o.address FROM shopping_Order_Pro p ,"
				+"(SELECT * FROM shopping_Order WHERE OrderCode NOT IN(SELECT ordercode FROM shopping_order_ex WHERE createTime>=DATE_ADD(NOW(),INTERVAL -5 MINUTE))"
				+" AND mcode LIKE 'bbt%' AND `DATE_ADD`>=DATE_ADD(NOW(),INTERVAL -5 MINUTE) AND paystat='20') o,product pp "
				+" WHERE p.orderId=o.id and p.pid=pp.pid";
		JdbcOper db =JdbcOper.getInstance();// 创建DBHelper对象 

		boolean IsProduct = Play.application().configuration().getBoolean("production", false);
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			 SimpleDateFormat CHINESE_DATE_TIME_FORMAT_NORMAL = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
			while(rs.next()){
				Map<String,String> getmap=new HashMap<String,String>();
				String timstr= CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date());
				getmap.put("out_trade_no", rs.getString("orderCode"));
				getmap.put("time_stamp", timstr);
				getmap.put("token", "aeb13b6a5cb82d43ce66b7f34f44c175");
				String signstr=StringUtil.makeSig(getmap);
				try{
					//String tm="mail_num=1234567890123&time_stamp=2015-08-27 16:41:58&token=993aecabe55ca20db52f186b6b5b726a";
					MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		            messageDigest.reset();
		            messageDigest.update(signstr.getBytes("utf-8"));
		            byte[] digest = messageDigest.digest(); 

		            signstr=org.apache.commons.codec.binary.Base64.encodeBase64String(digest);
					logger.info(signstr);
				}
				catch(Exception e){}
				String mmcode=rs.getString("mcode");
				if(mmcode.equals("bbt"))
					mmcode="higo-jwsh";
				else if(mmcode.equals("bbtdwl"))
					mmcode="higo-dwl";
				com.fasterxml.jackson.databind.node.ObjectNode re=Json.newObject();
				re.put("sign", signstr);
				re.put("time_stamp", timstr);
				re.put("out_trade_no", rs.getShort("orderCode"));
				re.put("goods_name", rs.getString("title"));
				re.put("goods_detail",rs.getString("subtitle"));
				re.put("receiver_name", rs.getString("name"));
				re.put("receiver_mobile",rs.getString("phone"));
				re.put("receiver_address",rs.getString("province")+rs.getString("address"));
				re.put("need_warm_box", "0");
				re.put("store_code", mmcode);
				TenpayHttpClient httpClient = new TenpayHttpClient();
				
					String gettokenurl="http://api.neolix.cn/haigou/o2o/order/add";
					if(IsProduct)
						gettokenurl="http://api.ibbt.com/haigou/o2o/order/add";
					
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(gettokenurl);
					post.setHeader("Content-Type", "text/json; charset=UTF-8");
					try {
						Logger.info("发送订单数据到棒棒糖："+re.toString());
						post.setEntity(new StringEntity(re.toString(),"UTF-8"));
						HttpResponse res = client.execute(post);
						String strResult = EntityUtils.toString(res.getEntity(), "UTF-8");
						try{
							JSONObject json=JSONObject.fromObject(strResult);
							String err=json.getString("result");
							if(err.equals("ok")){
								String billnum=json.getString("mail_num");
								//添加订单数据
								String sqls="select count(1) as count from shopping_order_ex where billnum='"+billnum+"' and ordercode='"+rs.getString("orderCode")+"' and actname='"+mmcode+"'";
								Integer count=0;
								try{
									db.getPrepareStateDao(sql);
									ResultSet rss = db.pst.executeQuery();// 执行语句，得到结果集
									if(rss.next()){
										count=rs.getInt("count");
									}
									if(count<1){
										sqls="insert into shopping_order_ex(createTime,billnum,ordercode,actname) values('"+CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date())+"','"+billnum+"','"+rs.getString("orderCode")+"','"+mmcode+"')";
										db.getPrepareStateDao(sqls);
										db.pst.execute();
									}
								}catch(Exception e){}
							}
						}
						catch(Exception ee){}
			
						Logger.info("发送订单数据到棒棒糖返回结果："+strResult);	
					}
					catch(Exception e){}
			}
		}catch(Exception e){}
		finally{
			db.close();
		}
	}
	public void timer(){
		 final long timeInterval = 300000;
		  Runnable runnable = new Runnable() {
		  public void run() {
		    while (true) {
		      // ------- code for task to run
		    	String sql="SELECT * FROM shopping_order_ex WHERE actname like 'bbt%' AND ((STATUS=0 AND createTime<=DATE_ADD(NOW(),INTERVAL -3 MINUTE)) OR (syncstatus=0 AND STATUS>0))";
		    	JdbcOper db =JdbcOper.getInstance();// 创建DBHelper对象 
		    	 try {
		    		 db.getPrepareStateDao(sql); 
		    		 ResultSet rs = db.pst.executeQuery();
		    		 while(rs.next()){
		    			 getbill(rs.getLong("id"),rs.getString("billnum"),rs.getString("ordercode"));
		    		 }
		    	 }catch(Exception dbe){}
		    	 finally{
		    		 db.close();
		    	 }
		     
		    	 //异步同步
		    	 bbtsync();
		      // ------- ends here
			      try {
			       Thread.sleep(timeInterval);
			      } catch (InterruptedException e) {
			        //e.printStackTrace();
			      }
		      }
		    }
		  };
		  Thread thread = new Thread(runnable);
		  thread.start();
	  }
	
}
