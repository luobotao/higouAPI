package controllers.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.KdtService;
import utils.BeanUtils;
import utils.FileUtils;
import vo.KdtProductVO;
import vo.KdtTradeGetVO;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 微商口袋通Controller
 * @author luobotao
 *
 */
@Named
@Singleton
public class KdtAPIController extends BaseApiController {
	
	private static final Logger.ALogger LOGGER = Logger.of(KdtAPIController.class);
	
	//根据微商唯一商品ID去获取该商品
	public static Result getProduct(){
		response().setContentType("application/json;charset=utf-8");
		List<JsonNode> resultList = new ArrayList<>();
		JsonNode req = request().body().asJson();
		if(req==null || req.get("num_iids")==null){
			return ok(Json.toJson(resultList));
		}
		Iterator<JsonNode> it = req.get("num_iids").elements();
		while(it.hasNext()){
			String num_iidStr = it.next().asText();
			long num_iid = Long.valueOf(num_iidStr);
			JsonNode result = KdtService.getInstance().getProduct(num_iid);
			resultList.add(result);
		}
		
		return ok(Json.toJson(resultList));
	}
	
	//根据微商唯一商品ID去删除该商品
	public static Result deleteProduct(){
		response().setContentType("application/json;charset=utf-8");
		List<JsonNode> resultList = new ArrayList<>();
		JsonNode req = request().body().asJson();
		if(req==null || req.get("num_iids")==null){
			return ok(Json.toJson(resultList));
		}
		Iterator<JsonNode> it = req.get("num_iids").elements();
		while(it.hasNext()){
			String num_iidStr = it.next().asText();
			long num_iid = Long.valueOf(num_iidStr);
			JsonNode result = KdtService.getInstance().deleteProduct(num_iid);
			resultList.add(result);
		}
		return ok(Json.toJson(resultList));
	}
	
	// 口袋通新增一个商品
	public static Result productAdd() {
		KdtProductVO kdtProductVO = new KdtProductVO();
		JsonNode req = request().body().asJson();
		List<File> fileList = new ArrayList<>();
		Iterator<String> it = req.fieldNames();
		while(it.hasNext()){
			String key = it.next();
			JsonNode valueJson=req.get(key);
			if(valueJson.elements().hasNext()){
				List<String> filePathsList = new ArrayList<String>();
				Iterator<JsonNode> filePaths = valueJson.elements();
				while(filePaths.hasNext()){
					String url=filePaths.next().asText();
					File file = FileUtils.getFileFromUrl(url);
					fileList.add(file);
					filePathsList.add(file.getAbsolutePath());
				}
				BeanUtils.setValue(kdtProductVO, key, filePathsList);
			}else{
				try {
					BeanUtils.setValue(kdtProductVO, key, valueJson.asText());
				} catch (Exception e) {
					LOGGER.info(key+":set fail");
				}
				
			}
		}
		
		JsonNode result = KdtService.getInstance().productAdd(kdtProductVO);
		for(File file:fileList){
			file.delete();
		}
		response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson(result));
	}
	// 口袋通更新一个商品
	public static Result productUpdate() {
		KdtProductVO kdtProductVO = new KdtProductVO();
		JsonNode req = request().body().asJson();
		List<File> fileList = new ArrayList<>();
		Iterator<String> it = req.fieldNames();
		while(it.hasNext()){
			String key = it.next();
			JsonNode valueJson=req.get(key);
			if(valueJson.elements().hasNext()){
				List<String> filePathsList = new ArrayList<String>();
				Iterator<JsonNode> filePaths = valueJson.elements();
				while(filePaths.hasNext()){
					String url=filePaths.next().asText();
					File file = FileUtils.getFileFromUrl(url);
					fileList.add(file);
					filePathsList.add(file.getAbsolutePath());
				}
				BeanUtils.setValue(kdtProductVO, key, filePathsList);
			}else{
				try {
					BeanUtils.setValue(kdtProductVO, key, valueJson.asText());
				} catch (Exception e) {
					LOGGER.info(key+":set fail");
				}
			}
		}
		JsonNode result = KdtService.getInstance().productUpdate(kdtProductVO);
		response().setContentType("application/json;charset=utf-8");
		
		for(File file:fileList){
			file.delete();
		}
		return ok(Json.toJson(result));
	}

	// 口袋通获取卖家的交易记录
	public static Result getTrades(){
		KdtTradeGetVO kdtTradeGetVO = new KdtTradeGetVO();
		JsonNode req = request().body().asJson();
		Iterator<String> it = req.fieldNames();
		while(it.hasNext()){
			String key = it.next();
			JsonNode valueJson=req.get(key);
			try {
				BeanUtils.setValue(kdtTradeGetVO, key, valueJson.asText());
			} catch (Exception e) {
				LOGGER.info(key+":set fail");
			}
		}
		JsonNode result = KdtService.getInstance().getTrades(kdtTradeGetVO);
		response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson(result));
	}
	
	
	public static Result getCategories(){
		JsonNode result = KdtService.getInstance().getCategories();
		response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson(result));
	}
	public static Result getCategoriesTags(){
		JsonNode result = KdtService.getInstance().getCategoriesTags();
		response().setContentType("application/json;charset=utf-8");
		return ok(Json.toJson(result));
	}
}
