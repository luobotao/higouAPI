package controllers.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Comment;
import models.Product;
import models.User;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.api.CommentService;
import services.api.ProductService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Dates;
import utils.Numbers;
import utils.StringUtil;
import vo.comment.CommentListVO;
import vo.comment.CommentListVO.CommentItem;
import assets.CdnAssets;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author luobotao
 * @Date 2015年5月4日
 */
@Named
@Singleton
public class CommentAPIController extends BaseApiController {
	private static final Logger.ALogger logger = Logger.of(CommentAPIController.class);
	
	private final UserService userService;
	private final CommentService commentService;
	private final ProductService productService;
	
	@Inject
    public CommentAPIController(final UserService userService,final CommentService commentService,final ProductService productService) {
        this.userService = userService;
        this.commentService = commentService;
        this.productService = productService;
    }
	
	//(一)	评论增加接口(POST方式) comment_add.php
	public Result comment_add() {
		response().setContentType("application/json;charset=utf-8");
		
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pid");
		String content = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "content");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		//ResponseResult result = new ResponseResult();
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("commentID", "");
			result.put("msg", "校验失败");	
			return ok(Json.toJson(result));
		}
		
		result.put("status", "3");
		result.put("commentID", "");
		result.put("msg", "");		
		Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
		User user = userService.getUserByUid(Numbers.parseLong(uid, 0L));
		if(product==null){
			//result.setStatus(3);
			result.put("status", "3");
			result.put("msg", "产品不存在");
			return ok(result);
		}
		if(user!=null){
			Comment comment = new Comment();
			comment.setContent(content);
			comment.setDate_add(new Date());
			comment.setNickname(user.getNickname());
			comment.setUid(Numbers.parseLong(uid, 0L));
			comment.setPid(Numbers.parseLong(pid, 0L));
			comment.setHeadIcon(user.getHeadIcon());
			comment.setStatus(1);
			comment.setEditor(1);
			comment.setNsort(1);
			try{	
				comment=commentService.saveComment(comment);
				result.put("status", "1");
				result.put("commentID", comment.getId().toString());
			}catch(Exception ex){
				result.put("status", "4");
				result.put("msg", "检查评论是否有表情等非法符号");
			}
		}else{
			result.put("status", "3");
			result.put("msg", "用户不存在");
		}
		return ok(result);
	}
	
	//(二)	获取评论列表接口(GET方式) comment_list.php
	public Result comment_list() {
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String page = AjaxHellper.getHttpParam(request(), "page");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		//ResponseResult result = new ResponseResult();
		CommentListVO commentListVO = new CommentListVO();
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			commentListVO.setStatus("0");
			return ok(Json.toJson(commentListVO));
		}
		
		Page<Comment> comments = commentService.commentPage(Numbers.parseInt(page, 0), Numbers.parseLong(pid, 0L));
		commentListVO.setStatus("1");
		commentListVO.setTotal(comments.getTotalElements());
		List<CommentItem> commentItemList = new ArrayList<CommentItem>();
		for(Comment comment:comments.getContent()){
			CommentItem commentItem = new CommentItem();
			commentItem.content = comment.getContent();
			commentItem.editor = String.valueOf(comment.getEditor());
			commentItem.nsort = String.valueOf(comment.getNsort());
			commentItem.id = String.valueOf(comment.getId());
			commentItem.headIcon = StringUtils.isBlank(comment.getHeadIcon())?domainimg+"images/sheSaidImages/default_headicon_girl.png":comment.getHeadIcon();
			commentItem.nickname = comment.getNickname();
			commentItem.date_add = Dates.formatDate(comment.getDate_add(),"yyyy-MM-dd HH:mm:ss");
			commentItemList.add(commentItem);
		}
		commentListVO.setComment(commentItemList);
		response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson(commentListVO));
	}

	
	
}
class ResponseResult{
	private int status;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}


