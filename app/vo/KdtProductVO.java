package vo;

import java.util.List;

/**
 * 口袋通商品VO
 * 
 * @author luobotao
 * 
 */
public class KdtProductVO {
	
	 private String num_iid ;//商品数字编号
	 private String cid ;//商品分类的叶子类目id
	 private String promotion_cid;//商品推广栏目id
	 private String tag_ids;//商品标签id串，结构如：1234,1342,...
	 private String price;//商品价格。取值范围：0.01-100000000；精确到2位小数；单位：元。需要在Sku价格所决定的的区间内
	 private String title;//商品标题。不能超过100字，受违禁词控制
	 private String desc;//商品描述。字数要大于5个字符，小于25000个字符 ，受违禁词控制
	 private String is_virtual;//是否是虚拟商品。0为否，1为是。目前不支持虚拟商品
	 private String post_fee;//运费。取值范围：0.00-999.00；精确到2位小数；单位：元
	 private String sku_properties ;//Sku的属性串。如：颜色:黄色;尺寸:M;重量:1KG,颜色:黄色;尺寸:S;重量:1KG 格式：pText:vText;pText:vText，多个sku之间用逗号分隔，如：颜色:黄色;尺寸:M,颜色:黄色;尺寸:S。pText和vText文本中不可以存在冒号和分号以及逗号  为了兼顾移动端商品界面展示的美观，目前有赞仅支持Sku的属性个数小于等于三个（比如：颜色、尺寸、重量 这三个属性）。无Sku则为空
	 private String sku_quantities ;//Sku的数量串。结构如：num1,num2,num3 如：2,3。无Sku则为空
	 private String sku_prices ;//Sku的价格串。结构如：10.00,5.00,... 精确到2位小数。单位:元。无Sku则为空
	 private String sku_outer_ids ;//Sku的商家编码（商家为Sku设置的外部编号）串。结构如：1234,1342,... 。sku_properties, sku_quantities, sku_prices, sku_outer_ids在输入数据时要一一对应，即使商家编码为空，也要用逗号相连。无Sku则为空
	 private String skus_with_json;//商品Sku信息的Json字符串，[{"sku_price":"10.00","sku_property":{"颜色":"黄色","尺寸":"M","重量":"1KG"},"sku_quantity":"2","sku_outer_id":"1234"},{"sku_price":"5.00","sku_property":{"颜色":"黄色","尺寸":"S","重量":"1KG"},"sku_quantity":"3","sku_outer_id":"1242"}]  调用时，参数 sku_properties、sku_quantities、sku_prices、sku_outer_ids四个字段组合方式 和 skus_with_json 单字段输入方式 选其一个方式即可，无Sku则为空。 具体参见kdt.item.update文档描述。
	 private String origin_price ;//显示在“原价”一栏中的信息
	 private String buy_url ;//该商品的外部购买地址。当用户购买环境不支持微信或微博支付时会跳转到此地址
	 private String outer_id ;//商品货号（商家为商品设置的外部编号）
	 private String buy_quota;//每人限购多少件。0代表无限购，默认为0
	 private String quantity;//商品总库存。当商品没有Sku的时候有效，商品有Sku时，总库存会自动按所有Sku库存之和计算
	 private String hide_quantity;//是否隐藏商品库存。在商品展示时不显示商品的库存，默认0：显示库存，设置为1：不显示库存
	 private String fields;//需要返回的商品对象字段，如title,price,desc等。可选值：Item商品结构体中所有字段均可返回；多个字段用“,”分隔。如果为空则返回所有
	 private String fileKey = "images[]";// images[]//商品图片文件列表，可一次上传多张。最大支持 1M，支持的文件类型：gif,jpg,jpeg,png注：图片参数不参与通讯协议签名，参数名中的中括号"[]"必须有，否则不能正常工作
	 private List<String> filePaths;//
	
	public String getNum_iid() {
		return num_iid;
	}
	public void setNum_iid(String num_iid) {
		this.num_iid = num_iid;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getPromotion_cid() {
		return promotion_cid;
	}
	public void setPromotion_cid(String promotion_cid) {
		this.promotion_cid = promotion_cid;
	}
	public String getTag_ids() {
		return tag_ids;
	}
	public void setTag_ids(String tag_ids) {
		this.tag_ids = tag_ids;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getIs_virtual() {
		return is_virtual;
	}
	public void setIs_virtual(String is_virtual) {
		this.is_virtual = is_virtual;
	}
	public String getPost_fee() {
		return post_fee;
	}
	public void setPost_fee(String post_fee) {
		this.post_fee = post_fee;
	}
	public String getSku_properties() {
		return sku_properties;
	}
	public void setSku_properties(String sku_properties) {
		this.sku_properties = sku_properties;
	}
	public String getSku_quantities() {
		return sku_quantities;
	}
	public void setSku_quantities(String sku_quantities) {
		this.sku_quantities = sku_quantities;
	}
	public String getSku_prices() {
		return sku_prices;
	}
	public void setSku_prices(String sku_prices) {
		this.sku_prices = sku_prices;
	}
	public String getSku_outer_ids() {
		return sku_outer_ids;
	}
	public void setSku_outer_ids(String sku_outer_ids) {
		this.sku_outer_ids = sku_outer_ids;
	}
	public String getSkus_with_json() {
		return skus_with_json;
	}
	public void setSkus_with_json(String skus_with_json) {
		this.skus_with_json = skus_with_json;
	}
	public String getOrigin_price() {
		return origin_price;
	}
	public void setOrigin_price(String origin_price) {
		this.origin_price = origin_price;
	}
	public String getBuy_url() {
		return buy_url;
	}
	public void setBuy_url(String buy_url) {
		this.buy_url = buy_url;
	}
	public String getOuter_id() {
		return outer_id;
	}
	public void setOuter_id(String outer_id) {
		this.outer_id = outer_id;
	}
	public String getBuy_quota() {
		return buy_quota;
	}
	public void setBuy_quota(String buy_quota) {
		this.buy_quota = buy_quota;
	}
	public String getQuantity() {
		return quantity;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public String getHide_quantity() {
		return hide_quantity;
	}
	public void setHide_quantity(String hide_quantity) {
		this.hide_quantity = hide_quantity;
	}
	public String getFields() {
		return fields;
	}
	public void setFields(String fields) {
		this.fields = fields;
	}
	public String getFileKey() {
		return fileKey;
	}
	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}
	public List<String> getFilePaths() {
		return filePaths;
	}
	public void setFilePaths(List<String> filePaths) {
		this.filePaths = filePaths;
	}
	 
	 

}
