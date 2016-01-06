package vo.endorsment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserBalanceLogVO {
	public String status;
	public int endflag;
	public String noticeMsg;//钱包提现说明
	public String rulesurl;///钱包提现规则H5地址
	public String limitmoney;//提现最小限额
	public dataitm data=new dataitm();
	
	
	public static class dataitm{
		public String balance="";
		public List<UserBalanceLogItem> balancelist=new ArrayList<UserBalanceLogItem>();
	}
	public static class UserBalanceLogItem{
		public Long id;
		public BigDecimal beforBalance;
		public BigDecimal curentBalance;	//变更后余额
		public String balance;	//发生金额
		public String createTime; //创建时间
		public int flg;	//变更类型1收入，2支出,3冻结收入
		public String remark;	//支出、收入明细说明
	}
}