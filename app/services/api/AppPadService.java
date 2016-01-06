package services.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import models.PadChannelPro;
import models.Product;
import play.Logger;
import repositories.PadChannelProRepository;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import vo.appPad.AppPadChannelProVO;
import vo.appPad.appPadChannelVO;

/**
 * 优惠券相关Service
 * @author luobotao
 * @Date 2015年5月12日
 */
@Named
@Singleton
public class AppPadService {

    private static final Logger.ALogger logger = Logger.of(AppPadService.class);
    private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @Inject
    private PadChannelProRepository padChannelProRepository;
	public appPadChannelVO getPadChannelList(String uid){
		appPadChannelVO result= new appPadChannelVO();
		result.setStatus("1");
		result.setMsg("");
		List<appPadChannelVO.Channel> cList = new ArrayList<appPadChannelVO.Channel>();
		String sql = "{call `sp_padChannel_get`("+uid+")}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				appPadChannelVO.Channel cInfo = new appPadChannelVO.Channel();
				cInfo.cid = rs.getInt("id")+"";
				cInfo.cname = rs.getString("cname");
				cList.add(cInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setStatus("0");
			result.setMsg("数据库异常");
		} finally {
			db.close();
		}
		result.setChannels(cList);
		return result;
	}
	
	public AppPadChannelProVO.channelInfo getPadChannelProList(String uid,String cid,String key,String page,String pagesize){
		String domain = StringUtil.getPICDomain();
		AppPadChannelProVO.channelInfo result = new AppPadChannelProVO.channelInfo();
		result.setMid("1");
		result.setMtype("2");
		List<AppPadChannelProVO.productInfo> pList = new ArrayList<AppPadChannelProVO.productInfo>(); 
		String sql = "{call `sp_padMould_get`("+uid+","+cid+","+page+","+pagesize+",'"+key+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				AppPadChannelProVO.productInfo pInfo = new AppPadChannelProVO.productInfo();
				pInfo.title = rs.getString("title");
				pInfo.subtitle = rs.getString("subtitle");
				pInfo.price = rs.getInt("rmbprice")+"";
				pInfo.listprice = "专柜价¥"+rs.getInt("chinaprice")+"";
				pInfo.soldoutimg="";
				String nstockstr="";
				if (rs.getInt("nstock")<=0){
					nstockstr="0";
					pInfo.soldoutimg=domain+"/pimgs/site/sellout-app.png";	
				}else{
					nstockstr= rs.getInt("nstock")+"";
				}
				pInfo.nstock =nstockstr;
				BigDecimal divide =new BigDecimal(1);
				String image = StringUtil.getWebListpic(rs.getString("skucode"),rs.getString("listpic"),"1080_640",divide);
				
				pInfo.img= domain+image;
				pInfo.imgmask="";
				if (!StringUtils.isBlank(rs.getString("nationalFlag")))
				{
					pInfo.nationalflagimg = domain+"/pimgs/site/"+rs.getString("nationalFlag").replace(".png", "-app.png");
				}else{
					pInfo.nationalflagimg = "";
				}	
				switch(rs.getString("nationalFlag")){
				case "usa.png":
					pInfo.nationalflag="美国直供";
					break;
				case "jpn.png":
					pInfo.nationalflag="日本直供";
					break;
				case "kor.png":
					pInfo.nationalflag="韩国直供";
					break;
				case "aus.png":
					pInfo.nationalflag="澳大利亚直供";
					break;
				case "ger.png":
					pInfo.nationalflag="德国直供";
					break;
				case "phi.png":
					pInfo.nationalflag="菲律宾直供";
					break;
				case "mys.png":
					pInfo.nationalflag="马来西亚直供";
					break;
				case "bga.png":
					pInfo.nationalflag="保加利亚直供";
					break;
				default:
					pInfo.nationalflag="";
					break;
				}
				pInfo.pid = rs.getInt("pid")+"";
				pInfo.linkurl = "pDe://pid="+rs.getInt("pid");
				
				BigDecimal b= new BigDecimal(rs.getDouble("rmbprice")/rs.getDouble("chinaprice")).setScale(2,RoundingMode.CEILING);
				if(b.doubleValue()>0)
				{
					String distinct=b.multiply(new BigDecimal(10)).setScale(1).toString();
					if(distinct.endsWith(".0")){
						distinct = distinct.replace(".0","");
					}
					pInfo.discount =distinct+"折";
				}else{
					pInfo.discount ="";
				}
				pList.add(pInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.setProductlist(pList);
		return result;
	}
	
	public List<String> getPadDimensionalimg(String uid,String pid){
		List<String> strList = new ArrayList<String>();
		String domain = StringUtil.getPICDomain();
		String sql = "SELECT dimensionalimg,endorsementPrice FROM endorsementduct WHERE userid="+uid+" AND productid="+pid;
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				if (!StringUtils.isBlank(rs.getString("dimensionalimg")))
				{
					strList.add(domain+rs.getString("dimensionalimg"));
					
				}else{
					strList.add("");
				}
				if (rs.getInt("endorsementPrice")>0)
				{
					strList.add(String.valueOf(rs.getInt("endorsementPrice")));
					
				}else{
					strList.add("100");
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return strList;
	}
	
	public AppPadChannelProVO.channelInfo getPadSearchProList(String uid,String cid,String key,String page,String pagesize){
		String domain = StringUtil.getPICDomain();
		AppPadChannelProVO.channelInfo result = new AppPadChannelProVO.channelInfo();
		result.setMid("1");
		result.setMtype("3");
		List<AppPadChannelProVO.productInfo> pList = new ArrayList<AppPadChannelProVO.productInfo>(); 
		String sql = "{call `sp_padMould_get`("+uid+","+cid+","+page+","+pagesize+",'"+key+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				AppPadChannelProVO.productInfo pInfo = new AppPadChannelProVO.productInfo();
				pInfo.title = rs.getString("title");
				pInfo.subtitle = rs.getString("subtitle");
				pInfo.price = rs.getInt("rmbprice")+"";
				pInfo.listprice = "专柜价¥"+rs.getInt("chinaprice")+"";
				pInfo.soldoutimg="";
				String nstockstr="";
				if (rs.getInt("nstock")<=0){
					nstockstr="0";
					pInfo.soldoutimg=domain+"/pimgs/site/sellout-app.png";	
				}else{
					nstockstr= rs.getInt("nstock")+"";
				}
				pInfo.nstock =nstockstr;
				BigDecimal divide =new BigDecimal(1);
				String image = StringUtil.getWebListpic(rs.getString("skucode"),rs.getString("listpic"),"1080_640",divide);
				
				pInfo.img= domain+image;
				pInfo.imgmask="";
				if (!StringUtils.isBlank(rs.getString("nationalFlag")))
				{
					pInfo.nationalflagimg = domain+"/pimgs/site/"+rs.getString("nationalFlag").replace(".png", "-app.png");
				}else{
					pInfo.nationalflagimg = "";
				}	
				switch(rs.getString("nationalFlag")){
				case "usa.png":
					pInfo.nationalflag="美国直供";
					break;
				case "jpn.png":
					pInfo.nationalflag="日本直供";
					break;
				case "kor.png":
					pInfo.nationalflag="韩国直供";
					break;
				case "aus.png":
					pInfo.nationalflag="澳大利亚直供";
					break;
				case "ger.png":
					pInfo.nationalflag="德国直供";
					break;
				case "phi.png":
					pInfo.nationalflag="菲律宾直供";
					break;
				case "mys.png":
					pInfo.nationalflag="马来西亚直供";
					break;
				case "bga.png":
					pInfo.nationalflag="保加利亚直供";
					break;
				default:
					pInfo.nationalflag="";
					break;
				}
				pInfo.pid = rs.getInt("pid")+"";
				pInfo.linkurl = "pDe://pid="+rs.getInt("pid");
				BigDecimal b= new BigDecimal(rs.getDouble("rmbprice")/rs.getDouble("chinaprice")).setScale(2,RoundingMode.CEILING);
				if(b.doubleValue()>0)
				{
					String distinct=b.multiply(new BigDecimal(10)).setScale(1).toString();
					if(distinct.endsWith(".0")){
						distinct = distinct.replace(".0","");
					}
					pInfo.discount =distinct+"折";
				}else{
					pInfo.discount ="";
				}
				pList.add(pInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.setProductlist(pList);
		return result;
	}

	/**
	 * 根据频道ID获取该频道下的Banner
	 * @param channelId
	 * @return
	 */
	public List<PadChannelPro> getPadChannelBannerList(final String channelId) {
		return padChannelProRepository.findAll(
				new Specification<PadChannelPro>() {
					@Override
					public Predicate toPredicate(Root<PadChannelPro> padChannelPro, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						List<Predicate> predicates = new ArrayList<>();
						Path<Long> cid = padChannelPro.get("cid");
						Path<String> typ = padChannelPro.get("typ");//0不显示 1显示
						Path<String> typFlag = padChannelPro.get("typFlag");//0商品1Banner
						predicates.add(builder.equal(cid, channelId));
						predicates.add(builder.equal(typFlag, "1"));
						predicates.add(builder.equal(typ, "1"));
						
						Predicate[] param = new Predicate[predicates.size()];
						predicates.toArray(param);
						return query.where(param).getRestriction();
					}
				},new Sort(Direction.DESC, "nsort"));
	}
	
	/*
	 * 查询商户频道商品列表
	 */
	public Map<Integer,List<Product>> getChannelProlist(String uid,String cid,String key,String page,String pagesize){
		String domain = StringUtil.getPICDomain();
		List<Product> pList = new ArrayList<Product>(); 
		String sql = "{call `sp_padMould_get`("+uid+","+cid+","+page+","+pagesize+",'"+key+"')}";
		logger.info(sql);
		Integer totalcount=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Product pInfo = new Product();
				pInfo.setTitle(rs.getString("title"));
				pInfo.setSubtitle(rs.getString("subtitle"));
				pInfo.setRmbprice(rs.getDouble("rmbprice"));
				pInfo.setChinaprice(rs.getDouble("chinaprice"));
				pInfo.setNstock(rs.getLong("nstock"));
				pInfo.setSkucode(rs.getString("skucode"));
				pInfo.setListpic(rs.getString("listpic"));				
				pInfo.setEndorsementPrice(rs.getBigDecimal("ePrice").doubleValue());
				
				pInfo.setImgurl(domain+rs.getString("imgurl"));
				pInfo.setLinkurl(rs.getString("linkurl"));
				pInfo.setTypFlag(rs.getString("typFlag"));
				
				
				if (!StringUtils.isBlank(rs.getString("nationalFlag")))
				{
					pInfo.setNationalFlag(domain+"/pimgs/site/"+rs.getString("nationalFlag").replace(".png", "-app.png"));
				}
				switch(rs.getString("nationalFlag")){
				case "usa.png":					
					pInfo.setWayremark("美国直供");
					break;
				case "jpn.png":
					pInfo.setWayremark("日本直供");
					break;
				case "kor.png":
					pInfo.setWayremark("韩国直供");
					break;
				case "aus.png":
					pInfo.setWayremark("澳大利亚直供");
					break;
				case "ger.png":
					pInfo.setWayremark("德国直供");
					break;
				case "phi.png":
					pInfo.setWayremark("菲律宾直供");
					break;
				case "mys.png":
					pInfo.setWayremark("马来西亚直供");
					break;
				case "bga.png":
					pInfo.setWayremark("保加利亚直供");
					break;
				default:
					pInfo.setWayremark("");
					break;
				}
				pInfo.setPid(rs.getLong("pid"));
				pInfo.setEndorsementId(rs.getLong("eid"));
				totalcount=rs.getInt("cnt");
				BigDecimal b= new BigDecimal(rs.getDouble("ePrice")/rs.getDouble("chinaprice")).setScale(2,RoundingMode.CEILING);
				if(b.doubleValue()>0)
				{
					String distinct=b.multiply(new BigDecimal(10)).setScale(1).toString();

					pInfo.setZhekou(distinct);
				}else{
					pInfo.setZhekou("0");
				}
				pList.add(pInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}	
		Map<Integer,List<Product>> pmap=new HashMap<Integer,List<Product>>();
		pmap.put(totalcount, pList);
		return pmap;
	}
}
