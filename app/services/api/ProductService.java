package services.api;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Category;
import models.Currency;
import models.Fromsite;
import models.HotSearchKey;
import models.Parcels;
import models.Product;
import models.ProductDetail;
import models.ProductDetailPram;
import models.ProductGroup;
import models.ProductPriceExt;
import models.ProductUnion;
import models.Product_images;
import models.ShoppingOrder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Configuration;
import play.Logger;
import repositories.CategoryRepository;
import repositories.CurrencyRepository;
import repositories.FromSiteRepository;
import repositories.HotSearchKeynterface;
import repositories.OrderProductRepository;
import repositories.ParcelsRepository;
import repositories.ProductGroupRepository;
import repositories.ProductImageRepository;
import repositories.ProductRepository;
import repositories.ShoppingCartRepository;
import repositories.ShoppingOrderRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.Dates;
import utils.JdbcOper;
import utils.StringUtil;
import vo.channel.ChannelMouldVO.ProductChannelItem;
import vo.endorsment.EnorsmentDetailVO.EndorsementDetailItem;
import vo.product.ProductDetailCostpresellVO.ProductDetailCostpresellItem;
import vo.product.ProductDetailVO.ProductDetailItem;
import vo.product.ProductEndorsementVO.ProductEndorsementItem;
import vo.product.ProductQueryVO;
import vo.product.ProductRecomVO.ProductRecomItem;
import vo.product.ProductSearchMouldDetailVO.ProductSearchMouldDetailItem;
import vo.product.ProductVO;
import vo.shoppingCart.ShoppingCartLovelyVO.ShoppingCartLovelyItem;
import vo.shoppingOrder.ShoppingOrderInfoVO.FromSiteItem;
import vo.shoppingOrder.ShoppingOrderInfoVO.ProductItem;
import vo.shoppingOrder.ShoppingOrderInfoVO.WeightItem;
import vo.shoppingOrder.ShoppingOrderResultVO.PackageProductItem;
import vo.subject.SubjectMouldVO.ProductSubjectItem;
import vo.user.UserGuessLikeMouldVO.ProductGuessLikeItem;
import vo.user.UserlikeMouldVO.ProductUserLikeItem;

import com.google.common.base.Strings;

/**
 * 商品相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
/**
 * @author luobotao
 * @Date 2015年9月10日
 */
@Named
@Singleton
public class ProductService {

