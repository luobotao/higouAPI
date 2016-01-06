package controllers.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Channel;
import models.Reffer;

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
import vo.channel.ChannelMouldVO;
import vo.channel.ChannelVO;

/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class ChannelAPIController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_MONTH = new SimpleDateFormat("yyyyMM");
	private final ProductService productService;
	private final ChannelService channelService;
	private final UserService userService;
	private final RefferService refferService;
	@Inject
	public ChannelAPIController(final ChannelService channelService,final UserService userService,final ProductService productService,final RefferService refferService){
		this.channelService = channelService;
		this.userService = userService;
		this.productService = productService;
		this.refferService=refferService;
	}
	
	//(十六)	获取频道列表接口(GET方式) channel.php
	public Result channel(){
		response().setContentType("application/json;charset=utf-8");
		ChannelVO result = new ChannelVO();
		
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		if (!StringUtil.checkMd5(devid,wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_PINDAO);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			List<Channel> channelItemList = new ArrayList<Channel>();
			List<Object[]> channelList = channelService.getChannelList();
			String appTemp = appversion.replace(".", "");
			for(Object[] objectArray:channelList){
				Channel channelModel = new Channel();
				
				String channelIdStr = String.valueOf(objectArray[0]);						
				channelModel.setChannelId(objectArray[0]+"");
				channelModel.setChannelName(objectArray[1]+"");
				channelItemList.add(channelModel);
				
			}
			result.setReffer("Typ="+Constants.MAIDIAN_PINDAO);
			result.setData(channelItemList);
			result.setStatus("1");
		}else{
			result.setStatus("0");
		}
		
		return ok(Json.toJson(result));
	}
	//(十七)	获取频道卡片列表接口(GET方式) （修改） channel_mouldlist.php
	public Result channel_mouldlist(){
		response().setContentType("application/json;charset=utf-8");
		ChannelMouldVO result = new ChannelMouldVO();
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String cid = AjaxHellper.getHttpParam(request(), "cid");
		String tag = AjaxHellper.getHttpParam(request(), "tag")==null?"0":AjaxHellper.getHttpParam(request(), "tag");
		String page = AjaxHellper.getHttpParam(request(), "page");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		if(StringUtils.isBlank(cid)||StringUtils.isBlank(uid)||StringUtils.isBlank(page)){
			result.status="0";
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status="0";
			return ok(Json.toJson(result));
		}
		//加埋点
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_PINDAO);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String resolution = UserService.getResolution(uid,devid);
			result.channelId=cid;
			if("0".equals(tag)){
				result.refreshnum="0";
			}else{
				result.refreshnum = channelService.getChannel_Model_RefreshNum(Numbers.parseInt(cid, 0),Numbers.parseInt(tag, 0));
			}
			result.retag = channelService.getChannel_Model_tag(Numbers.parseInt(cid, 0));
			String tNumstr = channelService.getChannel_Model_RefreshNum(Numbers.parseInt(cid, 0),0);
			if((Numbers.parseInt(page, 0)+1)*10>=Numbers.parseInt(tNumstr, 0))
			{
				result.endflag = 1;
			}
			result.data = channelService.getChannel_Model(Numbers.parseInt(cid, 0),Numbers.parseInt(page, 0),Constants.PAGESIZE,productService,uid,appversion,resolution);
			List<Channel> channelItemList = new ArrayList<Channel>();
			List<Object[]> channelList = channelService.getChannelList();
			for(Object[] objectArray:channelList){
				Channel channelModel = new Channel();
				channelModel.setChannelId(objectArray[0]+"");
				channelModel.setChannelName(objectArray[1]+"");
				channelModel.setReffer("Typ="+Constants.MAIDIAN_PINDAO+"&Lid="+channelModel.getChannelId());
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
