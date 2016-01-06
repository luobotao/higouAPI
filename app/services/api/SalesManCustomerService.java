package services.api;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Comment;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import assets.CdnAssets;
import play.Logger;
import repositories.CommentRepository;
import utils.Constants;
import utils.JdbcOper;
import utils.Numbers;
import vo.UserRegisterVO;
import vo.appSalesMan.AppSalesManAddressVO;
import vo.appSalesMan.AppSalesManCustomerVO;

/**
 * 用户相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class SalesManCustomerService {

    private static final Logger.ALogger logger = Logger.of(SalesManCustomerService.class);
    
    public int getCustomerCnt(String uid,String key){
    	int count=0;
    	String sqlcnt = "SELECT count(firstPY) as cnt FROM address WHERE uid = "+uid+" and (name like '%"+key+"%' or firstPY like '%"+key+"%') GROUP BY firstPY";
		JdbcOper dbcnt = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			dbcnt.getPrepareStateDao(sqlcnt);
			ResultSet rs = dbcnt.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				count = rs.getInt("cnt");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbcnt.close();
		}
		return count;
    }
    /*
	 */
	public List<AppSalesManCustomerVO.dataInfo> getCustomerDataInfo(String uid, String page,String key) {
		int pagesize=10;
		int pageindex = Numbers.parseInt(page, 0)*pagesize;
		
		List<AppSalesManCustomerVO.dataInfo> datalist = new ArrayList<AppSalesManCustomerVO.dataInfo>();
	
		String sql = "SELECT firstPY FROM address WHERE uid = "+uid+" and (name like '%"+key+"%' or firstPY like '%"+key+"%') GROUP BY firstPY ORDER BY firstPY limit "+pageindex+",10";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				AppSalesManCustomerVO.dataInfo dInfo = new AppSalesManCustomerVO.dataInfo();
				String indexPY = rs.getString("firstPY");
				dInfo.setIndexNum(indexPY);
				List<AppSalesManCustomerVO.customerInfo> cList= new ArrayList<AppSalesManCustomerVO.customerInfo>();
				cList=getCustomerList(uid,indexPY,key);
				dInfo.setCustomerList(cList);
				datalist.add(dInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return datalist;
	}
	
	public List<AppSalesManCustomerVO.customerInfo> getCustomerList(String uid, String indexPY,String key) {
		List<AppSalesManCustomerVO.customerInfo>  datalist = new ArrayList<AppSalesManCustomerVO.customerInfo>();
		String sql = "SELECT distinct name FROM address WHERE uid = "+uid+" and (name like '%"+key+"%' or firstPY like '%"+key+"%') and firstPY='"+indexPY+"'";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				AppSalesManCustomerVO.customerInfo cInfo = new AppSalesManCustomerVO.customerInfo();
				
				String name = rs.getString("name");
				cInfo.name=name;
				
				datalist.add(cInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return datalist;
	}
	
	public List<AppSalesManAddressVO.dataInfo> getAddressList(String uid, String name,String page) {
		List<AppSalesManAddressVO.dataInfo>  datalist = new ArrayList<AppSalesManAddressVO.dataInfo>();
		String sql = "SELECT * FROM address WHERE uid = "+uid+" and name='"+name+"'";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				AppSalesManAddressVO.dataInfo dInfo = new AppSalesManAddressVO.dataInfo();
				dInfo.addressId=rs.getString("id");
				dInfo.name=rs.getString("name");
				dInfo.phone=rs.getString("phone");
				dInfo.address=rs.getString("address");
				dInfo.cardId = rs.getString("cardId");
				dInfo.city = rs.getString("province");
				datalist.add(dInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return datalist;
	}
}
