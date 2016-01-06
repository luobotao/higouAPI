package services.api;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Named;
import javax.inject.Singleton;

import play.Logger;
import services.AliPayService;
import utils.JdbcOper;
import models.Reffer;

@Named
@Singleton
public class RefferService {
	private static final Logger.ALogger logger = Logger.of(AliPayService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT_NORMAL = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	/*
	 * 添加日志
	 */
	public void addReffer(Reffer ref,String tablename){
		//String sql="insert into "+tablename+"(reffer,ip,tid,typ,createtime) values('"+ref.getRefer()+"','"+ref.getIp()+"',"+ref.getTid()+",'"+ref.getTyp()+"','"+CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date())+"')";
		String sql="{call sp_create_reffertable('"+tablename+"','"+ref.getRefer()+"','"+ref.getIp()+"',"+ref.getTid()+",'"+ref.getTyp()+"')}";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		logger.info(sql);
		try {
			db.getPrepareStateDao(sql);
			db.pst.execute();
		}
		catch(Exception e){
			logger.info("sql err:"+e.toString());
		}finally {
			db.close();
		}
	}
}
