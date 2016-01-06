package services.api;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import assets.CdnAssets;
import models.APPConfig;
import models.AdLoading;
import models.EndorsementSort;
import models.InviteShareChannel;
import models.LuckDraw;
import models.Product;
import models.Share;
import models.User;
import models.UserDevice;
import models.UserLike;
import models.UserLog;
import models.UserRedFlag;
import models.UserVerify;
import models.Version;
import models.WxUser;
import models.admin.AdminCode;
import play.Configuration;
import play.Logger;
import repositories.AdLoadingRepository;
import repositories.AdminCodeRepository;
import repositories.LuckDrawInterface;
import repositories.ProductRepository;
import repositories.UserLikeRepository;
import repositories.UserLogRepository;
import repositories.UserRedFlagInterface;
import repositories.UserRepository;
import repositories.UserVerifyRepository;
import services.ICacheService;
import services.ServiceFactory;
import utils.BeanUtils;
import utils.JdbcOper;
import utils.Numbers;
import utils.WSUtils;
import vo.DevLoginVO;
import vo.StatusBindVO;
import vo.UserHuanXinVO;
import vo.UserRegisterVO;
import vo.appPad.appPadVO;
import vo.appSalesMan.AppSalesManDevLoginVO;
import vo.appSalesMan.AppSalesManUserCodeVO;
import vo.appSalesMan.AppSalesManUserVO;
import vo.loading.AppVersionVO.AppversionItem;
import vo.user.AlipayLoginUserVO;

