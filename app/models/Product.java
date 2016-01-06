package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import assets.CdnAssets;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import play.Configuration;
import utils.StringUtil;

/**
 * 商品实体
 * 
 * @author luobotao Date: 2015年4月16日 上午9:59:24
 */
@Entity
@Table(name = "product")
public class Product implements Serializable{

	private static final long serialVersionUID = 8329602721039751392L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long pid;

	private String skucode;
	private String title;
	private String subtitle;

	@Column(columnDefinition = "int(10) DEFAULT 0 ")
	private int category;
	@Column(nullable = false, columnDefinition = "decimal(12,4) NOT NULL DEFAULT '999999.9900'")
	private Double price;
	@Column(nullable = false, columnDefinition = "decimal(12,4) NOT NULL DEFAULT '999999.9900'")
	private Double list_price;
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private int discount;
	@Column(columnDefinition = " tinyint(4) unsigned DEFAULT '0' ")
	private int imgnums;

	private String exturl;
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private int salesrank;
	@Column(columnDefinition = " tinyint(4) DEFAULT '0' ")
	private int status;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_upd;
	
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int nlikes;
	@Column(columnDefinition = " tinyint(4) DEFAULT '0' ")
	private int ishot;
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int version;
	private String extcode;
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int fromsite;
	@Column(columnDefinition = " int(10) DEFAULT '0' ")
	private int currency;
	private String imgstr;
	private String listpic;
	private String adstr1;
	private String adstr3;
	private String detail;
	private int sort;
	@Column(columnDefinition = " decimal(12,4) ")
	private Double chinaprice;
	private Long nstock;
	@Column(columnDefinition = " tinyint(4) DEFAULT '0' COMMENT '1自动更新; 0不更新' ")
	private int nstock_autoupd;
	@Column(columnDefinition = " varchar(2) DEFAULT '0' ")
	private String islovely;
	@Column(columnDefinition = " varchar(2) DEFAULT '1' ")
	private String typ;//1代下单；2自营
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private Double weight;
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private Double freight;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String wayremark;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int wishcount;
	@Column(columnDefinition = " varchar(16) ")
	private String activityname;
	@Column(columnDefinition = " varchar(64) ")
	private String activityimage;
	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String PromiseURL;
	@Column(columnDefinition = " int(11) DEFAULT '-1' ")
	private int limitcount;
	@Column(columnDefinition = " decimal(3,1) DEFAULT '10.0' ")
	private Double lovelydistinct;
	@Column(columnDefinition = " decimal(6,2) DEFAULT NULL ")
	private Double rmbprice;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int islockprice;
	@Column(columnDefinition = " varchar(64) DEFAULT '' ")
	private String distinctimg;
	@Column(columnDefinition = " varchar(2) DEFAULT '0' ")
	private String sendmailflg;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int backnstock;
	@Column(columnDefinition = " varchar(128) DEFAULT NULL ")
	private String specifications;
	private Long ppid;
	private String stitle;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int isopenid;
	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String specpic;
	private String btim;
	private String etim;
	@Column(columnDefinition = " varchar(2) DEFAULT '1' ")
	private String ptyp;//1普通 2撒娇 3预售 4定时开抢 5首购商品 6仅一次商品
	
	@Column(columnDefinition = " varchar(2) DEFAULT '0' ")
	private String newMantype;//1首购商品 2仅一次商品 0普通（不做处理）
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private Double deposit;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int mancnt;
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int stage;
	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String preselltoast;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String rtitle;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String num_iid;
	@Column(columnDefinition = " varchar(128) DEFAULT '' ")
	private String paytim;
	
	@Column
	private String commision_average;	//人均赚取佣金值
	
	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private Integer forbiddenCnt;	//限制购买数量
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String newSku;
	
	@Column(name="is_sync_erp",columnDefinition = " int(4) DEFAULT '2' COMMENT '更新ERP库存：1、<=5置0; 2、<=0置0; 9、不更新'")
	private int isSyncErp;
	@Column(nullable = false, columnDefinition = "decimal(9,2) NOT NULL DEFAULT '0.00'")
	private Double costPrice;//消费税
	@Column(columnDefinition = " int(2) DEFAULT '0' ")
	private Integer isFull;//代言是否已满 0未满，１已满
	

	private String reffer;//来源地址
	
	@Transient
	private ProductUnion proUnion;
	@Transient
	private Long endorsementId;//代言ID
	
