package com.dallaslu.geekhub.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

import com.dallaslu.geekhub.api.auth.GeekHubCommonAuth;
import com.dallaslu.geekhub.api.page.GeekHubPost;
import com.dallaslu.geekhub.api.page.MoleculePost;
import com.dallaslu.geekhub.api.page.PageDefination;
import com.dallaslu.utils.http.HttpClientHelper;

@DisplayName("GeekHubApi 测试")
public class GeekHubApiTests {

	private GeekHubApi gh;

	@Before
	public void init() {
		gh = GeekHubApi.builder()
				.httpHelper(HttpClientHelper.create(GeekHubApi.DEFAULT_URL_BASE, GeekHubApi.USER_AGENT))
				.identityProvider(GeekHubCommonAuth.builder().dataPath("<data-path>").username("dallaslu")
						.password("<password>").build())
				.build();
		gh.init();
	}

	@Test
	@DisplayName("分子页面标题")
	public void simpleTitleTest() {
		GeekHubApiResult<MoleculePost> apiResult = gh.fetchPage(PageDefination.MOLECULE, "1");
		Assertions.assertTrue(apiResult.getContent().getTitle().contains("AirPods"));
	}

	@Test
	@DisplayName("Poster")
	public void simplePosterTest() {
		GeekHubApiResult<GeekHubPost> apiResult = gh.fetchPage(PageDefination.DEFAULT_POST, "2296");
		Assertions.assertTrue(apiResult.getContent().getPoster().equals("37丫37"));
	}

	@Test
	@DisplayName("CheckIn")
	public void checkInsTest() {
		Assertions.assertTrue(gh.checkins());
	}
}
