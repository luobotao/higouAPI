package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.w3c.dom.Document;

import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * WS工具类
 * 
 * @author luobotao
 *
 */
public class WSUtils {

	public static JsonNode getResponseAsJson(final String url) {
		Promise<JsonNode> jsonPromise = WS
				.url(url)
				.setContentType("application/x-www-form-urlencoded;charset=utf-8")
				.get().map(new Function<WSResponse, JsonNode>() {
					public JsonNode apply(WSResponse response) {
						JsonNode json = response.asJson();
						return json;
					}
				});
		return jsonPromise.get(100000);

	}

	public static String getResponseAsString(final String url) {
		Promise<String> jsonPromise = WS
				.url(url)
				.setContentType("application/x-www-form-urlencoded;charset=utf-8")
				.get().map(new Function<WSResponse, String>() {
					public String apply(WSResponse response) {
						byte[] str = response.asByteArray();
						try {
							return new String(str, "utf-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
							return new String(str);
						}
					}
				});
		return jsonPromise.get(100000);

	}

	public static Document getResponseAsXML(final String url) {
		Promise<Document> documentPromise = WS.url(url).get()
				.map(new Function<WSResponse, Document>() {
					public Document apply(WSResponse response) {
						Document xml = response.asXml();
						return xml;
					}
				});
		return documentPromise.get(100000);
	}

	public static File getResponseAsFile(final String url) {
		Promise<File> filePromise = WS.url(url).get()
				.map(new Function<WSResponse, File>() {
					public File apply(WSResponse response) throws Throwable {
						InputStream inputStream = null;
						OutputStream outputStream = null;
						try {
							inputStream = response.getBodyAsStream();
							// write the inputStream to a File
							final File file = new File("/tmp/response.txt");
							outputStream = new FileOutputStream(file);

							int read = 0;
							byte[] buffer = new byte[1024];

							while ((read = inputStream.read(buffer)) != -1) {
								outputStream.write(buffer, 0, read);
							}
							return file;
						} catch (IOException e) {
							throw e;
						} finally {
							if (inputStream != null) {
								inputStream.close();
							}
							if (outputStream != null) {
								outputStream.close();
							}
						}
					}
				});
		return filePromise.get(100000);
	}

	public static JsonNode postByForm(final String url,final Map<String, String> mapBody) {
		// 构建请求体
		StringBuilder formBodyBuilder = new StringBuilder();
		for (Map.Entry<String, String> entry : mapBody.entrySet()) {
			if (formBodyBuilder.length() > 0) {
				formBodyBuilder.append("&");
			}
			formBodyBuilder.append(formParamEncode(entry.getKey()));
			formBodyBuilder.append("=");
			formBodyBuilder.append(formParamEncode(entry.getValue()));
		}
		String formBody = formBodyBuilder.toString();
		// 发送请求
		Promise<WSResponse> jsonPromise = WS
				.url(url)
				.setContentType("application/x-www-form-urlencoded;charset=utf-8")
				.post(formBody);
		return jsonPromise.get(100000).asJson();
	}

	public static JsonNode postByFormWithAuth(final String url,final Map<String, String> mapBody, final String auth) {
		// 构建请求体
		StringBuilder formBodyBuilder = new StringBuilder();
		for (Map.Entry<String, String> entry : mapBody.entrySet()) {
			if (formBodyBuilder.length() > 0) {
				formBodyBuilder.append("&");
			}
			formBodyBuilder.append(formParamEncode(entry.getKey()));
			formBodyBuilder.append("=");
			formBodyBuilder.append(formParamEncode(entry.getValue()));
		}
		String formBody = formBodyBuilder.toString();
		// 发送请求
		Promise<WSResponse> jsonPromise = WS
				.url(url)
				.setContentType("application/x-www-form-urlencoded;charset=utf-8")
				.setHeader("Authorization", auth).post(formBody);
		return jsonPromise.get(100000).asJson();
	}

	public static JsonNode postByJSONWithAuth(final String url,final JsonNode json, final String auth) {
		// 发送请求
		Promise<WSResponse> jsonPromise = WS
				.url(url)
				.setContentType("application/x-www-form-urlencoded;charset=utf-8")
				.setHeader("Authorization", auth).post(json);

		return jsonPromise.get(100000).asJson();
	}

	public static JsonNode postByJSON(final String url, final JsonNode json) {
		// 发送请求
		Promise<WSResponse> jsonPromise = WS
				.url(url)
				.setContentType(
						"application/x-www-form-urlencoded;charset=utf-8")
				.post(json);

		return jsonPromise.get(100000).asJson();
	}

	public static Document postByXML(final String url, final String xml) {
		// 发送请求
		Promise<WSResponse> jsonPromise = WS
				.url(url)
				.setContentType(
						"text/xml;charset=utf-8")
				.post(xml);

		return jsonPromise.get(100000).asXml();
	}
	private static String formParamEncode(String value) {
		if (null == value) {
			return "";
		}
		try {
			return URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
