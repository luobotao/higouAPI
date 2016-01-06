package services.api;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Endorsement;
import models.OrderLoveLyBack;
import models.OrderProduct;
import models.Parcels;
import models.Product;
import models.ProductPriceExt;
import models.ShoppingOrder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import repositories.CategoryRepository;
import repositories.OrderLovelyBackRepository;
import repositories.OrderProductRepository;
import repositories.ParcelsRepository;
import repositories.ProductRepository;
import repositories.ShoppingOrderRepository;
import utils.Constants;
import utils.JdbcOper;
import utils.JdbcOperWithClose;
import utils.Numbers;
import utils.StringUtil;
import vo.product.ProductNewVO;
import vo.shoppingCart.ShoppingCartCategoryVO;
import vo.shoppingCart.ShoppingCartItemVO;
import vo.shoppingOrder.ShoppingOrderCostVO;
import vo.shoppingOrder.ShoppingOrderCostVO.ShoppingOrderCostItem;
import vo.shoppingOrder.ShoppingOrderQueryVO;
import vo.shoppingOrder.ShoppingOrderResultVO.PackageProductItem;
import vo.shoppingOrder.ShoppingOrderWayBillVO;
import vo.shoppingOrder.ShoppingOrderWayBillVO.ShoppingOrderWayBillItem;

