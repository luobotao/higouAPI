package services.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Currency;
import models.Product;
import play.Logger;
import utils.JdbcOper;

@Named
@Singleton
public class H5ShoppingService {

	private static final Logger.ALogger logger = Logger.of(H5ShoppingService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final ProductService productService=new ProductService();
	/*
	 * 大转盘抽奖
	 */
	public boolean choujiang(Long uid,Integer couponid){
		String sql="{call `sp_User_actDay`("+uid+","+couponid+")}";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		boolean suc=false;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				suc=rs.getInt("sta")==1?true:false;
			}
		}
		catch(Exception e){
		}finally {
			db.close();
		}
		return suc;
	}
	/*
	 * 获取当日投资次数
	 */
	public Integer getcjCount(Long uid){
		String sql="SELECT COUNT(id) as cc FROM user_actLog WHERE uid="+uid+" AND DATE_FORMAT(`date_add`,'%Y-%m-%d')=DATE_FORMAT(NOW(),'%Y-%m-%d')";
		JdbcOper db=JdbcOper.getInstance();
		Integer count=0;
		try{
			db.getPrepareStateDao(sql);
			ResultSet rs=db.pst.executeQuery();
			if(rs.next()){
				count=rs.getInt("cc");
			}
		}catch(Exception e){
			logger.info(e.toString());
		}finally {
			db.close();
		}
		return count;
	}
	
	/*
	 * 取首页频道商品列表
	 */
	public Map<Integer,List<Product>> getprolistByCid(Integer cid,Integer pageNo,Integer pagesize){
		String sql="{call `sp_H5List_get`('"+cid+"','"+pageNo+"','"+pagesize+"')}";
		JdbcOper db=JdbcOper.getInstance();
		Map<Integer,List<Product>> hmp=new HashMap<Integer,List<Product>>();
		
		List<Product> plist=new ArrayList<Product>();
		Integer totalcount=0;
		try{
			db.getPrepareStateDao(sql);
			ResultSet rs=db.pst.executeQuery();
			while(rs.next()){
				Product p=new Product();
				p.setPid(rs.getLong("pid"));
				p.setSkucode(rs.getString("skucode"));
				p.setTitle(rs.getString("title"));
				p.setSubtitle(rs.getString("subtitle"));
				p.setCategory(rs.getInt("category"));
				p.setPrice(rs.getDouble("price"));
				p.setList_price(rs.getDouble("list_price"));
				p.setDiscount(rs.getInt("discount"));
				p.setImgnums(rs.getInt("imgnums"));
				p.setExturl(rs.getString("exturl"));
				p.setSalesrank(rs.getInt("salesrank"));
				p.setStatus(rs.getInt("status"));
				p.setDate_add(rs.getDate("date_add"));
				p.setDate_upd(rs.getDate("date_upd"));
				p.setNlikes(rs.getInt("nlikes"));
				p.setIshot(rs.getInt("ishot"));
				p.setVersion(rs.getInt("version"));
				p.setExtcode(rs.getString("extcode"));
				p.setFromsite(rs.getInt("fromsite"));
				p.setCurrency(rs.getInt("currency"));
				p.setImgstr(rs.getString("imgstr"));
				p.setListpic(rs.getString("listpic"));
				p.setAdstr1(rs.getString("adstr1"));
				p.setAdstr3(rs.getString("adstr3"));
				p.setDetail(rs.getString("detail"));
				p.setSort(rs.getShort("sort"));
				p.setChinaprice(rs.getDouble("chinaprice"));
				p.setNstock(rs.getLong("nstock"));
				p.setNstock_autoupd(rs.getInt("nstock_autoupd"));
				p.setIslovely(rs.getString("islovely"));
				p.setTyp(rs.getString("typ"));
				p.setWeight(rs.getDouble("weight"));
				p.setFreight(rs.getDouble("freight"));
				p.setWayremark(rs.getString("wayremark"));
				p.setWishcount(rs.getInt("wishcount"));
				p.setActivityimage(rs.getString("activityimage"));
				p.setPromiseURL(rs.getString("promiseURL"));
				p.setLimitcount(rs.getInt("limitcount"));
				p.setLovelydistinct(rs.getDouble("lovelydistinct"));
				p.setRmbprice(rs.getDouble("rmbprice"));
				p.setIslockprice(rs.getInt("islockprice"));
				p.setDistinctimg(rs.getString("distinctimg"));
				p.setSendmailflg(rs.getString("sendmailflg"));
				p.setBacknstock(rs.getInt("backnstock"));
				p.setSpecifications(rs.getString("specifications"));
				p.setPpid(rs.getLong("ppid"));
				p.setStitle(rs.getString("stitle"));
				p.setIsopenid(rs.getInt("isopenid"));
				p.setSpecpic(rs.getString("specpic"));
				p.setNum_iid(rs.getString("num_iid"));
				p.setWx_upd(rs.getDate("wx_upd"));
				p.setWx_flg(rs.getString("wx_flg"));
				p.setStock(rs.getInt("stock"));
				p.setBtim(rs.getString("btim"));
				p.setDeposit(rs.getDouble("deposit"));
				p.setEtim(rs.getString("etim"));
				p.setMancnt(rs.getInt("mancnt"));
				p.setPreselltoast(rs.getString("preselltoast"));
				p.setPtyp(rs.getString("ptyp"));
				p.setRtitle(rs.getString("rtitle"));
				p.setStage(rs.getInt("stage"));
				p.setPaytim(rs.getString("paytim"));
				p.setJpntitle(rs.getString("jpntitle"));
				p.setJpncode(rs.getString("jpncode"));
				p.setNationalFlag(rs.getString("nationalFlag"));
				p.setEndorsementCount(rs.getInt("endorsementCount"));
				p.setIsFull(rs.getInt("isFull"));
				p.setMaxEndorsementCount(rs.getInt("maxEndorsementCount"));
				p.setNewSku(rs.getString("newSku"));
				p.setIsopenid(rs.getInt("isopenid"));
				p.setIsEndorsement(rs.getInt("isEndorsement"));
				p.setCommision(rs.getDouble("commision"));
				p.setCommisionTyp(rs.getInt("commisionTyp"));
				p.setEndorsementPrice(rs.getDouble("endorsementPrice"));
				p.setCommision_average(rs.getString("commision_average"));
				p.setForbiddenCnt(rs.getInt("forbiddenCnt"));
				p.setNewMantype(rs.getString("newMantype"));
				p.setIsSyncErp(rs.getInt("is_sync_erp"));
				p.setCostPrice(rs.getDouble("costPrice"));
				
				if (p.getTyp().equals("2")) {
					// 算折扣
					if (p.getRmbprice() > 0 && p.getList_price() > 0) {
						if (p.getChinaprice() != null
								&& p.getChinaprice().doubleValue() > 0) {
							BigDecimal mData = new BigDecimal(10
									* p.getRmbprice() / p.getChinaprice())
									.setScale(1, BigDecimal.ROUND_UP);
							p.setZhekou(mData.toString());
						} else {
							p.setZhekou("0");
						}
					} else
						p.setZhekou("0");
				} else {
					Currency currency = productService.queryCurrencyById(p
							.getCurrency());
					BigDecimal rate = new BigDecimal(currency.getRate() / 100)
							.setScale(4, BigDecimal.ROUND_CEILING);
					BigDecimal price = new BigDecimal(p.getPrice() / 100)
							.setScale(2, BigDecimal.ROUND_CEILING);
					BigDecimal rmb_price = rate.multiply(price).setScale(0,
							BigDecimal.ROUND_CEILING);
					p.setRmbprice(Double.valueOf(rmb_price.toString()));
					if (p.getChinaprice() > 0) {
						BigDecimal mData = new BigDecimal(10 * p.getRmbprice()
								/ p.getChinaprice()).setScale(1,
								BigDecimal.ROUND_UP);
						p.setZhekou(mData.toString());
					} else
						p.setZhekou("0");

				}
				
				
				totalcount=rs.getInt("cnt");
				plist.add(p);
			}
		}catch(Exception e){
			logger.info(e.toString());
		}finally {
			db.close();
		}

		hmp.put(totalcount, plist);
		return hmp;
	}
}
