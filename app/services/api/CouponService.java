package services.api;

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

import models.Coupon;
import models.Coupon_user;
import models.User;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import play.Logger;
import repositories.CouponRepository;
import repositories.CouponUserRepository;
import repositories.ProductRepository;
import repositories.UserRepository;
import utils.Constants;
import utils.JdbcOper;
import utils.Numbers;
import vo.coupon.CouponQueryVO;
import vo.coupon.PayEndResultVO;
import vo.coupon.PayEndResultVO.CouponVO;

/**
 * 优惠券相关Service
 * @author luobotao
 * @Date 2015年5月12日
 */
@Named
@Singleton
public class CouponService {

    private static final Logger.ALogger logger = Logger.of(CouponService.class);
    private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private UserRepository userRepository;
    @Inject
    private CouponRepository couponRepository;
    @Inject
    private CouponUserRepository couponUserRepository;
    @Inject
    private ProductRepository productRepository;

    
	public List<Coupon> getCouponListByUseId(String couponUid,String state,String uid){
		return couponRepository.getCouponListByUseId(couponUid,state,uid);
	}
	public List<Coupon> getByUserId(Integer couponUid){
		return couponRepository.getByUserId(couponUid);
	}

	public PayEndResultVO getPayendInfo(String uid,String orderCode){
		PayEndResultVO result= new PayEndResultVO();
		String sql = "call sp_order_payend("+uid+",'"+orderCode+"')";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setStatus(rs.getNString("STATUS"));
				result.setDisplayflg(rs.getInt("displayflg"));
				result.setToast(rs.getNString("toast"));
				result.setCouponId(rs.getInt("couponId"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	public void bindcoupon(String uid, String marketCode) {
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		// TODO Auto-generated method stub
		String sql = "update user set marketCode='" + marketCode
				+ "' where uid='" + uid + "'";
		String sql2 = "INSERT INTO `coupon_user`(couponId,coupon_code,uid,states)	SELECT c.id,u.marketCode AS coupon_code,u.uid,'0' AS states FROM coupon c,`user` u WHERE c.market = u.marketCode AND u.marketCode<>'' AND c.states='0' AND u.uid='"
				+ uid
				+ "' AND c.id NOT IN (SELECT couponId FROM `coupon_user` WHERE uid='"
				+ uid + "')";
		try {
			db.getPrepareStateDao(sql);
			db.pst.execute();// 执行语句，得到结果集
			db.getPrepareStateDao(sql2);
			db.pst.execute();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	public List<Coupon> getCouponList(String couponId){
		return couponRepository.getCouponListById(couponId);
	}
	
	public Coupon getCouponById(Long couponId){
		return couponRepository.findOne(couponId);
	}
	public Page<Coupon> getCouponPage(CouponQueryVO couponQuery, Integer page) {
		if(StringUtils.isBlank(couponQuery.uid)){
			if(Numbers.parseDouble(couponQuery.price, 0D)>0){
				
			}
			List<Coupon_user> couponUserList = couponUserRepository.findByUid(Numbers.parseLong(couponQuery.uid, 0L));
			for(Coupon_user coupon_user:couponUserList){
				couponQuery.ids.add(coupon_user.getCouponId());
			}
		}
		return couponRepository.findAll(new CouponQuery(couponQuery),new PageRequest(page, Constants.PAGESIZE));
	}
	

    /**
     * 商品查询内部类
     * @author luobotao
     * @Date 2015年5月11日
     */
    private static class CouponQuery implements Specification<Coupon> {

        private final CouponQueryVO couponQueryVO;

        public CouponQuery(final CouponQueryVO couponQueryVO) {
            this.couponQueryVO = couponQueryVO;
        }

        @Override
        public Predicate toPredicate(Root<Coupon> coupon, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<Long> id = coupon.get("id");
            
            
            List<Predicate> predicates = new ArrayList<>();
            if (!StringUtils.isBlank(couponQueryVO.uid) && couponQueryVO.ids.size()>0) {
            		predicates.add(id.in(couponQueryVO.ids));
            }
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            
            return query.where(param).getRestriction();
        }
    }


	public List<Coupon> getCouponListByUidAndPrice(String uid,
			String price, String pidStr,String page, Integer pagesize) {
		List<Coupon> result = new ArrayList<Coupon>();
		Double priceDouble = Numbers.parseDouble(price, 0D);
		String sql="CALL `sp_coupon_list`("+uid+","+priceDouble+",'"+pidStr+"'," + page + ","+pagesize+")";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Coupon coupon = new Coupon();
				coupon.setBtim(rs.getString("btim"));
				coupon.setCounts(rs.getInt("counts"));
				coupon.setCouponpic(rs.getString("couponpic"));
				coupon.setCouponprice(rs.getDouble("couponprice"));
				coupon.setDate_add(rs.getDate("date_add"));
				coupon.setDurable(rs.getString("durable"));
				coupon.setEtim(rs.getString("etim"));
				coupon.setId(rs.getLong("couponId"));
				coupon.setMarket(rs.getString("market"));
				coupon.setPid(rs.getLong("pid"));
				coupon.setRemark(rs.getString("remark"));
				coupon.setStates(rs.getString("states"));
				coupon.setTitle(rs.getString("title"));
				coupon.setTprice(rs.getDouble("tprice"));
				coupon.setTyp(rs.getInt("typ"));
				coupon.setTag(rs.getString("tag"));
				result.add(coupon);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	
	public List<CouponVO> conponListCovertTocouponVOList(List<Coupon> couponList){
		List<CouponVO> result = new ArrayList<PayEndResultVO.CouponVO>();
		for(Coupon coupon : couponList){
			CouponVO couponVO =new CouponVO();
			couponVO.id=coupon.getId().toString();
			couponVO.title=coupon.getTitle();
			couponVO.description=coupon.getRemark();
			couponVO.couponpic=coupon.getCouponpic();
			if (StringUtils.isBlank(couponVO.couponpic))
			{
				couponVO.flg="1";
			}else{
				couponVO.flg="2";
			}
			couponVO.coupontxt=coupon.getCouponprice().intValue()+"";
			if ("0".equals(coupon.getStates()))
			{
				if ("1900-01-01".equals(coupon.getBtim()))
				{
					couponVO.validdate="即日起至"+coupon.getEtim();	
				}else{
					couponVO.validdate=coupon.getBtim()+"至"+coupon.getEtim();
				}
			}else{
				couponVO.validdate="已失效";
			}
			if ("1".equals(coupon.getTag()))
			{
				coupon.setStates(coupon.getTag());
			}else{
				if ("3".equals(coupon.getTag()))
				{
					coupon.setStates("2");
				}else{
					coupon.setStates(coupon.getTag());
				}	
			}
			switch(coupon.getTyp())
			{
				case 1:
					couponVO.type="代金券";
					break;
				case 2:
					couponVO.type="满减券";
					break;
				case 3:
					couponVO.type="商品券";
					break;
				case 4:
					couponVO.type="满送商品券";
					break;
				case 5:
					couponVO.type="指定代金券";
					break;
				case 6:
					couponVO.type="指定满减券";
					break;
			}
			couponVO.status=coupon.getStates();
			result.add(couponVO);
		}
		return result;
	}
	
	public int getCouponPrice(String p_couponUserId, String p_pidstr) {
		int result =-1;
		String sql="{CALL `sp_coupon_getprice`('"+p_couponUserId+"','"+p_pidstr+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt("price");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	} 
	
	public int checkCode(String uid, String couponcode) {
		int result =-1;
		String sql="{CALL `sp_coupon_checkCode`('"+uid+"','"+couponcode+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt("states");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	} 
	
	public int getUserCouponCnt(String uid, Integer total_fee,String pidstr) {
		int result = 0;
		String sql="call `sp_coupon_listCnt`('"+uid+"','"+total_fee+"','"+pidstr+"')";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			logger.info(sql);
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt("cnt");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	} 
	
	//根据用户编号及来源获取优惠券总金额
	public Integer gettotalCoupunmoney(Long uid){
		String sql="SELECT SUM(c.couponprice) AS price FROM coupon c,coupon_user uc WHERE c.id=couponId AND uc.uid="+uid+" AND uc.source in(1,2)";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer cprice=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				cprice = rs.getInt("price");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return cprice;
	}
	//根据用户编号获取邀请人优惠券列表
	public List<Coupon> getregcouplist(Long uid,Integer page){
		// String sql="SELECT c.id,c.typ,c.title,c.remark,c.tprice,c.couponprice,c.btim,c.etim,c.counts,uc.states,uc.uid,u.nickname FROM coupon c,coupon_user uc,`user` u WHERE c.id=couponId AND uc.uid=u.uid AND uc.source=1 and uc.fromuserid="+uid+" order by uc.date_add desc limit "+page*10+",10";
		String sql="SELECT 0 AS id,1 AS typ,'邀请' AS title,''  AS remark,0 AS tprice,5 AS couponprice,'' AS btim,'' AS etim,1 AS counts,'0' AS states,0 AS uid,uc.phone AS nickname,uc.date_add AS tim FROM coupon_phone uc"
					+" WHERE uc.fromUid="+uid+" AND uc.phone NOT IN(SELECT phone FROM `user`)"
					+" UNION"
					+" SELECT c.id,c.typ,c.title,c.remark,c.tprice,c.couponprice,c.btim,c.etim,c.counts,'1' AS states,uc.uid,u.nickname,uc.date_add AS tim FROM coupon c,coupon_user uc,`user` u" 
					+" WHERE c.id=couponId AND uc.fromuserId=u.uid AND uc.source=1 AND uc.uid="+uid
					+" UNION"
					+" SELECT c.id,c.typ,c.title,c.remark,c.tprice,c.couponprice,c.btim,c.etim,c.counts,'2' AS states,uc.uid,u.nickname,uc.date_add AS tim FROM coupon c,coupon_user uc,`user` u" 
					+" WHERE c.id=couponId AND uc.fromuserId=u.uid AND uc.source=2 AND uc.uid="+uid
					+" ORDER BY tim DESC limit "+page*10+",10";
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<Coupon> clist=new ArrayList<Coupon>();
		
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Coupon c=new Coupon();
				c.setId(rs.getLong("id"));
				c.setTyp(rs.getInt("typ"));
				c.setTitle(rs.getString("title"));
				c.setRemark(rs.getString("remark"));
				c.setTprice(rs.getDouble("tprice"));
				c.setCouponprice(rs.getDouble("couponprice"));
				c.setBtim(rs.getString("btim"));
				c.setEtim(rs.getString("etim"));
				c.setCounts(rs.getInt("counts"));
				c.setStates(rs.getString("states"));
				User usr=new User();
				usr.setUid(rs.getLong("uid"));
				usr.setNickname(rs.getString("nickname"));
				c.setUser(usr);
				
				clist.add(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return clist;
	}
	
	//根据用户编号获取邀请人优惠券列表总数
		public Integer getregcoupCount(Long uid){
			String sql="SELECT count(uc.uid) as count FROM coupon_user uc WHERE uc.fromuserid="+uid;
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			List<Coupon> clist=new ArrayList<Coupon>();
			Integer count=0;
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				if(rs.next())
					count=rs.getInt("count");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			return count;
		}
		
	//绑定用户优惠券
	public Coupon_user savecouponUser(Coupon_user cu){
		return couponUserRepository.save(cu);
	}
	
	//根据优惠券编号及用户编号获取领取的优惠券
	public List<Coupon> getUserCoupon(String phone,Long couponid){
		String sql="SELECT c.* FROM coupon c,coupon_phone cu WHERE c.`id`=cu.`couponId` AND cu.`couponId`="+couponid+" AND cu.`phone`='"+phone+"'  GROUP BY c.`id` ORDER BY cu.`id` DESC";
	
	JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
	List<Coupon> clist=new ArrayList<Coupon>();
	
	try {
		db.getPrepareStateDao(sql);
		ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
		while(rs.next()){
			Coupon c=new Coupon();
			c.setId(rs.getLong("id"));
			c.setTyp(rs.getInt("typ"));
			c.setTitle(rs.getString("title"));
			c.setRemark(rs.getString("remark"));
			c.setTprice(rs.getDouble("tprice"));
			c.setCouponprice(rs.getDouble("couponprice"));
			c.setBtim(rs.getString("btim"));
			c.setEtim(rs.getString("etim"));
			c.setCounts(rs.getInt("counts"));
			c.setStates(rs.getString("states"));			
			clist.add(c);
		}
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		db.close();
	}
	return clist;
	}

	//根据优惠券编号获取被领取的优惠券数量
		public Integer getPhoneCouponCount(Long couponid){
			String sql="SELECT count(id) as cc from coupon_phone WHERE couponId="+couponid;
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer count=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				count=rs.getInt("cc");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return count;
		}
	public boolean insertCouponPone(Long couponid,String phone,String market){
		String sql="SELECT count(1) as count FROM coupon_phone where phone='"+phone+"' and couponId="+couponid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<Coupon> clist=new ArrayList<Coupon>();
		Integer count=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next())
				count=rs.getInt("count");
			if(count<1){
				String sqlin="insert into coupon_phone(date_add,couponId,phone,marketCode) values('"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"',"+couponid+",'"+phone+"','"+market+"')";
				db.getPrepareStateDao(sqlin);
				db.pst.execute();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return false;
	}
	
	/*
	 *添加优惠券
	 */
	public void addUserCoupon(Long userid, Long couponid) {
		String sql="select count(1) as cc from coupon_user where uid="+userid.longValue()+" and couponId="+couponid.longValue();
		String sqladd="INSERT INTO coupon_user(`date_add`,couponId,coupon_code,uid,states,source,fromuserId) SELECT NOW(),id,market,"+userid+",0,0,0 FROM coupon WHERE id="+couponid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		int count=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next())
				count=rs.getInt("cc");
			if(count<1){				
				db.getPrepareStateDao(sqladd);
				db.pst.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
}
