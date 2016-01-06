package vo.endorsment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.EndorsementImg;

public class EnorsmentDetailVO {
	public String status;
	public EnorsmentDetailVOItem data=new EnorsmentDetailVOItem();
	
	public static class EnorsmentDetailVOItem{
		public Long eid=0L;
		public String createTime="";
		public Long userId=0L;
		public String userNickName="";
		public String userHeadIcon="";
		public Long productId=0L;
		public String remark="";
		public String preImgPath="";
		public Integer status=0;
		public Integer praiseNums=0;
		public Integer picnums=0;
		public String H5pushURL="";//分享地址
		public List<EndorsementImg> imglist=new ArrayList<EndorsementImg>();
		public List<userLikeImg> headImglist=new ArrayList<userLikeImg>();
		public EndorsementDetailItem pinfo=new EndorsementDetailItem();
		public String endorBadgeUrl="";//徽章图片地址
		public String badgeHeadUrl="";//用户徽章头像
		public String badgeUserName="";//徽章用户名
		public String shareType="";//
		public String isLiked="";
		public String shareTitle="";//分享标题
		public String shareDescription="";//分享内容
		public String reffer;
	}
	
	public static class userLikeImg{
		public String userId="";
		public String headIcon="";
	}
	
	public static class EndorsementDetailItem{
		public String pid="";                       
		public String pcode="";                     
		public String title="";
		public String subtitle="";
		public String rmb_price="";
		public String china_price="";   
		public String linkUrl="";   
		public String listpic="";
		public String nationalFlagImg="";
		public Double rmb_price_no_symbol=0D;
		public String endorsementCount="";
		public String maxEndorsementCount="";
		public String endorsementPrice="";
		public String commision="";
		public String commisionTyp="";
	}
}
