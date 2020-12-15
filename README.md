# geekhub-java-api
geekhub.com Java API 爬虫版

## Usage

### Create Instance

```java
GeekHubApi gh = GeekHubApi.builder()
		.httpHelper(HttpClientHelper.create(GeekHubApi.DEFAULT_URL_BASE, GeekHubApi.USER_AGENT))
		.identityProvider(GeekHubCommonAuth.builder().dataPath("<data-path>").username("<username>")
				.password("<password>").build())
		.build();
gh.init();
```

### Checkins

```java
gh.checkIns();
```

For first run, there will be a captcha image file in `<data-path>/tmp`, such as `9e916356-0e73-46cc-8d01-85827d35c1cd.jpg`. Create a text file with the captcha value, and rename it to `9e916356-0e73-46cc-8d01-85827d35c1cd.jpg.txt`.

### Post

#### Example

```java
GeekHubApiResult<GeekHubPost> apiResult = gh.fetchPage(PageDefination.DEFAULT_POST, "2296");

String poster = apiResult.getContent().getPoster();	// It will be '37丫37'
```

```java
GeekHubApiResult<MoleculePost> apiResult = gh.fetchPage(PageDefination.MOLECULE, "1");
String title = apiResult.getContent().getTitle();// It will be '{ 活动队列，暂未开始 } GeekHub Launch！抢到楼层，这一箱 AirPods 2 就是你的了。'
```