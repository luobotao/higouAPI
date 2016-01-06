package controllers.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Channel;
import models.Reffer;
import models.Subject;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Result;
import services.api.ChannelService;
import services.api.ProductService;
import services.api.RefferService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import vo.subject.SubjectMouldVO;

/**
 * 专题卡片
 * @author luobotao
 * @Date 2015年5月9日
 */
@Named
@Singleton
public class SubjectAPIController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_MONTH = new SimpleDateFormat("yyyyMM");
	private final ProductService productService;
	private final ChannelService channelService;
	private final UserService userService;
	private final RefferService refferService;
	@Inject
	public SubjectAPIController(final ChannelService channelService,final UserService userService,final ProductService productService,final RefferService refferService){
		this.channelService = channelService;
		this.userService = userService;
		this.productService = productService;
		this.refferService=refferService;
	}
	
	
	//获取专题卡片列表接口(GET方式) （修改）subject_mouldlist.php
	public Result subject_mouldlist(){
		response().setContentType("application/json;charset=utf-8");
		SubjectMouldVO result = new SubjectMouldVO();
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String sid = AjaxHellper.getHttpParam(request(), "sid");
		String tag = AjaxHellper.getHttpParam(request(), "tag")==null?"0":AjaxHellper.getHttpParam(request(), "tag");
		String page = AjaxHellper.getHttpParam(request(), "page");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status="0";
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(sid)||StringUtils.isBlank(uid)||StringUtils.isBlank(page)||StringUtils.isBlank(tag)){
			result.status="0";
			return ok(Json.toJson(result));
		}
		//加埋点
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_CAINIXIHUAN);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			result.reffer="Typ="+Constants.MAIDIAN_ZHUANTI+"&Lid="+sid;
			result.subjectId=sid;
			result.retag = channelService.getSubject_Model_tag(Numbers.parseInt(sid, 0));
			result.refreshnum = channelService.getSubject_Model_RefreshNum(Numbers.parseInt(sid, 0),Numbers.parseInt(tag, 0));
			String tNumstr = channelService.getSubject_Model_RefreshNum(Numbers.parseInt(sid, 0),0);
			if((Numbers.parseInt(page, 0)+1)*10>=Numbers.parseInt(tNumstr, 0))
			{
				result.endflag = 1;
			}
			Subject subject = channelService.getSubject(Numbers.parseLong(sid, 0L));
			result.subjectName = subject.getSname();
			result.data = channelService.getSubject_Model(Numbers.parseInt(sid, 0),Numbers.parseInt(page, 0),Constants.PAGESIZE,productService,uid,devid);
			List<Channel> channelItemList = new ArrayList<Channel>();
			List<Object[]> channelList = channelService.getChannelList();
			for(Object[] objectArray:channelList){
				Channel channelModel = new Channel();
				channelModel.setChannelId(objectArray[0]+"");
				channelModel.setChannelName(objectArray[1]+"");
				channelItemList.add(channelModel);
			}
			result.status="1";
			return ok(Json.toJson(result));
		}else{
			result.status="4";
			return ok(Json.toJson(result));
		}
		
	}

}
