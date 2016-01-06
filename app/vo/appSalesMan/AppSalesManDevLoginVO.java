package vo.appSalesMan;

import java.util.List;

import play.Configuration;
import models.APPConfig;
import models.Share;
import vo.VersionVo;
import vo.appSalesMan.AppSalesManUserVO.UserInfo;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppSalesManDevLoginVO {
		private String loading;
		private String status;
		private String msg;
		private VersionVo version;
		private UserInfo userInfo;
		private String token;
	
		
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		
		public UserInfo getUserInfo() {
			return userInfo;
		}
		public void setUserInfo(UserInfo userInfo) {
			this.userInfo = userInfo;
		}
		public String getLoading() {
			return loading;
		}
		public void setLoading(String loading) {
			boolean IsProduct = Configuration.root().getBoolean("production", false);
			String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
			if(IsProduct){
				domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
			}
			this.loading = domain + Configuration.root().getString("adload","/pimgs/adload/") + loading;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
	
		public VersionVo getVersion() {
			return version;
		}
		public void setVersion(VersionVo version) {
			this.version = version;
		}
}