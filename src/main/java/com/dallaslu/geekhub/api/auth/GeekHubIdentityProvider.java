package com.dallaslu.geekhub.api.auth;

import java.util.List;

import org.apache.http.cookie.Cookie;

import com.dallaslu.geekhub.api.GeekHubApi;

/**
 * 认证
 */
public interface GeekHubIdentityProvider {
	/**
	 * 初始化时加载 Cookies
	 * 
	 * @param geekHubApi
	 *            接口实例
	 * @return Cookie 列表
	 */
	public List<Cookie> loadCookie(GeekHubApi geekHubApi);

	/**
	 * 登录状态失效时，重新获取 Cookie
	 * 
	 * @param geekHubApi
	 * @return Cookie 列表
	 */
	default List<Cookie> getNewCookie(GeekHubApi geekHubApi) {
		return null;
	}

	/**
	 * 用于避免多个需要登录的页面触发多个登录请求
	 * 
	 * @return 是否正在忙于获取 Cookie
	 */
	default boolean isBusy() {
		return false;
	}
}
