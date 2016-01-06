package vo.endorsment;

import java.util.Date;
import java.util.List;

import models.EndorsementPraise;

public class EndorsmentVO {

	public String status;
	public String reffer;
	public List<EnorsmentVOItem> data;
	
	public static class EnorsmentVOItem{
		public String eid;
		public String count;
		public String createTime;
		public String userId;
		public String isLiked;
		public String userNickName;
		public String userHeadIcon;
		public String remark;
		public String preImgPath;
		public String linkURL;
		public List<userLikeImg> headImglist;
		public String endorBadgeUrl;
		public String endorTagImgUrl;
	}
	
	public static class userLikeImg{
		public String userId;
		public String headIcon;
	}
}
