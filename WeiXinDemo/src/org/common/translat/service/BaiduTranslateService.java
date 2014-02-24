package org.common.translat.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.common.translate.TranslateResult;

import com.google.gson.Gson;

/**
 * 代码解读： 1）第21-53行封装了一个http请求方法httpRequest()，相信读过之前教程的读者已经很熟悉了。
 * 
 * 2）第61-69行封装了一个urlEncodeUTF8()方法，用于对url中的参数进行UTF-8编码。
 * 
 * 3）第81行代码中的client_id需要替换成自己申请的api key。
 * 
 * 4）第83行代码是对url中的中文进行编码。以后凡是遇到通过url传递中文参数的情况，一定要显示地对中文进行编码，否则很可能出现程序在本机能正常运行，
 * 但部署到服务器上却有问题，因为本机与服务器的默认编码方式可能不一样。
 * 
 * 
 * 5）第88行代码就是调用百度翻译API。
 * 
 * 6）第90行代码是使用Gson工具将json字符串转换成TranslateResult对象，是不是发现Gson的使用真的很简单？另外，
 * 前面提到过调用百度翻译API返回的json里如果有中文是用unicode表示的
 * ，形如“\u4eca\u5929”，那为什么这里没有做任何处理？因为Gson的内部实现已经帮我们搞定了。
 * 
 * 
 * @author liufeng
 * @date 2013-10-21
 */
public class BaiduTranslateService {
	/**
	 * 发起http请求获取返回结果
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @return
	 */
	public static String httpRequest(String requestUrl) {
		StringBuffer buffer = new StringBuffer();
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url
					.openConnection();

			httpUrlConn.setDoOutput(false);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);

			httpUrlConn.setRequestMethod("GET");
			httpUrlConn.connect();

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	/**
	 * utf编码
	 * 
	 * @param source
	 * @return
	 */
	public static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = java.net.URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 翻译（中->英 英->中 日->中 ）
	 * 
	 * @param source
	 * @return
	 */
	public static String translate(String source) {
		String dst = null;

		// 组装查询地址
		String requestUrl = "http://openapi.baidu.com/public/2.0/bmt/translate?client_id=2X3arr9E4y6B4SUBMylPwwSp&q={keyWord}&from=auto&to=auto";
		// 对参数q的值进行urlEncode utf-8编码
		requestUrl = requestUrl.replace("{keyWord}", urlEncodeUTF8(source));

		// 查询并解析结果
		try {
			// 查询并获取返回结果
			String json = httpRequest(requestUrl);
			// 通过Gson工具将json转换成TranslateResult对象
			TranslateResult translateResult = new Gson().fromJson(json,
					TranslateResult.class);
			// 取出translateResult中的译文
			dst = translateResult.getTrans_result().get(0).getDst();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null == dst)
			dst = "翻译系统异常，请稍候尝试！";
		return dst;
	}

	public static void main(String[] args) {
		// 翻译结果：The network really powerful
		System.out.println(translate("网络真强大"));
	}
}
