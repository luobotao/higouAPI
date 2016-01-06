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
import models.Product;
import models.ProductDetail;
import models.ProductDetailPram;
import models.Product_images;
import models.ShoppingOrder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import play.Configuration;
import play.Logger;
import repositories.CategoryRepository;
import repositories.CurrencyRepository;
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

/**
 * 包裹相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ParcelsService {

    private static final Logger.ALogger logger = Logger.of(ProductService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private ParcelsRepository parcelsRepository;
    
    public Parcels getParcelsById(Long id) {
        return parcelsRepository.findOne(id);
    } 
    
    @Transactional(readOnly = true)
	public Parcels getParcelsByParcelCode(String pardelCode) {
		if(StringUtils.isBlank(pardelCode)){
    		return null;
    	}
		 return parcelsRepository.getParcelsByParcelCode(pardelCode);
	}

	public Parcels saveParcels(Parcels parcels) {
		return parcelsRepository.save(parcels);
	}

	public List<Parcels> findParcelsWithDateAdd(Date lasttime) {
		return parcelsRepository.findParcelsWithDateAdd(lasttime);
	}

	/**
	 * 获取到当前时间段内所有的包裹的商品newsku
	 * <p>Title: findNewSkusWithDateAdd</p> 
	 * <p>Description: </p> 
	 * @param lasttime
	 * @return
	 */
	public List<String> findNewSkusWithDateAdd(Date lasttime) {
		String sql="SELECT DISTINCT pd.newSku as newSku FROM pardels p,pardels_Pro pp,product pd WHERE p.id = pp.pardelsId AND pp.pid = pd.pid AND pd.typ=2 and pd.newSku<>'' and p.date_add >= '"+lasttime+"'";
		logger.info(sql);
		List<String> newSkus = new ArrayList<String>();
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				newSkus.add(rs.getString("newSku"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return newSkus;
	}
    
    
   
}