	@Transient
	private String zhekou;//显示折扣
	
	@Transient
	private String imgurl;// 商铺banner图片地址
	@Transient
	private String linkurl;//商铺banner连接地址 
	@Transient
	private String typFlag;//商铺banner类型 0：商品 1：banner
	
	
	
	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	public String getLinkurl() {
		return linkurl;
	}

	public void setLinkurl(String linkurl) {
		this.linkurl = linkurl;
	}

	public String getTypFlag() {
		return typFlag;
	}

	public void setTypFlag(String typFlag) {
		this.typFlag = typFlag;
	}

	public String getZhekou() {
		return zhekou;
	}

	public void setZhekou(String zhekou) {
		this.zhekou = zhekou;
	}

	public ProductUnion getProUnion() {
		return proUnion;
	}
	
	public Long getEndorsementId() {
		return endorsementId;
	}

	public void setEndorsementId(Long endorsementId) {
		this.endorsementId = endorsementId;
	}

	public void setProUnion(ProductUnion proUnion) {
		this.proUnion = proUnion;
	}
	public String getReffer() {
		return reffer;
	}
	public void setReffer(String reffer) {
		this.reffer = reffer;
	}
	public Integer getIsFull() {
		return isFull;
	}
	public void setIsFull(Integer isFull) {
		this.isFull = isFull;
	}
	public String getNewMantype() {
		return newMantype;
	}
	public void setNewMantype(String newMantype) {
		this.newMantype = newMantype;
	}
	public String getCommision_average() {
		return commision_average;
	}
	public void setCommision_average(String commision_average) {
		this.commision_average = commision_average;
	}
	public Integer getForbiddenCnt() {
		return forbiddenCnt;
	}
	public void setForbiddenCnt(Integer forbiddenCnt) {
		this.forbiddenCnt = forbiddenCnt;
	}
	public Double getEndorsementPrice() {
		return endorsementPrice;
	}
	public void setEndorsementPrice(Double endorsementPrice) {
		this.endorsementPrice = endorsementPrice;
	}
	public Double getCommision() {
		return commision;
	}
	public void setCommision(Double commision) {
		this.commision = commision;
	}
	public Integer getCommisionTyp() {
		return commisionTyp;
	}
	public void setCommisionTyp(Integer commisionTyp) {
		this.commisionTyp = commisionTyp;
	}
	private String jpntitle;
	private String jpncode;
	private Date wx_upd;
	@Column(columnDefinition = "  varchar(1) DEFAULT '0' ")
	private String wx_flg;
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int stock;
	@Transient
	private int counts;
	private String nationalFlag;
	/*
	 * 物流对象
	 */
	@Transient
	private Fromsite fromobj;
	@Transient
	private String date_txt;
	/*
	 * 是否可代言
	 */
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int isEndorsement;
	
	/*
	 * 代言次数
	 */
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int endorsementCount;
	/*
	 * 代言上限数量
	 */
	@Column(columnDefinition = "  int(11) DEFAULT '0' ")
	private int maxEndorsementCount;
	/*
	 * 代言价格（好友价格）
	 */
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00'")
	private Double	endorsementPrice;
	/*
	 * 佣金
	 */
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00'")
	private Double	commision;
	/*
	 * 佣金类型 1：金额  2：百分比
	 */
	@Column(columnDefinition = " int(5) DEFAULT '1'")
	private Integer commisionTyp; 
	
	/*
	 * 邮费首重500克,每100g续重
	 */
	@Transient
	private Integer postfee;
	
	public Integer getPostfee() {
		return postfee;
	}
	public void setPostfee(Integer postfee) {
		this.postfee = postfee;
	}
	public int getMaxEndorsementCount() {
		return maxEndorsementCount;
	}
	public void setMaxEndorsementCount(int maxEndorsementCount) {
		this.maxEndorsementCount = maxEndorsementCount;
	}
	public int getIsEndorsement() {
		return isEndorsement;
	}
	public void setIsEndorsement(int isEndorsement) {
		this.isEndorsement = isEndorsement;
	}
	public int getEndorsementCount() {
		return endorsementCount;
	}
	public void setEndorsementCount(int endorsementCount) {
		this.endorsementCount = endorsementCount;
	}
	
