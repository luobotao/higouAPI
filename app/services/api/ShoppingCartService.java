package services.api;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.qualified.TypeOfBiometricData;
import org.springframework.transaction.annotation.Transactional;

import models.Currency;
import models.Endorsement;
import models.Fromsite;
import models.OrderLoveLy;
import models.Product;
import models.ProductPriceExt;
import models.ShoppingCart;
import models.ShoppingCartEndorse;
import play.Logger;
import repositories.LovelyRepository;
import repositories.ShoppingCartEndorseInterface;
import repositories.ShoppingCartRepository;
import utils.Constants;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import vo.appSalesMan.AppSalesManCartVO;
import vo.shoppingCart.ShoppingCartCategoryVO;
import vo.shoppingCart.ShoppingCartItemVO;

/**
 * 购物车Service
 * @author luobotao
 * Date: 2015年5月8日 下午2:26:14
 */
@Named
@Singleton
public class ShoppingCartService {

    private static final Logger.ALogger logger = Logger.of(ShoppingCartService.class);

    @Inject
    private ShoppingCartRepository shoppingCartRepository;
    @Inject
    private LovelyRepository lovelyRepository;
    @Inject
    private UserService userService;
    @Inject
    private ProductService productService;
    @Inject 
    private ShoppingCartEndorseInterface shopCartEninterface;

