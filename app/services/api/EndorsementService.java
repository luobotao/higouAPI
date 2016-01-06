package services.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.BalanceOperLog;
import models.CountH5;
import models.EndoresementOpLog;
import models.Endorsement;
import models.EndorsementContent;
import models.EndorsementImg;
import models.EndorsementPraise;
import models.EndorsementPre;
import models.EndorsementReport;
import models.Qanswer;
import models.Question;
import models.User;
import models.UserBalance;
import models.UserBalanceLog;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.BalanceOperInterface;
import repositories.CountH5Interface;
import repositories.EndorseContentInterface;
import repositories.EndorsementImgInterface;
import repositories.EndorsementInterface;
import repositories.EndorsementPayInterface;
import repositories.EndorsementReportInterface;
import repositories.EndorsementpreInterface;
import repositories.ProductRepository;
import repositories.QuestionInterface;
import repositories.UserBalanceInterface;
import repositories.UserbalanceLogInterface;
import utils.JdbcOper;
import utils.JdbcOperWithClose;
import utils.StringUtil;
import vo.endorsment.EndorsePaylogVO;
import vo.endorsment.EndorsePaylogVO.EndorsePayLogVoItem;
import vo.endorsment.EndorsePaylogVO.EnorsmentpayVOItem;
import assets.CdnAssets;

@Named
@Singleton
public class EndorsementService {
	private static final Logger.ALogger logger = Logger.of(EndorsementService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat CHINESE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private static final String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
	@Inject
	private EndorsementInterface endorsmentinterface;
	@Inject
	private EndorsementpreInterface endorsementpreinterface;
	@Inject
	private EndorseContentInterface endosecontentinterface;
	@Inject
	private EndorsementImgInterface endorsementimginterface;
	@Inject
	private EndorsementPayInterface endorsementpayinterface;
	@Inject
	private EndorsementReportInterface endorsementReportInterface;
	@Inject
	private UserBalanceInterface userbalanceinterface;
	@Inject
	private UserService u;
	@Inject
	private BalanceOperInterface balanceoperinterface;
	@Inject
	private UserbalanceLogInterface userbalanceloginterface;
	
	@Inject
	private ProductRepository productinterface;
	
	@Inject
	private QuestionInterface questioninterface;
	
	@Inject
	private CountH5Interface countH5interface;
	/*
	 * 20150526添加,查询代言
	 */
	@Transactional(readOnly = true)
	public Page<Endorsement> getEndorsmentlist(final Long userid, final Integer stats,Integer page,final Integer isall){		
		Sort sort = new Sort(Sort.Direction.DESC,"sort","createTime");//(new Sort(Sort.Direction.DESC, "createTime"));
		
		return endorsmentinterface.findAll(new Specification<Endorsement>(){
			@Override
			public Predicate toPredicate(Root<Endorsement> endors,CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				
	            List<Predicate> predicates = new ArrayList<>();
	            Path<Integer> status=endors.get("status");
	            Path<Long> gid=endors.get("gid");
				if (userid>0L){
					Path<Long> uid=endors.get("userId");
					if(isall==1){
						Predicate andown=builder.and(builder.equal(uid, userid),builder.equal(status,0));
						Predicate others = builder.or(andown,builder.equal(status, 1));
						predicates.add(others);
					}
					else
					{
						predicates.add(builder.equal(uid, userid));
						Predicate andown=builder.or(builder.equal(status, 0),builder.equal(status, 1));
						predicates.add(andown);
					}
		        }else{
					predicates.add(builder.equal(status, stats));
		        }
				predicates.add(builder.notEqual(gid, 6));
				predicates.add(builder.notEqual(gid, 4));
				predicates.add(builder.isNotNull(gid));
//				Predicate gidd=builder.or(builder.notEqual(gid, 4),builder.notEqual(gid, 6)),builder.isNull(gid));
//				
//				predicates.add(gidd);
	            Predicate[] param = new Predicate[predicates.size()];
	            predicates.toArray(param);            
	            return query.where(param).getRestriction();
			}
		}, new PageRequest(page,10,sort));
	}
	
	//检查用户是否对该代言点赞
	public boolean getendorsmentIsPraise(Long emid,Long userid){
		Integer cnt=0;
		String sqlc="select count(*) as cnt from endorsement_praise where eid="+emid+" and userId="+userid;
		try {
			JdbcOperWithClose db = JdbcOperWithClose.getInstance();
			db.getPrepareStateDao(sqlc);
			ResultSet rsc=db.pst.executeQuery();
			if(rsc.next()){
				cnt=rsc.getInt("cnt");
			}
		}catch(Exception ex){
			cnt=0;
		}
		if (cnt>0){
			return true;
		}else{
			return false;
		}
	}
	
	//代言点赞
	public boolean endorsmentPraise(Long emid,Long userid){
		String sqlc="select * from endorsement_praise where eid="+emid+" and userId="+userid;
		logger.info(sqlc);
		
		String sql = "insert into endorsement_praise(ImgPath,createTime,eid,userId) select headIcon,'"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"',"+emid+",uid from user where uid="+userid;
		logger.info(sql);
		
		String sqladd = "update endorsementduct set count=count+1 where eid="+emid;
		logger.info(sqladd);
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sqlc);
			ResultSet rs = db.pst.executeQuery();
			if(rs.next()){
				return false;
			}
			else
			{
				db.updateSql(sql);
				db.updateSql(sqladd);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			db.close();
		}
		return true;
		
	}
	