	public Fromsite getFromobj() {
		return fromobj;
	}
	public void setFromobj(Fromsite fromobj) {
		this.fromobj = fromobj;
	}
	
	
	public String getNationalFlag() {
		return nationalFlag;
	}
	public void setNationalFlag(String nationalFlag) {
		this.nationalFlag = nationalFlag;
	}

	
	public String getDate_txt() {
		return date_txt;
	}
	public void setDate_txt(String date_txt) {
		this.date_txt = date_txt;
	}
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getSkucode() {
		return skucode;
	}
	public void setSkucode(String skucode) {
		this.skucode = skucode;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public String getPaytim() {
		return paytim;
	}
	public void setPaytim(String paytim) {
		this.paytim = paytim;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Double getList_price() {
		return list_price;
	}
	public void setList_price(Double list_price) {
		this.list_price = list_price;
	}
	public int getDiscount() {
		return discount;
	}
	public void setDiscount(int discount) {
		this.discount = discount;
	}
	public int getImgnums() {
		return imgnums;
	}
	public void setImgnums(int imgnums) {
		this.imgnums = imgnums;
	}
	public String getExturl() {
		return exturl;
	}
	public void setExturl(String exturl) {
		this.exturl = exturl;
	}
	public int getSalesrank() {
		return salesrank;
	}
	public void setSalesrank(int salesrank) {
		this.salesrank = salesrank;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public Date getDate_upd() {
		return date_upd;
	}
	public void setDate_upd(Date date_upd) {
		this.date_upd = date_upd;
	}
	public int getNlikes() {
		return nlikes;
	}
	public void setNlikes(int nlikes) {
		this.nlikes = nlikes;
	}
	public int getIshot() {
		return ishot;
	}
	public void setIshot(int ishot) {
		this.ishot = ishot;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getExtcode() {
		return extcode;
	}
	public void setExtcode(String extcode) {
		this.extcode = extcode;
	}
	
	public String getJpntitle() {
		return jpntitle;
	}
	public void setJpntitle(String jpntitle) {
		this.jpntitle = jpntitle;
	}
	public String getJpncode() {
		return jpncode;
	}
	public void setJpncode(String jpncode) {
		this.jpncode = jpncode;
	}
	public int getFromsite() {
		return fromsite;
	}
	public void setFromsite(int fromsite) {
		this.fromsite = fromsite;
	}
	public int getCurrency() {
		return currency;
	}
	public void setCurrency(int currency) {
		this.currency = currency;
	}
	public String getImgstr() {
		return imgstr;
	}
	public void setImgstr(String imgstr) {
		this.imgstr = imgstr;
	}

	public String getListpic() {
		if(StringUtils.isBlank(listpic)){
			return CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/p_e.jpg";
		}
		return StringUtil.getListpic(listpic);
	}
	
	public String getListpic(Long pid) {		
		if(StringUtils.isBlank(listpic)){
			return CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/p_e.jpg";
		}
		return StringUtil.getListpic(listpic);
	}
	public void setListpic(String listpic) {
		this.listpic = listpic;
	}
	public String getAdstr1() {
		return adstr1;
	}
	public void setAdstr1(String adstr1) {
		this.adstr1 = adstr1;
	}
	public String getAdstr3() {
		return adstr3;
	}
	public void setAdstr3(String adstr3) {
		this.adstr3 = adstr3;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public Double getChinaprice() {
		return chinaprice;
	}
	public void setChinaprice(Double chinaprice) {
		this.chinaprice = chinaprice;
	}
	
	public Long getNstock() {
		return nstock;
	}
	public void setNstock(Long nstock) {
		this.nstock = nstock;
	}
	public int getNstock_autoupd() {
		return nstock_autoupd;
	}
	public void setNstock_autoupd(int nstock_autoupd) {
		this.nstock_autoupd = nstock_autoupd;
	}
	public String getIslovely() {
		return islovely;
	}
	public void setIslovely(String islovely) {
		this.islovely = islovely;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public Double getFreight() {
		return freight;
	}
	public void setFreight(Double freight) {
		this.freight = freight;
	}
	public String getWayremark() {
		return wayremark;
	}
	public void setWayremark(String wayremark) {
		this.wayremark = wayremark;
	}
	public int getWishcount() {
		return wishcount;
	}
	public void setWishcount(int wishcount) {
		this.wishcount = wishcount;
	}
	public String getActivityname() {
		return activityname;
	}
	public void setActivityname(String activityname) {
		this.activityname = activityname;
	}
	public String getActivityimage() {
		return activityimage;
	}
	public void setActivityimage(String activityimage) {
		this.activityimage = activityimage;
	}
	public String getPromiseURL() {
		return PromiseURL;
	}
	public void setPromiseURL(String promiseURL) {
		PromiseURL = promiseURL;
	}
	public int getLimitcount() {
		return limitcount;
	}
	public void setLimitcount(int limitcount) {
		this.limitcount = limitcount;
	}
	public Double getLovelydistinct() {
		return lovelydistinct;
	}
	public void setLovelydistinct(Double lovelydistinct) {
		this.lovelydistinct = lovelydistinct;
	}
	public Double getRmbprice() {
		return rmbprice;
	}
	public void setRmbprice(Double rmbprice) {
		this.rmbprice = rmbprice;
	}
	public int getIslockprice() {
		return islockprice;
	}
	public void setIslockprice(int islockprice) {
		this.islockprice = islockprice;
	}
	public String getDistinctimg() {
		return distinctimg;
	}
	public void setDistinctimg(String distinctimg) {
		this.distinctimg = distinctimg;
	}
	public String getSendmailflg() {
		return sendmailflg;
	}
	public void setSendmailflg(String sendmailflg) {
		this.sendmailflg = sendmailflg;
	}
	public int getBacknstock() {
		return backnstock;
	}
	public void setBacknstock(int backnstock) {
		this.backnstock = backnstock;
	}
	public String getSpecifications() {
		return specifications;
	}
	public void setSpecifications(String specifications) {
		this.specifications = specifications;
	}
	
	public Long getPpid() {
		return ppid;
	}
	public void setPpid(Long ppid) {
		this.ppid = ppid;
	}
	public String getStitle() {
		return stitle;
	}
	public void setStitle(String stitle) {
		this.stitle = stitle;
	}
	public int getIsopenid() {
		return isopenid;
	}
	public void setIsopenid(int isopenid) {
		this.isopenid = isopenid;
	}
	public String getSpecpic() {
		String domains=StringUtil.getDomainAPI();
		if(!StringUtils.isBlank(specpic)){
			//return StringUtil.getOSSUrl()+"/"+Configuration.root().getString("oss.upload.specpic.image", "upload/specpic/images/")+specpic;
			return StringUtil.getListpic(specpic);
		}
		else
			return specpic;
	}
	public void setSpecpic(String specpic) {
		this.specpic = specpic;
	}
	public String getBtim() {
		return btim;
	}
	public void setBtim(String btim) {
		this.btim = btim;
	}
	public String getEtim() {
		return etim;
	}
	public void setEtim(String etim) {
		this.etim = etim;
	}
	public String getPtyp() {
		return ptyp;
	}
	public void setPtyp(String ptyp) {
		this.ptyp = ptyp;
	}
	public Double getDeposit() {
		return deposit;
	}
	public void setDeposit(Double deposit) {
		this.deposit = deposit;
	}
	public int getMancnt() {
		return mancnt;
	}
	public void setMancnt(int mancnt) {
		this.mancnt = mancnt;
	}
	public int getStage() {
		return stage;
	}
	public void setStage(int stage) {
		this.stage = stage;
	}
	public String getPreselltoast() {
		return preselltoast;
	}
	public void setPreselltoast(String preselltoast) {
		this.preselltoast = preselltoast;
	}
	public String getRtitle() {
		return rtitle;
	}
	public void setRtitle(String rtitle) {
		this.rtitle = rtitle;
	}
	public String getNum_iid() {
		return num_iid;
	}
	public void setNum_iid(String num_iid) {
		this.num_iid = num_iid;
	}
	public Date getWx_upd() {
		return wx_upd;
	}
	public void setWx_upd(Date wx_upd) {
		this.wx_upd = wx_upd;
	}
	public String getWx_flg() {
		return wx_flg;
	}
	public void setWx_flg(String wx_flg) {
		this.wx_flg = wx_flg;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public String getNewSku() {
		return newSku;
	}
	public void setNewSku(String newSku) {
		this.newSku = newSku;
	}
	public int getIsSyncErp() {
		return isSyncErp;
	}
	public void setIsSyncErp(int isSyncErp) {
		this.isSyncErp = isSyncErp;
	}
	public Double getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(Double costPrice) {
		this.costPrice = costPrice;
	}

}