/**
 * 商品相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ShoppingOrderService {
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger.ALogger logger = Logger.of(ShoppingOrderService.class);

    @Inject
    private ProductRepository productRepository;
    @Inject
    private ShoppingOrderRepository shoppingOrderRepository;
    @Inject
    private ParcelsRepository pardelsRepository;
    @Inject
    private OrderProductRepository orderProductRepository;
    @Inject
    private OrderLovelyBackRepository orderLovelyBackRepository;
    @Inject
    private CategoryRepository categoryRepository;
    @Inject
    private UserService userService;
    @Inject
    private ProductService productService;
    
	@SuppressWarnings("static-access")
	public boolean pardelsDel(String status,Long uid,Long pid) {
		String sql = "UPDATE pardels p INNER JOIN shopping_Order o ON p.orderId=o.id SET p.status='"+status+"' WHERE o.uId="+uid+" AND p.id="+pid;
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.updateSql(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return true;
	}
	
	public List<String> getHgPaymentType() {
		List<String> hgPaymentType = new ArrayList<String>();
		String sql = "{call get_HGPaymentType()}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				if("1".equals(rs.getNString("HGPaymentType_WXPay"))){
					hgPaymentType.add("0");
				}
				if("1".equals(rs.getNString("HGPaymentType_AliPay"))){
					hgPaymentType.add("1");
				}
				if("1".equals(rs.getNString("HGPaymentType_AliWebPay"))){
					hgPaymentType.add("2");
				}
				if("1".equals(rs.getNString("HGPaymentType_AliPayInternational"))){
					hgPaymentType.add("3");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return hgPaymentType;
	}
	
	public boolean order_back(String orderCode) {
		String sql = "{call sp_order_back('"+orderCode+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.executeQuery();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return true;
	}
	
	
	public String updateOrderStatus(Long uid,String orderCode,String status) {
		String res_status="0";
		String sql = "{call sp_order_setStatus("+uid+",'"+orderCode+"','"+status+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				res_status = rs.getNString("status");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return res_status;
		
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
   

	
	/**
	 * 查询订单
	 * @param formPage
	 * @return
	 */
	@Transactional(readOnly = true)
	public Page<ShoppingOrder> queryShoppingOrderPage(ShoppingOrderQueryVO shoppingOrderQueryVO,Integer page,Integer pageSize) {
        return this.shoppingOrderRepository.findAll(new ShoppingOrderQuery(shoppingOrderQueryVO),new PageRequest(page, pageSize,new Sort(Direction.DESC, "id")));
    }
	
	
	/**
	 * 查询包裹
	 * @param formPage
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Parcels> queryPardelsByOrderId(Long orderId) {
		List<Parcels> parcelsList = pardelsRepository.findByOrderId(orderId);
		List<Parcels> result = new ArrayList<Parcels>();
		for(Parcels parcels:parcelsList){
			if(parcels.getStatus()>=0){
				result.add(parcels);
			}
		}
		return result;
    }
	
	/**
	 * 根据包裹ID查询包裹下的商品
	 * @param parcelsId
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Product> queryProductListByParcelsId(Long parcelsId) {
		List<Product> productList = this.productRepository.queryProductListByParcelsId(parcelsId);
		for(Product product:productList){
			product.setCounts(this.productRepository.queryProductCountsInparcel(parcelsId,product.getPid()));
		}
        return productList;
    }
	
	/**
     * 订单查询内部类
     * @author luobotao
     * @Date 2015年5月11日
     */
    private static class ShoppingOrderQuery implements Specification<ShoppingOrder> {

        private final ShoppingOrderQueryVO shoppingOrderQueryVO;

        public ShoppingOrderQuery(final ShoppingOrderQueryVO shoppingOrderQueryVO) {
            this.shoppingOrderQueryVO = shoppingOrderQueryVO;
        }

        @Override
        public Predicate toPredicate(Root<ShoppingOrder> shoppingOrder, CriteriaQuery<?> query,
                                     CriteriaBuilder builder) {
            Path<Integer> status = shoppingOrder.get("status");
            Path<String> uId = shoppingOrder.get("uId");
            Path<String> ordertype = shoppingOrder.get("ordertype");
            
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(uId, shoppingOrderQueryVO.uid));
            
            if ("0".equals(shoppingOrderQueryVO.typ))
    		{
            	 predicates.add(builder.greaterThanOrEqualTo(status,0));
    		}	
            if ("1".equals(shoppingOrderQueryVO.typ))
            {
            	predicates.add(builder.or(builder.equal(status, 0), builder.equal(status, 22)));
            }	
            if ("2".equals(shoppingOrderQueryVO.typ))
            {
            	predicates.add(builder.or(builder.and(builder.greaterThan(status, 0),builder.lessThan(status, 3)), builder.equal(status, 21)));
            }
            if ("3".equals(shoppingOrderQueryVO.typ))
            {
            	predicates.add(builder.or(builder.equal(status, 15), builder.equal(status, 3), builder.equal(status, 16)));
            }
            if ("4".equals(shoppingOrderQueryVO.typ))
    		{
            	 predicates.add(builder.equal(status,5));
    		}
            if ("5".equals(shoppingOrderQueryVO.typ))
    		{
            	predicates.add(builder.greaterThanOrEqualTo(status,0));
            	predicates.add(builder.equal(ordertype,3));
    		}
            predicates.add(builder.greaterThanOrEqualTo(status,0));
            Predicate[] param = new Predicate[predicates.size()];
            predicates.toArray(param);
            
            return query.where(param).getRestriction();
        }
    }
    
    public double getProductPrice(String orderCode,String pid){
    	double a=0;
    	String sql = "SELECT sp.price FROM `shopping_Order_Pro` sp,`shopping_Order` s WHERE s.id = sp.orderId AND sp.pid='"+pid+"' AND s.OrderCode='"+orderCode+"'";
    	List<PackageProductItem> result = new ArrayList<PackageProductItem>();
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				a=rs.getDouble("price");
			}
		} catch (Exception e) {
			a=0;
			e.printStackTrace();
		} finally {
			db.close();
		}
    	return a;
    }
	public List<PackageProductItem> getOutPardelsProduct_ByOrderCode(String orderCode, int status,String orderType) {
		String sql = "SELECT  p.*,c.rate,f.name as fromsite,sp.price,sp.counts FROM `shopping_Order_Pro` sp,product p,`shopping_Order` s,currency c,fromsite f WHERE s.id = sp.orderId and p.currency=c.id and p.fromsite=f.id and sp.pid = p.pid AND sp.flg<>'3' AND s.OrderCode='"+orderCode+"'";
		if (status!=99)
		{
			sql = "SELECT  p.*,c.rate,f.name as fromsite,sp.price,sp.counts FROM `shopping_Order_Pro` sp,product p,`shopping_Order` s,currency c,fromsite f WHERE s.id = sp.orderId and p.currency=c.id and p.fromsite=f.id and sp.pid = p.pid AND sp.flg<>'1' AND sp.flg<>'3' AND s.OrderCode='"+orderCode+"'";
		}
		List<PackageProductItem> result = new ArrayList<PackageProductItem>();
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				PackageProductItem packageProductItem = new PackageProductItem();
				packageProductItem.pid=rs.getString("pid");
				 packageProductItem.skucode=rs.getString("skucode");
				 packageProductItem.title=rs.getString("title");
				 packageProductItem.exturl=rs.getString("exturl");
				 packageProductItem.listpic=StringUtil.getListpic(rs.getString("listpic"));
				 packageProductItem.specifications=rs.getString("specifications");
				 packageProductItem.counts=rs.getString("counts");
				 BigDecimal rate = new BigDecimal(rs.getDouble("rate")/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
				 BigDecimal price = new BigDecimal(rs.getDouble("price")/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
				 BigDecimal freight = new BigDecimal(rs.getDouble("freight") );
				 BigDecimal counts = new BigDecimal(rs.getInt("counts") );
				 BigDecimal totalfee = freight.add(price).multiply(counts);
				 packageProductItem.price=totalfee.toString();
				 packageProductItem.rmbprice=freight.add(price).doubleValue();
				 if ("2".equals(orderType)  || "5".equals(orderType)){
					if (rs.getString("lovelydistinct")!=null && ".0".equals(rs.getString("lovelydistinct").substring(rs.getString("lovelydistinct").length()-2, rs.getString("lovelydistinct").length())))
					{
						//"撒娇再享"+lovelydistinct.substring(0, lovelydistinct.length()-2)+"折";
						packageProductItem.toast= "撒娇已享受"+rs.getString("lovelydistinct").substring(0, rs.getString("lovelydistinct").length()-2).replace(".0", "")+"折";
					}else{
						packageProductItem.toast = "撒娇已享受"+rs.getString("lovelydistinct").replace(".0", "")+"折";
					}
						
				}else{
					packageProductItem.toast ="";
				}
				 packageProductItem.paytime=rs.getString("paytim");
				 result.add(packageProductItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public List<PackageProductItem> getRefundProduct_ByOrderCode(String orderCode, int status,String orderType) {
		String sql = "SELECT  p.*,c.rate,f.name as fromsite,sp.price,sp.counts FROM `shopping_Order_Pro` sp,product p,`shopping_Order` s,currency c,fromsite f WHERE s.id = sp.orderId and p.currency=c.id and p.fromsite=f.id and sp.pid = p.pid AND sp.flg='3' AND s.OrderCode='"+orderCode+"'";
		List<PackageProductItem> result = new ArrayList<PackageProductItem>();
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				PackageProductItem packageProductItem = new PackageProductItem();
				packageProductItem.pid=rs.getString("pid");
				 packageProductItem.skucode=rs.getString("skucode");
				 packageProductItem.title=rs.getString("title");
				 packageProductItem.exturl=rs.getString("exturl");
				 packageProductItem.listpic=StringUtil.getListpic(rs.getString("listpic"));
				 packageProductItem.specifications=rs.getString("specifications");
				 packageProductItem.counts=rs.getString("counts");
				 BigDecimal rate = new BigDecimal(rs.getDouble("rate")/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
				 BigDecimal price = new BigDecimal(rs.getDouble("price")/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
				 BigDecimal freight = new BigDecimal(rs.getDouble("freight") );
				 BigDecimal counts = new BigDecimal(rs.getInt("counts") );
				 BigDecimal totalfee = freight.add(price).multiply(counts);
				 packageProductItem.price=totalfee.toString();
				 packageProductItem.rmbprice=freight.add(price).doubleValue();
				 if ("2".equals(orderType)  || "5".equals(orderType)){
					if (rs.getString("lovelydistinct")!=null && ".0".equals(rs.getString("lovelydistinct").substring(rs.getString("lovelydistinct").length()-2, rs.getString("lovelydistinct").length())))
					{
						//"撒娇再享"+lovelydistinct.substring(0, lovelydistinct.length()-2)+"折";
						packageProductItem.toast= "撒娇已享受"+rs.getString("lovelydistinct").substring(0, rs.getString("lovelydistinct").length()-2).replace(".0", "")+"折";
					}else{
						packageProductItem.toast = "撒娇已享受"+rs.getString("lovelydistinct").replace(".0", "")+"折";
					}
						
				}else{
					packageProductItem.toast ="";
				}
				 packageProductItem.paytime=rs.getString("paytim");
				 result.add(packageProductItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	
	public ShoppingOrderCostItem getShoppingOrderCostList(Long uid,Long pid,String priceFrom,String counts,String fname,String wayremark) {
		ShoppingOrderCostItem result = new ShoppingOrderCostItem();
		List<Object> p_list = new ArrayList<Object>();
		String sql="SELECT p.*,f.name AS fname,f.img AS fimg,c.symbol AS symbol,c.rate as rate FROM product p,fromsite f,currency c WHERE p.fromsite=f.id AND p.currency=c.id AND p.pid='"+pid+"'";
		logger.info(sql);
		int totalRow = 0;
		JdbcOperWithClose db2 = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		try {
			db2.getPrepareStateDao(sql);
			ResultSet rs2 = db2.pst.executeQuery();// 执行语句，得到结果集
			while(rs2.next()){
				totalRow++;
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			db2.close();
		}
		
		String weight="0";
		BigDecimal tfreight=new BigDecimal(0);
		BigDecimal tweight=new BigDecimal(0);
		BigDecimal tfreight_new=new BigDecimal(0);
		BigDecimal tgoodsfee=new BigDecimal(0);
		BigDecimal totalfee=new BigDecimal(0);
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String fromsite = rs.getString("fname");
				String fromsiteimg = rs.getString("fimg");
				String wayremarkTemp = rs.getString("wayremark");
				Integer fromsiteId = rs.getInt("fromsite");
				weight = rs.getString("weight");
				String symbol = rs.getString("symbol");
				String listpic =rs.getString("listpic");
				Integer limitcount =rs.getInt("limitcount");
				Integer nstock=rs.getInt("nstock"); 
				Integer ishot=rs.getInt("ishot"); 
				if(ishot!=null && ishot.intValue()==1){
					nstock = (int) productService.dealNstockWithProduct(pid);
				}
				String specifications =rs.getString("specifications");
				
				if(!fname.equals(rs.getString("fname"))||!wayremark.equals(rs.getString("wayremark"))){
					ShoppingCartCategoryVO shoppingCartCategoryVO=new ShoppingCartCategoryVO();
					shoppingCartCategoryVO.setFromsite(fromsite);
					shoppingCartCategoryVO.setFromsiteimg(fromsiteimg);
					shoppingCartCategoryVO.setTyp("1");
					shoppingCartCategoryVO.setWayremark(wayremarkTemp);
					wayremark = wayremarkTemp;
					p_list.add(shoppingCartCategoryVO);
					
				}
				result.fname=rs.getString("fname");
				result.wayremark=rs.getString("wayremark");
				ShoppingCartItemVO shoppingCartItemVO = new ShoppingCartItemVO();
				shoppingCartItemVO.fromsite = fromsite;
				shoppingCartItemVO.fromsiteimg = fromsiteimg;
				shoppingCartItemVO.typ="2";
				wayremark=wayremark.trim();
				fname=fromsite;
				shoppingCartItemVO.pid = rs.getString("pid");
				shoppingCartItemVO.pcode = rs.getString("skucode");
				if ( "3".equals(rs.getString("ptyp")))
				{
					shoppingCartItemVO.linkurl = "presellDetail://pid="+rs.getString("pid");;
				}else{
					shoppingCartItemVO.linkurl = "pDe://pid="+rs.getString("pid");;
				}
				shoppingCartItemVO.title = rs.getString("title");
				shoppingCartItemVO.subtitle = rs.getString("subtitle");
				shoppingCartItemVO.currency = rs.getString("currency");
				shoppingCartItemVO.adstr1 = rs.getString("adstr1");
				
				Double chinaprice = rs.getDouble("chinaprice");
				BigDecimal bigChinaPrice = new BigDecimal(chinaprice);
				shoppingCartItemVO.chinaprice = "¥"+bigChinaPrice.setScale(2, BigDecimal.ROUND_CEILING);

				BigDecimal price = new BigDecimal(rs.getDouble("price")/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
				BigDecimal rate = new BigDecimal(rs.getDouble("rate")/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
				BigDecimal list_price = new BigDecimal(rs.getDouble("list_price")/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
				
				shoppingCartItemVO.rate = rate.toString();
				Integer rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING).intValue();
				if ("1".equals(rs.getString("islockprice"))) {
					rmb_price = new BigDecimal(rs.getDouble("rmbprice")).setScale(0, BigDecimal.ROUND_CEILING).intValue();
				} 
				//新人价
				if(userService.checkFirstFlag(uid.toString())){
					ProductPriceExt pe=productService.getProductPrice(pid,Constants.getSystemGroupOne("newman"),"rmbprice");
					if(pe!=null)
						rmb_price=Integer.valueOf(new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING).toString());
				}
				BigDecimal freight_o = new BigDecimal(ShoppingCartService.getfreight(fromsiteId, weight));
				
//				BigDecimal totalfeeTemp =freight_o.add(new BigDecimal(rmb_price));
				shoppingCartItemVO.logisticsFee = String.valueOf(freight_o.intValue());
				shoppingCartItemVO.rmb_price =  "¥"+rmb_price;
				shoppingCartItemVO.rmbprice = String.valueOf(rmb_price);
				
				if ("円".equals(symbol))
				{
					shoppingCartItemVO.price= price + symbol;
					shoppingCartItemVO.list_price = list_price+ symbol;
				}
				else
				{
					shoppingCartItemVO.price = symbol+price;
					shoppingCartItemVO.list_price = symbol+list_price;
				}
				
				BigDecimal discount = list_price.intValue()==0 ? new BigDecimal(0) : price.divide(list_price,2,BigDecimal.ROUND_CEILING).multiply(new BigDecimal(10)).setScale(1, BigDecimal.ROUND_CEILING);
				Double discountDouble = discount.doubleValue();
				shoppingCartItemVO.discount =  (discountDouble>10?10:discountDouble )+"";
				shoppingCartItemVO.img = StringUtil.getListpic(listpic);
				
				
				if (nstock<=limitcount && nstock>=0)
				{
					shoppingCartItemVO.limitcount=nstock+"";
				}else{
					shoppingCartItemVO.limitcount=limitcount+""; 
				}
				int countsInt = Numbers.parseInt(counts, 0);
				if (nstock>=countsInt)
				{
					if (limitcount<countsInt && limitcount>0)
					{
						shoppingCartItemVO.counts=limitcount+"";
					}else{
						shoppingCartItemVO.counts=countsInt+"";
					}
				}else{
					if (limitcount<nstock && limitcount>0)
					{
						shoppingCartItemVO.counts=limitcount+"";
					}else{
						shoppingCartItemVO.counts=nstock+"";
					}
				}
				shoppingCartItemVO.specifications=specifications;
				if(!"a".equals(priceFrom))
				{
					shoppingCartItemVO.iscoupon="0";
					if(Numbers.parseInt(shoppingCartItemVO.counts, 0)>0){
						tgoodsfee = tgoodsfee.add(new BigDecimal(shoppingCartItemVO.rmbprice).multiply(new BigDecimal(counts)));
						tfreight=tfreight.add(freight_o.multiply(new BigDecimal(counts)));
						tfreight_new = tfreight_new.add(tfreight);
						totalfee=totalfee.add(new BigDecimal(shoppingCartItemVO.rmbprice).multiply(new BigDecimal(counts)));
						tweight=tweight.add(new BigDecimal(weight).multiply(new BigDecimal(counts)));
					}
				}else{
					shoppingCartItemVO.iscoupon="1";
				}
				p_list.add(shoppingCartItemVO);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		result.p_list = p_list;
		result.goods_fee = tgoodsfee.toString();
		result.foreignfee = tfreight_new.toString();
		result.total_fee = totalfee.intValue();
		return result;
	
	}

	public ProductNewVO OrderPay(String orderId,String usewallet) {
		String sql = "{call sp_order_pay ('"+orderId+"','"+usewallet+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductNewVO reslut = new ProductNewVO();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				reslut.orderCode=rs.getString("orderCode");
				reslut.orderId = rs.getInt("orderId");
				reslut.orderCode_Pay = rs.getString("orderCode_Pay");
				reslut.totalfee = rs.getDouble("totalfee");
				reslut.status=1;
				reslut.msg="";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return reslut;
	}

	
	public ProductNewVO newOrder(String uid, String addressid,
			BigDecimal p_domestic_fee, BigDecimal p_foreignfee,
			BigDecimal p_tariff_fee, BigDecimal p_cost_fee,
			BigDecimal p_total_fee, BigDecimal p_goods_fee, String p_typ,
			String p_lovely, int p_couponUserId, String p_coupon,
			Double p_coupon_price, Double p_distinct, BigDecimal p_deposit,
			BigDecimal p_finalpay, Double p_original_fee,String usewallet,String deviceId,String IPaddress,String pidstr) {
		String sql = "{call sp_order_add ('"+uid+"','"+addressid+"','"+p_domestic_fee.doubleValue()+"','"+p_foreignfee.doubleValue()+"','"+p_tariff_fee.doubleValue()+"','"+p_cost_fee.doubleValue()+"','"+p_total_fee.doubleValue()+"','"+p_goods_fee.doubleValue()+"','"+p_typ+"','"+p_lovely+"','"+p_couponUserId+"','"+p_coupon+"','"+p_coupon_price+"','"+p_distinct+"','"+p_deposit.doubleValue()+"','"+p_finalpay.doubleValue()+"','"+p_original_fee.doubleValue()+"','"+usewallet+"','"+deviceId+"','"+IPaddress+"','"+pidstr+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductNewVO reslut = new ProductNewVO();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				reslut.orderCode=rs.getString("orderCode");
				reslut.orderId = rs.getInt("orderId");
				reslut.orderCode_Pay = rs.getString("orderCode_Pay");
				reslut.totalfee = rs.getDouble("totalfee");
				reslut.status=1;
				reslut.msg=rs.getString("msg");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return reslut;
	}

	//app端内购买代言商品进行购买日志及反利操作
	public ProductNewVO newOrderEndorse(String uid, String addressid,
			BigDecimal p_domestic_fee, BigDecimal p_foreignfee,
			BigDecimal p_tariff_fee, BigDecimal p_cost_fee,
			BigDecimal p_total_fee, BigDecimal p_goods_fee, String p_typ,
			String p_lovely, int p_couponUserId, String p_coupon,
			Double p_coupon_price, Double p_distinct, BigDecimal p_deposit,
			BigDecimal p_finalpay, Double p_original_fee,String usewallet,String daiyanid) {
		String sql = "{call sp_order_add_app_daiyan ('"+uid+"','"+addressid+"','"+p_domestic_fee.doubleValue()+"','"+p_foreignfee.doubleValue()+"','"+p_tariff_fee.doubleValue()+"','"+p_cost_fee.doubleValue()+"','"+p_total_fee.doubleValue()+"','"+p_goods_fee.doubleValue()+"','"+p_typ+"','"+p_lovely+"','"+p_couponUserId+"','"+p_coupon+"','"+p_coupon_price+"','"+p_distinct+"','"+p_deposit.doubleValue()+"','"+p_finalpay.doubleValue()+"','"+p_original_fee.doubleValue()+"','"+usewallet+"','"+daiyanid+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductNewVO reslut = new ProductNewVO();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				reslut.orderCode=rs.getString("orderCode");
				reslut.orderId = rs.getInt("orderId");
				reslut.orderCode_Pay = rs.getString("orderCode_Pay");
				reslut.totalfee = rs.getDouble("totalfee");
				reslut.status=1;
				reslut.msg="";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return reslut;
	}
	//推广使用
	public ProductNewVO newOrderTG(String uid, String addressid,
			BigDecimal p_domestic_fee, BigDecimal p_foreignfee,
			BigDecimal p_tariff_fee, BigDecimal p_cost_fee,
			BigDecimal p_total_fee, BigDecimal p_goods_fee, String p_typ,
			String p_lovely, int p_couponUserId, String p_coupon,
			Double p_coupon_price, Double p_distinct, BigDecimal p_deposit,
			BigDecimal p_finalpay, Double p_original_fee,String usewallet,String mcode,Long pid) {
		String sql = "{call sp_order_add_tg('"+uid+"','"+addressid+"','"+p_domestic_fee.doubleValue()+"','"+p_foreignfee.doubleValue()+"','"+p_tariff_fee.doubleValue()+"','"+p_cost_fee.doubleValue()+"','"+p_total_fee.doubleValue()+"','"+p_goods_fee.doubleValue()+"','"+p_typ+"','"+p_lovely+"','"+p_couponUserId+"','"+p_coupon+"','"+p_coupon_price+"','"+p_distinct+"','"+p_deposit.doubleValue()+"','"+p_finalpay.doubleValue()+"','"+p_original_fee.doubleValue()+"','"+mcode+"','"+pid+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductNewVO reslut = new ProductNewVO();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				reslut.orderCode=rs.getString("orderCode");
				reslut.orderId = rs.getInt("orderId");
				reslut.orderCode_Pay = rs.getString("orderCode_Pay");
				reslut.totalfee = rs.getDouble("totalfee");
				reslut.status=1;
				reslut.msg=rs.getString("msg");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		if(StringUtils.isBlank(reslut.orderCode))
			reslut.status=0;
		return reslut;
	}
	/*
	 * 生成订单,代言使用
	 */
	public ProductNewVO newOrder(String uid, String addressid,
			BigDecimal p_domestic_fee, BigDecimal p_foreignfee,
			BigDecimal p_tariff_fee, BigDecimal p_cost_fee,
			BigDecimal p_total_fee, BigDecimal p_goods_fee, String p_typ,
			String p_lovely, int p_couponUserId, String p_coupon,
			Double p_coupon_price, Double p_distinct, BigDecimal p_deposit,
			BigDecimal p_finalpay, Double p_original_fee,Long eid,String iswx,String shareType,String usewallet) {
		String sql = "{call sp_order_add_daiyan('"+uid+"','"+addressid+"','"+p_domestic_fee.doubleValue()+"','"+p_foreignfee.doubleValue()+"','"+p_tariff_fee.doubleValue()+"','"+p_cost_fee.doubleValue()+"','"+p_total_fee.doubleValue()+"','"+p_goods_fee.doubleValue()+"','"+p_typ+"','"+p_lovely+"','"+p_couponUserId+"','"+p_coupon+"','"+p_coupon_price+"','"+p_distinct+"','"+p_deposit.doubleValue()+"','"+p_finalpay.doubleValue()+"','"+p_original_fee.doubleValue()+"','"+eid+"','"+iswx+"','"+shareType+"','"+usewallet+"')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		ProductNewVO reslut = new ProductNewVO();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				reslut.orderCode=rs.getString("orderCode");
				reslut.orderId = rs.getInt("orderId");
				reslut.orderCode_Pay = rs.getString("orderCode_Pay");
				reslut.totalfee = rs.getDouble("totalfee");
				reslut.msg=rs.getString("msg");
				reslut.status=1;
//				if(reslut.orderId>0)
//					reslut.status=1;
//				else
//					reslut.status=0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return reslut;
	}
	public void addProductToOrder(Integer orderId, String pid, String price,
			String cnt,String daiyanid) {
		String sql = "{call sp_orderPro_add ('"+orderId+"','"+pid+"','"+price+"','"+cnt+"','"+daiyanid+"','0')}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.executeQuery();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	//修改订单变更代言编号
	public void editorderEid(Integer orderId,Long eid){
		String sql = "update shopping_Order set endorsementid="+eid+" where id="+orderId;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			db.pst.executeQuery();// 执行语句，得到结果集
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	public List<OrderLoveLyBack> getOrderLovelyBack(String orderCode) {
		return orderLovelyBackRepository.findByOrderCode(orderCode);
	}

	public String getpaytim(String orderCode, String flag) {
		String result = "";
		String sql="SELECT MAX(paytime) AS tim FROM shopping_OrderPay WHERE OrderCode LIKE '"+orderCode+"%' GROUP BY LEFT('"+orderCode+"',10) HAVING COUNT(id)>1";
		if("0".equals(flag)){
			sql="SELECT MIN(paytime) AS tim FROM shopping_OrderPay WHERE OrderCode LIKE '"+orderCode+"%' ";
		}
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Date resultDate = rs.getTimestamp("tim");
				if(resultDate!=null)
					result = CHINESE_DATE_TIME_FORMAT.format(resultDate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public ShoppingOrder getOrderInfo_ByPackId(String packId) {
		return shoppingOrderRepository.queryShoppingOrderByParcelId(packId);
	}

	public ShoppingOrderWayBillVO getPardelsWaybill_ByPardelsId(
			String packId, ShoppingOrderWayBillVO result) {
		List<ShoppingOrderWayBillItem> shoppingOrderWayBillItemList = new ArrayList<ShoppingOrderWayBillItem>();
		String sql="SELECT p.src,p.status as pstatus,w.* FROM `pardels_Waybill` w,`pardels`p WHERE  w.pardelsId=p.id AND w.pardelsId='"+packId+"' ORDER BY nsort DESC,date_txt DESC ";
		logger.info(sql);
		
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.nowflag = rs.getString("pstatus");
				result.packagestatus = "";
				result.src = rs.getString("src");
				if ("4".equals(result.src)){
					result.src ="2";	
				}
				int pstatus = rs.getInt("pstatus");
				switch (pstatus) {
				case 0:
					result.packagestatus = "1";
					break;
				case 1:
					result.packagestatus = "1";
					break;
				case 2:
					result.packagestatus = "2";
					break;
				case 3:
					result.packagestatus = "2";
					break;
				case 4:
					result.packagestatus = "2";
					break;
				case 5:
					result.packagestatus = "3";
					break;
				case 11:
					result.packagestatus = "2";
					break;
				case 12:
					result.packagestatus = "3";
					break;
				}
				ShoppingOrderWayBillItem item = new ShoppingOrderWayBillItem();
				item.waybillCode = rs.getString("waybillCode");
				item.transport = rs.getString("transport");
				item.date_txt = rs.getString("date_txt");
				item.remark = rs.getString("remark");
				shoppingOrderWayBillItemList.add(item);
				List<ShoppingOrderWayBillItem> tempList = getPardelsWaybillInfo_ByWaybillId(rs.getString("id"),item.waybillCode,item.transport);
				if(tempList.size()>0){
					shoppingOrderWayBillItemList.addAll(tempList);
				}
				result.data = shoppingOrderWayBillItemList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
	public List<ShoppingOrderWayBillItem> getPardelsWaybillInfo_ByWaybillId(String waybillId,String waybillCode,String transport){
		List<ShoppingOrderWayBillItem> result = new ArrayList<ShoppingOrderWayBillVO.ShoppingOrderWayBillItem>();
		String sql="SELECT * FROM `pardels_Waybill_info` WHERE wayBillId='"+waybillId+"' order by nsort desc,date_txt DESC";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ShoppingOrderWayBillItem item = new ShoppingOrderWayBillItem();
				item.waybillCode = waybillCode;
				item.transport = transport;
				item.date_txt = rs.getString("date_txt");
				item.remark = rs.getString("remark");
				result.add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	public List<Product> getproductListByOrderCode(String orderCode) {
		List<Product> productList = productRepository.getproductListByOrderCode(orderCode);
		for(Product product:productList){
			product.setCounts(this.productRepository.queryOrderProductCounts(orderCode,product.getPid()));
		}
		return productList;
	}

	public ShoppingOrder checkOrderAmountStat(String orderCode, Double amount) {
		return shoppingOrderRepository.findByOrderCodeAndTotalFee(orderCode,amount);
	}
    

/*
    //根据订单编号获取订单扩展信息
	public ShoppingOrderPro getOrderpro(Long orderid){
		ShoppingOrderPro pro=null;
		String sql="SELECT * FROM `shopping_Order_Pro` WHERE orderId="+orderid;
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				pro=new ShoppingOrderPro();
				pro.setId(rs.getLong("id"));
				pro.setCid(rs.getInt("cid"));
				pro.setFlg(rs.getInt("flg"));
				pro.setCounts(rs.getInt("counts"));
				pro.setPid(rs.getLong("pid"));
				pro.setOrderId(rs.getLong("orderId"));
				pro.setPrice(rs.getDouble("price"));
				pro.setPrice(rs.getDouble("price"));
				pro.setTotalFee(rs.getDouble("totalFee"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return pro;
	}
*/	
	/*
	 * 根据订单编号获取产品销售列表
	 */
	public List<OrderProduct> getOrderproList(String orderCode){
		OrderProduct pro=null;
		String sql="SELECT p.* FROM shopping_Order_Pro p,shopping_Order o WHERE p.`orderId`=o.`orderId` AND o.`orderCode`='"+orderCode+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<OrderProduct> plist=new ArrayList<OrderProduct>();		
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				pro=new OrderProduct();
				pro.setId(rs.getLong("id"));
				pro.setCid(rs.getLong("cid"));
				pro.setCounts(rs.getInt("counts"));
				pro.setpId(rs.getLong("pid"));
				pro.setOrderId(rs.getLong("orderId"));
				pro.setPrice(rs.getDouble("price"));
				pro.setTotalFee(rs.getDouble("totalFee"));
				pro.setFlg(rs.getString("flg"));
				plist.add(pro);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return plist;
	}
	/*
	 * 添加扩展订单表，棒棒糖使用
	 */
	public void addShoppingOrderEx(String billnum,String ordercode,String actname){
		String sql="select count(1) as count from shopping_order_ex where billnum='"+billnum+"' and ordercode='"+ordercode+"' and actname='"+actname+"'";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer count=0;
		try{
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			if(rs.next()){
				count=rs.getInt("count");
			}
			if(count<1){
				sql="insert into shopping_order_ex(createTime,billnum,ordercode,actname) values('"+CHINESE_DATE_TIME_FORMAT.format(new Date())+"','"+billnum+"','"+ordercode+"','"+actname+"')";
				db.getPrepareStateDao(sql);
				db.pst.execute();
			}
		}catch(Exception e){}
		finally{
			db.close();
		}
	}
	
	//根据订单编号获取代言列表
	public List<Endorsement> getEndorselistByOrderId(Long orderId){
		String sql="SELECT e.*,pp.*,p.counts FROM shopping_Order_Pro p,endorsementduct e,product pp WHERE p.endorsementid=e.eid and pp.pId=e.productId AND p.orderId="+orderId;
		JdbcOper db = JdbcOper.getInstance();
		List<Endorsement> elist=new ArrayList<Endorsement>();
		try{
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				Endorsement em=new Endorsement();
				em.setEid(rs.getLong("eid"));
				em.setEndorsementPrice(rs.getDouble("endorsementPrice"));
				em.setUserId(rs.getLong("userId"));
				em.setGid(rs.getLong("gid"));
				em.setProductId(rs.getLong("productId"));
				Product p=new Product();
				p.setPid(rs.getLong("pid"));
				p.setRmbprice(rs.getDouble("rmbprice"));
				p.setChinaprice(rs.getDouble("chinaprice"));
				p.setList_price(rs.getDouble("list_price"));
				p.setListpic(rs.getString("listpic"));
				p.setTitle(rs.getString("title"));
				p.setSubtitle(rs.getString("subtitle"));
				em.setCount(rs.getInt("counts"));
				em.setProducinfo(p);
				elist.add(em);
			}
		}catch(Exception e){}
		finally{
			db.close();
		}
		
		if(elist==null || elist.isEmpty())
			elist=null;
		return elist;
	}

	/**
	 * 检查这些商品ID串与addressid是否可购买
	 * @param pids
	 * @param addressid
	 * @return
	 */
	public ObjectNode checkOrderWithAddress(String pids, Long addressid) {
		ObjectNode result = Json.newObject();
		String sql = "{call sp_genOrder_check('"+pids+"',"+addressid+")}";
		logger.info(sql);
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				result.put("status", rs.getInt(1));
				result.put("toast", rs.getString(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}
}
