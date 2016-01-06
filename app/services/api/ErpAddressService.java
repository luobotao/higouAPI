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
import models.ErpAddress;
import models.Fromsite;
import models.Parcels;
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
import repositories.ErpAddressRepository;
import repositories.FromSiteRepository;
import repositories.OrderProductRepository;
import repositories.ParcelsRepository;
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

/**ERP对应包裹地址相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ErpAddressService {

    private static final Logger.ALogger logger = Logger.of(ErpAddressService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private ErpAddressRepository erpAddressRepository;
    
    /**
     * 根据包裹地址处理成ERP省市区信息
     * @param province
     * @return
     */
	public String[] dealParcelsWithProvince(String[] strs, String province) {
		List<ErpAddress> addresses = erpAddressRepository.getErpAddressWithProvince(province);
		if(addresses!=null&&addresses.size()>0){
			for (ErpAddress erpAddress : addresses) {
				if(!StringUtils.isBlank(erpAddress.getName())){
					int tier = erpAddress.getTier();
					//用于判断parentid是否等于上一层的id
					if(tier>1){
						if(!StringUtils.isBlank(strs[tier-2])){
							String[] tempStr = strs[tier-2].split("-");
							if(tempStr[0].equals(String.valueOf(erpAddress.getParentid()))){
								strs[tier-1]=erpAddress.getId()+"-"+erpAddress.getName();
							}
						}
					}else{
						strs[tier-1]=erpAddress.getId()+"-"+erpAddress.getName();
					}
				}
			}
		}
		return strs;
	}
    
    
    
}
