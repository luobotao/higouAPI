package vo.endorsment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.EndorsementPayLog;

public class EndorsePaylogVO {

	public String status;	//状态
	public int endflag;
	public String total;	 //当前金额
	public String availtotal; //可用金额
	public String entotal;	 //冻结金额
	public String rulesurl;//代言规则h5地址
	public List<EnorsmentpayVOItem> data=new ArrayList<EnorsmentpayVOItem>();
	
	public static class EnorsmentpayVOItem{
		public String createTime;
		public String totalbalance;
		public List<EndorsePayLogVoItem> loglist=new ArrayList<EndorsePayLogVoItem>();
	}
	
	public static class EndorsePayLogVoItem{
		public String title;
		public String producttitle;
		public String tim;
		public String balance;
		public String status;
		public String buyerName;
		public String imageUrl;
		public String count;
	}
}
