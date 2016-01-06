package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.APPConfig;
import models.AdLoading;
import models.Share;
import models.User;
import models.UserDevice;

import org.hibernate.annotations.NamedNativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface UserRepository extends JpaRepository<User, Long>,JpaSpecificationExecutor<User> {
	
	public User findByPhone(String phone);
	
	public User findOne(Long uid);
	
	@Query(value="SELECT * FROM `user` WHERE phone=?1 and passwords<>''",nativeQuery=true)
	public User getPwdsIsBind(String phone);
	
	@Query(value="SELECT * FROM `user` WHERE phone=?1 and passwords<>'' and gid=5 and fromUid>0",nativeQuery=true)
	public User getGid5IsBind(String phone);
	
	@Query(value="SELECT data_key,data_value FROM devconfig WHERE id>0",nativeQuery=true)
	public List<Object[]> getAppConfigList();

	@Query(value="SELECT sharetitle,shareimg,shareurl,sharetxt FROM `share`",nativeQuery=true)
	public List<Object[]> getShareList();
	
	@Query(value="SELECT ostype,latestver,isforced,remind_time,message,url FROM appversion WHERE ostype=?1 AND `status`=6 ORDER BY date_upd DESC LIMIT 0,1",nativeQuery=true)
	public List<Object[]> getVersion(String ostype);
	
	@Query(value="SELECT DATEDIFF(NOW(),DATE_ADD) AS days FROM `user` WHERE uid=?1",nativeQuery=true)
	public int getDifDay(String uid);

	@Query(value="SELECT COUNT(id) FROM user_log WHERE atype=1 AND uid=?1",nativeQuery=true)
	public int getViewCount(String uid);
	
	@Query(value="SELECT u.*,d.solution,d.model FROM `user` u,`user_device` d WHERE d.uid=u.uid AND  u.uid=?1",nativeQuery=true)
	public List<Object[]> getDeviceUser(String uid);
	
	@Query(value="SELECT COUNT(*) AS COUNT FROM shopping_Order s,shopping_Order_Pro p,product pd WHERE s.id =p.orderId AND s.ordertype = '3' AND s.uid=?1 AND s.status IN ('21','22') AND  p.pid= pd.pid AND pd.stage='3' AND s.noticeflg='0' ",nativeQuery=true)
	public int getNoticeOrders(Long uid);

	@Modifying
	@Query(value=" UPDATE shopping_Order s INNER JOIN (shopping_Order_Pro p,product pd) ON (s.id =p.orderId AND p.pid= pd.pid) SET s.noticeflg='1' WHERE s.uid=?1 AND s.ordertype = '3' AND s.status IN ('21','22') AND s.noticeflg='0' AND pd.stage='3' ",nativeQuery=true)
	public void updateNoticeFlag(Long uid);

	@Query(value="select inituid from user_device where device_id=?1",nativeQuery=true)
	public Integer findInitUidByDevid(String devid);

	@Modifying
	@Query(value="update `user_device` set uid=?1 where device_id=?2",nativeQuery=true)
	public void updateUserDevice(Integer initUid, String devid);

	@Modifying
	@Query(value="update `user` set active=?1 where uid=?2",nativeQuery=true)
	public void updateUserActive(int active, int uid);
	
	@Modifying
	@Query(value="update `user` set phone=?2 where uid=?1",nativeQuery=true)
	public void userbindphone(Long uid,String phone);

	@Query(value="select count(*) from user where openId=?2 or unionid=?1",nativeQuery=true)
	public int checkBind(String unionid, String usid);

	@Query(value="select id,cname from channel where sta='1' order by nsort desc",nativeQuery=true)
	public List<Object[]> getChannelList();

	@Query(value="select pid from userlike where uid=?1",nativeQuery=true)
	public List<Integer> getLikes(Long uid);
	
	@Query(value="SELECT MAX(c.id) FROM channel_mould c,mould m WHERE m.id=c.mouldId  AND c.flag='1' AND c.cid=?1",nativeQuery=true)
	public String getChannel_Model_tag(Integer cid);

	@Query(value=" SELECT COUNT(p.id) FROM channel_mould c,mould m,channel_mould_pro p WHERE m.id=c.mouldId  AND c.id=p.cmid AND c.flag='1' AND c.cid=?1 AND c.id>?2",nativeQuery=true)
	public String getChannel_Model_RefreshNum(Integer cid, Integer tag);

	@Query(value=" SELECT COUNT(*) FROM subject_mould s,mould m WHERE m.id=s.mouldId AND s.sid=?1 AND s.id>?2 ",nativeQuery=true)
	public String getSubject_Model_RefreshNum(Integer sid, Integer tag);

	@Query(value="select max(s.id) from subject_mould s,mould m WHERE m.id=s.mouldId  AND s.sid=?1",nativeQuery=true)
	public String getSubject_Model_tag(Integer sid);

	@Query(value="SELECT * FROM `user` WHERE uid=?1 and phone<>''",nativeQuery=true)
	public User getIsBindPhone(String uid);
	
	@Modifying
	@Query(value="update user set sex=?4,headIcon=?2,nickname=?3,isHeadimgEdit=?5 where uid=?1",nativeQuery=true)
	public void updateUserInfo(Long uid,String headIcon,String nickname,String sex,Integer isHeadimgEdit);
	
	@Modifying
	@Query(value="update user set sex=?3,nickname=?2 where uid=?1",nativeQuery=true)
	public void updateUsernoimg(Long uid, String nickname,String sex);
	
	@Modifying
	@Query(value="update user set cardNO=2?,cardType=?3 where uid=?1",nativeQuery=true)
	public void updateUserCard(Long uid,String cardNO,int cardtype);
	
	@Query(value="select * from user where openId=?1 and unionid=?2",nativeQuery=true)
	public User getUserByopenid(String openId,String unionid);
	
	@Modifying
	@Query(value="update user set isEndorsement=1,endorsementCode=?2 where uid=?1",nativeQuery=true)
	public void EditEndorsementCode(Long uid,String code);
	
	@Modifying
	@Query(value="update endorsment_code set status=1,uid=?1 where code=?2",nativeQuery=true)
	public void EditEndorseCode(Long uid,String code);
	
	@Modifying
	@Query(value="update user set openId=?1,unionid=?2 where uid=?3",nativeQuery=true)
	public void editUnionid(String openId,String unionid,Long uid);
	
	@Modifying
	@Query(value="update user set openId=?1,unionid=?2,mcode=?4 where uid=?3",nativeQuery=true)
	public void editUnionidtg(String openId,String unionid,Long uid,String mcode);
	
	@Query(value="SELECT * FROM `user` WHERE phone=?1",nativeQuery=true)
	public User getUserByPhone(String phone);

	@Modifying
	@Query(value="update User set passwords=?2 where phone=?1")
	public int updateUserPasswordByPhone(String phone, String password);
}