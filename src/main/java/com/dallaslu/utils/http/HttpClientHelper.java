package com.dallaslu.utils.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.dallaslu.utils.http.HttpHelper.ResponseResult.ResponseResultBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpClientHelper implements HttpHelper {

	private CookieStore cookieStore;
	private CloseableHttpClient client;
	private RequestConfig requestConfig;

	private HttpClientHelper() {
		cookieStore = new BasicCookieStore();
		requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public static HttpClientHelper create() {
		return create("", null);
	}

	public static HttpClientHelper create(String siteBase) {
		return create(siteBase, null);
	}

	public static HttpClientHelper create(String siteBase, String userAgent) {
		HttpClientHelper instance = new HttpClientHelper();
		if (siteBase.startsWith("https")) {
			instance.client = createSSLClientDefault(instance.getCookieStore(), userAgent);
		} else {
			HttpClientBuilder builder = HttpClients.custom().setDefaultCookieStore(instance.getCookieStore());
			if (userAgent != null) {
				builder.setUserAgent(userAgent);
			}
			instance.client = builder.build();
		}
		return instance;
	}

	public void close() {
		try {
			this.client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class TM implements TrustManager, X509TrustManager {

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {
			// don't check
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {
			// don't check
		}
	}

	private static CloseableHttpClient createSSLClientDefault(CookieStore cookieStore, String userAgent) {
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates())
				.build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
				socketFactoryRegistry);
		HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connectionManager)
				.setDefaultCookieStore(cookieStore);
		if (userAgent != null) {
			builder.setUserAgent(userAgent);
		}
		CloseableHttpClient httpClient = builder.build();
		return httpClient;
	}

	private static SSLConnectionSocketFactory trustAllHttpsCertificates() {
		SSLConnectionSocketFactory socketFactory = null;
		TrustManager[] trustAllCerts = new TrustManager[1];
		TrustManager tm = new TM();
		trustAllCerts[0] = tm;
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, null);
			socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return socketFactory;
	}

	public String post(String url, Map<String, Object> params) {
		return this.post(url, params, null);
	}

	public String post(String url, Map<String, Object> params, Map<String, String> headers) {
		HttpPost httppost = new HttpPost(url);
		httppost.setConfig(requestConfig);

		if (headers != null) {
			Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				httppost.addHeader(entry.getKey(), entry.getValue());
			}
		}

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		String jsonString = "";
		for (String key : params.keySet()) {
			formparams.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
		}
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);
			System.out.println("executing request " + httppost.getURI());
			CloseableHttpResponse response = client.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					jsonString = EntityUtils.toString(entity, "UTF-8");
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return jsonString;
	}

	public ResponseResult<String> get(String url, Map<String, Object> params) {
		StringBuffer sb = new StringBuffer();
		sb.append(url).append("?");
		Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		String foo = sb.toString();
		String requestUrl = foo.substring(0, foo.length() - 1);
		return get(requestUrl);
	}

	public ResponseResult<String> get(String url, Map<String, Object> params, Map<String, String> headers) {
		StringBuffer sb = new StringBuffer();
		sb.append(url).append("?");
		Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		String foo = sb.toString();
		String requestUrl = foo.substring(0, foo.length() - 1);
		return getWithHeaders(requestUrl, headers);
	}

	public ResponseResult<String> getWithHeaders(String url, Map<String, String> headers) {
		ResponseResult.builder();
		ResponseResultBuilder<String> resultBuilder = ResponseResult.<String>builder();
		try {
			HttpGet httpget = new HttpGet(url);
			httpget.setConfig(requestConfig);
			log.debug("executing get request " + httpget.getURI());
			Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				httpget.addHeader(entry.getKey(), entry.getValue());
			}
			CloseableHttpResponse response = client.execute(httpget);
			try {
				HttpEntity entity = response.getEntity();
				resultBuilder.status(response.getStatusLine().getStatusCode());
				if (entity != null) {
					String jsonString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
					resultBuilder.content(jsonString);
				}
				resultBuilder.success(true);
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return resultBuilder.build();
	}

	@SuppressWarnings("unchecked")
	public ResponseResult<String> get(String url) {
		return getWithHeaders(url, Collections.EMPTY_MAP);
	}

	public String postWithJson(String url, String json) {
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			StringEntity requestEntity = new StringEntity(json, "utf-8");
			requestEntity.setContentEncoding("UTF-8");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setEntity(requestEntity);
			return client.execute(httpPost, responseHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public InputStream getInputStream(String url) {
		try {
			HttpGet httpget = new HttpGet(url);
			httpget.setConfig(requestConfig);
			CloseableHttpResponse response = client.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void download(String urlString, String filename, String savePath) {
		File sf = new File(savePath);
		if (!sf.exists()) {
			sf.mkdirs();
		}
		try (OutputStream os = new FileOutputStream(sf.getPath() + File.separator + filename);
				InputStream is = this.getInputStream(urlString);) {
			byte[] bs = new byte[1024];
			int len;
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addCookies(List<Cookie> cookies) {
		for (Cookie c : cookies) {
			this.getCookieStore().addCookie(c);
		}
	}

	@Override
	public List<Cookie> getCookies() {
		return this.getCookieStore().getCookies();
	}
}
