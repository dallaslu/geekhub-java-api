package com.dallaslu.utils.http;

import java.util.List;
import java.util.Map;

import org.apache.http.cookie.Cookie;

import lombok.Builder;
import lombok.Getter;

public interface HttpHelper {
	@Getter
	@Builder
	public static class ResponseResult<T> {
		private boolean success;
		private T content;
		private int status;
		private String locationHeader;
	}

	/**
	 * Post 提交表单
	 * 
	 * @param url
	 *            Form action
	 * @param params
	 *            Form fields
	 * @param headers
	 *            Headers
	 * @return 成功时返回原始响应内容，失败时为 null
	 */
	public String post(String url, Map<String, Object> params, Map<String, String> headers);

	/**
	 * 发起 GET 请求
	 * 
	 * @param url
	 *            URL
	 * @return 成功时返回原始响应内容，失败时为 null
	 */
	public ResponseResult<String> get(String url);

	/**
	 * 下载文件到本地
	 * 
	 * @param urlString
	 *            URL
	 * @param filename
	 *            文件名
	 * @param savePath
	 *            保存路径
	 */
	public void download(String urlString, String filename, String savePath);

	/**
	 * 设置 Cookie
	 * 
	 * @param cookies
	 */
	public void addCookies(List<Cookie> cookies);
	
	/**
	 * 获取当前的全部 Cookie
	 * @return Cookie 列表
	 */
	public List<Cookie> getCookies();
}
