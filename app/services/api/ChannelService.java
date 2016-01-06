package services.api;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.ChannelMouldPro;
import models.Product;
import models.Subject;

import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import play.Logger;
import repositories.ChannelMouldProRepository;
import repositories.ProductRepository;
import repositories.SubjectRepository;
import repositories.UserRepository;
import services.ServiceFactory;
import utils.Constants;
import utils.JdbcOperWithClose;
import utils.Numbers;
import utils.StringUtil;
import vo.channel.ChannelMouldVO;
import vo.subject.SubjectMouldVO.PInfo;

/**
 * 频道相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class ChannelService {

    private static final Logger.ALogger logger = Logger.of(ChannelService.class);

    @Inject
    private UserRepository userRepository;
    @Inject
    private SubjectRepository subjectRepository;
    @Inject
    private ProductRepository productRepository;
    @Inject
    private ProductService productService;
    @Inject
    private ChannelMouldProRepository channelMouldProRepository;
    
	public List<Object[]> getChannelList() {
		return userRepository.getChannelList();
		
	}

	public String getChannel_Model_tag(Integer cid) {
		return userRepository.getChannel_Model_tag(cid);
	}



	public String getChannel_Model_RefreshNum(Integer cid,Integer tag) {
		return userRepository.getChannel_Model_RefreshNum(cid,tag);
	}



	public List<ChannelMouldVO.ChannelDataInfo> getChannel_Model(Integer cid, Integer page,
			Integer pagesize,ProductService productService,String uid,String appversion,String resolution) {
		//String sql="SELECT c.id,c.title,c.datetxt,c.nsort,c.flag,c.sectionPic,m.mname,m.typ,m.structure,m.stream,m.cnt FROM channel_mould c,mould m WHERE m.id=c.mouldId  AND c.flag='1' AND c.cid='"+cid+"'  ORDER BY c.nsort DESC,id DESC  LIMIT "+page*pagesize+","+pagesize+"";
		String sql="CALL sp_channelMould_get("+cid+","+page+","+pagesize+","+uid+")";
		logger.info(sql);
		List<ChannelMouldVO.ChannelDataInfo> result = new ArrayList<ChannelMouldVO.ChannelDataInfo>();
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				ChannelMouldVO.ChannelDataInfo dataInfo = new ChannelMouldVO.ChannelDataInfo();
				dataInfo.cardId=rs.getString("id");
				dataInfo.mould=rs.getString("typ");
				dataInfo.struct=rs.getString("structure");
				dataInfo.stxt=rs.getString("title");
				
				String domain = StringUtil.getPICDomain();
				if(StringUtils.isBlank(rs.getString("sectionPic"))){
					dataInfo.sectionPic="";
				}else{
					dataInfo.sectionPic=domain + Configuration.root().getString("adload","/pimgs/adload/")+ rs.getString("sectionPic");
				}
				dataInfo.sdate=rs.getString("datetxt");
				String stream=rs.getString("stream");
				List<BigDecimal> divideList = new ArrayList<BigDecimal>();
				Integer divide =1;
				try {
					divide = Numbers.parseInt(dataInfo.struct.substring(dataInfo.struct.lastIndexOf('*')+1, dataInfo.struct.length()), 0);
				} catch (Exception e) {
					logger.info(e.toString());
				}
				
				List<ChannelMouldVO.LayoutInfo> dataList = new ArrayList<ChannelMouldVO.LayoutInfo>();
				for(String temp : stream.split("-")){
					ChannelMouldVO.LayoutInfo layoutInfo = new ChannelMouldVO.LayoutInfo();
					layoutInfo.start = temp.split("~")[0];
					layoutInfo.end =temp.split("~")[1] ;
					dataList.add(layoutInfo);
					try {
						Integer start =Numbers.parseInt(layoutInfo.start.substring(0, layoutInfo.start.lastIndexOf('_')), 0);
						Integer end =Numbers.parseInt(layoutInfo.end.substring(0, layoutInfo.end.lastIndexOf('_')), 0);
						if(end.intValue()>start.intValue() && end.intValue()!=0){
							BigDecimal divideTemp =new BigDecimal(divide).divide(new BigDecimal(end-start), 2, BigDecimal.ROUND_CEILING);
							divideList.add(divideTemp);
						}else{
							divideList.add(new BigDecimal(1));
						}
					} catch (Exception e) {
						logger.info(e.toString());
					}
				}
				dataInfo.layout = dataList;
				dataInfo.plist = getChannel_Model_Pro(rs.getString("id"),productService,uid,dataInfo.mould,cid,appversion,resolution,divideList);
				result.add(dataInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}

	/**
	 * 根据频道卡片ID获取该频道卡片商品列表
	 * @param mouldId
	 * @return
	 */
	public List<ChannelMouldPro> findChMoProListByCmId(Long cmId,Long channelId) {
		List<ChannelMouldPro> result = new ArrayList<ChannelMouldPro>();
		List<Long> ids = null;
		try{
			ids =(List<Long>) ServiceFactory.getCacheService().getObject(Constants.channelMouldProIds_KEY+cmId );
		}catch(Exception e){
			logger.info("findChMoProListByCmId err cache key:"+Constants.channelMouldProIds_KEY+cmId);
		}
		if(ids==null || ids.size()==0){
			ids=new ArrayList<Long>();
			List<Integer> tmpids = channelMouldProRepository.findIdsByCmid(cmId);
			if(tmpids!=null)
			{
				for(Integer tid:tmpids){
					if(tid!=null){
						ids.add(Numbers.parseLong(tid.toString().trim(), 0L));
					}
				}
				ServiceFactory.getCacheService().setObject(Constants.channelMouldProIds_KEY+cmId,ids,0 );
			}
		}
		if(ids!=null && ids.size()>0){
			for(Long id:ids){
				ChannelMouldPro ChannelMouldPro = findChMoPrById(id,channelId);
				if(ChannelMouldPro !=null){
					result.add(ChannelMouldPro);
				}
			}
		}
		return result;
	}
	/**
	 * 根据频道卡片商品ID获取该频道卡片商品
	 * @param mouldId
	 * @return
	 */
	public ChannelMouldPro findChMoPrById(Long id,Long channelId) {
		ChannelMouldPro channelMouldPro = (ChannelMouldPro) ServiceFactory.getCacheService().getObject(Constants.channel_mould_pro_KEY+id );//从缓存读入
    	if(channelMouldPro==null){
    		channelMouldPro = channelMouldProRepository.findOne(id);
    		if(channelMouldPro!=null){
    			String imag=channelMouldPro.getImgurl()==null?"":channelMouldPro.getImgurl().replace(StringUtil.getPICDomain(), "");
    			if(imag.indexOf("/pimgs/adload/")>=0 || imag.indexOf("/pimgs")>=0 ||imag.indexOf("/upload")>=0){
    				imag = StringUtil.getPICDomain()+imag;
    			}else{
    				Long pid=channelMouldPro.getPid();
    				if(pid==null || pid.longValue()==0){
    					imag = StringUtil.getPICDomain()+"/pimgs/adload/"+channelId+"/"+imag;
    				}else{
    					imag=StringUtil.getListpicOld(imag);
    				}
    			}
    			channelMouldPro.setImgurl(imag);
    		}
    		ServiceFactory.getCacheService().setObject(Constants.channel_mould_pro_KEY+id, channelMouldPro,0 );//写入缓存
    	}
		return channelMouldPro;
	}
	
	public List<ChannelMouldVO.ChannelPInfo> getChannel_Model_Pro(String mid,ProductService productService,String uid,String mould,Integer cid,String appversion,String resolution, List<BigDecimal> divideList) {
		String domain = StringUtil.getPICDomain();
		String domainstr = "";
//		String sql="SELECT c.*,p.title,p.typ as ptyp,p.ptyp as pptyp,p.skucode AS pcode FROM channel_mould_pro c LEFT JOIN product p ON c.pid =p.pid WHERE c.cmid='"+mid+"' order by c.nsort desc ";
		List<ChannelMouldVO.ChannelPInfo> plist = new ArrayList<ChannelMouldVO.ChannelPInfo>();
		List<ChannelMouldPro> chmoProList = findChMoProListByCmId(Numbers.parseLong(mid, 0L),Long.valueOf(cid));
		int i=0;
		for(ChannelMouldPro channelMouldPro:chmoProList){
			ChannelMouldVO.ChannelPInfo tempResult = new ChannelMouldVO.ChannelPInfo();
			String pcode = "";
			Product product = productService.getProductById(channelMouldPro.getPid());
			String imag = channelMouldPro.getImgurl();
			String linkurl = channelMouldPro.getLinkurl();
			if("http://".equals(linkurl)){
				tempResult.linkurl ="";
			}else{
				tempResult.linkurl =linkurl;
			}
			if(product!=null){
				pcode = product.getSkucode();
				if ("1".equals(product.getTyp())){
					if(!StringUtils.isBlank(imag) && (imag.indexOf("/pimgs/")>=0||imag.indexOf("/upload/")>=0)){
						domainstr = domain;
					}else{
						domainstr = domain+"/pimgs/adload/"+cid+"/";
					}
				}else{
					domainstr = domain;
				}
				if("3".equals(product.getPtyp()))
				{
					tempResult.linkurl="presellDetail://pid="+product.getPid();
				}
				tempResult.pinfo = productService.covertToProductChannelItem(product,productService,uid);
				tempResult.pinfo.cardMask = StringUtil.getProductIcon(product.getPid().intValue(), mould);
			}
			BigDecimal divide =new BigDecimal(1);
			if(divideList.size()-1>=i){
				divide= divideList.get(i);
			}
			i++;
			if("2".equals(mould)){//双图
				String image = StringUtil.getProductListpic(pcode,imag,resolution).replaceAll(domainstr, "");
				tempResult.img =  domainstr + image;
			}else{
				if("3".equals(mould)||"4".equals(mould)){//3metro 、4banner
					String image = StringUtil.getWebListpic("",imag,resolution,divide).replaceAll(domainstr, "");
					String kjdImagePre = Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre");
					if(image.startsWith(kjdImagePre)){
						tempResult.img = image;
					}else{
						tempResult.img =  domainstr + image;
					}
				}else{
					String image = StringUtil.getWebListpic(pcode,imag,resolution,divide).replaceAll(domainstr, "");
					String kjdImagePre = Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre");
					if(image.startsWith(kjdImagePre)){
						tempResult.img = image;
					}else{
						tempResult.img =  domainstr + image;
					}
				}
			}	
			
			Date dt = channelMouldPro.getBeginTime();
			
			Date now = new Date();
			
			if(dt !=null){
				try{
				    long between=(dt.getTime()-now.getTime())/1000;
				    if (between>0){
				    	tempResult.countDownSeconds=between+"";
				    	tempResult.countDownTitle="正在抢购";
				    }
				    else{
				    	tempResult.countDownSeconds="";
				    	tempResult.countDownTitle="正在抢购";
				    }
				}catch(Exception ex){
					tempResult.countDownSeconds="";
					tempResult.countDownTitle="正在抢购";
				}
			}else{
				tempResult.countDownSeconds="";
				tempResult.countDownTitle="";
			}
			plist.add(tempResult);
		}
		return plist;
	}


	public String getSubject_Model_RefreshNum(Integer sid,Integer tag) {
		return userRepository.getSubject_Model_RefreshNum(sid,tag);
	}



	public String getSubject_Model_tag(Integer sid) {
		return userRepository.getSubject_Model_tag(sid);
	}



	public Subject getSubject(Long sid) {
		return subjectRepository.findOne(sid);
	}



	public List< vo.subject.SubjectMouldVO.DataInfo> getSubject_Model(Integer sid, Integer page,
			Integer pagesize, ProductService productService,String uid,String devid) {
		String sql="SELECT  s.id,s.nsort,s.title,s.datetxt,s.flag,s.sectionPic,m.mname,m.typ,m.structure,m.stream,m.cnt FROM subject_mould s,mould m WHERE m.id=s.mouldId and s.flag='1' AND s.sid='"+sid+"'  ORDER BY s.nsort DESC,id DESC  LIMIT "+page*pagesize+","+pagesize;
		logger.info(sql);
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		List<vo.subject.SubjectMouldVO.DataInfo> result = new ArrayList<vo.subject.SubjectMouldVO.DataInfo>();
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				vo.subject.SubjectMouldVO.DataInfo dataInfo = new vo.subject.SubjectMouldVO.DataInfo();
				dataInfo.cardId=rs.getString("id");
				dataInfo.mould=rs.getString("typ");
				dataInfo.struct=rs.getString("structure");
				dataInfo.stxt=rs.getString("title");
				if(StringUtils.isBlank(rs.getString("sectionPic"))){
					dataInfo.sectionPic="";
				}else{
					dataInfo.sectionPic=domain + Configuration.root().getString("adload","/pimgs/adload/")+ rs.getString("sectionPic");
				}
				dataInfo.sectionPic=rs.getString("sectionPic");
				dataInfo.sdate=rs.getString("datetxt");
				String stream=rs.getString("stream");
				List<vo.subject.SubjectMouldVO.LayoutInfo> dataList = new ArrayList<vo.subject.SubjectMouldVO.LayoutInfo>();
				List<BigDecimal> divideList = new ArrayList<BigDecimal>();
				Integer divide =1;
				try {
					divide = Numbers.parseInt(dataInfo.struct.substring(dataInfo.struct.lastIndexOf('*')+1, dataInfo.struct.length()), 0);
				} catch (Exception e) {
					logger.info(e.toString());
				}
				for(String temp : stream.split("-")){
					vo.subject.SubjectMouldVO.LayoutInfo layoutInfo = new vo.subject.SubjectMouldVO.LayoutInfo();
					layoutInfo.start = temp.split("~")[0];
					layoutInfo.end =temp.split("~")[1] ;
					dataList.add(layoutInfo);
					try {
						Integer start =Numbers.parseInt(layoutInfo.start.substring(0, layoutInfo.start.lastIndexOf('_')), 0);
						Integer end =Numbers.parseInt(layoutInfo.end.substring(0, layoutInfo.end.lastIndexOf('_')), 0);
						if(end.intValue()>start.intValue() && end.intValue()!=0){
							BigDecimal divideTemp =new BigDecimal(divide).divide(new BigDecimal(end-start), 2, BigDecimal.ROUND_CEILING);
							logger.info(divideTemp+"=------------------=="+divide+"++++++++++++"+(end-start));
							divideList.add(divideTemp);
						}else{
							divideList.add(new BigDecimal(1));
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				dataInfo.layout = dataList;
				dataInfo.plist = getSubject_Model_Pro(rs.getString("id"),productService,uid,sid,dataInfo.mould,devid,divideList);
				result.add(dataInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return result;
	}



	private List<PInfo> getSubject_Model_Pro(String smid,
			ProductService productService,String uid,Integer sid,String mould,String devid, List<BigDecimal> divideList) {
		String sql=" SELECT s.*,p.title,p.typ as ptyp,p.skucode as pcode FROM subject_mould_pro s LEFT JOIN product p ON s.pid =p.pid WHERE s.smid='"+smid+"' ";
		logger.info(sql);
		List<PInfo> plist = new ArrayList<PInfo>();
		JdbcOperWithClose db = JdbcOperWithClose.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			String domain = StringUtil.getPICDomain();
			String domainstr = "";
			String resolution = UserService.getResolution(uid,devid);
			int i=0;
			while(rs.next()){
				PInfo tempResult = new PInfo();
				
				if ("1".equals(rs.getString("typ"))){
					domainstr = domain+"/pimgs/adload/"+sid+"/";
				}else{
					domainstr = domain;
				}
				BigDecimal divide =new BigDecimal(1);
				if(divideList.size()-1>=i){
					divide= divideList.get(i);
				}
				if("2".equals(mould)){
					//tempResult.img = StringUtil.getListpic(rs.getString("imgurl"));
						tempResult.img =  domainstr + StringUtil.getProductListpic(rs.getString("pcode"),rs.getString("imgurl"),resolution).replaceAll(domainstr, "");
				}else{
					if("3".equals(mould)||"4".equals(mould)){
						tempResult.img = domainstr + StringUtil.getWebListpic("",rs.getString("imgurl"),resolution,divide).replaceAll(domainstr, "");
					}else{
						tempResult.img = domainstr + StringUtil.getWebListpic(rs.getString("pcode"),rs.getString("imgurl"),resolution,divide).replaceAll(domainstr, "");
					}
				}
				i++;
				String linkurl = rs.getString("linkurl");
				if("http://".equals(linkurl)){
					tempResult.linkurl ="";
				}else{
					tempResult.linkurl =linkurl;
				}
				
				Product product = productService.getProductById(rs.getLong("pid"));
				if(product!=null && "3".equals(product.getPtyp()))
				{
					tempResult.linkurl="presellDetail://pid="+rs.getLong("pid");
				}
				
				tempResult.pinfo = productService.covertToProductSubjectItem(product,productService,uid);
				if ("1".equals(mould) || "2".equals(mould))
				{
					if (tempResult.pinfo!= null){
						tempResult.pinfo.cardMask = StringUtil.getProductIcon(11, mould);
					}
				}	
				plist.add(tempResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.close();
		} 
		return plist;
	}

}