    private static final Logger.ALogger logger = Logger.of(ProductService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private ProductRepository productRepository;
    @Inject
    private ShoppingOrderRepository shoppingOrderRepository;
    @Inject
    private ProductGroupRepository productGroupRepository;

    @Inject
    private OrderProductRepository orderProductRepository;
    @Inject
    private FromSiteRepository fromSiteRepository;
    @Inject
    private CurrencyRepository currencyRepository;
    @Inject
    private CategoryRepository categoryRepository;
    @Inject
    private ProductImageRepository productImageRepository;
    @Inject
    private HotSearchKeynterface hotsearchKeyinterface;
    @Inject
    private UserService userService;
    
    @Inject
    private ShoppingCartRepository shoppingCartRepository;
    
    public Product saveProduct(Product pro){
    	return productRepository.save(pro);
    }
    public Product getProductById(Long pid) {
    	if(pid==null || pid.longValue()==0){
    		return null;
    	}
    	Product product = (Product) ServiceFactory.getCacheService().getObject(Constants.product_KEY+pid );//从缓存读入
    	if(product==null){
    		product = productRepository.findOne(pid);
    		if(product == null){
    			return null;
    		}else{
    			if(product.getIshot()!=1 && ("1".equals(product.getPtyp())||"2".equals(product.getPtyp()))){//只写入普通和撒娇商品  预售和定时开抢将不写redis
    	    		ServiceFactory.getCacheService().setObject(Constants.product_KEY+product.getPid(), product, 0);//将商品写入cache
    	    	}
    		}
    	}
        return product;
    }
    
    public Product getStatProductById(Long id,String stat){
    	 return productRepository.queryProductInfoById(id,"10");
    }
    
    public ShoppingOrder getShoppingOrderById(Long id) {
    	return shoppingOrderRepository.findOne(id);
    }
    
    
    /**
     * 根据订单号去获取此订单
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public ShoppingOrder getShoppingOrderByOrderCode(String orderCode) {
    	if(StringUtils.isBlank(orderCode)){
    		return null;
    	}
    	if(orderCode.length()>10){
    		orderCode = orderCode.substring(0, 10);
    	}
    	return shoppingOrderRepository.findByOrderCode(orderCode);
    }
    
    /**
     * 保存订单
     * @param shoppingOrder
     * @return
     */
    @Transactional
    public ShoppingOrder saveShoppingOrder(ShoppingOrder shoppingOrder) {
    	return shoppingOrderRepository.save(shoppingOrder);
    }
    
	@Transactional(readOnly = true)
	public List<Product> queryProductListByParcelsId(Long parcelsId) {
		List<Product> productList = this.productRepository.queryProductListByParcelsId(parcelsId);
		for(Product product:productList){
			product.setCounts(this.productRepository.queryProductCountsInparcel(parcelsId,product.getPid()));
//			product.setRmbprice(productRepository.queryProductPriceInparcel(parcelsId,product.getPid()));
		}
        return productList;
    }
	
    
	@Transactional(readOnly = true)
	public List<Product> queryProductListByIds(Map<Long,String> maps) {
		if(maps.isEmpty()){
			return new ArrayList<Product>();
		}
		List<Long> ids = new ArrayList<Long>();
		for(Long id:maps.keySet()){
			ids.add(id);
		}
		List<Product> productList = this.productRepository.queryProductListByIds(ids);
		for(Product product:productList){
			product.setDate_txt(maps.get(product.getPid()));
		}
        return productList;
    }
	
	/**
	 * @param ppid
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Product> queryProductListByPpId(Long ppid) {
		List<Product> result = new ArrayList<Product>();
		List<Product> productList = this.productRepository.findByPpid(ppid);
		for(Product product:productList){
			if(product.getStatus()!=10){
				product.setNstock(0L);
			}
			result.add(product);
		}
		return result;
	}
	
	
	/**
	 * 获取所有品类
	 * @return
	 */
	public List<Category> getCategoryList() {
		return categoryRepository.findAll();
	}
	
	public boolean productWish(String uid, String pid){
		if(checkIsWish(uid,pid)){
			String sql = "insert into userwish (uid,pid) values("+uid+","+pid+")";
			JdbcOper.updateSql(sql);
			sql = "update product set wishcount=wishcount+1 where pid="+pid;
			JdbcOper.updateSql(sql);
			return true;	
		}else{
			return false;
		}
		
	}
	
	public boolean checkIsWish(String uid,String pid){
		String sql = "select count(*) as count from userwish where uid="+uid+" and pid="+pid;
		int count=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				count = rs.getInt("count");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(count>0){
			return false;
		}else
			return true;
	}
	
	public String checkIsUserlike(String uid, String pid){
		String sql = "SELECT count(*) as count from userlike where uid="+uid+" and pid="+pid;
		int count=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				count = rs.getInt("count");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(count>0){
			return "1";
		}else
			return "0";
	}
	
	public boolean checkIsPresell(String uid, String pid, String orderCode) {
		String sql = "SELECT count(*) as count FROM shopping_Order s,shopping_Order_Pro p WHERE s.id =p.orderId AND s.ordertype = '3' AND p.pid ='"+pid+"' and s.uid='"+uid+"' AND s.status IN ('21','22')";
		if (!StringUtils.isBlank(orderCode))	{
			sql =sql+" and s.orderCode='"+orderCode+"'";
		}
		int count=0;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				count = rs.getInt("count");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(count>0){
			return true;
		}else
			return false;
	}

	public Fromsite queryFnamyByFromSite(int id) {
		
		Fromsite fromsite = (Fromsite) ServiceFactory.getCacheService().getObject(Constants.fromsite_KEY+id );//从缓存读入
    	if(fromsite==null){
    		logger.info("query fromsite form DB========"+id);
    		fromsite = fromSiteRepository.findOne(id);
    		if(fromsite == null){
    			return null;
    		}
    		ServiceFactory.getCacheService().setObject(Constants.fromsite_KEY+id, fromsite,0 );//写入缓存
    	}
        return fromsite;
	}
	public Currency queryCurrencyById(int id) {
		if(id==0){
    		return new Currency();
    	}
		Currency currency = (Currency) ServiceFactory.getCacheService().getObject(Constants.currency_KEY+id );//从缓存读入
    	if(currency==null){
    		logger.info("query currency form DB========"+id);
    		currency = currencyRepository.findOne(id);
    		if(currency == null){
    			return null;
    		}
    		ServiceFactory.getCacheService().setObject(Constants.currency_KEY+id, currency,0 );//写入缓存
    	}
        return currency;
	}

	public ProductDetailItem covertToProductDetailItem(Product productTemp ,ProductService productService,String uid) {
		if(productTemp==null){
			return null;
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		ProductDetailItem productDetailItem = new ProductDetailItem();
		productDetailItem.pid = productTemp.getPid().toString();
	    productDetailItem.pcode=productTemp.getSkucode();                   
	    productDetailItem.title=productTemp.getTitle();                   
	    productDetailItem.subtitle=productTemp.getSubtitle();                
	    productDetailItem.rtitle=productTemp.getRtitle();                  
	    productDetailItem.remark=productTemp.getPreselltoast(); 
	    Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
	    String fname =  fromsite.getName();
	    String fromsitemsg =  "";
		
		if ("嗨个购".equals(fname))
		{
			fromsitemsg=fname+"国内仓发货（5天内到货）";
		}
		else{
			if ("日本亚马逊".equals(fname)){
				fromsitemsg=fname+"发货（20天左右到货）";
			}else{
				fromsitemsg=fname+"发货（15天左右到货）";
			}
		}
	    productDetailItem.fromsite=fromsitemsg;                
	    productDetailItem.fromsiteimg=fromsite.getImg(); 
	    
	    productDetailItem.nationalFlagImg="http://ht.neolix.cn/pimgs/site/"+productTemp.getNationalFlag();
	    
	    Currency currency = queryCurrencyById(productTemp.getCurrency());
	    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
	    productDetailItem.rate= rate.toString();                    
	    productDetailItem.symbol=currency.getSymbol();                  
	    productDetailItem.adstr3=productTemp.getAdstr3();                  
	    productDetailItem.linkurl="pDe://pid="+productTemp.getPid(); 
	    String ratemsg ="";
		switch(productDetailItem.symbol){
			case "$":
			ratemsg ="(汇率1美元兑换成"+productDetailItem.rate+"人民币)";
			break;
			case "円":
			ratemsg ="(汇率1日元兑换成"+productDetailItem.rate+"人民币)";
			break;
			case "₩":
			ratemsg ="(汇率1韩元兑换成"+productDetailItem.rate+"人民币)";
			break;
			case "€":
			ratemsg ="(汇率1欧元兑换成"+productDetailItem.rate+"人民币)";
			break;
		}
	    productDetailItem.ratemsg=ratemsg;   
	    		
	    BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
	    BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
	    BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
	    
	    productDetailItem.chinaprice=chinaprice+"元";
	    if ("円".equals(productDetailItem.symbol.trim())){
	    	productDetailItem.price = price+productDetailItem.symbol.trim();
	    	productDetailItem.list_price = list_price+productDetailItem.symbol.trim();
		}else
		{
			productDetailItem.price = productDetailItem.symbol.trim() + price;
	    	productDetailItem.list_price = productDetailItem.symbol.trim() + list_price;
		}
	    
	    BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		Double weight = productTemp.getWeight();
		Double freight=ShoppingCartService.getfreight(productTemp.getFromsite(),String.valueOf(weight));
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
			productDetailItem.discount_price = (chinaprice.subtract(totalfee))+"元";
		}else{
			productDetailItem.discount_price = "0元";
			productDetailItem.chinaprice="暂无销售";
		}
		if ("0元".equals(productDetailItem.chinaprice))
		{
			productDetailItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productDetailItem.discount =  (discountDouble>10?10:discountDouble )+"";
		
		int nstock = getnstrock(productTemp.getPid());
	    
		
	    productDetailItem.limitcount=productTemp.getLimitcount()+"";            
	    productDetailItem.sta=productTemp.getStatus()+"";
	    
	    Long ppid=productTemp.getPpid();
		String ppskucode="1000"+ppid;
		
		if("2".equals(productTemp.getTyp()))
		{
			 productDetailItem.exturl=""; 
			 productDetailItem.showlogistics="1"; 
		}else{
			productDetailItem.exturl = domain+"/www/exturl.php?pid="+ppskucode;
			productDetailItem.showlogistics="1"; 
		}	
		
	                    
	    productDetailItem.Html5url=domain+"/www/productDetails.php?pid="+ppskucode;   
	    productDetailItem.ShareURL=domain+"/www/details.php?pid="+ppskucode;             
	    productDetailItem.logisticsIntroURL=domain+"/lovely/introduce_logistics.html"; 
	    
	    productDetailItem.PromisePic=productTemp.getTitle(); 
	    
	    productDetailItem.PromiseURL=domain+"/www/wap/index.html";             
	    productDetailItem.nlikes=productTemp.getNlikes()+""; 
	    
	    productDetailItem.islovely=productTemp.getIslovely();                
	    productDetailItem.typ=productTemp.getTyp();//1：代下单 2：自营                   
	    productDetailItem.weight=weight+"Kg";     
	    if (freight>0)
		{
	    	productDetailItem.logisticsFee=freight+"元";
		}else{
			productDetailItem.logisticsFee="包邮";
		}
	    productDetailItem.rmb_price= "¥"+String.valueOf(totalfee);               
	    productDetailItem.rmb_price_no_symbol= String.valueOf(totalfee);               
	    productDetailItem.tariff_fee="0";              
	    productDetailItem.cost_fee="0"; 
	    String wayremark = productTemp.getWayremark();
	    String[] wayremarkArray = wayremark.split("_");
	    if(wayremarkArray!=null && wayremarkArray.length>0){
	    	
	    	productDetailItem.logisticsDesc="由"+wayremarkArray[0]+"发往"+wayremarkArray[1]; 
	    }
	    productDetailItem.specifications=productTemp.getSpecifications();  
	    List<String> imgList = new ArrayList<String>();
	    for(Product_images image :getProductImages(productTemp.getPid())){
	    	imgList.add(image.getFilename());
	    }
	    
	    String imageArray[] = new String[imgList.size()];  
	    productDetailItem.img = imgList.toArray(imageArray);
	    
	    productDetailItem.specpic=StringUtils.isBlank(productTemp.getSpecpic())?"":productTemp.getSpecpic();                 
	    productDetailItem.ppid=String.valueOf(productTemp.getPpid());                   
	    productDetailItem.ptyp=productTemp.getPtyp();                    
	    productDetailItem.btm=productTemp.getBtim();                    
	    productDetailItem.etm=productTemp.getEtim(); 
	    productDetailItem.is_like=this.checkIsUserlike(uid,productTemp.getPid().toString());
		
	    if (nstock>=99999){
	    	productDetailItem.nstock="";
	    }else{
	    	productDetailItem.nstock=String.valueOf(nstock);
	    }
	    if (nstock<=0)
	    {
	    	nstock=0;
	    	productDetailItem.nstock="0";
	    }
	    
		if (productDetailItem.nstock=="")
		{
			productDetailItem.showStockCount="0";
		}else{
			productDetailItem.showStockCount="1";
		}
		
		if ("3".equals(productTemp.getPtyp())){
			productDetailItem.showStockCount="0";
		}
		
		
	    if(!StringUtils.isBlank(productTemp.getBtim())){
	    	Date now = new Date();
	    	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	Date start;
			try {
				start = DATE_FORMAT.parse(productTemp.getBtim());
				productDetailItem.seconds=(start.getTime()-now.getTime())<0?0:(start.getTime()-now.getTime())/1000; 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (productDetailItem.seconds<=0)
			{
				productDetailItem.seconds=0L;
			}
	    }
	    
	    productDetailItem.deposit="¥"+new BigDecimal(productTemp.getDeposit()).setScale(0, BigDecimal.ROUND_CEILING).intValue(); 
	    BigDecimal deposit = new BigDecimal(productTemp.getDeposit()).setScale(0, BigDecimal.ROUND_CEILING);
	    productDetailItem.pay_fee=deposit.toString();
	    productDetailItem.finalpay="¥"+totalfee.subtract(deposit); 
	    if (totalfee.subtract(deposit).intValue()>0)
		{
	    	productDetailItem.toast=productTemp.getPaytim()+"支付尾款\n商品结算尾款时付款¥"+totalfee.subtract(deposit);
		}else{
			productDetailItem.toast="";
		}	
	    productDetailItem.mancnt=String.valueOf(productTemp.getMancnt()); 
	    productDetailItem.stage=String.valueOf(productTemp.getStage()); 
	    return productDetailItem;
	}
	
	public ProductChannelItem covertToProductChannelItem(Product productTemp ,ProductService productService,String uid) {
		if(productTemp==null){
			return null;
		}
		String domain = StringUtil.getPICDomain();
		ProductChannelItem productDetailItem = new ProductChannelItem();
		productDetailItem.pid = productTemp.getPid().toString();
		productDetailItem.pcode=productTemp.getSkucode();                   
		productDetailItem.title=productTemp.getTitle();                   
		productDetailItem.subtitle=productTemp.getSubtitle();                
		productDetailItem.ptyp=productTemp.getPtyp();    
		productDetailItem.islovely=productTemp.getIslovely();
		productDetailItem.nationalFlagImg = domain+"/pimgs/site/"+productTemp.getNationalFlag();
		productDetailItem.ptypeImg = "";
		
		
		Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
		String fname =  fromsite.getName();
		String fromsitemsg =  "";
		
		if ("嗨个购".equals(fname))
		{
			fromsitemsg=fname+"国内仓发货（5天内到货）";
		}
		else{
			if ("日本亚马逊".equals(fname)){
				fromsitemsg=fname+"发货（20天左右到货）";
			}else{
				fromsitemsg=fname+"发货（15天左右到货）";
			}
		}
		
		productDetailItem.fromsite=fromsitemsg;                
		productDetailItem.fromsiteimg=fromsite.getImg(); 
		
		Currency currency = queryCurrencyById(productTemp.getCurrency());
		BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
		productDetailItem.rate= rate.doubleValue();                    
		productDetailItem.symbol=currency.getSymbol();                  
		productDetailItem.adstr3=productTemp.getAdstr3();                  
		productDetailItem.lovelydistinct=String.valueOf(productTemp.getLovelydistinct());                  
		
		BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
		BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()==null?0.0:productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
		BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
		
		//productDetailItem.chinaprice="原价：¥"+chinaprice;
		productDetailItem.chinaprice="";
		if ("円".equals(productDetailItem.symbol.trim())){
			productDetailItem.price = price+productDetailItem.symbol.trim();
			productDetailItem.list_price = list_price+productDetailItem.symbol.trim();
		}else
		{
			productDetailItem.price = productDetailItem.symbol.trim() + price;
			productDetailItem.list_price = productDetailItem.symbol.trim() + list_price;
		}
		
		BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		}
		
		//新人价
		if(userService.checkFirstFlag(uid)){
			ProductPriceExt pe=productService.getProductPrice(productTemp.getPid(),Constants.getSystemGroupOne("newman"),"rmbprice");
			if(pe!=null)
				rmb_price=new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING);
		}
		
		Double weight = productTemp.getWeight();
		Double freight=ShoppingCartService.getfreight(productTemp.getFromsite(),String.valueOf(weight));
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
			productDetailItem.discount_price = (chinaprice.subtract(totalfee))+"元";
		}else{
			productDetailItem.discount_price = "0元";
			productDetailItem.chinaprice="暂无销售";
		}
		if ("0元".equals(productDetailItem.chinaprice))
		{
			productDetailItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productDetailItem.discount =  (discountDouble>10?10:discountDouble )+"";
		int nstock = 0;
		if(productTemp.getIshot()==1){
			nstock = (int) dealNstockWithProduct(productTemp.getPid());
		}else{
			nstock = getnstrock(productTemp.getPid());
		}
		
		productDetailItem.nlikes=productTemp.getNlikes()+""; 
		
		productDetailItem.islovely=productTemp.getIslovely();
		if ("1".equals(productTemp.getIslovely())){
			productDetailItem.ptypeImg = domain+"/pimgs/site/lovely.png";
		}
		productDetailItem.typ=productTemp.getTyp();//1：代下单 2：自营
		if ("2".equals(productTemp.getTyp())){
			// 自营图片
			productDetailItem.typimg = domain+"/pimgs/site/proprietary.png";
		}else{
			productDetailItem.typimg = "";
		}
		if ("3".equals(productTemp.getPtyp())){
			// 预售图片
			productDetailItem.ptypeImg = domain+"/pimgs/site/presell.png";
			productDetailItem.discount = "10";
		}
		productDetailItem.weight=weight+"Kg";     
		if (freight>0)
		{
			productDetailItem.logisticsFee=freight+"元";
		}else{
			productDetailItem.logisticsFee="包邮";
		}
		productDetailItem.rmb_price= "¥"+String.valueOf(totalfee);               
		productDetailItem.rmb_price_no_symbol= String.valueOf(totalfee);               
		productDetailItem.adstr1=productTemp.getAdstr1();
		String wayremark = productTemp.getWayremark();
		String[] wayremarkArray = wayremark.split("_");
		if(wayremarkArray!=null && wayremarkArray.length>0){
			
			productDetailItem.logisticsDesc="由"+wayremarkArray[0]+"发往"+wayremarkArray[1]; 
		}
		List<String> imgList = new ArrayList<String>();
		for(Product_images image :getProductImages(productTemp.getPid())){
			imgList.add(StringUtil.getListpic(image.getFilename()));
		}
		
		productDetailItem.ptyp=productTemp.getPtyp();                    
		productDetailItem.is_like=this.checkIsUserlike(uid,productTemp.getPid().toString());
		
		if (nstock>=99999){
			productDetailItem.nstock="";
		}else{
			productDetailItem.nstock=String.valueOf(nstock);
		}
		if (nstock<=0){
			nstock=0;
			productDetailItem.nstock="0";
			// 售罄图片
			productDetailItem.ptypeImg= domain+"/pimgs/site/sellout.png";
		}
		
		if(!StringUtils.isBlank(productTemp.getBtim())){
			Date now = new Date();
			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date start;
			try {
				start = DATE_FORMAT.parse(productTemp.getBtim());
				productDetailItem.seconds=(start.getTime()-now.getTime())<0?0:(start.getTime()-now.getTime())/1000; 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (productDetailItem.seconds<=0)
			{
				productDetailItem.seconds=0L;
			}
		}
		
		productDetailItem.deposit="订金：¥"+new BigDecimal(productTemp.getDeposit()).setScale(0, BigDecimal.ROUND_CEILING).intValue(); 
		productDetailItem.mancnt="参与人数："+productTemp.getMancnt(); 
		return productDetailItem;
	}
	
	public ProductSubjectItem covertToProductSubjectItem(Product productTemp ,ProductService productService,String uid) {
		if(productTemp==null){
			return null;
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		ProductSubjectItem productDetailItem = new ProductSubjectItem();
		productDetailItem.pid = productTemp.getPid().toString();
		productDetailItem.pcode=productTemp.getSkucode();                   
		productDetailItem.title=productTemp.getTitle();                   
		productDetailItem.subtitle=productTemp.getSubtitle();                
		productDetailItem.ptyp=productTemp.getPtyp();    
		productDetailItem.islovely=productTemp.getIslovely();
		productDetailItem.nationalFlagImg = domain+"/pimgs/site/"+productTemp.getNationalFlag();
		productDetailItem.ptypeImg = "";
	
		if("1".equals(productDetailItem.islovely)){
			productDetailItem.adstr1="撒娇支付";
			productDetailItem.ptypeImg = domain+"/pimgs/site/lovely.png";
		}else{
			productDetailItem.adstr1=productTemp.getAdstr1();
		}
		Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
		String fname =  fromsite.getName();
		String fromsitemsg =  "";
		
		if ("嗨个购".equals(fname))
		{
			fromsitemsg=fname+"国内仓发货（5天内到货）";
		}
		else{
			if ("日本亚马逊".equals(fname)){
				fromsitemsg=fname+"发货（20天左右到货）";
			}else{
				fromsitemsg=fname+"发货（15天左右到货）";
			}
		}
		
		productDetailItem.fromsite=fromsitemsg;                
		productDetailItem.fromsiteimg=fromsite.getImg(); 
		
		Currency currency = queryCurrencyById(productTemp.getCurrency());
		BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
		productDetailItem.rate= rate.doubleValue();                    
		productDetailItem.symbol=currency.getSymbol();                  
		productDetailItem.adstr3=productTemp.getAdstr3();                  
		productDetailItem.lovelydistinct=String.valueOf(productTemp.getLovelydistinct());                  
		
		BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
		BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
		BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
		
		productDetailItem.chinaprice="原价：¥"+chinaprice;
		if ("円".equals(productDetailItem.symbol.trim())){
			productDetailItem.price = price+productDetailItem.symbol.trim();
			productDetailItem.list_price = list_price+productDetailItem.symbol.trim();
		}else
		{
			productDetailItem.price = productDetailItem.symbol.trim() + price;
			productDetailItem.list_price = productDetailItem.symbol.trim() + list_price;
		}
		
		BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		//新人价
		if(userService.checkFirstFlag(uid)){
			ProductPriceExt pe=productService.getProductPrice(productTemp.getPid(),Constants.getSystemGroupOne("newman"),"rmbprice");
			if(pe!=null)
				rmb_price=new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING);
		}
		Double weight = productTemp.getWeight();
		Double freight=ShoppingCartService.getfreight(productTemp.getFromsite(),String.valueOf(weight));
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
			productDetailItem.discount_price = (chinaprice.subtract(totalfee))+"元";
		}else{
			productDetailItem.discount_price = "0元";
			productDetailItem.chinaprice="暂无销售";
		}
		if ("0元".equals(productDetailItem.chinaprice))
		{
			productDetailItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productDetailItem.discount =  (discountDouble>10?10:discountDouble )+"";
		
		int nstock = 0;
		if(productTemp.getIshot()==1){
			nstock = (int) dealNstockWithProduct(productTemp.getPid());
		}else{
			nstock = getnstrock(productTemp.getPid());
		}
		
		if (nstock<=0){
			nstock=0;
			// 售罄图片
			productDetailItem.ptypeImg= domain+"/pimgs/site/sellout.png";
		}
		productDetailItem.nlikes=productTemp.getNlikes()+""; 
		
		productDetailItem.islovely=productTemp.getIslovely();                
		productDetailItem.typ=productTemp.getTyp();//1：代下单 2：自营
		
		if ("2".equals(productTemp.getTyp())){
			// 自营图片
			productDetailItem.typimg = domain+"/pimgs/site/proprietary.png";
		}else{
			productDetailItem.typimg = "";
		}
		
		if ("3".equals(productTemp.getPtyp())){
			// 预售图片
			productDetailItem.ptypeImg = domain+"/pimgs/site/presell.png";
			productDetailItem.discount = "10";
		}
		
		
		productDetailItem.weight=weight+"Kg";     
		if (freight>0)
		{
			productDetailItem.logisticsFee=freight+"元";
		}else{
			productDetailItem.logisticsFee="包邮";
		}
		productDetailItem.rmb_price= "¥"+String.valueOf(totalfee);               
		productDetailItem.rmb_price_no_symbol= String.valueOf(totalfee);               
		
		String wayremark = productTemp.getWayremark();
		String[] wayremarkArray = wayremark.split("_");
		if(wayremarkArray!=null && wayremarkArray.length>0){
			
			productDetailItem.logisticsDesc="由"+wayremarkArray[0]+"发往"+wayremarkArray[1]; 
		}
		List<String> imgList = new ArrayList<String>();
		for(Product_images image :getProductImages(productTemp.getPid())){
			imgList.add(StringUtil.getListpic(image.getFilename()));
		}
		
		productDetailItem.ptyp=productTemp.getPtyp();                    
		productDetailItem.is_like=this.checkIsUserlike(uid,productTemp.getPid().toString());
		
		if (nstock>=99999){
			productDetailItem.nstock="";
		}else{
			productDetailItem.nstock=String.valueOf(nstock);
		}
		
		
		if(!StringUtils.isBlank(productTemp.getBtim())){
			Date now = new Date();
			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date start;
			try {
				start = DATE_FORMAT.parse(productTemp.getBtim());
				productDetailItem.seconds=(start.getTime()-now.getTime())<0?0:(start.getTime()-now.getTime())/1000; 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (productDetailItem.seconds<=0)
			{
				productDetailItem.seconds=0L;
			}
		}
		
		productDetailItem.deposit="订金：¥"+new BigDecimal(productTemp.getDeposit()).setScale(0, BigDecimal.ROUND_CEILING).intValue(); 
		productDetailItem.mancnt="参与人数："+productTemp.getMancnt(); 
		return productDetailItem;
	}
	
	/**
	 * 根据pid去获取库存
	 * @param pid
	 * @return
	 */
	private Integer getnstrock(Long pid) {
		Integer result=0;
		String sql = "SELECT SUM(nstock) AS count FROM product WHERE ppid IN  (SELECT ppid FROM product WHERE pid = '"+pid+"')";// SQL语句
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt("count");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public ProductGuessLikeItem covertToProductGuessLikeItem(Product productTemp ,ProductService productService,String uid) {
		if(productTemp==null){
			return null;
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		ProductGuessLikeItem productGuessItem = new ProductGuessLikeItem();
		
		productGuessItem.pid = productTemp.getPid().toString();
		productGuessItem.pcode=productTemp.getSkucode();                   
		productGuessItem.title=productTemp.getTitle();                   
		productGuessItem.subtitle=productTemp.getSubtitle();                
		productGuessItem.exturl=productTemp.getExturl(); 
		productGuessItem.reffer=productTemp.getReffer();
		
		BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
		BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
		BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
		Currency currency = queryCurrencyById(productTemp.getCurrency());
		productGuessItem.chinaprice="¥"+chinaprice;
		if ("円".equals(currency.getSymbol().trim())){
			productGuessItem.price = price+currency.getSymbol().trim();
			productGuessItem.list_price = list_price+currency.getSymbol().trim();
		}else
		{
			productGuessItem.price = currency.getSymbol().trim() + price;
			productGuessItem.list_price = currency.getSymbol().trim() + list_price;
		}
		
		BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
		productGuessItem.rate= rate.toString();  
		Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
		String fromsitemsg =  fromsite.getName();
		productGuessItem.fromsite=fromsitemsg;                
		productGuessItem.fromsiteimg=fromsite.getImg(); 
		                  
		productGuessItem.adstr1=productTemp.getAdstr1();                  
		productGuessItem.linkurl="pDe://pid="+productTemp.getPid()+"&lmt="+(productTemp.getDate_upd()==null?0L:productTemp.getDate_upd().getTime()); 
		
		if ("3".equals(productTemp.getPtyp()))
		{
			productGuessItem.linkurl = "presellDetail://pid="+productTemp.getPid();
		}
		
		
		BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
		}else{
			productGuessItem.chinaprice="暂无销售";
		}
		if ("¥0".equals(productGuessItem.chinaprice))
		{
			productGuessItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productGuessItem.discount =  (discountDouble>10?10:discountDouble )+"";
		
		int nstock = getnstrock(productTemp.getPid());
		
		Long ppid=productTemp.getPpid();
		String ppskucode="1000"+ppid;
		
		if("2".equals(productTemp.getTyp()))
		{
			productGuessItem.exturl=""; 
		}else{
			productGuessItem.exturl = domain+"/www/exturl.php?pid="+ppskucode;
		}
		productGuessItem.nlikes=productTemp.getNlikes()+""; 
		productGuessItem.rmb_price_no_symbol= String.valueOf(totalfee);               
		productGuessItem.rmb_price= "¥"+String.valueOf(totalfee);               
		productGuessItem.img_src = productTemp.getListpic();
		productGuessItem.is_like=this.checkIsUserlike(uid,productTemp.getPid().toString());
		productGuessItem.date_txt = productTemp.getDate_txt();
		if (nstock>=99999){
			productGuessItem.nstock="";
		}else{
			productGuessItem.nstock=String.valueOf(nstock);
		}
		return productGuessItem;
	}
	
	public ProductVO.ProductItem covertToProductItem(Product productTemp ,ProductService productService,String uid) {
		if(productTemp==null){
			return null;
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn:9004");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		String domainshare = Configuration.root().getString("domain.dev","http://ht2.neolix.cn:9004");
		if(IsProduct){
			domainshare = "http://h5.higegou.com";
		}
		ProductVO.ProductItem productItem = new ProductVO.ProductItem();
		productItem.pid = productTemp.getPid().toString();
	    productItem.pcode=productTemp.getSkucode();                   
	    productItem.title=productTemp.getTitle();                   
	    productItem.subtitle=productTemp.getSubtitle();                
	    productItem.rtitle=productTemp.getRtitle();                  
	    productItem.remark=productTemp.getPreselltoast(); 
	    Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
	    String fname =  fromsite.getName();
	    String fromsitemsg =  "";
		
	    int adminid=0;
	    String sql="SELECT * FROM `adminproduct` WHERE pid="+productTemp.getPid();
	    JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				adminid = rs.getInt("adminid");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(adminid>0){
			fname = "嗨个购国内仓";
		}
		if ("嗨个购".equals(fname))
		{
			fromsitemsg=fname+"国内仓发货（5天内到货）";
		}
		else{
			if ("日本亚马逊".equals(fname)){
				fromsitemsg=fname+"发货（20天左右到货）";
			}else{
				fromsitemsg=fname+"发货（15天左右到货）";
			}
		}
		
	    productItem.fromsite=fromsitemsg;                
	    productItem.fromsiteimg=fromsite.getImg(); 
	    productItem.nationalFlagImg="http://ht.neolix.cn/pimgs/site/"+productTemp.getNationalFlag();
	    
	    Currency currency = queryCurrencyById(productTemp.getCurrency());
	    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
	    productItem.rate= rate.toString();                    
	    productItem.symbol=currency.getSymbol();                  
	    productItem.adstr3=productTemp.getAdstr3();
	    if ("3".equals(productTemp.getPtyp()))
		{
	    	productItem.linkurl = "presellDetail://pid="+productTemp.getPid();
		}else{
			productItem.linkurl = "pDe://pid="+productTemp.getPid();
		}
	    //productItem.linkurl="pDe://pid="+ String.valueOf(productTemp.getPid());
	    String ratemsg ="";
		switch(productItem.symbol){
			case "$":
			ratemsg ="(汇率1美元兑换成"+productItem.rate+"人民币)";
			break;
			case "円":
			ratemsg ="(汇率1日元兑换成"+productItem.rate+"人民币)";
			break;
			case "₩":
			ratemsg ="(汇率1韩元兑换成"+productItem.rate+"人民币)";
			break;
			case "€":
			ratemsg ="(汇率1欧元兑换成"+productItem.rate+"人民币)";
			break;
		}
	    productItem.ratemsg=ratemsg;   
	    		
	    BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
	    BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()==null?0.0:productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
	    BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
	    
	    productItem.chinaprice="¥"+chinaprice;
	    if ("円".equals(productItem.symbol.trim())){
	    	productItem.price = price+productItem.symbol.trim();
	    	productItem.list_price = list_price+productItem.symbol.trim();
		}else
		{
			productItem.price = productItem.symbol.trim() + price;
	    	productItem.list_price = productItem.symbol.trim() + list_price;
		}
	    
	    BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		//新人价
		if(userService.checkFirstFlag(uid)){
			ProductPriceExt pe=productService.getProductPrice(productTemp.getPid(),Constants.getSystemGroupOne("newman"),"rmbprice");
			if(pe!=null)
				rmb_price=new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING);
		}
		Double weight = productTemp.getWeight();
		Double freight=ShoppingCartService.getfreight(productTemp.getFromsite(),String.valueOf(weight));
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
			productItem.discount_price = (chinaprice.subtract(totalfee))+"元";
		}else{
			productItem.discount_price = "0元";
			productItem.chinaprice="暂无销售";
		}
		if ("0元".equals(productItem.chinaprice))
		{
			productItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productItem.discount =  (discountDouble>10?10:discountDouble )+"";
		
		int nstock = 0;
		if(productTemp.getIshot()==1){
			nstock = (int) dealNstockWithProduct(productTemp.getPid());
		}else{
			//nstock = getnstrock(productTemp.getPid());
			nstock = productTemp.getNstock().intValue();
		}
		
		
	    productItem.limitcount=productTemp.getLimitcount()+"";            
	    productItem.sta=productTemp.getStatus()+"";
	    
	    Long ppid=productTemp.getPpid();
		String ppskucode="1000"+ppid;
		
		if("2".equals(productTemp.getTyp()))
		{
			 productItem.exturl=""; 
			 productItem.showlogistics="1"; 
		}else{
			// productItem.exturl = domain+"/www/exturl.php?pid="+ppskucode;
			productItem.exturl="";
			productItem.showlogistics="1"; 
		}	
		
	                    
	    productItem.Html5url=domain+"/www/productDetails.php?pid="+ppskucode;   
	    //productItem.ShareURL=domain+"/www/details.php?pid="+ppskucode;      
	    productItem.ShareURL=domainshare+"/H5/pro?pid="+productTemp.getPid();
	    
	    productItem.logisticsIntroURL=domain+"/lovely/introduce_logistics.html"; 
	    
	    productItem.PromisePic=productTemp.getTitle(); 
	    
	    productItem.PromiseURL=domain+"/www/wap/index.html";             
	    productItem.nlikes=productTemp.getNlikes()+""; 
	    
	    productItem.islovely=productTemp.getIslovely();                
	    productItem.typ=productTemp.getTyp();//1：代下单 2：自营                   
	    productItem.weight=weight+"Kg";     
	    if (freight>0)
		{
	    	productItem.logisticsFee=freight+"元";
		}else{
			productItem.logisticsFee="包邮";
		}
	    productItem.rmb_price= "¥"+String.valueOf(totalfee);               
	    productItem.rmb_price_no_symbol= String.valueOf(totalfee);               
	    productItem.tariff_fee="0";              
	    productItem.cost_fee="0"; 
	    
	    String wayremark = productTemp.getWayremark();
	    String[] wayremarkArray = wayremark.split("_");
	    if(wayremarkArray!=null && wayremarkArray.length>1){
	    	
	    	productItem.logisticsDesc="由"+wayremarkArray[0]+"发往"+wayremarkArray[1]; 
	    }
		if ("1".equals(productItem.islovely))
		{
			String lovelydistinct = String.valueOf(productTemp.getLovelydistinct());
			productItem.lovelydistinct=lovelydistinct;
			if (".0".equals(lovelydistinct.substring(lovelydistinct.length()-2, lovelydistinct.length())))
			{
				productItem.discountActivityName="撒娇再享"+lovelydistinct.substring(0, lovelydistinct.length()-2)+"折";
			}else{
				productItem.discountActivityName="撒娇再享"+lovelydistinct+"折";
			}
			productItem.discountActivityImage=domain+"/pimgs/site/Boff.png"; 
		}	
	    productItem.specifications=productTemp.getSpecifications();  
	    List<String> imgList = new ArrayList<String>();
	    for(Product_images image :getProductImages(productTemp.getPid())){
	    	imgList.add(image.getFilename());
	    }
	    
	    String imageArray[] = new String[imgList.size()];  
	    productItem.img = imgList.toArray(imageArray);
	    
	    productItem.specpic=StringUtils.isBlank(productTemp.getSpecpic())?"":productTemp.getSpecpic();                 
	    productItem.ppid=String.valueOf(productTemp.getPpid());                   
	    productItem.ptyp=productTemp.getPtyp();                    
	    productItem.btm=productTemp.getBtim();                    
	    productItem.etm=productTemp.getEtim(); 
	    productItem.is_like=this.checkIsUserlike(uid,productTemp.getPid().toString());
		
	    if (nstock>=99999){
	    	productItem.nstock="";
	    }else{
	    	if(nstock<0){
	    		nstock=0;
	    	}
	    	productItem.nstock=String.valueOf(nstock);
	    }
	    
		if (productItem.nstock=="")
		{
			productItem.showStockCount="0";
		}else{
			productItem.showStockCount="1";
		}
		
		if ("3".equals(productTemp.getPtyp())){
			productItem.showStockCount="0";
		}
		
		
	    if(!StringUtils.isBlank(productTemp.getBtim())){
	    	Date now = new Date();
	    	SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	Date start;
			try {
				start = DATE_FORMAT.parse(productTemp.getBtim());
				productItem.seconds=(start.getTime()-now.getTime())<0?0:(start.getTime()-now.getTime())/1000; 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (productItem.seconds<=0)
			{
				productItem.seconds=0L;
			}
	    }
	    
	    return productItem;
	}
	
	public ProductDetailCostpresellItem covertToProductDetailCostPresellItem(Product productTemp ,ProductService productService,String uid) {
		if(productTemp==null){
			return null;
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		ProductDetailCostpresellItem productDetailItem = new ProductDetailCostpresellItem();
		productDetailItem.pid = productTemp.getPid().toString();
	    productDetailItem.pcode=productTemp.getSkucode();                   
	    productDetailItem.title=productTemp.getTitle();                   
	    productDetailItem.subtitle=productTemp.getSubtitle();                
	    productDetailItem.rtitle=productTemp.getRtitle();                  
	    productDetailItem.remark=productTemp.getPreselltoast(); 
	    Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
	    String fname =  fromsite.getName();
	    String fromsitemsg =  "";
		
		if ("嗨个购".equals(fname))
		{
			fromsitemsg=fname+"国内仓发货（5天内到货）";
		}
		else{
			if ("日本亚马逊".equals(fname)){
				fromsitemsg=fname+"发货（20天左右到货）";
			}else{
				fromsitemsg=fname+"发货（15天左右到货）";
			}
		}
		
	    productDetailItem.fromsite=fromsitemsg;                
	    productDetailItem.fromsiteimg=fromsite.getImg(); 
	    
	    Currency currency = queryCurrencyById(productTemp.getCurrency());
	    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
	    productDetailItem.rate= rate.toString();                    
	    productDetailItem.symbol=currency.getSymbol();                  
	    productDetailItem.linkurl="presellDetail://pid="+productTemp.getPid(); 
	    		
	    BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
	    BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
	    BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
	    
	    productDetailItem.chinaprice=chinaprice+"元";
	    if ("円".equals(productDetailItem.symbol.trim())){
	    	productDetailItem.price = price+productDetailItem.symbol.trim();
	    	productDetailItem.list_price = list_price+productDetailItem.symbol.trim();
		}else
		{
			productDetailItem.price = productDetailItem.symbol.trim() + price;
	    	productDetailItem.list_price = productDetailItem.symbol.trim() + list_price;
		}
	    
	    BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		//新人价
		if(userService.checkFirstFlag(uid)){
			ProductPriceExt pe=productService.getProductPrice(productTemp.getPid(),Constants.getSystemGroupOne("newman"),"rmbprice");
			if(pe!=null)
				rmb_price=new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING);
		}
		
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
			productDetailItem.discount_price = (chinaprice.subtract(totalfee))+"元";
		}else{
			productDetailItem.discount_price = "0元";
			productDetailItem.chinaprice="暂无销售";
		}
		if ("0元".equals(productDetailItem.chinaprice))
		{
			productDetailItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productDetailItem.discount =  (discountDouble>10?10:discountDouble )+"";
		
		if("3".equals(productTemp.getPtyp())){
			productDetailItem.discount ="10";
		}
	    productDetailItem.rmb_price="¥"+String.valueOf(totalfee);               
	    productDetailItem.rmb_price_no_symbol=String.valueOf(totalfee);               
	    productDetailItem.rmbprice=String.valueOf(totalfee);               
	    
	    productDetailItem.specifications=productTemp.getSpecifications();  
	    List<String> imgList = new ArrayList<String>();
	    for(Product_images image :getProductImages(productTemp.getPid())){
	    	imgList.add(image.getFilename());
	    }
	    
	    productDetailItem.img = imgList.size()>0?imgList.get(0):"";
	    productDetailItem.paytim=productTemp.getPaytim();  
	    productDetailItem.adstr1=productTemp.getAdstr1();  
	    
	    productDetailItem.deposit=  "¥"+new BigDecimal(productTemp.getDeposit()).setScale(0, BigDecimal.ROUND_CEILING).intValue(); 
	    BigDecimal deposit = new BigDecimal(productTemp.getDeposit()).setScale(0, BigDecimal.ROUND_CEILING);
	    productDetailItem.pay_fee=deposit.toString();
	    productDetailItem.finalpay=totalfee.subtract(deposit).toString(); 
	    return productDetailItem;
	}
	public ProductUserLikeItem covertToProductUserLikeItem(Product productTemp ,ProductService productService,String uid,String DATE_ADD) {
		if(productTemp==null){
			return null;
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		ProductUserLikeItem productDetailItem = new ProductUserLikeItem();
		productDetailItem.pid = productTemp.getPid().toString();
		productDetailItem.skucode=productTemp.getSkucode();                   
		productDetailItem.title=productTemp.getTitle();                   
		productDetailItem.subtitle=productTemp.getSubtitle();  
		productDetailItem.category = String.valueOf(productTemp.getCategory());
		productDetailItem.imgnums = String.valueOf(productTemp.getImgnums());
		productDetailItem.salesrank=String.valueOf(productTemp.getSalesrank());
		productDetailItem.status=String.valueOf(productTemp.getStatus());
		productDetailItem.rtitle=productTemp.getRtitle();
		productDetailItem.DATE_ADD=DATE_ADD;
		productDetailItem.date_add=productTemp.getDate_add()==null?"":CHINESE_DATE_TIME_FORMAT.format(productTemp.getDate_add());
		productDetailItem.date_upd=productTemp.getDate_upd()==null?"":CHINESE_DATE_TIME_FORMAT.format(productTemp.getDate_upd());
		productDetailItem.ishot=String.valueOf(productTemp.getIshot());
		productDetailItem.version=String.valueOf(productTemp.getVersion());
		productDetailItem.extcode=String.valueOf(productTemp.getExtcode());
		productDetailItem.fromsite=String.valueOf(productTemp.getFromsite());
		productDetailItem.currency=String.valueOf(productTemp.getCurrency());
		productDetailItem.imgstr=productTemp.getImgstr();
		productDetailItem.listpic=productTemp.getListpic();
		productDetailItem.adstr1=productTemp.getAdstr1();
		productDetailItem.adstr3=productTemp.getAdstr3();
		productDetailItem.detail=productTemp.getDetail();
		productDetailItem.sort=String.valueOf(productTemp.getSort());
		productDetailItem.nstock_autoupd=String.valueOf(productTemp.getNstock_autoupd());
		
		productDetailItem.weight=String.valueOf(productTemp.getWeight()); 
		productDetailItem.freight=String.valueOf(productTemp.getFreight()); 
		productDetailItem.wayremark=productTemp.getWayremark(); 
		productDetailItem.wishcount=String.valueOf(productTemp.getWishcount()); 
		productDetailItem.activityname=productTemp.getActivityname(); 
		productDetailItem.activityimage=productTemp.getActivityimage(); 
		productDetailItem.PromiseURL=productTemp.getPromiseURL(); 
		productDetailItem.limitcount=String.valueOf(productTemp.getLimitcount());            
		productDetailItem.lovelydistinct=String.valueOf(productTemp.getLovelydistinct());            
		productDetailItem.rmbprice=String.valueOf(productTemp.getRmbprice());            
		productDetailItem.islockprice=String.valueOf(productTemp.getIslockprice());            
		productDetailItem.distinctimg=productTemp.getDistinctimg(); 
		productDetailItem.sendmailflg=productTemp.getSendmailflg();            
		productDetailItem.backnstock=String.valueOf(productTemp.getBacknstock());            
		productDetailItem.specifications=productTemp.getSpecifications();            
		productDetailItem.stitle=productTemp.getStitle();            
		productDetailItem.isopenid=String.valueOf(productTemp.getIsopenid());            
		productDetailItem.preselltoast=productTemp.getPreselltoast();            
		productDetailItem.num_iid=productTemp.getNum_iid();            
		productDetailItem.wx_upd=productTemp.getWx_upd()==null?"":CHINESE_DATE_TIME_FORMAT.format(productTemp.getWx_upd());         
		productDetailItem.stock=String.valueOf(productTemp.getStock());            
		productDetailItem.paytim=productTemp.getPaytim();            
		productDetailItem.jpntitle=productTemp.getJpntitle();            
		productDetailItem.jpncode=productTemp.getJpncode();    
		productDetailItem.nationalFlagImg = domain+"/pimgs/site/"+productTemp.getNationalFlag();
		productDetailItem.ptypeImg = "";
		
		if ("3".equals(productTemp.getPtyp())){
			// 预售图片
			productDetailItem.ptypeImg = domain+"/pimgs/site/presell.png";
		}
		
		if ("1".equals(productTemp.getIslovely())){
			productDetailItem.ptypeImg = domain+"/pimgs/site/lovely.png";
		}
		productDetailItem.typ=productTemp.getTyp();//1：代下单 2：自营
		if ("2".equals(productTemp.getTyp())){
			// 自营图片
			productDetailItem.typimg = domain+"/pimgs/site/proprietary.png";
		}else{
			productDetailItem.typimg = "";
		}
		
		Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
		
		
		
		String fname =  fromsite.getName();
		String fromsitemsg =  "";
		
		if ("嗨个购".equals(fname))
		{
			fromsitemsg=fname+"国内仓发货（5天内到货）";
		}
		else{
			if ("日本亚马逊".equals(fname)){
				fromsitemsg=fname+"发货（20天左右到货）";
			}else{
				fromsitemsg=fname+"发货（15天左右到货）";
			}
		}
		
		productDetailItem.fromsitename=fname;                
		productDetailItem.fromsiteimg=fromsite.getImg(); 
		
		Currency currency = queryCurrencyById(productTemp.getCurrency());
		BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
		productDetailItem.rate= rate.toString();                    
		productDetailItem.symbol=currency.getSymbol();                  
		productDetailItem.adstr3=productTemp.getAdstr3();                  
		String ratemsg ="";
		switch(productDetailItem.symbol){
		case "$":
			ratemsg ="(汇率1美元兑换成"+productDetailItem.rate+"人民币)";
			break;
		case "円":
			ratemsg ="(汇率1日元兑换成"+productDetailItem.rate+"人民币)";
			break;
		case "₩":
			ratemsg ="(汇率1韩元兑换成"+productDetailItem.rate+"人民币)";
			break;
		case "€":
			ratemsg ="(汇率1欧元兑换成"+productDetailItem.rate+"人民币)";
			break;
		}
		
		BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
		BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
		BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
		
		productDetailItem.chinaprice="原价：¥"+chinaprice;
		if ("円".equals(productDetailItem.symbol.trim())){
			productDetailItem.price = price+productDetailItem.symbol.trim();
			productDetailItem.list_price = list_price+productDetailItem.symbol.trim();
		}else
		{
			productDetailItem.price = productDetailItem.symbol.trim() + price;
			productDetailItem.list_price = productDetailItem.symbol.trim() + list_price;
		}
		
		BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		Double weight = productTemp.getWeight();
//		Double freight=ShoppingCartService.getfreight(productTemp.getFromsite(),String.valueOf(weight));
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
		}else{
			productDetailItem.chinaprice="暂无销售";
		}
		if ("0元".equals(productDetailItem.chinaprice))
		{
			productDetailItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productDetailItem.discount =  (discountDouble>10?10:discountDouble )+"";
		
		int nstock = getnstrock(productTemp.getPid());
		
		Long ppid=productTemp.getPpid();
		String ppskucode="1000"+ppid;
		
		if("2".equals(productTemp.getTyp()))
		{
			productDetailItem.exturl=""; 
		}else{
			productDetailItem.exturl = domain+"/www/exturl.php?pid="+ppskucode;
		}	
		
		
		productDetailItem.PromiseURL=domain+"/www/wap/index.html";             
		productDetailItem.nlikes=productTemp.getNlikes()+""; 
		
		productDetailItem.islovely=productTemp.getIslovely();                
		productDetailItem.typ=productTemp.getTyp();//1：代下单 2：自营                   
		productDetailItem.weight=weight+"Kg";     
		productDetailItem.rmb_price_no_symbol=totalfee.toString();               
		productDetailItem.rmb_price="¥"+totalfee;               
		/*productDetailItem.tariff_fee="0";              
		productDetailItem.cost_fee="0"; */
		
//		String wayremark = productTemp.getWayremark();
//		String[] wayremarkArray = wayremark.split("_");
		/*if(wayremarkArray!=null && wayremarkArray.length>0){
			
			productDetailItem.logisticsDesc="由"+wayremarkArray[0]+"发往"+wayremarkArray[1]; 
		}
		if ("1".equals(productDetailItem.islovely))
		{
			String lovelydistinct = String.valueOf(productTemp.getLovelydistinct());
			if (".0".equals(lovelydistinct.substring(lovelydistinct.length()-2, lovelydistinct.length())))
			{
				productDetailItem.discountActivityName="撒娇再享"+lovelydistinct.substring(0, lovelydistinct.length()-2)+"折";
			}else{
				productDetailItem.discountActivityName="撒娇再享"+lovelydistinct+"折";
			}
			productDetailItem.discountActivityImage=domain+"/pimgs/site/Boff.png"; 
		}	*/
		List<String> imgList = new ArrayList<String>();
		for(Product_images image :getProductImages(productTemp.getPid())){
			imgList.add(StringUtil.getListpic(image.getFilename()));
		}
		
		productDetailItem.img_src = productTemp.getListpic();
		
		productDetailItem.specpic=StringUtils.isBlank(productTemp.getSpecpic())?"":productTemp.getSpecpic();                 
		productDetailItem.ppid=String.valueOf(productTemp.getPpid());                   
		productDetailItem.ptyp=productTemp.getPtyp();                    
		productDetailItem.btim=productTemp.getBtim();                    
		productDetailItem.etim=productTemp.getEtim(); 
		
		if (nstock>=99999){
			productDetailItem.nstock="";
		}else{
			productDetailItem.nstock=String.valueOf(nstock);
		}
		
		if(nstock<=0){
			nstock=0;
			productDetailItem.nstock="0";
			// 售罄图片
			productDetailItem.ptypeImg= domain+"/pimgs/site/sellout.png";
		}
		
		if(!StringUtils.isBlank(productTemp.getBtim())){
			Date now = new Date();
			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date start;
			try {
				start = DATE_FORMAT.parse(productTemp.getBtim());
				productDetailItem.seconds=(start.getTime()-now.getTime())<0?0:(start.getTime()-now.getTime())/1000; 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (productDetailItem.seconds<=0)
			{
				productDetailItem.seconds=0L;
			}
		}
		
		productDetailItem.deposit="订金：¥"+new BigDecimal(productTemp.getDeposit()).setScale(0, BigDecimal.ROUND_CEILING).intValue(); 
		productDetailItem.mancnt=String.valueOf(productTemp.getMancnt()); 
		productDetailItem.stage=String.valueOf(productTemp.getStage()); 
		return productDetailItem;
	}
	public ProductSearchMouldDetailItem covertToProductSearchMouldItem(Product productTemp ,ProductService productService,String uid) {
		if(productTemp==null){
			return null;
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		
		ProductSearchMouldDetailItem productDetailItem = new ProductSearchMouldDetailItem();
		productDetailItem.pid = productTemp.getPid().toString();
		productDetailItem.pcode=productTemp.getSkucode();                   
		productDetailItem.title=productTemp.getTitle();                   
		productDetailItem.subtitle=productTemp.getSubtitle();
		productDetailItem.nationalFlagImg = domain+"/pimgs/site/"+productTemp.getNationalFlag();
		productDetailItem.ptypeImg = "";
		if ("3".equals(productTemp.getPtyp())){
			// 预售图片
			productDetailItem.ptypeImg = domain+"/pimgs/site/presell.png";
		}
		Fromsite fromsite = queryFnamyByFromSite(productTemp.getFromsite());
		String fname =  fromsite.getName();
		String fromsitemsg =  "";
		if ("嗨个购".equals(fname))
		{
			fromsitemsg=fname+"国内仓发货（5天内到货）";
		}
		else{
			if ("日本亚马逊".equals(fname)){
				fromsitemsg=fname+"发货（20天左右到货）";
			}else{
				fromsitemsg=fname+"发货（15天左右到货）";
			}
		}
		productDetailItem.fromsite=fromsitemsg;                
		productDetailItem.fromsiteimg=fromsite.getImg(); 
		
		Currency currency = queryCurrencyById(productTemp.getCurrency());
		BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
		productDetailItem.rate= rate.toString();                    
		productDetailItem.symbol=currency.getSymbol();                  
		productDetailItem.adstr1=productTemp.getAdstr1();                  
		
		BigDecimal	price=	new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);              
		BigDecimal	chinaprice =new BigDecimal(productTemp.getChinaprice()).setScale(0,  BigDecimal.ROUND_CEILING);                
		BigDecimal list_price = new BigDecimal(productTemp.getList_price()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
		
		productDetailItem.chinaprice="¥"+chinaprice;
		if ("円".equals(productDetailItem.symbol.trim())){
			productDetailItem.price = price+productDetailItem.symbol.trim();
			productDetailItem.list_price = list_price+productDetailItem.symbol.trim();
		}else
		{
			productDetailItem.price = productDetailItem.symbol.trim() + price;
			productDetailItem.list_price = productDetailItem.symbol.trim() + list_price;
		}
		
		BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (productTemp.getIslockprice()==1) {
			rmb_price = new BigDecimal(productTemp.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		//新人价
		if(userService.checkFirstFlag(uid)){
			ProductPriceExt pe=productService.getProductPrice(productTemp.getPid(),Constants.getSystemGroupOne("newman"),"rmbprice");
			if(pe!=null)
				rmb_price=new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING);
		}
		BigDecimal totalfee=rmb_price;
		if (chinaprice.subtract(totalfee).intValue()>0)
		{
		}else{
			productDetailItem.chinaprice="暂无销售";
		}
		BigDecimal discount = chinaprice.intValue()==0 ? new BigDecimal(0) : totalfee.divide(chinaprice,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
		Double discountDouble = discount.doubleValue();
		productDetailItem.discount =  (discountDouble>10?10:discountDouble )+"";
		
		int nstock = 0;
		if(productTemp.getIshot()==1){
			nstock = (int) dealNstockWithProduct(productTemp.getPid());
		}else{
			nstock = getnstrock(productTemp.getPid());
		}
		productDetailItem.nlikes=productTemp.getNlikes()+""; 
		productDetailItem.rmb_price_no_symbol= String.valueOf(totalfee);               
		productDetailItem.rmb_price= "¥"+String.valueOf(totalfee);               
		List<String> imgList = new ArrayList<String>();
		for(Product_images image :getProductImages(productTemp.getPid())){
			imgList.add(StringUtil.getListpic(image.getFilename()));
		}
		
		
		productDetailItem.ptyp=productTemp.getPtyp();                    
		productDetailItem.is_like=this.checkIsUserlike(uid,productTemp.getPid().toString());
		
		productDetailItem.islovely=productTemp.getIslovely();
		if ("1".equals(productTemp.getIslovely())){
			productDetailItem.ptypeImg = domain+"/pimgs/site/lovely.png";
		}
		productDetailItem.typ=productTemp.getTyp();//1：代下单 2：自营
		if ("2".equals(productTemp.getTyp())){
			// 自营图片
			productDetailItem.typimg = domain+"/pimgs/site/proprietary.png";
		}else{
			productDetailItem.typimg = "";
		}
		
		if (nstock>=99999){
			productDetailItem.nstock="";
		}else{
			productDetailItem.nstock=String.valueOf(nstock);
		}
		
		if (nstock<=0){
			nstock=0;
			productDetailItem.nstock="0";
			// 售罄图片
			productDetailItem.ptypeImg= domain+"/pimgs/site/sellout.png";
		}
		
		if(!StringUtils.isBlank(productTemp.getBtim())){
			Date now = new Date();
			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date start;
			try {
				start = DATE_FORMAT.parse(productTemp.getBtim());
				productDetailItem.seconds=(start.getTime()-now.getTime())<0?0:(start.getTime()-now.getTime())/1000; 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (productDetailItem.seconds<=0)
			{
				productDetailItem.seconds=0L;
			}
		}
		productDetailItem.deposit= "¥"+productTemp.getDeposit().intValue(); 
		productDetailItem.mancnt=String.valueOf(productTemp.getMancnt()); 
		return productDetailItem;
	}
	
	public PackageProductItem covertToPackageProductItem(Product productTemp,Parcels parcels,String orderType ) {
		PackageProductItem packageProductItem = new PackageProductItem();
		packageProductItem.pid=productTemp.getPid()+"";
		 packageProductItem.skucode=productTemp.getSkucode();
		 packageProductItem.title=productTemp.getTitle();
		 packageProductItem.exturl=productTemp.getExturl();
		 packageProductItem.listpic=productTemp.getListpic();
		 String img="";
		 List<String> imgList = new ArrayList<String>();
		 for(Product_images image :getProductImages(productTemp.getPid())){
		   	img=image.getFilename();
		   	break;
		 }
		 packageProductItem.listpic=img;
		    
		 packageProductItem.specifications=productTemp.getSpecifications();
		 packageProductItem.counts=productRepository.queryProductCountsInparcel(parcels.getId(), productTemp.getPid())+"";
		 BigDecimal price = new BigDecimal(productTemp.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
		 BigDecimal freight = productTemp.getFreight()==null?new BigDecimal(0):new BigDecimal(productTemp.getFreight());
		 BigDecimal counts = new BigDecimal( packageProductItem.counts);
		 BigDecimal totalfee = freight.add(price).multiply(counts);
		 packageProductItem.price=totalfee.toString();
		 packageProductItem.rmbprice=freight.add(price).doubleValue();
		 if ("2".equals(orderType)  || "5".equals(orderType)){
			 String lovelydistinct = String.valueOf(productTemp.getLovelydistinct());
			if (lovelydistinct!=null && ".0".equals(lovelydistinct.substring(lovelydistinct.length()-2, lovelydistinct.length())))
			{
				//"撒娇再享"+lovelydistinct.substring(0, lovelydistinct.length()-2)+"折";
				packageProductItem.toast= "撒娇已享受"+lovelydistinct.substring(0, lovelydistinct.length()-2).replace(".0", "")+"折";
			}else{
				packageProductItem.toast = "撒娇已享受"+lovelydistinct.replace(".0", "")+"折";
			}
				
		}else{
			packageProductItem.toast ="";
		}
		 packageProductItem.paytime=productTemp.getPaytim();
		return packageProductItem;
	}

	public List<Object> covertToOrderInfoList(List<Product> productList) {
		Comparator<Product> comparator = new Comparator<Product>() {
			public int compare(Product o1, Product o2) {
				return ( o1.getFromsite() < o2.getFromsite() ? -1 : (o1.getFromsite() == o2.getFromsite() ? 0 : 1)); 
			}
		};
		Collections.sort(productList, comparator);//按照开始时间进行排序
		List<Object> result = new ArrayList<Object>();
		int fromsiteId=0; 
		String wayremark="";
		BigDecimal weight = new BigDecimal(0 );
		for (int i=0;i<productList.size();i++) {
			Product product = productList.get(i);
			Fromsite fromsite = queryFnamyByFromSite(product.getFromsite());
			String fname = fromsite.getName();
			String fromsitemsg = fromsite.getImg();
			if(i==0){
				fromsiteId=product.getFromsite();
				FromSiteItem fromSiteItem = new FromSiteItem();
				fromSiteItem.fromsiteimg = fromsitemsg;
				fromSiteItem.fromsite = fromsite.getName();
				fromSiteItem.typ="1";
				fromSiteItem.wayremark=product.getWayremark();
				wayremark = product.getWayremark();
				result.add(fromSiteItem);
			}
			
			if(fromsiteId!=product.getFromsite()||!wayremark.equals(product.getWayremark())) {
				WeightItem weightItem = new WeightItem();
				weightItem.typ="3";
				weightItem.freight = "¥"+new BigDecimal(ShoppingCartService.getfreight(fromsiteId, weight.toString())).intValue();
				result.add(weightItem);
				weight= new BigDecimal(0 );
				
				fromsiteId=product.getFromsite();
				FromSiteItem fromSiteItem = new FromSiteItem();
				fromSiteItem.fromsiteimg = fromsitemsg;
				fromSiteItem.fromsite = fromsite.getName();
				fromSiteItem.typ="1";
				fromSiteItem.wayremark=product.getWayremark();
				wayremark = product.getWayremark();
				result.add(fromSiteItem);
			}
			
			weight = weight.add(new BigDecimal(product.getWeight()).multiply(new BigDecimal(product.getCounts())));
			
			ProductItem productItem = new ProductItem();
			productItem.fromsite = fname;
			productItem.fromsiteimg = fromsitemsg;
			productItem.typ = "2";
			productItem.pid = String.valueOf(product.getPid());
			productItem.pcode = product.getSkucode();
			productItem.title = product.getTitle();
			if ("3".equals(product.getPtyp()))
			{
				productItem.linkurl="presellDetail://pid="+ String.valueOf(product.getPid());
			}else{
				productItem.linkurl="pDe://pid="+ String.valueOf(product.getPid());
			}
			
			productItem.iscoupon = "0";
			
			Currency currency = queryCurrencyById(product.getCurrency());
		    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
		    BigDecimal price = new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
		    BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
			if(product.getIslockprice()==1){
				rmb_price = new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
			}
			productItem.rmbprice = rmb_price.toString();
			productItem.rmb_price = "¥"+rmb_price.toString();
			productItem.img = product.getListpic();
			productItem.stage = String.valueOf(product.getStage());
			productItem.specifications = product.getSpecifications();
			productItem.counts = String.valueOf(product.getCounts());  
			result.add(productItem);
			
			if(i==productList.size()-1){
				WeightItem weightItem = new WeightItem();
				weightItem.typ="3";
				weightItem.freight = "¥"+new BigDecimal(ShoppingCartService.getfreight(fromsiteId, weight.toString())).intValue();
				result.add(weightItem);
				weight= new BigDecimal(0 );
			}
			
			
		}
		return result;
	}
	public List<Object> covertToOrderPresellInfoList(List<Product> productList) {
		Comparator<Product> comparator = new Comparator<Product>() {
			public int compare(Product o1, Product o2) {
				return ( o1.getFromsite() < o2.getFromsite() ? -1 : (o1.getFromsite() == o2.getFromsite() ? 0 : 1)); 
			}
		};
		Collections.sort(productList, comparator);//按照开始时间进行排序
		List<Object> result = new ArrayList<Object>();
		BigDecimal weight = new BigDecimal(0 );
		for (int i=0;i<productList.size();i++) {
			Product product = productList.get(i);
			Fromsite fromsite = queryFnamyByFromSite(product.getFromsite());
			String fname = fromsite.getName();
			String fromsitemsg = fromsite.getImg();
			
			weight = weight.add(new BigDecimal(product.getWeight()).multiply(new BigDecimal(product.getCounts())));
			
			ProductItem productItem = new ProductItem();
			productItem.fromsite = fname;
			productItem.fromsiteimg = fromsitemsg;
			productItem.typ = "2";
			productItem.pid = String.valueOf(product.getPid());
			productItem.pcode = product.getSkucode();
			productItem.title = product.getTitle();
			if ("3".equals(product.getPtyp()))
			{
				productItem.linkurl="presellDetail://pid="+ String.valueOf(product.getPid());
			}else{
				productItem.linkurl="pDe://pid="+ String.valueOf(product.getPid());
			}
			
			productItem.iscoupon = "0";
			
			BigDecimal rmb_price = new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
			productItem.rmbprice = rmb_price.toString();
			productItem.rmb_price = "¥"+rmb_price.toString();
			productItem.img = product.getListpic();
			productItem.stage = String.valueOf(product.getStage());
			productItem.specifications = product.getSpecifications();
			productItem.counts = String.valueOf(product.getCounts());
			productItem.rtitle = product.getRtitle();
			productItem.remark = product.getPreselltoast();
			result.add(productItem);
			
			if(i==productList.size()-1){
				WeightItem weightItem = new WeightItem();
				weightItem.typ="3";
				weightItem.freight = "¥"+new BigDecimal(ShoppingCartService.getfreight(product.getFromsite(), weight.toString())).setScale(0, BigDecimal.ROUND_CEILING);
				result.add(weightItem);
				weight= new BigDecimal(0 );
			}
			
			
		}
		return result;
	}
	
	
	public List<Product_images> getProductImages(Long pid){
		return productImageRepository.findByPid(pid);
	}
	
	
	/**
	 * 查询商品
	 * @param formPage
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<Product> queryProductsPage(ProductQueryVO productQueryVO,Integer page,Integer pageSize) {
        return this.productRepository.findAll(new ProductQuery(productQueryVO),new PageRequest(page, pageSize,new Sort(Direction.DESC, "nstock")));
    }
	
	/*
	 * 查询关键字与分类名称相关联产品
	 */
	public List<Product> searchProductlist(Integer page,Integer pageSize,String k){
		String sql="SELECT * FROM product WHERE category IN("
					+" SELECT id FROM category_new c INNER JOIN ("
					+" SELECT typecode FROM category_new  WHERE LOCATE('"+k+"',`name`) > 0 LIMIT 3) a"
					+" WHERE  c.`typecode` LIKE CONCAT(a.typecode,'%')) and status='10' and newMantype='0' and pid=ppid ORDER BY nstock DESC"
					+" limit "+page*pageSize+","+pageSize;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<Product> plist=new ArrayList<Product>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Product p=new Product();
				p.setPid(rs.getLong("pid"));
				p.setSkucode(rs.getString("skucode"));
				p.setTitle(rs.getString("title"));
				p.setSubtitle(rs.getString("subtitle"));
				p.setCategory(rs.getInt("category"));
				p.setPrice(rs.getDouble("price"));
				p.setList_price(rs.getDouble("list_price"));
				p.setDiscount(rs.getInt("discount"));
				p.setImgnums(rs.getInt("imgnums"));
				p.setExturl(rs.getString("exturl"));
				p.setSalesrank(rs.getInt("salesrank"));
				p.setStatus(rs.getInt("status"));
				p.setDate_add(rs.getDate("date_add"));
				p.setDate_upd(rs.getDate("date_upd"));
				p.setNlikes(rs.getInt("nlikes"));
				p.setIshot(rs.getInt("ishot"));
				p.setVersion(rs.getInt("version"));
				p.setExtcode(rs.getString("extcode"));
				p.setFromsite(rs.getInt("fromsite"));
				p.setCurrency(rs.getInt("currency"));
				p.setImgstr(rs.getString("imgstr"));
				p.setListpic(rs.getString("listpic"));
				p.setAdstr1(rs.getString("adstr1"));
				p.setAdstr3(rs.getString("adstr3"));
				p.setDetail(rs.getString("detail"));
				p.setSort(rs.getShort("sort"));
				p.setChinaprice(rs.getDouble("chinaprice"));
				p.setNstock(rs.getLong("nstock"));
				p.setNstock_autoupd(rs.getInt("nstock_autoupd"));
				p.setIslovely(rs.getString("islovely"));
				p.setTyp(rs.getString("typ"));
				p.setWeight(rs.getDouble("weight"));
				p.setFreight(rs.getDouble("freight"));
				p.setWayremark(rs.getString("wayremark"));
				p.setWishcount(rs.getInt("wishcount"));
				p.setActivityimage(rs.getString("activityimage"));
				p.setPromiseURL(rs.getString("promiseURL"));
				p.setLimitcount(rs.getInt("limitcount"));
				p.setLovelydistinct(rs.getDouble("lovelydistinct"));
				p.setRmbprice(rs.getDouble("rmbprice"));
				p.setIslockprice(rs.getInt("islockprice"));
				p.setDistinctimg(rs.getString("distinctimg"));
				p.setSendmailflg(rs.getString("sendmailflg"));
				p.setBacknstock(rs.getInt("backnstock"));
				p.setSpecifications(rs.getString("specifications"));
				p.setPpid(rs.getLong("ppid"));
				p.setStitle(rs.getString("stitle"));
				p.setIsopenid(rs.getInt("isopenid"));
				p.setSpecpic(rs.getString("specpic"));
				p.setNum_iid(rs.getString("num_iid"));
				p.setWx_upd(rs.getDate("wx_upd"));
				p.setWx_flg(rs.getString("wx_flg"));
				p.setStock(rs.getInt("stock"));
				p.setBtim(rs.getString("btim"));
				p.setDeposit(rs.getDouble("deposit"));
				p.setEtim(rs.getString("etim"));
				p.setMancnt(rs.getInt("mancnt"));
				p.setPreselltoast(rs.getString("preselltoast"));
				p.setPtyp(rs.getString("ptyp"));
				p.setRtitle(rs.getString("rtitle"));
				p.setStage(rs.getInt("stage"));
				p.setPaytim(rs.getString("paytim"));
				p.setJpntitle(rs.getString("jpntitle"));
				p.setJpncode(rs.getString("jpncode"));
				p.setNationalFlag(rs.getString("nationalFlag"));
				p.setEndorsementCount(rs.getInt("endorsementCount"));
				p.setIsFull(rs.getInt("isFull"));
				p.setMaxEndorsementCount(rs.getInt("maxEndorsementCount"));
				p.setNewSku(rs.getString("newSku"));
				p.setIsopenid(rs.getInt("isopenid"));
				p.setIsEndorsement(rs.getInt("isEndorsement"));
				p.setCommision(rs.getDouble("commision"));
				p.setCommisionTyp(rs.getInt("commisionTyp"));
				p.setEndorsementPrice(rs.getDouble("endorsementPrice"));
				p.setCommision_average(rs.getString("commision_average"));
				p.setForbiddenCnt(rs.getInt("forbiddenCnt"));
				p.setNewMantype(rs.getString("newMantype"));
				p.setIsSyncErp(rs.getInt("is_sync_erp"));
				p.setCostPrice(rs.getDouble("costPrice"));
				plist.add(p);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return plist;
	}
	
	/*
	 * 关键字查询总条数
	 */
	/*
	 * 查询关键字与分类名称相关联产品
	 */
	public Integer searchProductCountByKey(String k){
		String sql="SELECT count(pid) as cnt FROM product WHERE category IN("
					+" SELECT id FROM category_new c INNER JOIN ("
					+" SELECT typecode FROM category_new  WHERE LOCATE('"+k+"',`name`) > 0 LIMIT 3) a"
					+" WHERE LOCATE(a.typecode,c.typecode) > 0) and status='10'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<Product> plist=new ArrayList<Product>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				return rs.getInt("cnt");
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			return 0;
		} finally {
			db.close();
		} 
		return 0;
	}
	/*
	 * 查询关键字与分类名称相关联产品
	 */
	public Integer searchProductcount(String k){
		String sql="SELECT count(pid) as cc FROM product WHERE category IN("
					+" SELECT id FROM category_new c INNER JOIN ("
					+" SELECT typecode FROM category_new WHERE '"+k+"' LIKE CONCAT('%',`name`,'%') LIMIT 1) a"
					+" WHERE c.typecode = a.typecode AND c.typecode LIKE CONCAT(c.typecode,'%'))";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer total=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				total=rs.getInt("cc");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return total;
	}
    /**
     * 商品查询内部类
     * @author luobotao
     * @Date 2015年5月11日
     */
    private static class ProductQuery implements Specification<Product> {

        private final ProductQueryVO productQuerylVO;

        public ProductQuery(final ProductQueryVO productQuerylVO) {
            this.productQuerylVO = productQuerylVO;
        }

        @Override
        public Predicate toPredicate(Root<Product> product, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<String> title = product.get("title");
            Path<String> status = product.get("status");
            Path<String> ppid = product.get("ppid");
            Path<String> pid = product.get("pid");
            Path<String> newMantype = product.get("newMantype");
            
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(pid, ppid));
            if (!Strings.isNullOrEmpty(productQuerylVO.title)) {
            		predicates.add(builder.like(title, "%"+productQuerylVO.title+"%"));
            }
            if (!Strings.isNullOrEmpty(productQuerylVO.status)) {
            	predicates.add(builder.equal(status, productQuerylVO.status));
            }
            predicates.add(builder.notEqual(newMantype, "3"));
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            
            return query.where(param).getRestriction();
        }
    }
    
    public EndorsementDetailItem getProductEndorsementInfo(Long pid){
    	EndorsementDetailItem productEndorItem = new EndorsementDetailItem();
    	String sql="SELECT a.pid AS pid, a.skucode AS pcode,a.title AS title,a.subtitle AS subtitle,a.rmbprice AS rmbprice,a.chinaprice AS chinaprice,a.exturl AS exturl,a.date_upd as date_upd,"+
    			" a.nationalFlag,a.isEndorsement,a.endorsementCount,a.maxEndorsementCount,a.endorsementPrice,a.commision,a.commisionTyp,f.name AS fromsite,f.img AS fromsiteimg,c.symbol AS symbol,c.rate AS rate,p.filename AS listpic"+
				" FROM product a, fromsite f,currency c,category_new ca,(SELECT MAX(pid) AS pid,filename FROM product_images GROUP BY pid) p"+
				" WHERE a.fromsite=f.id AND a.currency=c.id AND a.category=ca.id AND a.status='10' AND a.pid = p.pid "+
				" and a.pid="+pid;
    	JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				BigDecimal rmb_price = new BigDecimal(rs.getDouble("rmbprice"));
				BigDecimal chinaprice = new BigDecimal(rs.getDouble("chinaprice"));
				productEndorItem.pid = rs.getString("pid");
				productEndorItem.pcode = rs.getString("pcode");
				productEndorItem.title = rs.getString("title");
				productEndorItem.subtitle = rs.getString("subtitle");
				productEndorItem.rmb_price = "¥"+rmb_price;
				productEndorItem.china_price = "¥"+chinaprice;
				productEndorItem.linkUrl = "pDe://pid="+rs.getString("pid")+"&lmt="+(rs.getDate("date_upd")==null?"":rs.getDate("date_upd").getTime());
				productEndorItem.rmb_price_no_symbol = Double.valueOf(rmb_price.toString());
				productEndorItem.listpic =StringUtil.getListpic( rs.getString("listpic"));
				productEndorItem.maxEndorsementCount = String.valueOf(rs.getInt("maxEndorsementCount"));
				productEndorItem.endorsementCount =  String.valueOf(rs.getInt("endorsementCount"));
				productEndorItem.endorsementPrice = String.valueOf(rs.getDouble("endorsementPrice"));
				productEndorItem.commision = String.valueOf(rs.getDouble("commision"));
				productEndorItem.commisionTyp = String.valueOf(rs.getInt("commisionTyp"));
				productEndorItem.nationalFlagImg = rs.getString("nationalFlag");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		} 
    	return productEndorItem;
    }
    

    public List<ProductEndorsementItem> getProductEndorsement(Integer page){
    	List<ProductEndorsementItem> result = new ArrayList<ProductEndorsementItem>();
    	String sql="SELECT a.pid AS pid, a.commision_average AS commision_average,a.skucode AS pcode,a.title AS title,a.subtitle AS subtitle,a.price AS price,a.list_price AS list_price,a.exturl AS exturl,a.date_upd as date_upd,"+
    			" a.nationalFlag,a.isEndorsement,a.endorsementCount,a.maxEndorsementCount,a.endorsementPrice,a.commision,a.commisionTyp,f.name AS fromsite,f.img AS fromsiteimg,c.symbol AS symbol,c.rate AS rate,p.filename AS listpic"+
				" FROM product a, fromsite f,currency c,category_new ca,(SELECT MAX(pid) AS pid,filename FROM product_images GROUP BY pid) p"+
				" WHERE a.fromsite=f.id AND a.currency=c.id AND a.category=ca.id AND a.status='10' AND a.pid = p.pid "+
				" AND a.isEndorsement=1 and a.pid=a.ppid order by a.sort desc,a.date_add desc limit "+page*10+",10";
    	JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ProductEndorsementItem productEndorItem = new ProductEndorsementItem();
				BigDecimal rate = new BigDecimal(rs.getDouble("rate")).setScale(4,  BigDecimal.ROUND_CEILING) ;
				BigDecimal price = new BigDecimal(rs.getDouble("price"));
				BigDecimal list_price = new BigDecimal(rs.getDouble("list_price"));
				price = price.divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_CEILING) ;
				list_price = list_price.divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_CEILING) ;
				BigDecimal rmb_price = rate.divide(new BigDecimal(100)).setScale(4,  BigDecimal.ROUND_CEILING).multiply(price).setScale(0,  BigDecimal.ROUND_CEILING);
				
				productEndorItem.pid = rs.getString("pid");
				productEndorItem.pcode = rs.getString("pcode");
				productEndorItem.title = rs.getString("title");
				productEndorItem.subtitle = rs.getString("subtitle");
				productEndorItem.rmb_price = "¥"+rmb_price;
				productEndorItem.rmb_price_no_symbol = Double.valueOf(rmb_price.toString());
				productEndorItem.listpic =StringUtil.getListpic( rs.getString("listpic"));
				if (rs.getInt("endorsementCount")<rs.getInt("maxEndorsementCount")-10)
				{
					productEndorItem.type="1";	
				}else{
					if (rs.getInt("endorsementCount")>=rs.getInt("maxEndorsementCount")-10 && 
							rs.getInt("endorsementCount")<rs.getInt("maxEndorsementCount")){
						productEndorItem.type="2";
					}else{
						productEndorItem.type="3";
					}
				}
				BigDecimal endorseprice=new BigDecimal(rs.getDouble("endorsementPrice")).setScale(0,BigDecimal.ROUND_CEILING);
				
				if(endorseprice.equals(rmb_price))
					productEndorItem.endorsementPrice="";
				else
					productEndorItem.endorsementPrice = "¥"+String.valueOf(endorseprice);
				
				if ("1".equals(String.valueOf(rs.getInt("commisionTyp")))){
					productEndorItem.commision = "¥"+String.valueOf(new BigDecimal(rs.getDouble("commision")).setScale(0,BigDecimal.ROUND_CEILING))+"/笔";
				}else{
					productEndorItem.commision = String.valueOf(new BigDecimal(rs.getDouble("commision")).setScale(0,BigDecimal.ROUND_CEILING))+"%";
				}
				
				//人均赚取佣金值，需计算
				productEndorItem.commision_average="¥"+String.valueOf(new BigDecimal(rs.getDouble("commision_average")).setScale(0,BigDecimal.ROUND_CEILING));
				productEndorItem.nationalFlagImg = rs.getString("nationalFlag");
				productEndorItem.linkUrl = "pDe://pid="+rs.getInt("pid")+"&lmt="+(rs.getDate("date_upd")==null?"":rs.getDate("date_upd").getTime());
				result.add(productEndorItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		} 
    	return result;
    }

	public List<ProductRecomItem> getProductRecom(
			String uid, String pid, String resolution,String reffer) {
		List<ProductRecomItem> result = new ArrayList<ProductRecomItem>();
		String sql="SELECT a.pid AS pid, a.skucode AS pcode,a.title AS title,a.subtitle AS subtitle,a.price AS price,a.list_price AS list_price,a.exturl AS exturl,a.date_upd as date_upd,f.name AS fromsite,f.img AS fromsiteimg,c.symbol AS symbol,c.rate AS rate,p.filename AS listpic"+
				" FROM product a, fromsite f,currency c,category_new ca,(SELECT MAX(pid) AS pid,filename FROM product_images GROUP BY pid) p"+
				" WHERE a.fromsite=f.id AND a.currency=c.id AND a.category=ca.id AND a.status='10' AND a.pid = p.pid "+
				" AND a.pid<>'"+pid+"' AND a.newMantype<>'3' ORDER BY RAND() LIMIT 3";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ProductRecomItem productRecomItem = new ProductRecomItem();
				BigDecimal discount = new BigDecimal(0);
				BigDecimal rate = new BigDecimal(rs.getDouble("rate")).setScale(4,  BigDecimal.ROUND_CEILING) ;
				BigDecimal price = new BigDecimal(rs.getDouble("price"));
				BigDecimal list_price = new BigDecimal(rs.getDouble("list_price"));
				price = price.divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_CEILING) ;
				list_price = list_price.divide(new BigDecimal(100)).setScale(2,  BigDecimal.ROUND_CEILING) ;
				if(list_price.doubleValue()!=0){
					discount = price.multiply(new BigDecimal(10)).divide(list_price,2).setScale(1,  BigDecimal.ROUND_CEILING) ;
				}
				if("円".equals(rs.getString("symbol"))){
					productRecomItem.price=price+rs.getString("symbol");
					productRecomItem.list_price=list_price+rs.getString("symbol");
				}else{
					productRecomItem.price=rs.getString("symbol") + price;
					productRecomItem.list_price=rs.getString("symbol") + list_price;
				}
				BigDecimal rmb_price = rate.divide(new BigDecimal(100)).setScale(4,  BigDecimal.ROUND_CEILING).multiply(price).setScale(0,  BigDecimal.ROUND_CEILING);
				productRecomItem.pid = rs.getString("pid");
				productRecomItem.pcode = rs.getString("pcode");
				productRecomItem.title = rs.getString("title");
				productRecomItem.subtitle = rs.getString("subtitle");
				productRecomItem.exturl = rs.getString("exturl");
				productRecomItem.fromsite = rs.getString("fromsite");
				productRecomItem.fromsiteimg = rs.getString("fromsiteimg");
				productRecomItem.symbol = rs.getString("symbol");
				productRecomItem.listpic =StringUtil.getListpic(rs.getString("listpic"));
				productRecomItem.rate = rate.toString();
				productRecomItem.rmb_price = "¥"+rmb_price;
				productRecomItem.rmb_price_no_symbol = rmb_price.toString();
				
				productRecomItem.img_src =StringUtil.getListpic(rs.getString("listpic"));// WebpRecommend(productRecomItem.pcode,rs.getString("listpic"),resolution);
				productRecomItem.discount = discount.toString();
				productRecomItem.reffer=reffer;
				try {
					productRecomItem.date_upd = CHINESE_DATE_TIME_FORMAT.format(rs.getDate("date_upd")==null?"":rs.getDate("date_upd"));
				} catch (Exception e) {
					productRecomItem.date_upd ="";
				}
				productRecomItem.linkurl = "pDe://pid="+rs.getInt("pid")+"&lmt="+(rs.getDate("date_upd")==null?"":rs.getDate("date_upd").getTime());
				result.add(productRecomItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		} 
		return result;
	}

	private String WebpRecommend(String pcode, String listpic, String resolution) {
		if(StringUtils.isBlank(listpic)){
			return "";
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		String picArray[] = listpic.split("\\.");
		String fileName = picArray[0];
		String width = "360";
		if(listpic.indexOf("-")>0){
			String listPicArray[] = listpic.split("-");
			if (listPicArray.length > 2) {
				String pathTemp = listPicArray[1];
				listpic = domain + "/pimgs/p/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + fileName+"_"+width+".webp";
			}
		}else{
			String listPicArray[] = listpic.split("_");
			if (listPicArray.length > 1) {
				String pathTemp = listPicArray[0];
				listpic = domain + "/pimgs/p/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + fileName+"_"+width+".webp";
			}
		}
		return listpic;
	}

	public String getLovelyRemark(String type) {
		return productRepository.getLovelyRemark(type);
	}

	public int checkOrderPayStat(String out_trade_no, Double totalFee) {
		int result = 0;
		String sql = "{call sp_order_checkPayStat ('"+out_trade_no+"','"+totalFee+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getInt("STATUS");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public void setPayStatus(String out_trade_no, String method, String state,
			Double totalFee, String trade_no) {
		String sql = "{call sp_order_setPayStat_callback ('"+out_trade_no+"','"+method+"','"+state+"','"+totalFee+"','"+trade_no+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
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
	public void setPayStatusFast(String out_trade_no, String method, String state,
			Double totalFee, String trade_no) {
		String sql = "{call sp_order_setPayStat ('"+out_trade_no+"','"+method+"','"+state+"','"+totalFee+"','"+trade_no+"')}";// SQL语句// //调用存储过程
		logger.info(sql);
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

	public List<Product> getOutPardelsProduct_ByOrderCode(String orderCode,
			String status) {
		if(orderCode.length()>10){
    		orderCode = orderCode.substring(0, 10);
    	}
		List<Product> productlist =  productRepository.getOutPardelsProduct_ByOrderCode(orderCode);
		for(Product product:productlist){
			product.setCounts(this.productRepository.queryOrderProductCounts(orderCode,product.getPid()));
//			product.setRmbprice(productRepository.queryOrderProductPrice(orderCode,product.getPid()));
		}
		return productlist;
	}
	
	
	public List<Product> getRefundProduct_ByOrderCode(String orderCode,
			String status) {
		if(orderCode.length()>10){
    		orderCode = orderCode.substring(0, 10);
    	}
		List<Product> productlist =  productRepository.getRefundProduct_ByOrderCode(orderCode);
		for(Product product:productlist){
			product.setCounts(this.productRepository.queryOrderProductCounts(orderCode,product.getPid()));
//			product.setRmbprice(productRepository.queryOrderProductPrice(orderCode,product.getPid()));
		}
		return productlist;
	}

	public ShoppingCartLovelyItem coverToShoppingCart(Product product,String count) {
		ShoppingCartLovelyItem item = new ShoppingCartLovelyItem();
		item.pid = product.getPid()+"";
		item.title = product.getTitle();
		item.count = count;
		item.linkurl ="pDe://pid="+product.getPid(); 
		
		Currency currency = queryCurrencyById(product.getCurrency());
	    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
	    BigDecimal price = new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
	    BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
		if (product.getIslockprice()==1) {
			rmb_price = new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
		} 
		/*Fromsite fromsite = queryFnamyByFromSite(product.getFromsite());
	    String fname =  fromsite.getName();*/
		BigDecimal totalFee=rmb_price.multiply(new BigDecimal(count));	
		
		item.price = rmb_price.doubleValue();
		item.rmb_price="¥"+totalFee;
		item.rmbprice=totalFee+"";
		item.img = product.getListpic();
		return item;
	}
	
	public String getmaxImgUrl(Long pid) {
		String result="";
		String sql="SELECT MAX(id),pid,filename FROM product_images where pid='"+pid+"'";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getString("filename");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public String getPayTime(String orderCode) {
		String result="";
		String sql="SELECT MIN(paytime) AS tim FROM shopping_OrderPay WHERE OrderCode LIKE '"+orderCode+"%'";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result = rs.getString("tim");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	/*
	 * 获取商品来源
	 */
    public Fromsite getfrom(Integer pid){
    	return fromSiteRepository.findOne(pid);
    }
    
    /*
     * 根据产品编码查询商品详情 20150611
     */
    public List<ProductDetail> getdetailist(String skucode){
    	String sql="SELECT d.*,p.typ FROM product p,product_detail d WHERE p.pid=d.pid AND p.skucode='"+skucode+"'";
    	List<ProductDetail> dlist=new ArrayList<ProductDetail>();
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ProductDetail d=new ProductDetail();
				d.setId(rs.getLong("id"));
				d.setChname(rs.getString("chname"));
				d.setEnname(rs.getString("enname"));
				d.setDate_add(rs.getDate("date_add"));
				d.setDetail(rs.getString("detail"));
				d.setNsort(rs.getInt("nsort"));
				d.setPid(rs.getLong("pid"));
				d.setTyp(rs.getInt("typ"));
				dlist.add(d);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(dlist!=null && !dlist.isEmpty())
		{
			for(ProductDetail d:dlist){
				d.setPramlist(this.getdetailpramlist(d.getId()));
			}
		}
		if(dlist!=null && !dlist.isEmpty())
			return dlist;
		
		return null;
    }
    
    /*
     * 产品详情参数列表150611
     */
    public List<ProductDetailPram> getdetailpramlist(Long pdid){
       	String sql="select * from product_detail_info where pdid="+pdid;
    	List<ProductDetailPram> dlist=new ArrayList<ProductDetailPram>();
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ProductDetailPram d=new ProductDetailPram();
				d.setId(rs.getLong("id"));
				d.setDate_add(rs.getDate("date_add"));
				d.setNsort(rs.getInt("nsort"));
				d.setPdid(rs.getLong("pdid"));
				d.setKey(rs.getString("key"));
				d.setVal(rs.getString("val"));
				dlist.add(d);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(dlist!=null && !dlist.isEmpty())
			return dlist;
		
    	return null;
    }
	/*
	 * 20150526添加,查询代言商品
	 */
	//@Transactional(readOnly = true)
	//public Page<Product> getEndorsmentProductlist(String Uid, String page){
//	public List<Product> getEndorsmentProductList(Long Uid,Integer page){
		/*
		 * 如果表做关联，模型做定义用jpa关联查询
		 */
//		try
//		{
//			page=String.valueOf(Integer.parseInt(page));
//		}
//		catch(Exception e){page="0";}
//		return productRepository.findAll(new Specification<Product>(){
//			@Override
//			public Predicate toPredicate(Root<Product> product,CriteriaQuery<?> query,
//					CriteriaBuilder builder) {
//				
//				Path<String> uid=product.get("userId");
//	           
//	            
//	            
//	            
//	            List<Predicate> predicates = new ArrayList<>();
//	            if (!Strings.isNullOrEmpty(Uid) && !"-1".equals(Uid)) {
//	                predicates.add(builder.equal(uid, Uid));
//	            }
//	           
//
//	            Predicate[] param = new Predicate[predicates.size()];
//	            predicates.toArray(param);
//	            
//	            return query.where(param).getRestriction();
//			}
//		}, new PageRequest(Integer.parseInt(page),10));
//	}
    
	public List<Product> findProductsWithDateUpd(Date lasttime) {
		return productRepository.findProductsWithDateUpd(lasttime);
	}
	public List<Product> getProductByNewSku(String newSku) {
		return productRepository.getProductByNewSku(newSku);
	}
	public List<Product> findProductsWithDateAdd(Date lasttime) {
		return productRepository.findProductsWithDateAdd(lasttime);
	}
	public List<Product> findPproductByPid(Long pid,String status){
		return productRepository.findByPpid(pid,status);
	}
	/**
	 * 
	 * <p>Title: getOverStockInfo</p> 
	 * <p>Description: 获取商品库存大于指定值的集合</p> 
	 * @param overLine 
	 * @return
	 */
	public List<Product> getOverStockInfo(Integer overLine) {
		return productRepository.getOverStockInfo(overLine);
	}
	//根据输入词搜索关键词
	public HotSearchKey getkeyByWord(String word){
		List<HotSearchKey> hlist = new ArrayList<HotSearchKey>();
		//String sql="SELECT * FROM hot_search_key WHERE commentdesc LIKE '%"+word+"%' OR hotWordDes LIKE '%"+word+"%' OR hotWordTitle LIKE '%"+word+"%' order by sort desc";
		String sql="SELECT * FROM hot_search_key WHERE '"+word+"' LIKE CONCAT('%',`hotWordKey`,'%') OR '"+word+"' LIKE CONCAT('%',`hotWordDes`,'%') OR '"+word+"' LIKE CONCAT('%',`hotWordTitle`,'%') ORDER BY sort DESC";
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				HotSearchKey pe=new HotSearchKey();
				pe.setCommentdesc(rs.getString("commentdesc"));
				pe.setCreateTime(rs.getDate("createTime"));
				pe.setHotWordDes(rs.getString("hotWordDes"));
				pe.setHotWordImageUrl(rs.getString("hotWordImageUrl"));
				pe.setHotWordKey(rs.getString("hotWordKey"));
				pe.setHotWordTitle(rs.getString("hotWordTitle"));
				hlist.add(pe);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(hlist!=null && !hlist.isEmpty())
			return hlist.get(0);
		else
			return null;
	}
	//获取热词列表
	public List<HotSearchKey> getallhotkey(){
		return hotsearchKeyinterface.getkeywordlist();
	}
	//获取扩展价格
	public ProductPriceExt getProductPrice(Long pid,String mantype,String pricetype){
		String sql="select * from product_price where pid="+pid+" and manType='"+mantype+"' and pricetype='"+pricetype+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductPriceExt pe=null;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				pe=new ProductPriceExt();
				pe.setId(rs.getLong("id"));
				pe.setPid(rs.getLong("pid"));
				pe.setSaleprice(rs.getDouble("saleprice"));
				pe.setManType(rs.getString("manType"));
				pe.setPricetype(rs.getString("pricetype"));				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return pe;
	}
	
	//根据分类名称获取分类列表
	public List<Category> getCagtegoryList(String Nname){
		String sql="select * from category_new where name='"+Nname+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductPriceExt pe=null;
		List<Category> clist=new ArrayList<Category>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Category ca=new Category();
				ca.setId(rs.getLong("id"));
				ca.setName(rs.getString("name"));
				clist.add(ca);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return clist;
	}

	/**
	 * 根据组合商品ID获取子商品
	 * @param pgid
	 * @return
	 */
	public List<ProductGroup> findProductGroupListByPgId(Long pgid) {
		return productGroupRepository.findProductGroupListByPgId(pgid);
	}
	/**
	 * 根据组合商品id获取库存
	 * @param pgid
	 * @return
	 */
	public long dealNstockWithProduct(Long pgid) {
		Product productNow = getProductById(pgid);
		if(productNow!=null && productNow.getNstock()!=null && productNow.getNstock().longValue()>=0){
			return productNow.getNstock();
		}
		List<ProductGroup> productGroups = findProductGroupListByPgId(pgid);
		long nstock = 9999L;
		for (ProductGroup productGroup : productGroups) {
			Product product = getProductById(productGroup.getPid());
			if(product.getNstock()==0L||product.getNstock()<productGroup.getNum()){
				nstock = 0L;
				break;
			}
			long tempNstock = product.getNstock() / productGroup.getNum();
			if(tempNstock < nstock&&tempNstock>0){
				nstock = tempNstock;
			}
		}
		return nstock==9999L?0:nstock;
	}
	
	/*
	 * 取税费
	 */
	public Double getRateFee(String pids,Long uid){
		String sql="SELECT (f.taxRate*p.rmbprice*s.counts)/100 AS rate FROM product_union f,product p,shopping_Cart s WHERE f.pid=p.pid AND p.pid=s.pId AND p.pid IN('"+pids+"') and s.uId="+uid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Double fee=0D;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				fee=rs.getDouble("rate");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return new BigDecimal(fee).setScale(0, BigDecimal.ROUND_UP).doubleValue();
	}
	
	/*
	 * 取税费
	 */
	public Double getEndorseRateFee(String pids,String openid){
		String sql="SELECT (f.taxRate*p.rmbprice*s.counts)/100 AS rate FROM product_union f,product p,shopping_Cart_endorse s WHERE f.pid=p.pid AND p.pid=s.pId AND p.pid IN('"+pids+"') and s.openid='"+openid+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Double fee=0D;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				fee=rs.getDouble("rate");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return fee;
	}
	
	/*
	 * 取product_union
	 */
	public ProductUnion getproUnion(Long pid){
		String sql="select * from product_union where pid="+pid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductUnion pro=null;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				pro=new ProductUnion();
				pro.setPid(rs.getLong("pid"));
				pro.setId(rs.getLong("id"));
				pro.setBuyNowFlag(rs.getString("buyNowFlag"));
				pro.setTaxRate(rs.getDouble("taxRate"));				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return pro;
	}
	
	 /*
		  * 计算运费
		  */
		 public Double getWeightFee(List<Long> pidlist,Long uId){
			 Map<Integer,List<Product>> productMap=new HashMap<Integer, List<Product>>();
			 BigDecimal wfee = new BigDecimal(0);
			 for(Long pid:pidlist){
				 Product proinfo=this.getProductById(pid);
				 Integer counts=shoppingCartRepository.getTotalNumByUIdAndPId(uId, proinfo.getPid());
				 proinfo.setCounts(counts==null?0:counts);
				 proinfo.setFromobj(this.queryFnamyByFromSite(proinfo.getFromsite()));
				 List<Product> productList = productMap.get(proinfo.getFromsite());
					if(productList==null)
						productList = new ArrayList<Product>();
					productList.add(proinfo);
					productMap.put(proinfo.getFromsite(), productList);
				}
			 for(Integer fromesite:productMap.keySet()){
					BigDecimal weight = new BigDecimal(0);
					List<Product> productList = productMap.get(fromesite);
					for(Product product:productList){
						Integer weightTemp =  (int) (product.getWeight()*10);
						Integer countTemp =  product.getCounts();
						BigDecimal weightTotal = new BigDecimal(weightTemp*countTemp*0.1).setScale(1,BigDecimal.ROUND_HALF_UP);
						weight = weight.add(weightTotal);
					}
					wfee = wfee.add(new BigDecimal(ShoppingCartService.getfreight(fromesite, String.valueOf(weight))));
				}			 
			 return wfee.setScale(0, BigDecimal.ROUND_UP).doubleValue();
	 }
		 
		 /*
		  * 取代言计算运费
		  */
		 public Double getEndorseWeightFee(List<Long> pidlist,String openid,Integer counts){
			 Map<Integer,List<Product>> productMap=new HashMap<Integer, List<Product>>();
			 BigDecimal wfee = new BigDecimal(0);
			 for(Long pid:pidlist){
				 Product proinfo=this.getProductById(pid);				
				 proinfo.setCounts(counts==null?0:counts);
				 proinfo.setFromobj(this.queryFnamyByFromSite(proinfo.getFromsite()));
				 List<Product> productList = productMap.get(proinfo.getFromsite());
					if(productList==null)
						productList = new ArrayList<Product>();
					productList.add(proinfo);
					productMap.put(proinfo.getFromsite(), productList);
				}
			 for(Integer fromesite:productMap.keySet()){
					BigDecimal weight = new BigDecimal(0);
					List<Product> productList = productMap.get(fromesite);
					for(Product product:productList){
						Integer weightTemp =  (int) (product.getWeight()*10);
						Integer countTemp =  product.getCounts();
						BigDecimal weightTotal = new BigDecimal(weightTemp*countTemp*0.1).setScale(1,BigDecimal.ROUND_HALF_UP);
						weight = weight.add(weightTotal);
					}
					wfee = wfee.add(new BigDecimal(ShoppingCartService.getfreight(fromesite, String.valueOf(weight))));
				}			 
			 return wfee.setScale(0, BigDecimal.ROUND_UP).doubleValue();
	 }
		 		 
}
