package com.dallaslu.geekhub.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.dallaslu.geekhub.api.auth.GeekHubIdentityProvider;
import com.dallaslu.geekhub.api.model.CableUpdate;
import com.dallaslu.geekhub.api.model.GeekHubComment;
import com.dallaslu.geekhub.api.model.GeekHubPostItem;
import com.dallaslu.geekhub.api.page.GeekHubPage;
import com.dallaslu.geekhub.api.page.GeekHubPost;
import com.dallaslu.geekhub.api.page.GeekHubPostList;
import com.dallaslu.geekhub.api.page.GeekHubUserProfile;
import com.dallaslu.geekhub.api.page.PageDefination;
import com.dallaslu.geekhub.api.utils.ParseHelper;
import com.dallaslu.utils.http.HttpHelper;
import com.dallaslu.utils.http.HttpHelper.ResponseResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class GeekHubApi {

	public static final String VERSION = "0.0.1";
	private static final String GITHUB_URL = "https://github.com/dallslu/geekhub-java-api";
	public static final String GEEKHUB_DOMAIN = "www.geekhub.com";
	public static final String USER_AGENT = "GeekHub-Java/" + VERSION + " (+" + GITHUB_URL + ")";
	public static final String DEFAULT_URL_BASE = "https://" + GEEKHUB_DOMAIN;
	@Getter
	@Builder.Default
	private String webUrlBase = DEFAULT_URL_BASE;
	@Getter
	@Builder.Default
	private String geekHubDomain = "www.geekhub.com";
	@Getter
	private HttpHelper httpHelper;
	private GeekHubIdentityProvider identityProvider;
	@Builder.Default
	private String commentVia = GITHUB_URL;
	private final AtomicBoolean logon = new AtomicBoolean(false);
	private WebSocketClient ws;
	@Builder.Default
	private List<Consumer<CableUpdate>> listeners = new ArrayList<>();

	public static class GeekHubApiBuilder {
		@SuppressWarnings("unused")
		private GeekHubApiBuilder ws(WebSocketClient ws) {
			return this;
		}
	}

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
					CsrfData csrf = ParseHelper.parseCsrfData(html);
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
	public <T extends GeekHubPage> GeekHubApiResult<T> fetchPage(PageDefination<T> pd) {
		return this.fetchPage(pd, 1);
	}

	/**
	 * 获取页面解析后的内容
	 * 
	 * @param pd
	 *            页面定义
	 * @param url
	 *            URL
	 * @return 解析后的内容
	 */
	private <T extends GeekHubPage> GeekHubApiResult<T> fetchPage(PageDefination<T> pd, String url) {
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
			result.parse(doc, url);
			return GeekHubApiResult.<T>builder().content(result).success(true).build();
		} else {
			return GeekHubApiResult.<T>builder().build();
		}
	}

	/**
	 * 获取页面解析后的内容
	 * 
	 * @param pd
	 *            页面定义
	 * @param postId
	 *            附加参数
	 * @param page
	 *            页码
	 * @return 解析后的内容
	 */
	public <T extends GeekHubPage> GeekHubApiResult<T> fetchPage(PageDefination<T> pd, int page) {
		String url = String.format("%s/%s", webUrlBase, pd.getSlug()) + (page <= 1 ? "" : ("?page=" + page));
		return fetchPage(pd, url);
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
	public <T extends GeekHubPage> GeekHubApiResult<T> fetchPost(PageDefination<T> pd, String postId) {
		return this.fetchPost(pd, postId, -1);
	}

	/**
	 * 获取帖子
	 * 
	 * @param pd
	 *            页面定义
	 * @param postId
	 *            Post ID
	 * @param page
	 *            页码
	 * @return 解析后的内容
	 */
	public <T extends GeekHubPage> GeekHubApiResult<T> fetchPost(PageDefination<T> pd, String postId, int page) {
		String url = String.format("%s/%s/%s", webUrlBase, pd.getSlug(), postId + "?page=" + page);
		return fetchPage(pd, url);
	}

	/**
	 * 获取帖子列表
	 * 
	 * @param pd
	 *            页面定义
	 * @return 解析后的内容
	 */
	public <T extends GeekHubPage> GeekHubApiResult<GeekHubPostList> fetchPostList(PageDefination<T> pd) {
		return this.fetchPostList(pd, 1);
	}

	/**
	 * 获取帖子
	 * 
	 * @param pd
	 *            页面定义
	 * @param postId
	 *            Post ID
	 * @param page
	 *            页码
	 * @return 解析后的内容
	 */
	public <T extends GeekHubPage> GeekHubApiResult<GeekHubPostList> fetchPostList(PageDefination<T> pd, int page) {
		String url = String.format("%s/%s", webUrlBase, pd.getSlug()) + (page <= 1 ? "" : ("?page=" + page));
		return fetchPage(PageDefination.HOME, url);
	}

	/**
	 * 获取用户资料
	 * 
	 * @param username
	 *            用户名
	 * @return 解析后的内容
	 */
	public GeekHubApiResult<GeekHubUserProfile> fetchUserProfile(String username) {
		String url = String.format("/u/%s", webUrlBase, username);
		return fetchPage(PageDefination.USER, url);
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
		String url = String.format("%s/%s/%s", webUrlBase, pd.getSlug(), postId);
		ResponseResult<String> pageResult = this.fetchPage(url);
		if (!pageResult.isSuccess() || pageResult.getStatus() != HttpStatus.SC_OK) {
			return false;
		}

		if (!this.isLogon()) {
			tryLogin();
			return false;
		}

		String html = pageResult.getContent();
		CsrfData csrf = ParseHelper.parseCsrfData(html);
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
			List<Cookie> cookies = this.identityProvider.getNewCookie(this);
			if (cookies != null) {
				this.httpHelper.addCookies(cookies);
			}
		}
	}

	/**
	 * 登录
	 * 
	 * @return 是否已登录
	 */
	public boolean login() {
		if (!this.isLogon()) {
			tryLogin();
		}
		return this.isLogon();
	}

	/**
	 * 签到
	 * 
	 * @return 是否已签到
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
		CsrfData csrf = ParseHelper.parseCsrfData(result.getContent());

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

	public void init() {
		List<Cookie> cookies = this.identityProvider.loadCookie(this);
		if (cookies != null) {
			this.httpHelper.addCookies(cookies);
		}
	}

	public void addCableListener(Consumer<CableUpdate> listener) {
		if (this.ws == null || this.ws.isClosed() || this.ws.isClosing()) {
			synchronized ("GeekHubApi connect WebSocket".intern()) {
				if (this.ws == null || this.ws.isClosed() || this.ws.isClosing()) {
					connectCable();
				}
			}
		}
		listeners.add(listener);
	}

	private void connectCable() {
		String url = "wss://" + geekHubDomain + "/cable";
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if (uri == null) {
			log.error("ws uri is null!");
		}

		Map<String, String> headers = new HashMap<>();
		{
			List<Cookie> cookies = this.httpHelper.getCookies();
			List<String> foo = new ArrayList<>();
			for (Cookie c : cookies) {
				foo.add(c.getName() + "=" + c.getValue());
			}
			String cookieHeader = String.join("; ", foo);
			if (cookieHeader.length() > 0) {
				headers.put("Cookie", cookieHeader);
			}
		}
		headers.put("User-Agent", "GeekHub/Java");
		headers.put("Host", geekHubDomain);
		headers.put("Origin", getWebUrlBase());

		final GeekHubApi gh = this;
		ws = new WebSocketClient(uri, new Draft_6455(), headers) {

			@Override
			public void onOpen(ServerHandshake handshakedata) {
				log.info("connected");
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onMessage(String message) {
				try {
					do {
						Map<String, Object> json = new ObjectMapper().readValue(message, Map.class);
						String type = (String) json.get("type");
						if (type != null) {
							switch (type) {
							case "welcome":
								log.info("recieve message: welcome");
								// send("{\"command\":\"subscribe\",\"identifier\":\"{\\\"channel\\\":\\\"BoxsterChannel\\\"}\"}");
								send("{\"command\":\"subscribe\",\"identifier\":\"{\\\"channel\\\":\\\"ClassicChannel\\\"}\"}");
								break;
							case "ping":
								break;
							case "confirm_subscription":
								log.info("recieve message: confirm_subscription");
								break;
							default:
								break;
							}
							return;
						}

						String identifier = (String) json.get("identifier");
						if (identifier == null) {
							break;
						}
						Map<String, Object> identifierJson = new ObjectMapper().readValue(identifier, Map.class);
						if (identifierJson == null) {
							break;
						}
						String channel = (String) identifierJson.get("channel");
						if ("ClassicChannel".equals(channel)) {
							Map<String, Object> messageJson = (Map<String, Object>) json.get("message");
							if (messageJson == null) {
								break;
							}
							Map<String, Object> operations = (Map<String, Object>) messageJson.get("operations");
							if (operations == null) {
								break;
							}
							CableUpdate update = new CableUpdate();
							List<Map<String, Object>> insertAdjacenHtmlJson = (List<Map<String, Object>>) operations
									.get("insertAdjacentHtml");

							List<GeekHubComment> comments = new ArrayList<>();
							for (Map<String, Object> insertAdjacenHtml : insertAdjacenHtmlJson) {
								String html = (String) insertAdjacenHtml.get("html");
								Document doc = Jsoup.parse(html);
								Element ce = doc.selectFirst("div");
								GeekHubComment c = GeekHubPost.parseComment(ce);
								if (c != null) {
									comments.add(c);
								}
							}

							List<GeekHubPostItem> items = new ArrayList<>();
							List<Map<String, Object>> dispachEventJson = (List<Map<String, Object>>) operations
									.get("dispatchEvent");
							for (Map<String, Object> dispathEvent : dispachEventJson) {
								String name = (String) dispathEvent.get("name");
								if ("bump_to_top".equals(name)) {
									String html = (String) dispathEvent.get("detail");
									Document doc = Jsoup.parse(html);
									Element article = doc.selectFirst("article");
									GeekHubPostItem postItem = GeekHubPostList.parsePostItem(article);
									if (postItem != null) {
										items.add(postItem);
									}
								}
							}

							update.setComments(comments);
							update.setPosts(items);

							log.info("recieve update.");
							for (Consumer<CableUpdate> listenr : gh.listeners) {
								listenr.accept(update);
							}
							return;
						}
					} while (false);

					log.warn("unkown message: " + message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Exception ex) {
				log.error("ws error: ", ex);
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				log.warn(String.format("Close. code: %s, reason: %s", code, reason));
				gh.connectCable();
			}
		};
		ws.connect();
	}

}