/**
 * 用户相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class UserService {

    private static final Logger.ALogger logger = Logger.of(UserService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private UserRepository userRepository;
    @Inject
    private AdminCodeRepository adminCodeRepository;
    @Inject
    private ProductRepository productRepository;
    @Inject
    private AdLoadingRepository adLoadingRepository;
    @Inject
    private UserLikeRepository userLikeRepository;
    @Inject
    private UserVerifyRepository userVerifyRepository;
    @Inject
    private UserLogRepository userLogRepository;
    @Inject
    private UserRedFlagInterface userRgedFlagInterface;
	@Inject
	private LuckDrawInterface luckDrawInterface;
	@Inject
	private static ICacheService cache = ServiceFactory.getCacheService();
	/**
	 * 获取用户
	 * @return
	 */
	public User getUserByUid(Long id) {
		return userRepository.findOne(id);
	}
	
	public User saveUser(User uInfo){
		return userRepository.save(uInfo);
	}
	public List<AdLoading> getAdLoadingList() {
		return adLoadingRepository.findAll();
	}
	
	
	public List<APPConfig> getAppConfigList(){
		return BeanUtils.castEntity(userRepository.getAppConfigList(),APPConfig.class);
	}
	
	public List<Share> getShareList(){
		return BeanUtils.castEntity(userRepository.getShareList(), Share.class);
	}
	public List<Version> getVersion(String ostype){
		return BeanUtils.castEntity(userRepository.getVersion(ostype), Version.class);
	}
	
	public AppSalesManUserCodeVO checkregistcode(String registCode){
		AppSalesManUserCodeVO result = new AppSalesManUserCodeVO();
		String sql = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.admin_code='"+registCode+"' and c.sta='1'";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setStatus("1");
				result.setMsg("");
				result.setStoreID(rs.getString("id"));
				result.setStoreName(rs.getString("realname"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	public void setRegistCodeStat(String registCode,String status){
		String sql = "update admin_code set sta='"+status+"' where admin_code='"+registCode+"'";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			//ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			db.pst.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return;
	}
	
	public String getUserId(String devid){
		String userId = "";
		String sql = "SELECT uid FROM `user_device` WHERE device_id='"+devid+"'";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				userId =String.valueOf(rs.getInt("uid"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return userId;
	}
	
	
	
	public String getUserBalance(String uid){
		String balance = "";
		String sql = "SELECT balance FROM user_balance WHERE userId='"+uid+"'";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				balance =String.valueOf(rs.getDouble("balance"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return balance;
	}
	
	public DevLoginVO getUidAndPhone(String p_device_id,String p_osversion,String p_model,String p_pushToken,String p_resolution,String p_appversion,String p_marketCode,DevLoginVO devLoginVO){
		String sql = "{call sp_user_login ('"+p_device_id+"','"+p_osversion+"','"+p_model+"','"+p_pushToken+"','"+p_resolution+"','"+p_appversion+"','"+p_marketCode+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		List<String> authList = new ArrayList<String>();
		authList.add("0");
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				devLoginVO.setUid(rs.getString("uid"));
				devLoginVO.setPhone(rs.getString("phone"));
				devLoginVO.setOpencardId(rs.getString("opencardId"));
				devLoginVO.setNickname(rs.getString("nickname"));
				devLoginVO.setHeadIcon(rs.getString("headicon"));
				devLoginVO.setGender(rs.getString("sex"));
				if(StringUtils.isBlank(devLoginVO.getHeadIcon())){
					if(StringUtils.isBlank(devLoginVO.getGender()) || devLoginVO.getGender().equals("0"))
						devLoginVO.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
					else
						devLoginVO.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
				}
				if("1".equals(rs.getString("isEndorsement"))){
					authList.add("1");
				}
				if("4".equals(rs.getString("gid"))){
					authList.add("2");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		devLoginVO.setAuthorities(authList);
		return devLoginVO;
	}
	
	
	public vo.appSalesMan.AppSalesManUserVO.UserInfo getSalesManUidAndPhone(String p_device_id,String p_osversion,String p_model,String p_pushToken,String p_resolution,String p_appversion,String p_marketCode){
		vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
		String sql = "{call sp_user_login ('"+p_device_id+"','"+p_osversion+"','"+p_model+"','"+p_pushToken+"','"+p_resolution+"','"+p_appversion+"','"+p_marketCode+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		List<String> authList = new ArrayList<String>();
		authList.add("0");
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				userInfo.setGender(rs.getString("sex"));
				userInfo.setHeadIcon("http://higou-api.oss-cn-beijing.aliyuncs.com/pimgs/site/shopheaderIcon.png");
				userInfo.setPhone(rs.getString("phone"));
				userInfo.setNickname(rs.getString("nickname"));
				userInfo.setUid(String.valueOf(rs.getInt("uid")));
				userInfo.setHeadIcon("http://higou-api.oss-cn-beijing.aliyuncs.com/pimgs/site/shopheaderIcon.png");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(!StringUtils.isBlank(userInfo.getUid())){
			String sql2 = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.uid='"+userInfo.getUid()+"'";
			logger.info(sql2);
			JdbcOper db2 = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db2.getPrepareStateDao(sql2);
				ResultSet rs2 = db2.pst.executeQuery();// 执行语句，得到结果集
				while(rs2.next()){
					userInfo.setStoreID(rs2.getString("id"));
					userInfo.setStoreName(rs2.getString("realname"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db2.close();
			}
		}
		return userInfo;
	}

	public void insertFeedback(String contact, String content) {
		String sql = "insert into feedback (contact, content, date_add) values ('"+contact+"','"+content+"',now())";// SQL语句// //调用存储过程
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.execute();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		
	}


	/**
	 * 用户注册
	 * @param uid
	 * @param nickname
	 * @param marketCode
	 * @param appversion
	 * @param pwds
	 * @param usid
	 * @param headIcon
	 * @param cologin
	 * @param unionid
	 * @param platform
	 */
	public UserRegisterVO register(String uid, String devid,String nickname, String marketCode,
			String appversion,String phone, String pwds,String token,String usid, String headIcon,
			String cologin, String unionid, String platform) {
		UserRegisterVO result = new UserRegisterVO();
		List<String> authList = new ArrayList<String>();
		authList.add("0");
		String sql = "{call sp_user_register ('"+uid+"','"+devid+"','"+nickname+"','"+marketCode+"','"+appversion+"','"+phone+"','"+pwds+"','"+token+"','"+usid+"','"+headIcon+"','"+cologin+"','"+unionid+"','"+platform+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setHeadIcon(rs.getString("headIcon"));
				result.setNickname(rs.getString("nickname"));
				result.setPhone(rs.getString("phone"));
				result.setUid(rs.getInt("uid"));
				result.setGender(rs.getString("sex"));
				if(StringUtils.isBlank(result.getHeadIcon())){
					if(StringUtils.isBlank(result.getGender()) || result.getGender().equals("0"))
						result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
					else
						result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
				}
				if ("1".equals(rs.getString("isEndorsement"))){
					authList.add("1");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.setAuthorities(authList);
		return result;
	}

	/**
	 * 用户注册
	 * @param uid
	 * @param nickname
	 * @param marketCode
	 * @param appversion
	 * @param pwds
	 * @param usid
	 * @param headIcon
	 * @param cologin
	 * @param unionid
	 * @param platform
	 */
	public AppSalesManUserVO salesManregister(String uid, String devid,String nickname, String phone, String pwds,
			String storeId,String registCode) {
		AppSalesManUserVO result = new AppSalesManUserVO();
		String sql = "{call sp_user_register_salesMan ('"+uid+"','"+devid+"','"+nickname+"','"+phone+"','"+pwds+"','"+storeId+"','"+registCode+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setStatus("1");
				result.setMsg("");
				vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
				userInfo.setGender(rs.getString("sex"));
				userInfo.setHeadIcon("http://higou-api.oss-cn-beijing.aliyuncs.com/pimgs/site/shopheaderIcon.png");
				userInfo.setPhone(phone);
				userInfo.setNickname(rs.getString("nickname"));
				userInfo.setUid(String.valueOf(rs.getInt("uid")));
				result.setUserInfo(userInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(result!=null && result.getUserInfo()!=null){
			String sql2 = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.uid='"+result.getUserInfo().getUid()+"'";
			logger.info(sql2);
			JdbcOper db2 = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db2.getPrepareStateDao(sql2);
				ResultSet rs2 = db2.pst.executeQuery();// 执行语句，得到结果集
				while(rs2.next()){
					result.getUserInfo().setStoreID(rs2.getString("id"));
					result.getUserInfo().setStoreName(rs2.getString("realname"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db2.close();
			}
		}
		return result;
	}
	
	public appPadVO padlogin(String phone, String pwds,
			String devid, String marketCode) {
		appPadVO result = new appPadVO();
		String sql = "{call sp_user_checklogin('"+phone+"','"+pwds+"','"+devid+"','"+marketCode+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setStatus("1");
				result.setMsg("登录成功");
				result.setUid(rs.getInt("uid")+"");
				result.setUshopname(rs.getString("nickname"));
				result.setUshopicon("http://higou-api.oss-cn-beijing.aliyuncs.com/pimgs/site/shopheaderIcon.png");
				result.setPhone(phone);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result=null;
		} finally {
			db.close();
		}
		return result;
	}
	
	public AppSalesManUserVO salesManlogin(String phone, String pwds,
			String devid, String marketCode) {
		AppSalesManUserVO result = new AppSalesManUserVO();
		String sql = "{call sp_user_checklogin('"+phone+"','"+pwds+"','"+devid+"','"+marketCode+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setStatus("1");
				result.setMsg("登录成功");
				vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
				userInfo.setGender(rs.getString("sex"));
				userInfo.setHeadIcon("http://higou-api.oss-cn-beijing.aliyuncs.com/pimgs/site/shopheaderIcon.png");
				userInfo.setPhone(phone);
				userInfo.setNickname(rs.getString("nickname"));
				userInfo.setUid(String.valueOf(rs.getInt("uid")));
				result.setUserInfo(userInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result=null;
		} finally {
			db.close();
		}
		
		if(result!=null && result.getUserInfo()!=null){
			String sql2 = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.uid='"+result.getUserInfo().getUid()+"'";
			logger.info(sql2);
			JdbcOper db2 = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db2.getPrepareStateDao(sql2);
				ResultSet rs2 = db2.pst.executeQuery();// 执行语句，得到结果集
				while(rs2.next()){
					result.getUserInfo().setStoreID(rs2.getString("id"));
					result.getUserInfo().setStoreName(rs2.getString("realname"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db2.close();
			}
		}
		return result;
	}
	
	public UserRegisterVO loginWithPhoneAndPwd(String phone, String pwds,
			String devid, String marketCode) {
		UserRegisterVO result = new UserRegisterVO();
		List<String> authList = new ArrayList<String>();
		authList.add("0");
		String sql = "{call sp_user_checklogin('"+phone+"','"+pwds+"','"+devid+"','"+marketCode+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setHeadIcon(rs.getString("headIcon"));
				result.setNickname(rs.getString("nickname"));
				result.setPhone(rs.getString("phone"));
				result.setUid(rs.getInt("uid"));
				result.setGender(rs.getString("sex"));
				if(StringUtils.isBlank(result.getHeadIcon())){
					if(StringUtils.isBlank(rs.getString("sex")) || rs.getString("sex").equals("0"))
						result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
					else
						result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
				}
				if ("1".equals(rs.getString("isEndorsement"))){
					authList.add("1");
				}
				if("4".equals(rs.getString("gid"))){
					authList.add("2");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.setAuthorities(authList);
		return result;
	}

	public int getDifDay(String uid) {
		return userRepository.getDifDay(uid);
	}

	public int getViewCount(String uid) {
		return userRepository.getViewCount(uid);
	}

	public List<Object[]> getDeviceUser(String uid) {
		return userRepository.getDeviceUser(uid);
	}
	
	public UserDevice getDeviceUserInfo(String uid){
		UserDevice result = new UserDevice();
		String sql = "SELECT d.* FROM `user` u,`user_device` d WHERE d.uid=u.uid AND d.uid="+uid+" ORDER BY d.date_add DESC LIMIT 1";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setDevice_id(rs.getString("device_id"));
				result.setInituid(rs.getLong("inituid"));
				result.setUid(rs.getLong("uid"));
				result.setOstype(rs.getLong("ostype"));
				result.setOsversion(rs.getString("osversion"));
				result.setModel(rs.getString("model"));
				result.setPushToken(rs.getString("pushToken"));
				result.setSolution(rs.getString("solution"));
				result.setAppversion(rs.getString("appversion"));
				result.setMarketCode(rs.getString("marketCode"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public int getNoticeOrders(Long uid) {
		return userRepository.getNoticeOrders(uid);
	}
	@Transactional
	public void updateNoticeFlag(Long uid) {
		userRepository.updateNoticeFlag(uid);
	}

	public UserLike findByUidAndPid(Long uid, Long pid) {
		return userLikeRepository.findByUidAndPid(uid,pid);
	}

	@Transactional
	public UserLike addUserLike(UserLike userLike,Product product) {
		int nlikes = product.getNlikes()+1;
		product.setNlikes(nlikes);
		productRepository.save(product);
		return userLikeRepository.save(userLike);
	}
	@Transactional
	public void deleteUserLike(UserLike userLike,Product product) {
		int nlikes = product.getNlikes()-1;
		product.setNlikes(nlikes);
		productRepository.save(product);
		userLikeRepository.delete(userLike);
	}

	@Transactional
	public int bindLogout(String uid, String devid) {
		Integer initUid = userRepository.findInitUidByDevid(devid);
		if(initUid!=null && initUid>0){
			userRepository.updateUserDevice(initUid,devid);
			userRepository.updateUserActive(0,Numbers.parseInt(uid, 0));
			userRepository.updateUserActive(1,initUid);
			return initUid;
		}
		return 0;
	}

	public UserVerify getIsVerify(Long uid, String phone, String verify) {
		return userVerifyRepository.findByUidAndPhoneAndVerifyAndFlg(uid, phone, verify,"1");
	}
	
	@Transactional
	public void userbindphone(Long uid, String phone) {
		userRepository.userbindphone(uid, phone);
	}

	public boolean checkBind(String unionid, String usid) {
		int count = userRepository.checkBind(unionid,usid);
		if(count>0){
			return true;
		}
		return false;
	}

	public UserVerify checkVerify(String phone, String verify) {
		return userVerifyRepository.findByPhoneAndVerifyAndFlg( phone, verify,"1");
	}

	public User findByPhone(String phone){
		return userRepository.findByPhone(phone);
	}
	public boolean getPhoneIsBind(String phone) {
		User user = userRepository.findByPhone(phone);
		if(user==null){
			return false;
		}else{
			return true;
		}
	}
	
	public boolean getPhoneGid5IsBind(String phone){
		User user = userRepository.getGid5IsBind(phone);
		if(user==null){
			return false;
		}else{
			return true;
		}
	}
	
	public boolean getIsBindPhone(String uid) {
		User user = userRepository.getIsBindPhone(uid);
		if(user==null){
			return false;
		}else{
			return true;
		}
	}

	public boolean getPwdsIsBind(String phone) {
		User user = userRepository.getPwdsIsBind(phone);
		if(user==null){
			return false;
		}else{
			return true;
		}
	}

	public List<Integer> getLikes(Long uid) {
		return userRepository.getLikes(uid);
		
	}

	public List<UserLike> getUserLikePage(Long uid, Integer page,
			Integer pagesize) {
		List<UserLike> result = new ArrayList<UserLike>();
		String sql = "SELECT b.* FROM product a, userlike b WHERE a.pid=b.pid and a.status=10 ";
		if (uid!=0) {
			sql += " and b.uid='"+uid+"' ";
		}
		sql +=  " order by b.date_add desc limit "+(page * pagesize)+","+pagesize;
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				UserLike userlike = new UserLike();
				userlike.setId(rs.getLong("id"));
				userlike.setUid(uid);
				userlike.setDate_add(rs.getDate("date_add"));
				userlike.setPid(rs.getLong("pid"));
				result.add(userlike);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public String getPromisePic(Long uid, Long pid, String devid,
			String deviceType) {
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		String sql = "{call sp_user_getPromisePic('"+uid+"','"+pid+"','"+devid+"','"+deviceType+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		String result="";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result =domain+"/"+ rs.getString("promisePic");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}


	public String saveVerifyInVerfify(String ip, String uid, String phone,String code,String devid) {
		String result = "0";
		String sql = "{call `sp_check_sendsms`('" + uid + "','" + phone
				+ "' ,'" + code + "','" + ip + "','"+devid+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while (rs.next()) {
				result = rs.getString("errno");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public StatusBindVO getUserByBindOption(String unionid, String usid,
			String devid) {
		StatusBindVO result = new StatusBindVO();
		List<String> authList = new ArrayList<String>();
		authList.add("0");
		
		String sql = "{call sp_user_checkbind('"+unionid+"','"+usid+"','"+devid+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.setStatus("1");
				result.setUid(rs.getString("uid"));
				result.setPhone(rs.getString("phone"));
				result.setIspwds(rs.getString("ispwds"));
				result.setNickname(rs.getString("nickname"));
				result.setGender(rs.getString("sex"));
				result.setHeadIcon(rs.getString("headicon"));
				if(StringUtils.isBlank(result.getHeadIcon())){
					if(StringUtils.isBlank(result.getGender()) || result.getGender().equals("0"))
						result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
					else
						result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
				}
				if ("1".equals(rs.getString("isEndorsement"))){
					authList.add("1");
				}
				if("4".equals(rs.getString("gid"))){
					authList.add("2");
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.setAuthorities(authList);
		return result;
	}

	public int getGuessNum(String catstr) {
		int result = 0;
		String sql = "select count(*) from guess_productlist where date_txt>NOW()";
		if (!StringUtils.isBlank(catstr))
		{
			sql =sql+ " and categoryId in ("+catstr+")";
		}
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public Map<Long,String> getGuessProlist(String uid, String page,
			Integer pagesize, String catstr) {
		Map<Long,String> result = new HashMap<Long, String>();
		String sql = "SELECT pid,date_txt FROM guess_productlist g WHERE g.date_txt>NOW()";
		if (!StringUtils.isBlank(catstr))
		{
			sql += " and g.categoryId in ("+catstr+")";
		}
		sql += " order by g.pid desc limit "+(Numbers.parseInt(page, 0) * pagesize)+","+pagesize;				
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String date_txt = CHINESE_DATE_TIME_FORMAT.format(rs.getTimestamp("date_txt"));
				result.put(rs.getLong("pid"), date_txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public static String getResolution(String uid,String devid) {
		String sql = "{call sp_user_getResolution('"+uid+"','"+devid+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		String result="";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result =rs.getString("solution");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	
	

	public AppversionItem getLatest(int ostype, String version) {
		String sql = "select * from appversion where ostype='"+ostype+"' and status=6 order by date_upd desc limit 0,1";// SQL语句// //调用存储过程
		logger.info(sql);
		AppversionItem result=new AppversionItem();
		result.has_new = 0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.latest_version =rs.getString("latestver");
				result.client_version =version;
				int latestverInt = Numbers.parseInt(result.latest_version.replaceAll("\\.", ""),0);
				int versionInt = Numbers.parseInt(version.replaceAll("\\.", ""), 0);
				if(versionInt>=latestverInt){
					result.has_new = 0;
				}else{
					result.has_new = 1;
				}
				if(result.has_new==1){
					result.isforced = rs.getInt("isforced");
					result.remind = rs.getInt("remind_time");
					result.message =rs.getString("message");
					if (ostype==1)
						result.url =rs.getString("url");
					else{
						boolean IsProduct = Configuration.root().getBoolean("production", false);
						String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
						if(IsProduct){
							domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
						}
						String APKFILE_DIR = Configuration.root().getString("APKFILE_DIR","pimgs/apk/");
						result.url  = domain+"/"+APKFILE_DIR+rs.getString("url");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public String bindLogin(String devid, String uid, String access_token,
			String nickname, String usid, String headIcon, String cologin,
			String marketCode, String appversion) {
		
		String result = "";
		String sql = "{call sp_user_sinabind ('"+devid+"','"+uid+"','"+access_token+"','"+nickname+"','"+usid+"','"+headIcon+"','"+cologin+"','"+marketCode+"','"+appversion+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getString("uid");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public AlipayLoginUserVO checkAliPayUser(String user_id) {
		AlipayLoginUserVO result = new AlipayLoginUserVO();
		String sql = "{call sp_user_check_alipaylogin ('"+user_id+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.uid = rs.getString("uid");
				result.phone = rs.getString("phone");
				result.ispwds = rs.getString("ispwds");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public String getOpencardId() {
		String sql="select data_value from devconfig where id=0";
		logger.info(sql);
		String result="";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getString("data_value");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public int getOpencardIdByPids(String pids) {
		String sql="select COUNT(pid) AS total from product where pid in("+pids+") and isopenid='1'";
		logger.info(sql);
		int result=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt("total");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	public int getOpencardIdImgByPids(String pids) {
		String sql="select COUNT(pid) AS total from product where pid in("+pids+") and isopenidimg='1'";
		logger.info(sql);
		int result=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt("total");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	//修改性别，妮称，头像
	@Transactional
	public void EditUserInfo(Long uid,String headIcon,String nickname,String sex,Integer isHeadimgEdit){
		if(StringUtils.isBlank(headIcon))
			userRepository.updateUsernoimg(uid, nickname, sex);
		else
			userRepository.updateUserInfo(uid, headIcon, nickname, sex,isHeadimgEdit);
	}
	
	//修改用户邦定帐户信息
	@Transactional
	public void updateuserCard(Long uid,String cardNo,int cardtype){
		userRepository.updateUserCard(uid, cardNo, cardtype);
	}
	
	//修改用户的unionid,openid
	@Transactional
	public void updateUnionid(Long uid,String unionid,String openid){
		userRepository.editUnionid(openid, unionid, uid);
	}
	
	//修改用户的unionid,openid
		@Transactional
		public void updateUnionidtg(Long uid,String unionid,String openid,String mcode){
			userRepository.editUnionidtg(openid, unionid, uid,mcode);
		}
	//根据OPENID,UNIONID获取用户信息
	public User getUserByopenid(String openid,String unionid){
		return userRepository.getUserByopenid(openid, unionid);
	}
	//修改代言暗号
	@Transactional
	public void EditEndorsementCode(Long userid,String code){
		userRepository.EditEndorsementCode(userid, code);
	}
	
	//占用代言暗号
	@Transactional
	public void EditEndorseCode(Long userid,String code){
		userRepository.EditEndorseCode(userid, code);
	}
	
	//检验验证码并注册用户
	public Long checkandreguser(String nickname,String phone,String smscode,String headicon,String unionid,String openid,String channel,String fuid){
		if (fuid==null){
			fuid = "0";
		}
		String sql = "{call sp_user_register_wx ('"+nickname+"','"+phone+"','"+smscode+"','"+headicon+"','"+unionid+"','"+openid+"','"+channel+"','"+fuid+"')}";// SQL语句// //调用存储过程
		Long result=0L;				
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result =Numbers.parseLong(rs.getString("uid"),0L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	//检验验证码并注册用户
		public Long checkandreguserTG(String nickname,String phone,String smscode,String headicon,String unionid,String openid,String mcode,String channel,String fuid){
			
			String sql = "{call sp_user_register_tg ('"+nickname+"','"+phone+"','"+smscode+"','"+headicon+"','"+unionid+"','"+openid+"','"+mcode+"','"+channel+"','"+fuid+"')}";// SQL语句// //调用存储过程
			Long result=0L;				
			logger.info(sql);
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					result =Numbers.parseLong(rs.getString("uid"),0L);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			return result;
		}
	
	//判断是否是新人，true新用户，false老用户
	public boolean checkFirstFlag(String uid) {
		String sql = "{CALL sp_order_UserIsFirst(" + Numbers.parseInt(uid, 0)+ ")}";// SQL语句// //调用存储过程
		logger.info(sql);
		int result = 0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while (rs.next()) {
				result = rs.getInt("error_no");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if (result == 0)
			return true;
		else
			return false;
	}
	
	/*暗号部分使用/////////////////////////////////////////////////////////*/
	//验证暗号占用与否(true被占用)
	public boolean checkSecretCode(String code){
		String sql = "select * from secret_code where code='"+code+"'";// SQL语句// //调用存储过程
		logger.info(sql);
		int result = 1;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while (rs.next()) {
				if(rs.getLong("userid")==0L)
					result = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(result>0)
			return true;
		else
			return false;
	}
	
	//占用暗号
	public boolean usesecretcode(Long uid,String code){
		String sql = "select * from secret_code where code='"+code+"'";// SQL语句// //调用存储过程
		logger.info(sql);
		String sqlu="update secret_code set userid="+uid+",updatetime=now() where code='"+code+"'";
		Long Uuserid=0L;
		boolean has=false;
		boolean login=false;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while (rs.next()) {
				Uuserid=rs.getLong("userid");
				if(Uuserid==0L)
					has=true;
				else
				{
					if(Uuserid.compareTo(uid)==0){
						login=true;
					}
				}
			}
			if(has){
				db.getPrepareStateDao(sqlu);
				db.pst.execute(sqlu);
				login=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			login=false;
		} finally {
			db.close();
		}
		
		return login;
	}
	/*暗号结束////////////////////////////////////////////////////////////*/

	/**
	 * 判断此用户是否已经购买过0元商品
	 * @param uid
	 * @param pid
	 * @return
	 */
	public boolean checkBuyOrNotFlag(String uid, String pid) {
		String sql = "{CALL sp_order_IsZeroProduct(" + Numbers.parseInt(uid, 0)+ ","+Numbers.parseInt(pid, 0)+")}";// 调用存储过程
		logger.info(sql);
		int result = 0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while (rs.next()) {
				result = rs.getInt("error_no");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if (result == 0)
			return true;
		else
			return false;
	}

	/**
	 * 
	 * <p>Title: addUserLog</p> 
	 * <p>Description: 添加用户的搜索记录</p> 
	 * @param uid
	 * @param keyword
	 */
	public void addUserLog(String uid, String keyword) {
		UserLog userLog = new UserLog();
		userLog.setUid(Long.parseLong(uid));
		userLog.setAtype((long) 3);
		userLog.setViewid((long) 0);
		userLog.setContent(keyword);
		userLog.setDate_add(new Date());
		userLogRepository.save(userLog);
	}
	
	//获取用户红点数据
	public UserRedFlag getuserRedflag(Long userId){
		return userRgedFlagInterface.getUserFlag(userId);
	}
	
	//变更用户红点
	public void updateUserRedFlag (Long uid,String redcommon,Integer status){
		String sql="select count(1) as cc from user_red_flag where userId="+uid;
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer result=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if (rs.next()) {
				result = rs.getInt("cc");
			}
			if(result>0){
				//存在变更
				String sqlupdate="update user_red_flag set "+redcommon+"="+status+" where userId="+uid;
				db.getPrepareStateDao(sqlupdate);
				db.pst.execute();
			}else
			{
				//不存在添加
				UserRedFlag redflag=new UserRedFlag();
				redflag.setCouponRedFlag(0);
				redflag.setCreateTime(new Date());
				redflag.setCustomServiceRedFlag(0);
				redflag.setEndorseBalanceFlag(0);
				redflag.setGuessULikeRedFlag(0);
				redflag.setMyPresellsRedFlag(0);
				redflag.setUserId(uid);
				redflag.setUpdateTime(new Date());
				userRgedFlagInterface.save(redflag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	//获取安装用户数据
	public String getDevappId(String idfa){
		String sql="select * from marketIdfa where idfa='"+idfa+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String appid="";
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if (rs.next()) {
				appid = rs.getString("appid");
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
		return appid;
	}
	public void updateDevIdfa(String idfa,String appid,Integer status,String version,String channel,String mac){
		String sql="select * from marketIdfa where idfa='"+idfa+"'";
		
		String sqlup="update marketIdfa set flg="+status+" where idfa='"+idfa+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			if(rs.next())
			{
				sqlup="update marketIdfa set flg="+status+" where idfa='"+idfa+"'";
			}
			else
				sqlup="insert into marketIdfa(date_add,idfa,version,appid,channel,mac,flg) values('"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"','"+idfa+"','"+version+"','"+appid+"','"+channel+"','"+mac+"',1)";
			db.getPrepareStateDao(sqlup);
			db.pst.execute();
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
	}
	//获取安装用户数据，根据appid,idfa
			public Integer getDevappId(String idfa,String appid){
				String sql="select * from marketIdfa where idfa='"+idfa+"' and appid='"+appid+"'";
				JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
				Integer flag=0;
				try {
					db.getPrepareStateDao(sql);
					ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
					if (rs.next()) {
						flag=rs.getInt("flg");
					}
				} catch (Exception e) {
					//e.printStackTrace();
				} finally {
					db.close();
				}
				return flag;
			}
//根据idfa取渠道号
	public String getChannelByidfa(String idfa){
		String sql="select * from marketIdfa where idfa='"+idfa+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String cc="";
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if (rs.next()) {
				cc=rs.getString("channel");
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
		return cc;
	}
	//增加idfa反馈日志
	public void addidfaLog(String appid,String idfa,String channel){
		String sql="insert into Idfa_return_log(idfa,appid,channel,createTime) values('"+idfa+"','"+appid+"','"+channel+"','"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"')";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer flag=0;
		try {
			db.getPrepareStateDao(sql);
			db.pst.execute();
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
	}
	//根据手机号获取用户
	public User getUserByphone(String phone){
		return this.userRepository.getUserByPhone(phone);
	}
	
	//根据手机号获取用户
		public User getUserBydevId(String devId){
			User uInfo = new User();
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				String sqlp="SELECT u.* FROM `user` u,`user_device`ud WHERE ud.inituid=u.uid AND ud.device_id='"+devId+"'";
				db.getPrepareStateDao(sqlp);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				if (rs.next()) {
					uInfo.setUid(rs.getLong("uid"));
					uInfo.setPhone(rs.getString("phone"));
					uInfo.setNickname(rs.getString("nickname"));
					uInfo.setHeadIcon(rs.getString("headIcon"));
					uInfo.setSex(rs.getString("sex"));
				}
				//return returnmsg;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			return uInfo;
		}
		
	//加入领取优惠券绑定,根据ＩＰ获取领取优惠券的数量同一ＩＰ超过１００００不添加
	public String addCouponPhoneByIP(String phone,String ip,String marketcode,String fromUid){
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String returnmsg="";
		try {
			String sqlp="call `sp_phone_coupon_yaoqing`('"+phone+"','"+ip+"','"+marketcode+"','"+fromUid+"')";
			db.getPrepareStateDao(sqlp);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if (rs.next()) {
				returnmsg = rs.getString("msg");
			}
			//return returnmsg;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return returnmsg;
	}
	
	//获取用户所有绑定优惠券金额
	public Integer getTotalCouponmoney(String phone,String market){
		/*String sql=" SELECT SUM(a.fee) AS fee FROM(SELECT c.couponprice AS fee FROM `coupon_phone` cp,"
				+" coupon c WHERE c.id = cp.couponId AND cp.phone='"+phone+"' AND c.market='"+market+"'"
				+" UNION SELECT SUM(cc.couponprice) AS fee FROM coupon_phone p,coupon cc WHERE"
				+" p.marketCode=cc.market AND p.couponId=0 AND p.marketCode='"+market+"' AND p.phone='"+phone+"') AS a";*/
				
		String sql="SELECT SUM(cc.couponprice) AS fee FROM coupon_phone p,coupon cc WHERE"
				+" p.marketCode=cc.market AND p.couponId=0 AND p.marketCode='"+market+"' and p.phone='"
				+phone+"' AND DATE_FORMAT(cc.date_add,'%y-%m-%d')=DATE_FORMAT(NOW(),'%y-%m-%d')";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer cm=0;		
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next())
				cm=rs.getInt("fee");
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
		return cm;
	}
	

	public List<InviteShareChannel> getsharechannellist(String version){
		String sql="SELECT * FROM invite_share_channel WHERE VERSION='' OR VERSION>='2.2.0'";
		List<InviteShareChannel> clist=new ArrayList<InviteShareChannel>();
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				InviteShareChannel in=new InviteShareChannel();
				in.setId(rs.getLong("id"));
				String linkurl = rs.getString("linkurl");
				if("http://".equals(linkurl)){
					in.setLinkurl("");
				}else{
					in.setLinkurl(linkurl);
				}
				
				in.setLinkurl(rs.getString("linkurl"));
				in.setRemark(rs.getString("remark"));
				in.setTitle(rs.getString("title"));
				in.setIcon(rs.getString("icon"));
				in.setIslogin(rs.getInt("islogin"));
				clist.add(in);
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
		
		return clist;
	}
	
	//取前十名代言销售排名
	public List<EndorsementSort> getEendorsementSort(){
//		String sql="SELECT SUM(a.totalfee) AS tfee,SUM(a.oldprice) AS ofee,SUM(a.oldprice)-SUM(a.totalfee) AS pfee,a.nickname,a.phone,a.headIcon FROM"
//					+" (SELECT u.uId,u.nickname,u.headIcon,u.phone,s.id,s.endorsementId,s.totalfee,sp.pid,sp.counts*p.rmbprice AS oldprice"
//					+ " FROM shopping_Order s,shopping_Order_Pro sp,product p,endorsementduct e,user u"
//					+ " WHERE s.id=sp.orderId AND sp.pId=p.pId AND s.endorsementId=e.eid AND e.userId=u.uId"
//					+ " AND s.paystat='20' AND s.ordertype=6 GROUP BY s.id,sp.pid) a"
//					+ " GROUP BY a.uId,a.nickname"
//					+ " ORDER BY (SUM(a.oldprice)-SUM(a.totalfee)) DESC LIMIT 10";
		String sql="{call sp_endorsementCharts_get()}";
		List<EndorsementSort> eslist=new ArrayList<EndorsementSort>();
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			int i=0;
			while(rs.next()){
				i++;
				EndorsementSort es=new EndorsementSort();
				es.setId(String.valueOf(i));
				es.setNickname(rs.getString("nickname"));
				if(StringUtils.isBlank(es.getNickname()))
					es.setNickname("******"+rs.getString("phone").substring(rs.getString("phone").length()-6));
				es.setTotalfee(rs.getString("tfee"));
				es.setOldfee(rs.getString("ofee"));
				es.setPlusfee(rs.getString("pfee"));
				es.setHeadIcon(rs.getString("headIcon"));
				if(StringUtils.isBlank(es.getHeadIcon()))
					es.setHeadIcon(CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/default_headicon_girl.png");
				eslist.add(es);
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
		return eslist;
	}
	
	//添加抽奖日志
	public LuckDraw addLuckDraw(LuckDraw luck){
		return luckDrawInterface.save(luck);
	}
	
	//修改抽奖日志，修改手机号
	@Transactional(readOnly = true)
	public void editLuck(LuckDraw luck){
		luckDrawInterface.updateLuckDraw(luck.getId(),luck.getPhone());
	}
	
	//删除抽奖日志
	@Transactional(readOnly = true)
	public void delLuck(Long id){
		luckDrawInterface.delete(id);
	}
	
	//查询抽奖日志
	public List<LuckDraw> getLuckDraw(Long pid,String unionid,String phone){
		return luckDrawInterface.getLuckDrawbyall(pid, unionid, phone);
	}
	
	//根据编号获取抽奖日志
	public LuckDraw getLuckDrawById(Long id){
		return luckDrawInterface.findOne(id);
	}
	
	//添加微信用户数据
	public void addWxUser(WxUser wxu){
		String sqls="select * from wx_user where unionid='"+wxu.getUnionid()+"'";
		
		String sql="insert into wx_user(createTime,unionid,nickname,headicon) values('"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"','"+wxu.getUnionid()+"','"+wxu.getNickname()+"','"+wxu.getHeadicon()+"')";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer cm=0;		
		try {
			db.getPrepareStateDao(sqls);
			ResultSet rs = db.pst.executeQuery();
			if(rs.next())
				cm=1;
			if(cm==0){
				db.getPrepareStateDao(sql);
				db.pst.execute();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	//获取微信用户数据
	public WxUser getWxUser(String unionid){
		String sql="select * from wx_user where unionid='"+unionid+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		WxUser wu=new WxUser();	
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			while(rs.next()){
				wu.setUid(rs.getLong("uid"));
				wu.setUnionid(rs.getString("unionid"));
				wu.setNickname(rs.getString("nickname"));
				wu.setHeadicon(rs.getString("headicon"));
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
		}
		return wu;
	}

	//领取优惠券
	public boolean addCouponPhone(String phone,String couponid){
		String sql="{call sp_phone_coupon('"+phone+"','"+couponid+"')}";
		boolean suc=false;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next())
				suc=rs.getString("err_code").equals("1")?true:false;
		}
		catch(Exception e){
			return false;
		}finally {
			db.close();
		}
		return suc;
	}
	
	/****************免费送活动第一波**************************/
	/*
	 * 添加免费送记录
	 */
	public boolean addfreeone(String phone,Long pid,String fromphone,String unionid,String saveflag,String actname,String nickname,String headicon){
		//检查是否受邀成功
		//List<User> ulist=this.getInviteUserList(uid);
		
		String sql="";
		if(!StringUtils.isBlank(unionid))
		{	
			sql="select * from free_one where unionid='"+unionid+"' and actname='"+actname+"' order by phone desc limit 1";
		}else if(!StringUtils.isBlank(phone)){
			sql="select * from free_one where phone='"+phone+"' and actname='"+actname+"'";
		}
		logger.info(sql);
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer count=0;
		String rphone="";
		Long id=0L;
		String runinid="";
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			if(rs.next()){
				count=1;
				rphone=rs.getString("phone");
				id=rs.getLong("id");
				runinid=rs.getString("unionid");
			}
			if(saveflag.equals("save")){
				if(count>0){
					if(!StringUtils.isBlank(unionid) && StringUtils.isBlank(runinid)){					
						String sqlupdate="update free_one set unionid='"+unionid+"' where id="+id;
						logger.info(sqlupdate);
						db.getPrepareStateDao(sqlupdate);
						db.pst.execute();
					}
					if(!StringUtils.isBlank(phone) && StringUtils.isBlank(rphone)){
						String sqlupdate="update free_one set phone='"+phone+"',fromphone='"+fromphone+"' where id="+id;
						logger.info(sqlupdate);
						db.getPrepareStateDao(sqlupdate);
						db.pst.execute();
					}	
				}else{
					String sqla="insert into free_one(fromphone,pid,phone,unionid,createTime,nickname,headicon,actname) values('"+fromphone+"',"+pid+",'"+phone+"','"+unionid+"','"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"','"+nickname+"','"+headicon+"','"+actname+"')";
					logger.info(sqla);
					db.getPrepareStateDao(sqla);
					db.pst.execute();
					return true;
				}
			}else{
				if(StringUtils.isBlank(rphone)){					
					count=0;
				}	
				return count>0;
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		} finally {
			db.close();
		}
		return false;
	}
	
	/*
	 * 获取受邀用户列表
	 */
	public List<User> getInviteUserList(String phone,String actname){
		String sql="SELECT * FROM free_one WHERE fromphone='"+phone+"' and phone!='' and actname='"+actname+"' order by id desc";
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<User> ulist=new ArrayList();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			while(rs.next()){
				User u=new User();
				u.setUid(rs.getLong("uid"));
				u.setNickname(rs.getString("nickname"));
				u.setUnionid(rs.getString("unionid"));
				u.setPhone(rs.getString("phone"));
				u.setHeadIcon(rs.getString("headicon"));
				u.setDate_add(rs.getDate("createTime"));
				ulist.add(u);
			}
		}catch (Exception e) {
			return null;
			//e.printStackTrace();
		} finally {
			db.close();
					db.close();
		}
		return ulist;
	}
	
	/*
	 * 获取免费送领取用户列表
	 */
	public List<User> getFreeUserList(String fromphone,String actname,Integer pageno,Integer pagesize){
		String sql="SELECT * FROM free_one WHERE fromphone='"+fromphone+"' and phone!='' AND actname='"+actname+"' order by id desc limit "+pageno*pagesize+","+pagesize;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<User> ulist=new ArrayList();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			while(rs.next()){
				User u=new User();
				u.setUid(0L);
				u.setNickname(rs.getString("nickname"));
				u.setUnionid(rs.getString("unionid"));
				u.setPhone(rs.getString("phone"));
				u.setHeadIcon(rs.getString("headicon"));
				if(StringUtils.isBlank(u.getHeadIcon()))
					u.setHeadIcon(CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/default_headicon_boy.png");				
				u.setDate_add(rs.getTimestamp("createTime"));//取领取时间
				ulist.add(u);
			}
		}catch (Exception e) {
			return null;
			//e.printStackTrace();
		} finally {
			db.close();
					db.close();
		}
		return ulist;
	}
	
	/*
	 * 获取免费送领取用户列表第二波
	 */
	public List<User> getFreetwoUserList(String fromphone,String actname,Integer pageno,Integer pagesize){
		String sql="SELECT f.*,c.title FROM free_one f,coupon_phone cp,coupon c WHERE f.phone=cp.phone AND cp.couponId=c.id AND cp.marketCode='"+actname+"' AND f.actname='"+actname+"' and f.fromphone='"+fromphone+"' group by f.id order by f.id desc limit "+pageno*pagesize+","+pagesize;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<User> ulist=new ArrayList();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			while(rs.next()){
				User u=new User();
				u.setUid(0L);
				u.setNickname(rs.getString("nickname"));
				u.setUnionid(rs.getString("unionid"));
				u.setPhone(rs.getString("phone"));
				u.setHeadIcon(rs.getString("headicon"));
				if(StringUtils.isBlank(u.getHeadIcon()))
					u.setHeadIcon(CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/default_headicon_boy.png");				
				u.setDate_add(rs.getTimestamp("createTime"));//取领取时间
				u.setMcode(rs.getString("title"));
				ulist.add(u);
			}
		}catch (Exception e) {
			return null;
			//e.printStackTrace();
		} finally {
			db.close();
					db.close();
		}
		return ulist;
	}
	
	/*
	 * 变更免费送商品数量
	 */
	public void updateSystemConfig(String flagname,String flagvalue){
		String sql="update systemconfig set data_value='"+flagvalue+"' where data_key='"+flagname+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.execute();
			//清缓存
			cache.clear("SystemConfig_higou");
		}catch (Exception e) {
			//e.printStackTrace();
		} finally {
			db.close();
					db.close();
		}
	}
	/*
	 * 检查是否有免费领取资格,msg返回空有资格，否则返回描述语
	 */
	public String checkuserfree(Long uid){
		String sql="{call sp_free_checkuserfree('"+uid+"')}";
		String msg="";
		String errcode="0";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				errcode=rs.getString("err_code");
				if(!errcode.equals("1"))
					msg=rs.getString("err_msg");
			}
		}
		catch(Exception e){
		}finally {
			db.close();
		}
		return msg;
	}
	
	/*
	 * 根据unionid 获取领取的手机号码
	 */
	public String getFreePhone(String unionid){
		String sql="SELECT * FROM free_one WHERE unionid='"+unionid+"' order by phone desc limit 1";
		String phone="";
		String errcode="0";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				phone=rs.getString("phone");
			}
		}
		catch(Exception e){
		}finally {
			db.close();
		}
		return phone;
	}
	/*
	 * 校验验证码
	 */
	public boolean checkverifysms(String phone,String vsms){
		String sql="SELECT count(1) as cnt FROM userVerify WHERE phone='"+phone+"' AND verify='"+vsms+"' AND flg='1'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer cnt=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				cnt=rs.getInt("cnt");
			}
//			String sqlu="update userVerify set flg=0 WHERE phone='"+phone+"' AND verify='"+vsms+"' AND flg='1'";
//			db.getPrepareStateDao(sqlu);
//			db.pst.execute();
		}
		catch(Exception e){
		}finally {
			db.close();
		}
		return cnt>0;
	}
	
	/*
	 * 签到
	 */
	public Integer loginact(Long uid){
		String sql="{call sp_User_signDay('"+uid+"')}";
		Integer day=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				day=rs.getInt("days");
			}
		}
		catch(Exception e){
		}finally {
			db.close();
		}
		return day;
	}
	
	public UserHuanXinVO getHuanXinUser(int uid){
		UserHuanXinVO user = new UserHuanXinVO();
		String sql="{call  `sp_user_Huanxin`("+uid+")}";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				String flg = rs.getString("flg");
				String username = rs.getString("username");
				String userpass = rs.getString("userpass");
				String nickname = rs.getString("nickname");
				if ("0".equals(flg)){
					// 调用环信注册接口
					sendHuanXinUser(username,userpass,nickname);
				}
				user.setEmLoginID(username);
				user.setEmLoginPwd(userpass);
				
			}
		}
		catch(Exception e){
			logger.info("huanxinERR==============================="+e.getStackTrace());
		}finally {
			db.close();
		}
		return user;
	}
	
	public void sendHuanXinUser(String username,String userpass,String nickname){
		
		// 获取token
		  Map<String, String> hashMap = new HashMap<String, String>();  
		  hashMap.put("grant_type", "client_credentials");  
		  hashMap.put("client_id", "YXA6rDeokFUzEeWVzG2jpfaefw");  
		  hashMap.put("client_secret", "YXA6xxA_vhZX_dnddG1VbIcOjkMpwGY");
		  
		  String url="https://a1.easemob.com/higegou/higegou/token";
		  String auth="";
		  try  
		  {  
		    ObjectMapper objectMapper = new ObjectMapper();  
		    String userMapJson = objectMapper.writeValueAsString(hashMap);
		    JsonNode node = objectMapper.readTree(userMapJson);  
		    JsonNode jn = WSUtils.postByJSON(url, node);
		    auth=jn.get("access_token").textValue().toString();
		  }catch(Exception ex){
			  logger.info("huanxinToken:========"+ex.getMessage());
			  auth="";
		  }
		// 用户注册（授权）
		  if(!StringUtils.isBlank(auth)){
			  Map<String, String> userMap = new HashMap<String, String>();  
			  userMap.put("username", username);  
			  userMap.put("password", userpass);  
			  userMap.put("nickname", nickname);
			  url="https://a1.easemob.com/higegou/higegou/users";
			  auth = "Bearer "+auth;
			  try  
			  {  
			    ObjectMapper objectMapper = new ObjectMapper();  
			    String userMapJson = objectMapper.writeValueAsString(userMap);
			    JsonNode node = objectMapper.readTree(userMapJson);  
			    JsonNode jn = WSUtils.postByJSONWithAuth(url, node,auth);
			    setHuanXinUser(username);
			    //auth=jn.get("access_token").textValue().toString();
			  }catch(Exception ex){
				  //auth="";
				  logger.info("huanxinauth:========"+ex.getMessage());
			  } 
		  }
		  return;
		
	}
	
	public void setHuanXinUser(String userName){
		String sql="UPDATE huanxinUser SET flg='1' WHERE username='"+userName+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.execute();
		}
		catch(Exception e){
			
		}finally {
			db.close();
		}
		return;
	}
	
	
	public Map<String, String> getUserId_ByGuid(int uid,String devid,String guid,String flg){
		Map<String, String> pramt = new HashMap<String, String>();
		String sql="{call `sp_user_csGuid`("+uid+",'"+devid+"','"+guid+"','"+flg+"')}";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				pramt.put("userid",rs.getString("userid"));
				pramt.put("guid",rs.getString("guid"));
			}
		}
		catch(Exception e){
			
		}finally {
			db.close();
		}
		return pramt;
	}
	
	/*
	 * 获取签到
	 */
	public List<Integer> getLoginSign(Long uid){
		String sql="{call sp_User_getsignDay('"+uid+"')}";
		Integer day=0;
		Integer hasact=0;
		List<Integer> actlist=new ArrayList<Integer>();
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				day=rs.getInt("lastdays");
				hasact=rs.getInt("daysign");
			}
		}
		catch(Exception e){
		}finally {
			db.close();
		}
		actlist.add(day);
		actlist.add(hasact);
		return actlist;
	}
	
	/*
	 * 垛手领券检查
	 */
	public boolean checkSendCoupon(Long uid){
		String sql="{call sp_User_getBindCoupon('"+uid+"','1')}";
		Integer r=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				r=rs.getInt("errno");
			}
		}
		catch(Exception e){
		}finally {
			db.close();
		}
		return r>0;
	}
	/*
	 * 垛手领券
	 */
	public void getSendCoupon(Long uid){
		String sql="{call sp_User_getBindCoupon('"+uid+"','0')}";
		Integer r=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.execute();
		}
		catch(Exception e){
		}finally {
			db.close();
		}
	}
	/****************免费送活动第一波结束***********************/
	
	/*************** 免费活动第二波***************************/
	public String sendFreeCoupon(String phone,String fromphone,String ip,String vsms,String unionid,String actname){
		String sql="{call sp_free2_check('"+phone+"','"+vsms+"','"+unionid+"','"+fromphone+"','"+actname+"','"+ip+"')}";
		String err="0_";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				err=rs.getInt("error")+"_"+rs.getString("errmsg");
			}
		}
		catch(Exception e){
			logger.info("sql err:"+e.toString());
		}finally {
			db.close();
		}
		return err;
	}
	/*************** 免费活动第二波结束************************/
	
	/*
	 * 检查设备号是否存
	 */
	public boolean checkDeviceID(String devid){
		String sql="SELECT count(1) as cc FROM user_device WHERE device_id='"+devid+"'";
		Integer count=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				count=rs.getInt("cc");
			}
		}
		catch(Exception e){
			logger.info("sql err:"+e.toString());
		}finally {
			db.close();
		}
		return count>0;
	}
	
	/*
	 * 更新access_token
	 */
	public void updateAccessToken(String openid,String accesstoken){
		String sql="update user set access_token='"+accesstoken+"' where openid='"+openid+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
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
	/*
	 * 查询access_token
	 */
	public String getAccessToken(String accesstoken){
		String sql="SELECT access_token form user where access_token='"+accesstoken+"'";
		String ac="";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				ac=rs.getString("access_token");
			}
		}
		catch(Exception e){
			logger.info("sql err:"+e.toString());
		}finally {
			db.close();
		}
		return ac;
	}

	/**
	 * 根据手机号去修改密码
	 * @param phone
	 * @param password
	 * @return
	 */
	@Transactional
	public AppSalesManUserVO salesManPasswordModify(String phone, String password,String devId) {
		AppSalesManUserVO result = new AppSalesManUserVO();
		User user = userRepository.findByPhone(phone);
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		if(user!=null){
			user.setPasswords(password);
			user = userRepository.save(user);
			vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
			userInfo.setGender(user.getSex());
			userInfo.setHeadIcon(user.getHeadIcon());
			userInfo.setPhone(phone);
			userInfo.setNickname(user.getNickname());
			userInfo.setUid(String.valueOf(user.getUid()));
			if(StringUtils.isBlank(userInfo.getHeadIcon())){
				if(StringUtils.isBlank(userInfo.getGender()) || userInfo.getGender().equals("0"))
					userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
				else
					userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
			}
			String sql = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.uid="+user.getUid();
			logger.info(sql);
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					userInfo.setStoreID(rs.getString("id"));
					userInfo.setStoreName(rs.getString("realname"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
			result.setStatus("1");
			result.setMsg("");
			result.setUserInfo(userInfo);
		}else{
			vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
			User u = getUserBydevId("devId");
			if (u!=null){
				userInfo.setGender(u.getSex());
				userInfo.setHeadIcon(u.getHeadIcon());
				userInfo.setPhone(u.getPhone());
				userInfo.setNickname(u.getNickname());
				userInfo.setUid(u.getUid().toString());
				userInfo.setStoreID("");
				userInfo.setStoreName("");
				result.setStatus("0");
				result.setMsg("修改密码失败");
				result.setUserInfo(userInfo);
			}
		}
		return result;
	}
}
