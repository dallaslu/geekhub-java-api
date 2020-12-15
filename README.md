# geekhub-java-api
geekhub.com Java API 爬虫版

## Depends

(Java >8, Lombok, HttpClient, Slf4j)

## Usage

### Create Instance

#### Login With username and password
```java
GeekHubApi gh = GeekHubApi.builder()
		.httpHelper(HttpClientHelper.create(GeekHubApi.DEFAULT_URL_BASE, GeekHubApi.USER_AGENT))
		.identityProvider(GeekHubCommonAuth.builder().dataPath("<data-path>").username("<username>")
				.password("<password>").build())
		.build();
gh.init();
```
For first run, there will be a captcha image file in `<data-path>/tmp`, such as `9e916356-0e73-46cc-8d01-85827d35c1cd.jpg`. 
Create a text file with the captcha value, and rename it to `9e916356-0e73-46cc-8d01-85827d35c1cd.jpg.txt`.

And If the session is expired, it will run again when processing some pages which need login.  

#### Login with cookies

For custom cookies:
```java
GeekHubApi gh = GeekHubApi.builder()
		.httpHelper(HttpClientHelper.create(GeekHubApi.DEFAULT_URL_BASE, GeekHubApi.USER_AGENT))
		.identityProvider(new GeekHubIdentityProvider() {
			@Override
			public List<Cookie> loadCookie(GeekHubApi geekHubApi) {
				// load your cookie from anywhere
				return null;
			}
		}).build();
gh.init();
```
### Checkins (needs login)

```java
boolean checkedIn = gh.checkIns();
```

### Fetch Posts

```java
GeekHubApiResult<GeekHubPost> apiResult = gh.fetchPost(PageDefination.DEFAULT_POST, "2296");
String poster = apiResult.getContent().getPoster();	// It will be '37丫37'
```
```java
GeekHubApiResult<MoleculePost> apiResult = gh.fetchPost(PageDefination.MOLECULE, "1");
String title = apiResult.getContent().getTitle();// It will be '{ 活动队列，暂未开始 } GeekHub Launch！抢到楼层，这一箱 AirPods 2 就是你的了。'
```

### Fetch Users

```java
GeekHubApiResult<GeekHubUserProfile> apiResult = gh.fetchUserProfile("GM");
```

### Listener

```java
gh.addCableListener(update -> {
	for(GeekHubPostItem item : update.getPosts()){
		// do something
	}
	for(GeekHubComment comment : update.getComments()){
		// do something
	}
});
```