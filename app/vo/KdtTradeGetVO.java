package vo;


/**
 * 口袋通查询订单的检索条件
 * 
 * @author luobotao
 * 
 */
public class KdtTradeGetVO {
	
	private String fields;//需要返回的交易对象字段，如tid,title,receiver_city等。可选值：Trade交易结构体中所有字段均可返回；多个字段用“,”分隔。如果为空则返回所有
	private String status;//交易状态，默认查询所有交易状态的数据，除了默认值外每次只能查询一种状态。可选值：TRADE_NO_CREATE_PAY（没有创建支付交易）WAIT_BUYER_PAY（等待买家付款）WAIT_SELLER_SEND_GOODS（等待卖家发货，即：买家已付款）WAIT_BUYER_CONFIRM_GOODS（等待买家确认收货，即：卖家已发货）TRADE_BUYER_SIGNED（买家已签收）TRADE_CLOSED（付款以后用户退款成功，交易自动关闭）TRADE_CLOSED_BY_USER（付款以前，卖家或买家主动关闭交易）ALL_WAIT_PAY（包含：WAIT_BUYER_PAY、TRADE_NO_CREATE_PAY）ALL_CLOSED（包含：TRADE_CLOSED、TRADE_CLOSED_BY_USER）
	private String start_created;//交易创建开始时间。查询在该时间之后（包含该时间）创建的交易，为空则不限制
	private String end_created;//交易创建结束时间。查询在该时间之前创建的交易，为空则不限制
	private String start_update;//交易状态更新的开始时间。查询在该时间之后（包含该时间）交易状态更新过的交易，为空则不限制
	private String end_update;//交易状态更新的结束时间。查询在该时间之前交易状态更新过的交易，为空则不限制
	private String weixin_user_id;//微信粉丝ID
	private String buyer_nick;//买家昵称
	private String page_no;//页码
	private String page_size;//每页条数
	private String use_has_next;//是否启用has_next的分页方式，是的话返回的结果中不包含总记录数，但是会新增一个是否存在下一页的的字段
	
	public String getFields() {
		return fields;
	}
	public void setFields(String fields) {
		this.fields = fields;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStart_created() {
		return start_created;
	}
	public void setStart_created(String start_created) {
		this.start_created = start_created;
	}
	public String getEnd_created() {
		return end_created;
	}
	public void setEnd_created(String end_created) {
		this.end_created = end_created;
	}
	public String getStart_update() {
		return start_update;
	}
	public void setStart_update(String start_update) {
		this.start_update = start_update;
	}
	public String getEnd_update() {
		return end_update;
	}
	public void setEnd_update(String end_update) {
		this.end_update = end_update;
	}
	public String getWeixin_user_id() {
		return weixin_user_id;
	}
	public void setWeixin_user_id(String weixin_user_id) {
		this.weixin_user_id = weixin_user_id;
	}
	public String getBuyer_nick() {
		return buyer_nick;
	}
	public void setBuyer_nick(String buyer_nick) {
		this.buyer_nick = buyer_nick;
	}
	public String getUse_has_next() {
		return use_has_next;
	}
	public void setUse_has_next(String use_has_next) {
		this.use_has_next = use_has_next;
	}
	public String getPage_no() {
		return page_no;
	}
	public void setPage_no(String page_no) {
		this.page_no = page_no;
	}
	public String getPage_size() {
		return page_size;
	}
	public void setPage_size(String page_size) {
		this.page_size = page_size;
	}
	 
}