	public List<Object> getShoppingCart_list(Long uId,int appversionInt) {
		List<Object> resultList = new ArrayList<Object>();
//		String sql="SELECT p.*,s.counts,s.reffer,f.name AS fname,f.img AS fimg,c.symbol AS symbol,c.rate AS rate FROM shopping_Cart s,product p,fromsite f,currency c WHERE p.fromsite=f.id AND p.currency=c.id AND s.pid= p.pid AND s.uid='"+uId+"' ORDER BY p.fromsite,p.wayremark";
		String sql="SELECT pc.*,a.`adminid` FROM (SELECT p.*,s.counts,s.reffer AS sreffer,f.name AS fname,f.img AS fimg,c.symbol AS symbol,c.rate AS rate FROM shopping_Cart s,product p,fromsite f,currency c WHERE p.fromsite=f.id AND p.currency=c.id AND s.pid= p.pid AND s.uid='"+uId+"' ORDER BY p.fromsite,p.wayremark) pc LEFT JOIN adminproduct a ON a.pid=pc.pid";
		logger.info(sql);
		String wayremark="";
		String fname="";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String fromsite = rs.getString("fname");
				String fromsiteimg = rs.getString("fimg");
				String wayremarkTemp = rs.getString("wayremark");
				int adminid = rs.getInt("adminid");
				if(adminid>0){
					fromsite = "嗨个购国内仓";
				}
				if(!fname.equals(rs.getString("fname"))||!wayremark.equals(rs.getString("wayremark"))){
					ShoppingCartCategoryVO shoppingCartCategoryVO=new ShoppingCartCategoryVO();
					shoppingCartCategoryVO.setFromsite(fromsite);
					shoppingCartCategoryVO.setFromsiteimg(fromsiteimg);
					shoppingCartCategoryVO.setTyp("1");
					shoppingCartCategoryVO.setWayremark(wayremarkTemp);
					wayremark = wayremarkTemp;
					resultList.add(shoppingCartCategoryVO);
				}
				ShoppingCartItemVO shoppingCartItemVO = new ShoppingCartItemVO();
				shoppingCartItemVO.fromsite = fromsite;
				shoppingCartItemVO.fromsiteimg = fromsiteimg;
				shoppingCartItemVO.typ="2";
				wayremark=wayremark.trim();
				fname=fromsite;
				shoppingCartItemVO.pid = rs.getString("pid");
				shoppingCartItemVO.pcode = rs.getString("skucode");
				shoppingCartItemVO.reffer=rs.getString("sreffer");
				shoppingCartItemVO.isopenid=rs.getString("isopenid");
				
				if ( "3".equals(rs.getString("ptyp")))
				{
					shoppingCartItemVO.linkurl = "presellDetail://pid="+rs.getString("pid");;
				}else{
					shoppingCartItemVO.linkurl = "pDe://pid="+rs.getString("pid");;
				}
				shoppingCartItemVO.title = rs.getString("title");
				shoppingCartItemVO.subtitle = rs.getString("subtitle");
				shoppingCartItemVO.currency = rs.getString("currency");
				shoppingCartItemVO.adstr1 = rs.getString("adstr1");
				
				Double chinaprice = rs.getDouble("chinaprice");
				BigDecimal bigChinaPrice = new BigDecimal(chinaprice);
				//shoppingCartItemVO.chinaprice = "¥"+bigChinaPrice.setScale(2, BigDecimal.ROUND_CEILING);
				shoppingCartItemVO.chinaprice = "¥"+bigChinaPrice.setScale(0, BigDecimal.ROUND_CEILING);
				
				BigDecimal price = new BigDecimal(rs.getDouble("price")/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
				
				BigDecimal rate = new BigDecimal(rs.getDouble("rate")/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
				//BigDecimal list_price = new BigDecimal(rs.getDouble("list_price")/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
				BigDecimal list_price = new BigDecimal(rs.getDouble("list_price")/100).setScale(0,  BigDecimal.ROUND_CEILING) ;
				shoppingCartItemVO.rate = rate.toString();
					
				//Double rmb_price= rate.multiply(price).setScale(2,BigDecimal.ROUND_CEILING).doubleValue();
				Double rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING).doubleValue();
				if ("1".equals(rs.getString("islockprice"))) {
					//rmb_price = new BigDecimal(rs.getDouble("rmbprice")).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
					rmb_price = new BigDecimal(rs.getDouble("rmbprice")).setScale(0, BigDecimal.ROUND_CEILING).doubleValue();
				} 
				//新人价
				if(userService.checkFirstFlag(uId.toString())){
					ProductPriceExt pe=productService.getProductPrice(Numbers.parseLong(shoppingCartItemVO.pid, 0L),Constants.getSystemGroupOne("newman"),"rmbprice");
					if(pe!=null){
						//rmb_price=new BigDecimal(pe.getSaleprice()).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
						rmb_price=new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING).doubleValue();
					}
				}
				Double freight=rs.getDouble("freight");
				Double totalfee =rmb_price;
				
				shoppingCartItemVO.logisticsFee = String.valueOf(freight);
				shoppingCartItemVO.rmb_price =  "¥"+totalfee;
				shoppingCartItemVO.rmbprice = String.valueOf(totalfee);
				
				if ("円".equals(rs.getString("symbol")))
				{
					shoppingCartItemVO.price= price + rs.getString("symbol");
					shoppingCartItemVO.list_price = list_price+ rs.getString("symbol");
				}
				else
				{
					shoppingCartItemVO.price = rs.getString("symbol")+price;
					shoppingCartItemVO.list_price = rs.getString("symbol")+list_price;
				}
				
				BigDecimal discount = list_price.intValue()==0 ? new BigDecimal(0) : price.divide(list_price,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
				Double discountDouble = discount.doubleValue();
				shoppingCartItemVO.discount =  (discountDouble>10?10:discountDouble )+"";
				shoppingCartItemVO.img = StringUtil.getListpic(rs.getString("listpic")) ; 
				
				int nstock=rs.getInt("nstock"); 
				if (nstock<=rs.getInt("limitcount") && nstock>=0)
				{
					shoppingCartItemVO.limitcount=nstock+"";
				}else{
					shoppingCartItemVO.limitcount=rs.getInt("limitcount")+""; 
				}
				shoppingCartItemVO.counts=rs.getString("counts"); 
				shoppingCartItemVO.specifications=rs.getString("specifications")==null?"":rs.getString("specifications");
				resultList.add(shoppingCartItemVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return resultList;
	}
	

	public int getSalesManShoppingCart_cnt(String uid) {
		int count=0;
		List<AppSalesManCartVO.dataInfo> dList = new ArrayList<AppSalesManCartVO.dataInfo>();
		String sql="SELECT SUM(counts) as cnt FROM shopping_Cart_endorse WHERE uid="+uid;
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				count=rs.getInt("cnt");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return count;
	}
	
	public BigDecimal getPriceShoppingCart_eid(String uid,String pid){
		BigDecimal price = new BigDecimal(0);
		String sql="SELECT e.endorsementPrice "
				+ " FROM shopping_Cart_endorse s,product p,endorsementduct e "
				+ " WHERE s.pid= p.pid AND s.`eid`=e.`eid` AND s.uid='"+uid+"' and s.pid="+pid;
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				price = rs.getBigDecimal("endorsementPrice");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return price;
	}
	
	public AppSalesManCartVO getSalesManShoppingCart_list(String uid) {
		AppSalesManCartVO result = new AppSalesManCartVO();
		List<AppSalesManCartVO.dataInfo> dList = new ArrayList<AppSalesManCartVO.dataInfo>();
		String sql="SELECT p.*,s.id,s.date_add as sdate_add,s.pId as spId,s.eid,s.id as sid,s.Uid,s.counts,f.name as fname,f.url as furl,f.img as fimg,f.fee as ffee,f.addfee as faddfee,c.name as cname,c.symbol as csymbol,c.rate,c.rate1,c.rate2,c.rate3,c.rate4,c.rate5,e.endorsementPrice as endorseprice,e.gid "
				+ " FROM shopping_Cart_endorse s,product p,fromsite f,currency c,endorsementduct e "
				+ " WHERE p.fromsite=f.id AND p.currency=c.id AND s.pid= p.pid AND s.`eid`=e.`eid` AND s.uid='"+uid+"'";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				AppSalesManCartVO.dataInfo dInfo = new AppSalesManCartVO.dataInfo();
				dInfo.pid = rs.getString("pid");
				int rmb_price=0;
				rmb_price = new BigDecimal(rs.getDouble("endorseprice")).setScale(0, BigDecimal.ROUND_CEILING).intValue();
				dInfo.title = rs.getString("title");
				dInfo.china_price=String.valueOf(rmb_price);
				dInfo.chinaprice= "¥"+String.valueOf(rmb_price);
				dInfo.linkurl = "pDe://pid="+rs.getString("pid");
				dInfo.img = StringUtil.getListpic(rs.getString("listpic"));
				int nstock=rs.getInt("nstock"); 
				if (nstock<=rs.getInt("limitcount") && nstock>=0)
				{
					dInfo.limitcount=nstock+"";
				}else{
					dInfo.limitcount=rs.getInt("limitcount")+""; 
				}
				dInfo.counts=rs.getString("counts");
				dList.add(dInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.setStatus("1");
		/*result.setDescriptionimg("http://ht.neolix.cn/pimgs/site/720.png");
		result.setExemptionimg("http://higou-api.oss-cn-beijing.aliyuncs.com/pimgs/site/799265860073752523.png");
		result.setExemptiontxt("满59包邮");*/
		result.setDescriptionimg("");
		result.setExemptionimg("");
		result.setExemptiontxt("");
		result.setData(dList);
		return result;
	}
	
	
	/**
	 * 获取当前购物车里是否存在新人商品
	 * @param uId
	 * @param pid
	 * @return
	 */
	public boolean checkShoppingCart_newMan(Long uId,Long pid) {
		
		return shoppingCartRepository.checkShoppingCart_newMan(uId,pid)>0;
	}
	/**
	 * 获取当前购物车里是否存在0元商品
	 * @param uId
	 * @param pid
	 * @return
	 */
	public boolean checkShoppingCart_newManZero(Long uId,Long pid) {
		return shoppingCartRepository.checkShoppingCart_newMan(uId,pid)>0;
	}
	
	
	
	public static Double getfreight(int fromsiteid, String weight) {
		Double freight = 0D;
		String sql = "select fee,addfee from fromsite where id=" + fromsiteid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		db.getPrepareStateDao(sql);// 执行语句，得到结果集
		ResultSet rs;
		try {
			rs = db.pst.executeQuery();
			while (rs.next()) {
				int fee = rs.getInt("fee");
				int addfee = rs.getInt("addfee");
				Double weightTemp = 0.0;
				if (Numbers.parseDouble(weight, 0.0) > 0.5) {
					weightTemp = Numbers.parseDouble(weight, 0.0) - 0.5;
				}
				freight = fee + addfee * weightTemp / 0.1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return freight;
	}
	
	public OrderLoveLy getOrderLovely() {
		return lovelyRepository.findOne(1L);
	}

	public Integer gettotalNum(Long uId, Long pId) {
		if(pId!=0){
			return shoppingCartRepository.getTotalNumByUIdAndPId(uId,pId);
		}else{
			return shoppingCartRepository.getTotalNumByUId(uId);
		}
	}

	public ShoppingCart getShoppingCartByUIdAndPId(Long uId,
			Long pId) {
		return shoppingCartRepository.findByUIdAndPId(uId,pId);
	}

	public ShoppingCart saveShoppingCart(ShoppingCart shoppingCart) {
		return shoppingCartRepository.save(shoppingCart);
	}
	
	@Transactional
	public void deleteShoppingCartByPIds(Long uId,List<Long> ids) {
		shoppingCartRepository.deleteShoppingCartByPIds(uId,ids);
		
	}
	@Transactional
	public void updateShoppingCart(String uid, String pid, Long nstock) {
		shoppingCartRepository.updateShoppingCart(uid,pid,nstock);
		
	}

	/*********************代言购物车相关        ****************************/
	/*
	 * 根据用户及产品编号获取购物车列表，PID及EID为0 取当前用户的全部
	 */
	public int getEnCartPidCnts(String openid,Long pid){
		int returncnt=0;
		String sql="SELECT sum(s.counts) as cnts FROM shopping_Cart_endorse s,product p,fromsite f,currency c,endorsementduct e WHERE p.fromsite=f.id AND p.currency=c.id AND s.pid= p.pid AND s.`eid`=e.`eid` AND s.openid='"+openid+"'";
    	if(pid!=null && pid.longValue()>0)
    		sql=sql+" and s.pid="+pid;
    	
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		db.getPrepareStateDao(sql);// 执行语句，得到结果集
		ResultSet rs;
		try {
			rs = db.pst.executeQuery();
			while(rs.next()){
				returncnt=rs.getInt("cnts");
			}
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return returncnt;
	}
	
    public List<ShoppingCartEndorse> getEnCartlist(String openid,Long pid,Long eid){
    	String sql="SELECT p.*,s.id,s.date_add as sdate_add,s.pId as spId,s.eid,s.id as sid,s.openid,s.counts,f.name as fname,f.url as furl,f.img as fimg,f.fee as ffee,f.addfee as faddfee,c.name as cname,c.symbol as csymbol,c.rate,c.rate1,c.rate2,c.rate3,c.rate4,c.rate5,e.endorsementPrice as endorse_price,e.gid FROM shopping_Cart_endorse s,product p,fromsite f,currency c,endorsementduct e WHERE p.fromsite=f.id AND p.currency=c.id AND s.pid= p.pid AND s.`eid`=e.`eid` AND s.openid='"+openid+"'";
    	if(pid!=null && pid.longValue()>0)
    		sql=sql+" and s.pid="+pid;
    	if(eid!=null && eid.longValue()>0)
    		sql=sql+" and s.eid="+eid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		db.getPrepareStateDao(sql);// 执行语句，得到结果集
		ResultSet rs;				
		List<ShoppingCartEndorse> selist=null;
		try {
			rs = db.pst.executeQuery();
			selist=new ArrayList<ShoppingCartEndorse>();
			while(rs.next()){
				ShoppingCartEndorse se=new ShoppingCartEndorse();
				se.setCounts(rs.getInt("counts"));
				se.setOpenid(rs.getString("openid"));
				se.setEid(rs.getLong("eid"));
				se.setpId(rs.getLong("spId"));
				se.setDate_add(rs.getDate("sdate_add"));
				se.setId(rs.getLong("sid"));
				Product p=new Product();
				p.setActivityimage(rs.getString("activityimage"));
				p.setActivityname(rs.getString("activityname"));
				p.setAdstr1(rs.getString("adstr1"));
				p.setAdstr3(rs.getString("adstr3"));
				p.setBacknstock(rs.getInt("backnstock"));
				p.setBtim(rs.getString("btim"));
				p.setCategory(rs.getInt("category"));
				p.setChinaprice(rs.getDouble("chinaprice"));
				//p.setCommision(rs.getDouble("commision"));
				//p.setCommision_average(rs.getString("commision_average"));
				//p.setCommisionTyp(rs.getInt("commisionTyp"));
				p.setCostPrice(rs.getDouble("costPrice"));
				p.setCounts(rs.getInt("counts"));
				p.setEndorsementPrice(rs.getDouble("endorsementPrice"));
				p.setLimitcount(rs.getInt("limitcount"));
				p.setList_price(rs.getDouble("list_price"));
				p.setListpic(rs.getString("listpic"));
				p.setPid(rs.getLong("pid"));
				p.setPrice(rs.getDouble("price"));
				p.setRmbprice(rs.getDouble("rmbprice"));
				p.setSpecifications(rs.getString("specifications"));
				p.setNationalFlag(rs.getString("nationalFlag"));
				p.setNewMantype(rs.getString("newMantype"));
				p.setNewSku(rs.getString("newSku"));
				p.setNstock(rs.getLong("nstock"));
				p.setTitle(rs.getString("title"));
				p.setSubtitle(rs.getString("subtitle"));
				p.setIsEndorsement(rs.getInt("isEndorsement"));
				p.setStock(rs.getInt("stock"));
				p.setWeight(rs.getDouble("weight"));
				p.setWayremark(rs.getString("wayremark"));
				p.setPpid(rs.getLong("ppid"));
				p.setIsopenid(rs.getInt("isopenid"));
				se.setProinfo(p);
				Fromsite fr=new Fromsite();
				fr.setAddfee(rs.getInt("faddfee"));
				fr.setFee(rs.getInt("ffee"));
				fr.setImg(rs.getString("fimg"));
				fr.setName(rs.getString("fname"));
				fr.setUrl(rs.getString("furl"));
				se.setFromsite(fr);
				Currency cu=new Currency();
				cu.setName(rs.getString("cname"));
				cu.setRate(rs.getDouble("rate"));
				cu.setRate1(rs.getString("rate1"));
				cu.setRate2(rs.getString("rate2"));
				cu.setRate3(rs.getString("rate3"));
				cu.setRate4(rs.getString("rate4"));
				cu.setRate5(rs.getString("rate5"));
				cu.setSymbol(rs.getString("csymbol"));
				Endorsement endorse=new Endorsement();
				endorse.setEid(se.getEid());
				endorse.setGid(rs.getLong("gid"));
				endorse.setEndorsementPrice(rs.getDouble("endorse_price"));
				se.setEndorse(endorse);
				se.setCurrency(cu);
				selist.add(se);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(selist==null || selist.isEmpty())
			selist=null;
		return selist;
    }
	
    /*
     * 新增.修改购物车
     */
    public ShoppingCartEndorse saveShopCartEndorse(ShoppingCartEndorse sce){
    	return shopCartEninterface.save(sce);
    }

    /*
     * 删除购物车某商品，PID为0为清空购物车
     */
    public void delShopCartEndorse(String openid,String pids,String eids){
    	String sql="delete FROM shopping_Cart_endorse WHERE openid='"+openid+"'";
    	if(!StringUtils.isBlank(pids))
    		sql=sql+" and pId in("+pids+")";
    	if(!StringUtils.isBlank(eids))
    		sql=sql+" and eid in("+eids+")";
    	
    	logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		db.getPrepareStateDao(sql);// 执行语句，得到结果集
			
		List<ShoppingCartEndorse> selist=null;
		try {
			db.pst.execute();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
    }
    
    /*
     * 获取购物车数量某商品
     */
    public Integer getCartEndorseCount(String openid,Long pid,Long eid){
    	if(eid!=null && eid.longValue()>0)
    		return shopCartEninterface.getTotalNumByopenIdAndPId(openid, pid,eid);
    	else
    		return shopCartEninterface.getTotalNumByopenIdAndPId(openid, pid);
    }
    //取总数量
    public Integer getCartEndorseAllCount(String openid){
    	return shopCartEninterface.getTotalNumByopenId(openid);
    }
	/*********************代言购物车相关结束****************************/	
}
