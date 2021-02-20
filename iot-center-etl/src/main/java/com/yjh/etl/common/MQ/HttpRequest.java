package com.yjh.etl.common.MQ;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;

public class HttpRequest {
	private static Logger logger = LogManager.getLogger(HttpRequest.class.getName());

	/**
	 * httpPost
	 * 
	 * @param url
	 *            路径
	 * @param jsonParam
	 *            参数
	 * @return
	 */
	public static JSONObject httpPost(String url, JSONObject jsonParam) {
		return httpPost(url, jsonParam, false);
	}

	/**
	 * post请求
	 * 
	 * @param url
	 *            url地址
	 * @param jsonParam
	 *            参数
	 * @param noNeedResponse
	 *            不需要返回结果
	 * @return
	 */
	public static JSONObject httpPost(String url, JSONObject jsonParam, boolean noNeedResponse) {
		// post请求返回结果
		CloseableHttpClient httpClient = HttpClients.createDefault();

		//请求超时时间设置
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).
				setConnectionRequestTimeout(10000).setSocketTimeout(6000000).build();

		CloseableHttpResponse result = null;
		JSONObject jsonResult = null;
		HttpPost method = new HttpPost(url);
		method.setConfig(requestConfig);
		try {
			if (null != jsonParam) {
				// 解决中文乱码问题
				StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				method.setEntity(entity);
			}
			// ----------- 请求完成，断开连接 -------
			method.setHeader(HttpHeaders.CONNECTION, "close");
			
			method.setHeader("debug", "true");
			method.setHeader("token", "A200Token");
			
			result = httpClient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			/** 请求发送成功，并得到响应 **/
			if (result.getStatusLine().getStatusCode() == 200) {
				String str = "";
				try {
					/** 读取服务器返回过来的json字符串数据 **/
					str = EntityUtils.toString(result.getEntity());
					if (noNeedResponse) {
						return null;
					}
					/** 把json字符串转换成json对象 **/
					jsonResult = JSONObject.parseObject(str);
				} catch (Exception e) {
					logger.error("post请求提交失败:" + url, e);
				}
			}
		} catch (IOException e) {
			logger.error("post请求提交失败:" + url, e);
		} finally {
			// 释放连接资源
			if (null != method) {
				method.releaseConnection();
			}
			
			// 关闭资源
			if (null != result) {
				try {
					result.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}

			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		return jsonResult;
	}

	/**
	 * 文件上传服务器，得到返回值
	 * 
	 * @param url
	 *            发送地址
	 * @param url
	 * @param f
	 *            发送文件
	 * @return
	 */
	public static String SubmitPost(String url,File f) {
		HttpClient httpclient = new DefaultHttpClient();
		String returnData = "";
		InputStream fis = null;
		try {
			if (null != f) {
				HttpPost httppost = new HttpPost(url);
				fis = new FileInputStream(f);
				InputStreamBody ifbudy = new InputStreamBody(fis,f.getName());

				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("file", ifbudy);// file1为请求后台的File upload;属性
				httppost.setEntity(reqEntity);
				// ----------- 请求完成，断开连接 -------
				httppost.setHeader(HttpHeaders.CONNECTION, "close");

				// 发送请求
				HttpResponse response = httpclient.execute(httppost);
				// 获得状态码
				int statusCode = response.getStatusLine().getStatusCode();
				// 如果状态码为200
				if (statusCode == HttpStatus.SC_OK) {
					logger.info("服务器正常响应.....");
					HttpEntity resEntity = response.getEntity();
					// httpclient自带的工具类读取返回数据
					returnData = EntityUtils.toString(resEntity);
					logger.info(resEntity.getContent());
					EntityUtils.consume(resEntity);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (fis != null)
					fis.close();
			}catch (IOException e){
				logger.error(e.toString());
			}
			httpclient.getConnectionManager().shutdown();
		}
		return returnData;
	}



	
	public static Map<String, Object> parseJSON2Map(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 最外层解析
		JSONObject json = JSONObject.parseObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			// 如果内层还是数组的话，继续解析
			if (v instanceof JSONArray) {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Iterator<Object> it = ((JSONArray) v).iterator();
				while (it.hasNext()) {
					Object json2 = it.next();
					list.add(parseJSON2Map(json2.toString()));
				}
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), v);
			}
		}
		return map;
	}


	/**
	 *
	 * httpPost: 传递 params
	 *
	 * @param url
	 * @param json
	 * @param noNeedResponse
	 * @return
	 */
	public static JSONObject httpPost(String url, String json, boolean noNeedResponse){
		CloseableHttpClient httpClient = HttpClients.createDefault();

		CloseableHttpResponse result = null;
		JSONObject jsonResult = null;
		HttpPost method = new HttpPost(url);

		//请求超时时间设置
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(8000).
				setConnectionRequestTimeout(8000).setSocketTimeout(8000).build();
		method.setConfig(requestConfig);

		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("params", json));
			method.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			// ----------- 请求完成，断开连接 -------
			method.setHeader(HttpHeaders.CONNECTION, "close");
			result = httpClient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			/** 请求发送成功，并得到响应 **/
			if (result.getStatusLine().getStatusCode() == 200) {
				String str = "";
				try {
					/** 读取服务器返回过来的json字符串数据 **/
					str = EntityUtils.toString(result.getEntity());
					if (noNeedResponse) {
						return null;
					}

					/** 把json字符串转换成json对象 **/
					jsonResult = JSONObject.parseObject(str);
				} catch (Exception e) {
					logger.error("post请求提交失败:" + url, e);
					jsonResult = new JSONObject();
					jsonResult.put("code", 500);
					jsonResult.put("message", "post请求提交失败:" + url);
				}
			}
		} catch (IOException e) {
			logger.error("post请求提交失败:" + url, e);
			jsonResult = new JSONObject();
			jsonResult.put("code", 500);
			jsonResult.put("message", "post请求提交失败:" + url);
		} finally {
			// 释放连接资源
			if (null != method) {
				method.releaseConnection();
			}
			
			if (null != result) {
				try {
					result.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}

			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		return jsonResult;

	}
}