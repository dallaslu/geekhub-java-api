package com.dallaslu.geekhub.api.auth;

import java.util.List;

import org.apache.http.cookie.Cookie;

import com.dallaslu.geekhub.api.GeekHubApi;

/**
 * 认证
 */
public interface GeekHubIdentityProvider {
	public boolean isBusy();
	public List<Cookie> getCookie(GeekHubApi api);
	public List<Cookie> tryLoadCookie(GeekHubApi geekHubApi);
}
