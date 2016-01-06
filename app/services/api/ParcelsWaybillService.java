package services.api;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import models.Parcels;
import models.ParcelsWaybill;
import models.Product;
import models.ProductDetail;
import models.ProductDetailPram;
import models.Product_images;
import models.ShoppingOrder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import play.Configuration;
import play.Logger;
import repositories.CategoryRepository;
import repositories.CurrencyRepository;
import repositories.FromSiteRepository;
import repositories.OrderProductRepository;
import repositories.ParcelsRepository;
import repositories.ParcelsWaybillRepository;
import repositories.ProductImageRepository;
import repositories.ProductRepository;
import repositories.ShoppingOrderRepository;
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
 * 包裹相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ParcelsWaybillService {

    private static final Logger.ALogger logger = Logger.of(ProductService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private ParcelsWaybillRepository parcelsWaybillRepository;
    
    /**
     * 根据Erp返回的运单号，在pardels_Waybill表中生成一条记录
     * @param mail_no
     */
	public void addNewWaybillWith(Long pid, String mail_no, String expressCode) {
		ParcelsWaybill pwb = parcelsWaybillRepository.findByWaybillCode(mail_no);
		if(pwb==null){
			pwb = new ParcelsWaybill();
		}
		pwb.setDate_add(new Date());
		pwb.setPardelsId(pid);
		pwb.setWaybillCode(mail_no);
		pwb.setStatus("0");
		pwb.setDate_upd(CHINESE_DATE_TIME_FORMAT.format(new Date()));
		pwb.setDate_txt(CHINESE_DATE_TIME_FORMAT.format(new Date()));
		pwb.setNsort(0);
		if("YUNDA".equals(expressCode)){
			pwb.setTransport("韵达快递");
			pwb.setRemark("由韵达快递发货，快递单号："+mail_no);
			pwb.setTrancode("yunda");
		}else{
			pwb.setTransport("顺丰速运");
			pwb.setRemark("由顺丰速运发货，快递单号："+mail_no);
			pwb.setTrancode("shunfeng");
		}
		parcelsWaybillRepository.save(pwb);
	}
    
    
   
}
