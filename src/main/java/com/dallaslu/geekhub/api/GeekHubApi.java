package com.dallaslu.geekhub.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.dallaslu.geekhub.api.auth.GeekHubIdentityProvider;
import com.dallaslu.geekhub.api.page.GeekHubPost;
import com.dallaslu.geekhub.api.page.GeekHubPage;
import com.dallaslu.geekhub.api.page.GeekHubPostItem;
import com.dallaslu.geekhub.api.page.PageDefination;
import com.dallaslu.utils.http.HttpHelper;
import com.dallaslu.utils.http.HttpHelper.ResponseResult;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class GeekHubApi {
	public static final String VERSION = "0.0.1";
	private static final String GITHUB_URL = "https://github.com/dallslu/geekhub-java-api";
	public static final String USER_AGENT = "GeekHub-Java/" + VERSION + " (+" + GITHUB_URL + ")";
	public static final String DEFAULT_URL_BASE = "https://www.geekhub.com";
	@Getter
	@Builder.Default
	private String webUrlBase = DEFAULT_URL_BASE;
	@Getter
	private HttpHelper httpHelper;
	private GeekHubIdentityProvider identityProvider;
	@Builder.Default
	private String commentVia = GITHUB_URL;
	private final AtomicBoolean logon = new AtomicBoolean(false);

	/**
	 * 判断登录状态
	 * 
	 * @return 登录状态
	 */
	public boolean isLogon() {
		return this.logon.get();
	}

	/**
	 * 设置登录状态
	 * 
	 * @param logon
	 *            登录状态
	 */
	public void setLogon(boolean logon) {
		this.logon.compareAndSet(!logon, logon);
	}

	/**
	 * 获取页面原始内容
	 * 
	 * @param url
	 *            页面网址，例如https://www.geekhub.com/services 或 /services
	 * @return 通用请求结果
	 */
	public ResponseResult<String> fetchPage(String url) {
		return this.fetchPage(url, true);
	}

	/**
	 * 获取页面原始内容
	 * 
	 * @param url
	 *            页面网址，例如https://www.geekhub.com/services 或 /services
	 * @param autoCheckThemeMode
	 *            是否自动切换主题
	 * @return 通用请求结果
	 */
	public ResponseResult<String> fetchPage(String url, boolean autoSwcithTheme) {
		if (!url.startsWith(webUrlBase)) {
			url = webUrlBase + url;
		}

		ResponseResult<String> result = null;
		try {
			result = this.httpHelper.get(url);
		} catch (Exception e) {
			e.printStackTrace();
			result = ResponseResult.<String>builder().build();
		}

		if (result.isSuccess() && result.getStatus() == HttpStatus.SC_OK) {
			{
				Pattern p = Pattern.compile("href=\"/users/sign_out\">退出</a>");
				Matcher m = p.matcher(result.getContent());
				if (!m.find()) {
					// 登录信息已失效
					boolean signedOut = this.logon.compareAndSet(true, false);
					if (signedOut) {
						log.warn("登录信息已失效！");
					}
				} else {
					this.logon.set(true);
				}
			}

			if (autoSwcithTheme) {
				Pattern p = Pattern.compile("<span>信息流模式</span>");
				Matcher m = p.matcher(result.getContent());
				if (!m.find()) {
					log.info(String.format("swtich_theme ..."));
					String pageUrl = webUrlBase + "/switch_theme";
					String html = result.getContent();
					CsrfData csrf = parseCsrfData(html);
					Map<String, Object> param = new HashMap<>();
					param.put(csrf.getCsrfParam(), csrf.getCsrfToken());
					param.put("_method", "patch");
					Map<String, String> headers = new HashMap<>();
					headers.put("Referer", webUrlBase);
					headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					headers.put("Upgrade-Insecure-Requests", "1");
					headers.put("Origin", webUrlBase);
					String responseHtml = this.httpHelper.post(pageUrl, param, headers);
					log.info(responseHtml);
					return fetchPage(url, autoSwcithTheme);
				}
			}
		}

		return result;
	}

	/**
	 * 获取页面解析后的内容
	 * 
	 * @param pd
	 *            页面定义
	 * @param postId
	 *            附加参数
	 * @return 解析后的内容
	 */
	public <T extends GeekHubPage> GeekHubApiResult<T> fetchPage(PageDefination<T> pd, String postId) {
		String url = String.format("%s/%s/%s", webUrlBase, pd.getUrl(), postId);
		ResponseResult<String> pageResult = this.fetchPage(url);
		if (!pageResult.isSuccess() || pageResult.getStatus() != HttpStatus.SC_OK) {
			return GeekHubApiResult.<T>builder().build();
		}

		T result = null;
		try {
			result = pd.getPageClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (result != null) {
			Document doc = Jsoup.parse(pageResult.getContent());
			result.parse(doc);
			return GeekHubApiResult.<T>builder().content(result).build();
		} else {
			return GeekHubApiResult.<T>builder().build();
		}
	}

	/**
	 * 获取帖子
	 * 
	 * @param pd
	 *            页面定义
	 * @param postId
	 *            Post ID
	 * @return 解析后的内容
	 */
	public <T extends GeekHubPost> GeekHubApiResult<T> fetchPost(PageDefination<T> pd, String postId) {
		String url = String.format("%s/%s/%s", webUrlBase, pd.getUrl(), postId);
		ResponseResult<String> pageResult = this.fetchPage(url);
		if (!pageResult.isSuccess() || pageResult.getStatus() != HttpStatus.SC_OK) {
			return GeekHubApiResult.<T>builder().build();
		}

		T result = null;
		try {
			result = pd.getPageClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (result != null) {
			Document doc = Jsoup.parse(pageResult.getContent());
			result.parse(doc);
			return GeekHubApiResult.<T>builder().content(result).build();
		} else {
			return GeekHubApiResult.<T>builder().build();
		}
	}

	/**
	 * 评论
	 * 
	 * @param postType
	 *            Post 类型
	 * @param postId
	 *            Post ID
	 * @param content
	 *            评论内容
	 * @return 评论结果
	 */
	public <T extends GeekHubPage> boolean reply(PageDefination<T> pd, String postId, String content) {
		return this.reply(pd, postId, 0, content);
	}

	/**
	 * 回复他人评论
	 * 
	 * @param postType
	 *            Post 类型
	 * @param postId
	 *            Post ID
	 * @param replyTo
	 *            被回复的评论 ID
	 * @param content
	 *            评论内容
	 * @return 评论结果
	 */
	public <T extends GeekHubPage> boolean reply(PageDefination<T> pd, String postId, int replyTo, String content) {
		String url = String.format("%s/%s/%s", webUrlBase, pd.getUrl(), postId);
		ResponseResult<String> pageResult = this.fetchPage(url);
		if (!pageResult.isSuccess() || pageResult.getStatus() != HttpStatus.SC_OK) {
			return false;
		}

		if (!this.isLogon()) {
			tryLogin();
			return false;
		}

		String html = pageResult.getContent();
		CsrfData csrf = parseCsrfData(html);
		Map<String, Object> param = new HashMap<>();
		param.put(csrf.getCsrfParam(), csrf.getCsrfToken());
		param.put("comment[target_type]", pd.getType());
		param.put("comment[target_id]", postId);
		param.put("comment[reply_to_id]", replyTo);
		param.put("comment[content]", content);
		param.put("comment[ua]", commentVia);
		Map<String, String> headers = new HashMap<>();
		headers.put("Referer", url);
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Origin", webUrlBase);
		String responseHtml = this.httpHelper.post(webUrlBase + "/comments", param, headers);
		if (responseHtml
				.equals("<html><body>You are being <a href=\"" + webUrlBase + "/\">redirected</a>.</body></html>")) {
			return false;
		}
		log.info(responseHtml);
		return true;
	}

	private void tryLogin() {
		if (!this.identityProvider.isBusy()) {
			List<Cookie> cookies = this.identityProvider.getCookie(this);
			if (cookies != null) {
				this.httpHelper.addCookies(cookies);
			}
		}
	}

	/**
	 * 签到
	 */
	public boolean checkins() {
		String pageUrl = getWebUrlBase() + "/checkins";
		ResponseResult<String> result = this.fetchPage(pageUrl, false);
		if (!result.isSuccess() || result.getStatus() != HttpStatus.SC_OK) {
			return false;
		}

		if (!this.isLogon()) {
			tryLogin();
			return false;
		}
		CsrfData csrf = parseCsrfData(result.getContent());

		Map<String, Object> param = new HashMap<>();
		param.put(csrf.getCsrfParam(), csrf.getCsrfToken());
		param.put("_method", "post");
		Map<String, String> headers = new HashMap<>();
		headers.put("Referer", pageUrl);
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Origin", getWebUrlBase());
		String responseHtml = this.httpHelper.post(getWebUrlBase() + "/checkins/start", param, headers);
		if (responseHtml.equals("<html><body>You are being <a href=\"" + getWebUrlBase()
				+ "/checkins\">redirected</a>.</body></html>")) {
			return true;
		}
		log.info(responseHtml);
		return false;
	}

	public static CsrfData parseCsrfData(String html) {
		CsrfData cd = new CsrfData();
		{
			Pattern p = Pattern.compile("<meta name=\"csrf-param\" content=\"(.*?)\" />");
			Matcher matcher = p.matcher(html);
			if (matcher.find()) {
				cd.setCsrfParam(matcher.group(1));
			}
		}
		{
			Pattern p = Pattern.compile("<meta name=\"csrf-token\" content=\"(.*?)\" />");
			Matcher matcher = p.matcher(html);
			if (matcher.find()) {
				cd.setCsrfToken(matcher.group(1));
			}
		}
		return cd;
	}

	public void init() {
		List<Cookie> cookies = this.identityProvider.tryLoadCookie(this);
		if (cookies != null) {
			this.httpHelper.addCookies(cookies);
		}
	}

	public void addEventListener(Consumer<GeekHubPostItem> listener) {

	}

}