	//代言点赞--微信使用
		public boolean endorsmentPraise(Long emid,String headicon,String openid,String unionid,String nickname){
			String sqlc="select count(1) as count from endorsement_praise where eid="+emid+" and openId='"+openid+"'";
			logger.info(sqlc);
			
			String sql = "insert into endorsement_praise(ImgPath,createTime,eid,openId,unionId,nickName) values('"+headicon+"','"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"',"+emid+",'"+openid+"','"+unionid+"','"+nickname+"')";
			logger.info(sql);
			
			String sqladd = "update endorsementduct set count=count+1 where eid="+emid;
			logger.info(sqladd);
			
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sqlc);
				ResultSet rs = db.pst.executeQuery();
				Integer count=0;
				if(rs.next()){
					count=rs.getInt("count");
				}

				if(count<1)
				{
					db.updateSql(sql);
					db.updateSql(sqladd);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				db.close();
			}
			return true;
			
		}
	
	//点赞－非微信使用
		public boolean endorsmentPraise(Long emid,User user){
			String sqlc="select count(1) as count from endorsement_praise where eid="+emid+" and userId="+user.getUid();
			logger.info(sqlc);
			
			String sql = "insert into endorsement_praise(ImgPath,createTime,eid,userId,nickName) values('"+user.getHeadIcon()+"','"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"',"+emid+","+user.getUid()+",'"+user.getNickname()+"')";
			logger.info(sql);
			
			String sqladd = "update endorsementduct set count=count+1 where eid="+emid;
			logger.info(sqladd);
			
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sqlc);
				ResultSet rs = db.pst.executeQuery();
				Integer count=0;
				if(rs.next()){
					count=rs.getInt("count");
					logger.info(count+"");
				}

				if(count<1)
				{
					db.updateSql(sql);
					db.updateSql(sqladd);
				}
				else
					return false;
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				db.close();
			}
			return true;
			
		}
	//取消代言点赞
	public boolean unendorspraise(Long emid,Long uid){
		String sqlc="select * from endorsement_praise where eid="+emid+" and userId="+uid;
		logger.info(sqlc);
		
		String sql = "delete from endorsement_praise where eid="+emid+" and userId="+uid;
		logger.info(sql);
		
		String sqldel = "update endorsementduct set count=count-1 where eid="+emid;
		logger.info(sqldel);
	
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		
		try {		
			db.getPrepareStateDao(sqlc);
			ResultSet rs = db.pst.executeQuery();
			if(!rs.next()){
				return false;
			}
			else{
				db.updateSql(sql);
				db.updateSql(sqldel);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			db.close();
		}
		return true;
	}
	
	public EndorsementReport saveEndorsementReport(EndorsementReport endorsementReport){
		return endorsementReportInterface.save(endorsementReport);
	}
	
	//添加代言信息
	public Endorsement saveEndorsement(Endorsement endorsment){
		return endorsmentinterface.save(endorsment);
	}
	
	//根据用户编号获取代言申请信息
	public EndorsementPre getPreByUid(Long userid){
		return endorsementpreinterface.findOne(userid);
	}
	
	//根据产品编号,用户编号查询代言信息
	public List<Endorsement> getEndorsementInfo(Long userid,Long productid){
		//return endorsmentinterface.getEndorsementInfo(userid, productid);
		String sql="select * from endorsementduct where userId="+userid+" and productId="+productid;
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		List<Endorsement> endorselist=new ArrayList<Endorsement>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			
			while(rs.next()){
				Endorsement en=new Endorsement();
				en.setBannerimg(rs.getString("bannerimg"));
				en.setCount(rs.getInt("count"));
				en.setCreateTime(rs.getDate("createTime"));
				en.setEid(rs.getLong("eid"));
				en.setPicnums(rs.getInt("picnums"));
				en.setPreImgPath(rs.getString("preImgPath"));
				en.setProductId(rs.getLong("productId"));
				en.setRemark(rs.getString("remark"));
				en.setStatus(rs.getInt("status"));
				en.setUserId(rs.getLong("userId"));
				endorselist.add(en);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		endorselist=endorselist==null||endorselist.isEmpty()?null:endorselist;
		return endorselist;
	}
	//获取随机方案
	public EndorsementContent getRanContent(){
		return endosecontentinterface.getRanContent();
	}

	//更新代言信息
	@Transactional
	public void upodateEndorse(String content,Long dyid,int picnums){
		endorsmentinterface.updateEndorse(content, dyid,picnums);
	}
	
	//修改代言状态
	@Transactional
	public void updateEndorseSta(Long eid,Integer status){
		endorsmentinterface.updateEndoseStatus(eid, status);
	}
	
	//获取代言详情
	public Endorsement getEndorseDetail(Long eid,Integer praisesize){		
		Endorsement em=endorsmentinterface.findOne(eid);
		String domains = StringUtil.getOSSUrl();
		if(em!=null){
			em.setProducinfo(productinterface.findOne(em.getProductId()));			
			List<EndorsementImg> imglist=endorsementimginterface.querybyEid(eid);
			Integer picnums=em.getPicnums()==null?0:em.getPicnums();
			em.setEndorsImgList(new ArrayList<EndorsementImg>());
			if(imglist!=null && !imglist.isEmpty() && imglist.size()==picnums){
				//for(EndorsementImg emg:imglist){
				imglist.remove(0);
				for(int i=0;i<imglist.size();i++){
					EndorsementImg emg=imglist.get(i);
					if(!StringUtils.isBlank(emg.getImgPath()))
						emg.setImgPath(domains+emg.getImgPath());
					if(emg.getRemark()==null)
						emg.setRemark("");
					if(emg.getUpdateTime()==null)
						emg.setUpdateTime(new Date());
				}
				em.setEndorsImgList(imglist);
			}
			em.setEndorsPraiseList(this.getPraiselist(eid,praisesize));
		}
		return em;
	}
	
	//获取代言点赞列表
	public List<EndorsementPraise> getPraiselist(Long eid,Integer limit){
		String sql="SELECT p.*,u.sex FROM endorsement_praise p LEFT JOIN `user` u ON u.uid=p.userId WHERE p.eid="+eid+" AND ImgPath IS NOT NULL order by p.createTime desc limit "+limit;
		logger.info(sql);

		List<EndorsementPraise> plist=new ArrayList<EndorsementPraise>();
		
		String domains=CdnAssets.CDN_API_PUBLIC_URL;
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			
			while(rs.next()){
				EndorsementPraise p=new EndorsementPraise();
				p.setRid(rs.getLong("rid"));
				p.setCreateTime(rs.getDate("createTime"));
				p.setEid(rs.getLong("eid"));
				p.setImgPath(rs.getString("ImgPath"));
				if(StringUtils.isBlank(p.getImgPath()) || p.getImgPath().toLowerCase().equals("null"))
					p.setImgPath(domains+"images/sheSaidImages/default_headicon_girl.png");
								
				p.setSex(StringUtils.isBlank(rs.getString("sex"))?"0":rs.getString("sex"));
						
				p.setUserId(rs.getLong("userId"));
				plist.add(p);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(plist==null||plist.isEmpty())
			plist=null;
		
		return plist;
	}
	
	
	//获取代言流水明细列表实体
	public EndorsePaylogVO getEndorsepayloglist(Long uid,Integer pagesize,Integer page){
		String sql="select substr(createTime,1,10) as createTime from endorsement_pay_log where userId="+uid+" group by SUBSTR(createTime,1,10) order by createTime desc  LIMIT "+page*pagesize+","+pagesize;
		logger.info(sql);
		EndorsePaylogVO vo=new EndorsePaylogVO();
		
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		try {						
			vo.status="1";
			vo.total="0.00";
			vo.availtotal="0.00";
			vo.entotal="0.00";
			vo.endflag=0;
			vo.rulesurl=StringUtil.getDomainH5()+"/H5/endorse_rule";
			User uu=u.getUserByUid(uid);
			if(uu!=null && uu.getGid()==4){
				vo.rulesurl="http://ht.neolix.cn/www/wap/13/text-3.html";
			}
			
			UserBalance ub=userbalanceinterface.getbalance(uid);
			if(ub!=null)
				vo.availtotal=String.valueOf(ub.getBalance().setScale(2,BigDecimal.ROUND_FLOOR));
			String sqlc="select enbalance as balance from user_balance where userId="+uid;
			db.getPrepareStateDao(sqlc);
			ResultSet rsc=db.pst.executeQuery();
			while(rsc.next()){
				vo.entotal=rsc.getBigDecimal("balance")==null?"0":String.valueOf(rsc.getBigDecimal("balance").setScale(2,BigDecimal.ROUND_FLOOR));
			}
			
			BigDecimal sumEndorsementPay=new BigDecimal(0);
			String sqla="select sum(balance) as balance from endorsement_pay_log where userId="+uid+" and status<=1";
			db.getPrepareStateDao(sqla);
			ResultSet rsa=db.pst.executeQuery();
			while(rsa.next()){
				sumEndorsementPay=rsa.getBigDecimal("balance");
			}
		
			vo.total = sumEndorsementPay==null?"0":sumEndorsementPay.toString();
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			int i=0;
			while(rs.next()){
				i=i+1;
				EnorsmentpayVOItem itm=new EnorsmentpayVOItem();
				double curb=0;
				itm.createTime=rs.getString("createTime")==null?"":rs.getString("createTime");
				
				String sqld="SELECT e.eid,e.userId,e.balance,u.nickname,e.createTime,e.status,e.protitle,p.listpic,p.skucode,sp.counts"
						+" FROM endorsement_pay_log e,`user` u,product p,shopping_Order s,shopping_Order_Pro sp"
						+" WHERE  e.orderId = s.id AND s.uId=u.uId AND e.pid = p.pid AND s.id=sp.orderId AND e.pid=sp.pid"
						+" AND e.userId="+uid+" AND SUBSTR(e.createTime,1,10)='"+itm.createTime+"'"
						+" GROUP BY u.nickname,e.balance,e.eid,e.orderId,e.status,SUBSTR(e.createTime,1,10) ORDER BY e.createTime DESC";

				/*
				 	String sqld="SELECT e.eid,e.userId,e.balance,u.nickname,e.createTime,e.status,e.protitle,p.listpic,p.skucode,ps.counts"
						+" FROM endorsement_pay_log e,`user` u,product p,shopping_Order s,(SELECT SUM(counts) AS counts,orderId FROM shopping_Order_Pro GROUP BY orderId) ps"
						+" WHERE  e.orderId = s.id AND s.uId=u.uId AND e.pid = p.pid AND s.id=ps.orderId AND e.userId="+uid+" AND SUBSTR(e.createTime,1,10)='"+itm.createTime
						+"' GROUP BY u.nickname,e.balance,e.eid,e.orderId,e.status,SUBSTR(e.createTime,1,10) order by e.createTime desc";
				*/		
				db.getPrepareStateDao(sqld);
				ResultSet rsd=db.pst.executeQuery();
				List<EndorsePayLogVoItem> logList = new ArrayList<>();;
				while(rsd.next()){
					EndorsePayLogVoItem plog=new EndorsePayLogVoItem();
					plog.balance=String.valueOf(rsd.getDouble("balance"))==null?"":String.valueOf(rsd.getBigDecimal("balance").setScale(2,BigDecimal.ROUND_FLOOR));
					plog.producttitle=rsd.getString("protitle")==null?"":rsd.getString("protitle");
					plog.status=String.valueOf(rsd.getInt("status"));
					if(plog.status.equals("2")){
						plog.balance="- "+plog.balance;
						//计算当日金额						
						//curb=curb-rsd.getDouble("balance");
						plog.title=" 退订商品";
					}else if(plog.status.equals("1")){
						plog.balance="+ "+plog.balance;
						curb=curb+rsd.getDouble("balance");
						plog.title=" 购买商品";
					}
					else
					{
						curb=curb+rsd.getDouble("balance");
						plog.balance="+ "+plog.balance;
						plog.title=" 购买商品";
					}
					String backbuyerName="";
					if(rsd.getString("eid").equals("0")){
						backbuyerName="[APP购买]";
					}else{
						Endorsement e=getEndorseDetail(rsd.getLong("eid"),1);
						if (e!=null && e.getGid()==4){
							backbuyerName="[商铺购买]";
						}
					}
					plog.tim=rsd.getDate("createTime")==null?"":CHINESE_TIME_FORMAT.format(rsd.getTime("createTime"));
					plog.buyerName = rsd.getString("nickname")==null?"":backbuyerName + rsd.getString("nickname") + plog.title ;
					plog.count = String.valueOf(rsd.getInt("counts"))==null?"":String.valueOf(rsd.getInt("counts"));
					//plog.imageUrl = rsd.getString("listpic")==null?"":StringUtil.getOSSUrl()+"pimgs/site/"+rsd.getString("listpic");
					plog.imageUrl=StringUtil.getPICDomain()+StringUtil.getWebListpic(rsd.getString("skucode"),rsd.getString("listpic"),"480_800",new BigDecimal(1));
					logList.add(plog);				
				}
				itm.loglist = logList;
				itm.totalbalance=String.valueOf(new BigDecimal(curb).setScale(2, RoundingMode.HALF_UP));
				vo.data.add(itm);
			}
			if (i<pagesize || i==0){
				vo.endflag=1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		
		return vo;
	}
	
	//判断用户点过赞否
	public boolean getPraiseStatus(Long uid,Long eid){
		String sql="SELECT count(1) as cnt FROM endorsement_praise WHERE eid="+eid+" and userId="+uid;
		logger.info(sql);
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		Integer count=0;
		
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			
			while(rs.next()){
				count=rs.getInt("cnt");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(count>0)
			return true;
		else
			return false;
	}
	//修改用户余额
	@Transactional
	public void updatebalance(Long userId,BigDecimal balance){
		 userbalanceinterface.updateBalance(userId, balance);
	}
	//添加用户余额操作任务
	public BalanceOperLog addOperLog(BalanceOperLog oper){
		return balanceoperinterface.save(oper);
	}
	
	//删除代言
	@Transactional
	public void delEndorsement(Long uid,Long eid){
		 endorsmentinterface.updateEndoseStatus(uid, eid, 0);
	}
	
	//分页查询用户钱包流水信息
	@Transactional(readOnly = true)
	public Page<UserBalanceLog> userbalanceloglist(final Long userid, Integer pagesize,Integer page){
		Sort sort = new Sort(Sort.Direction.DESC, "createTime");
		return userbalanceloginterface.findAll(new Specification<UserBalanceLog>(){
			private boolean add;			
			@Override
			public Predicate toPredicate(Root<UserBalanceLog> endors,CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				Path<Long> uid=endors.get("userId");
				
	            List<Predicate> predicates = new ArrayList<>();
	               
	            predicates.add(builder.equal(uid, userid));	            

	            Predicate[] param = new Predicate[predicates.size()];
	            predicates.toArray(param);            
	            return query.where(param).getRestriction();
			}
		}, new PageRequest(page,pagesize,sort));
	}
	
	//获取用户余额信息
	public UserBalance getUserBalance(Long uid){
		return userbalanceinterface.getbalance(uid);
	}
	//保存用户余额信息
	public UserBalance saveUserBalance(UserBalance userBalance){
		return userbalanceinterface.save(userBalance);
	}
	//添加代言图片'
	public EndorsementImg saveEimg(EndorsementImg img){
		return endorsementimginterface.save(img);
	}
	
	//添加余额变动日志
	public UserBalanceLog addbalanceLog(UserBalanceLog ulog){
		return userbalanceloginterface.save(ulog);
	}
	
	//修改代言首图
	@Transactional(readOnly = true)
	public void updatePreimg(String preimgpath,Long eid){
		String sql = "update endorsementduct set preImgPath='"+preimgpath+"' where eid="+eid;
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {			
				db.updateSql(sql);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	//获取代言
	public Endorsement getEndorseById(Long eid){
		return endorsmentinterface.findOne(eid);
	}
	//取系统宣传代言，取状态为－99的数据
	public List<Endorsement> getEndorseByStatus(Integer status){
		String sql="select * from endorsementduct where status="+status;
		logger.info(sql);
		List<Endorsement> elist=new ArrayList<Endorsement>();
		JdbcOper db=JdbcOper.getInstance();// 创建DBHelper对象
		try{
			db.getPrepareStateDao(sql);
			ResultSet rs=db.pst.executeQuery();
			String dbcode="";
			while(rs.next()){
				Endorsement ep=new Endorsement();
				ep.setBannerimg(rs.getString("bannerimg"));
				ep.setCount(rs.getInt("count"));
				ep.setCreateTime(rs.getDate("createTime"));
				ep.setEid(rs.getLong("eid"));
				ep.setPreImgPath(rs.getString("preImgPath"));
				ep.seteType(rs.getInt("eType"));
				ep.setPicnums(rs.getInt("picnums"));
				ep.setProductId(rs.getLong("productId"));
				ep.setRemark(rs.getString("remark"));
				ep.setSort(rs.getInt("sort"));
				ep.setStatus(rs.getInt("status"));
				ep.setUserId(rs.getLong("userId"));
				elist.add(ep);
			}
		}
		catch(Exception e){
			logger.error(e.toString());
		}finally {
			db.close();
		}
		return elist;
	}
	
	//获取是暗号数据,根据暗号
	public boolean isEndorementcode(String code){
		String sql="select * from endorsment_code where code='"+code+"' and status=0";
		logger.info(sql);
		JdbcOper db=JdbcOper.getInstance();// 创建DBHelper对象
		try{
			db.getPrepareStateDao(sql);
			ResultSet rs=db.pst.executeQuery();
			String dbcode="";
			while(rs.next()){
				dbcode=rs.getString("code");
			}
			if(!StringUtils.isBlank(dbcode))
				return true;
			else
				return false;
		}
		catch(Exception e){
			logger.error(e.toString());
		}finally {
			db.close();
		}
		
		return false;
	}
	
	//H5代言问题保存
	public Question saveQuestion(Question q){
		return questioninterface.save(q);
	}
	//H5代言问题获取，根据用户编号
	public List<Question> getQuestionbyUid(Long uid){
		return questioninterface.getQuestbyUid(uid);
	}
	//H5代言答案保存
	public boolean saveAnswer(Qanswer an){
		String sql = "insert into qanswer(qkey,qvalue,addtime,pid) values('"+an.getQkey()+"','"+an.getQvalue()+"','"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"',"+an.getPid()+")";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {			
				db.updateSql(sql);				
		} catch (Exception e) {
			return false;
		} finally {
			db.close();
		}
		return true;
	}
	
	//添加抢代言商品日志
	public boolean addEndorseOptLog(EndoresementOpLog opt){
		String sql = "insert into endorsement_opt_log(userId,pid,createTime) values("+opt.getUserId()+","+opt.getPid()+",'"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"')";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {			
				db.updateSql(sql);				
		} catch (Exception e) {
			return false;
		} finally {
			db.close();
		}
		return true;
	}
	
	//修改抢代言日志（回填代言编号，发表时间）
	public boolean editEndorseOptLog(Long uid,Long pid,Long eid){
		String sqls="select * from endorsement_opt_log where userId="+uid+" and pid="+pid+" order by 1 desc limit 1";
		String sql="update endorsement_opt_log set eid="+eid+",updatetime='"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"' where id=";
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {		
			Long id=0L;
			db.getPrepareStateDao(sqls);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				id=rs.getLong("id");
				sql=sql+id;
			}
				
			if(id>0)
			{
				db.getPrepareStateDao(sql);
				db.updateSql(sql);
			}
		} catch (Exception e) {
			return false;
		} finally {
			db.close();
		}
		return true;
	}
	//检查是否能发表代言（暂定半小时内没被发表过的可以发表）
	public boolean checkEndorsement(Long uid,Long pid,Date tim){
		String sql="select * from endorsement_opt_log where userId="+uid+" and pid="+pid+" order by 1 desc limit 1";
		logger.info(sql);
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		Date etim=null;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			
			while(rs.next()){
				if(rs.getLong("eid")>0)
					return false;
				else
				{

					etim=rs.getTimestamp("createTime");
					long temp = tim.getTime() - etim.getTime(); 
					 long hours = temp / 1000 / 3600;  
					long temp2 = temp /1000 /60;
					if(temp2<30)
						return true;
					else
						return false;
				}
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			return false;
		} finally {
			db.close();
		}
			return false;
	}
	
	//删除代言图片
	public boolean delEmdorseImg(Long eid){
		String sql="delete from endorsementduct_img where eid="+eid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {	
				db.getPrepareStateDao(sql);
				db.updateSql(sql);
		} catch (Exception e) {
			return false;
		} finally {
			db.close();
		}
		return true;
	}
	
	//添加统计
	public CountH5 saveCount(CountH5 cnt){
		return countH5interface.save(cnt);
	}
	
	//根据产品编号获取代言列表及评论列表ispage 0不分页，１分页
		public List<Endorsement> getendorselistbyPid(Long uid,Long pid,Integer pagesize,Integer page,Integer ispage){
			String sql="SELECT a.*,0 as pingid,9 as editor FROM (SELECT e.userId,e.eid,preImgPath,remark,u.`nickname`,u.`headIcon`,u.`sex` FROM endorsementduct e,`user` u WHERE e.userId=u.`uid` AND e.gid='1' AND e.`productId`="+pid+" AND e.status=1 ORDER BY e.`sort` DESC,e.`createTime` DESC) a"
					+" UNION (SELECT uid as userId,0 AS eid,'' AS preImgPath,content AS remark,nickname,headIcon, 1 AS sex,id as pingid,editor FROM `comment`  WHERE (pid="+pid+" AND status=2) or (pid="+pid+" and uid="+uid+" and status=1) order by date_add DESC)"
					+" ORDER BY editor DESC,pingid desc";
			if(ispage==0)
				sql=sql+" limit "+pagesize;
			else
				sql=sql+" limit "+(page*pagesize)+","+pagesize;
			
			JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
			List<Endorsement> endorselist=new ArrayList<Endorsement>();
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				
				while(rs.next()){
					Endorsement en=new Endorsement();
					en.setEid(rs.getLong("eid"));
					en.setPreImgPath(rs.getString("preImgPath"));
					en.setRemark(rs.getString("remark"));
					en.setUserId(rs.getLong("userId"));
					User usr=new User();
					usr.setUid(en.getUserId());
					usr.setNickname(rs.getString("nickname"));
					usr.setHeadIcon(rs.getString("headIcon"));
					usr.setSex(rs.getString("sex"));
					if(usr.getHeadIcon()==null || StringUtils.isBlank(usr.getHeadIcon())){
						if(usr.getSex()==null || StringUtils.isBlank(usr.getSex()) || usr.getSex().equals("0"))
							usr.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
						else
							usr.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
					}
					usr.setPingid(rs.getLong("pingid"));					
					usr.setEditor(rs.getInt("editor"));
					en.setUser(usr);
					endorselist.add(en);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			endorselist=endorselist==null||endorselist.isEmpty()?null:endorselist;
			return endorselist;
		}
		
	//获取代言头像
	public List<EndorsementImg> getEnImglist(Long eid){
		return endorsementimginterface.querybyEid(eid);
	}
	
	//根据产品编号取有效代言及说评论数量
	public Integer getEPcount(Long pid,Long uid){
		String sql="SELECT SUM(cnt) as cnt FROM (SELECT '1' AS e,COUNT(1) AS cnt FROM endorsementduct WHERE productId="+pid+" AND STATUS=1 UNION"
				+ " SELECT '2' AS e,COUNT(1) AS cnt FROM `comment` WHERE (pid="+pid+" AND status=2) or (pid="+pid+" and uid="+uid+" and status=1)) a";
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		Integer count=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				count=rs.getInt("cnt");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return count;
	}	
	//根据产品编号取有效代言及说评论数量
	public Integer getPcount(Long pid,Long uid){
		String sql="SELECT COUNT(1) AS cnt FROM `comment` WHERE (pid="+pid+" AND status=2) or (pid="+pid+" and uid="+uid+" and status=1)";
		logger.info(sql);
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		Integer count=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				count=rs.getInt("cnt");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return count;
	}
	
	//根据产品编号获取代表列表
	@Transactional(readOnly = true)
	public Page<Endorsement> getEndorsmentsByPid(final Long pid, final Integer stats,Integer page){		
		Sort sort = new Sort(Sort.Direction.DESC,"sort").and(new Sort(Sort.Direction.DESC, "createTime"));
		
		return endorsmentinterface.findAll(new Specification<Endorsement>(){
			@Override
			public Predicate toPredicate(Root<Endorsement> endors,CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				
	            List<Predicate> predicates = new ArrayList<>();
	            Path<Integer> status=endors.get("status");
	            Path<Long> pid=endors.get("productId");
	            Predicate pobj=builder.and(builder.equal(pid, pid),builder.equal(status, stats));				
	            Predicate[] param = new Predicate[predicates.size()];
	            predicates.toArray(param);            
	            return query.where(param).getRestriction();
			}
		}, new PageRequest(page,10,sort));
	}
}
