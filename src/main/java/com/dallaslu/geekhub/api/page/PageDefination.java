package com.dallaslu.geekhub.api.page;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PageDefination<T extends GeekHubPage> {

	private static Map<String, PageDefination<?>> definations = new HashMap<>();

	public static final PageDefination<GeekHubPost> DEFAULT_POST = new PageDefination<>("Post", "posts", "话题",
			GeekHubPost.class);
	public static final PageDefination<GeekHubPostList> HOME = new PageDefination<>("Home", "", "首页",
			GeekHubPostList.class);
	public static final PageDefination<GeekHubPost> SECOND_HAND = new PageDefination<>("SecondHand",
			"second_hands", "二手", GeekHubPost.class);
	public static final PageDefination<GeekHubPost> AUCTION = new PageDefination<>("Auction", "auctions", "拍卖",
			GeekHubPost.class);
	public static final PageDefination<MoleculePost> MOLECULE = new PageDefination<>("Molecule", "molecules", "分子",
			MoleculePost.class);
	public static final PageDefination<GeekHubPost> GROUP_BUY = new PageDefination<>("GroupBuy", "group_buys",
			"拼车", GeekHubPost.class);
	public static final PageDefination<GeekHubPost> PRODUCT = new PageDefination<>("Product", "products", "商品",
			GeekHubPost.class);
	public static final PageDefination<GeekHubPost> SERVICE = new PageDefination<>("Service", "service", "服务",
			GeekHubPost.class);
	public static final PageDefination<GeekHubPost> WORK = new PageDefination<>("Work", "works", "产品",
			GeekHubPost.class);
	public static final PageDefination<GeekHubPost> CLUB = new PageDefination<>("Club", "club", "小组",
			GeekHubPost.class);
	public static final PageDefination<GeekHubProductList> GBIT_STORE = new PageDefination<>("GbitStore", "gbit_store",
			"福利社", GeekHubProductList.class);
	public static final PageDefination<GeekHubShop> SHOP = new PageDefination<>("Shop", "shop", "商店",
			GeekHubShop.class);

	public <P extends GeekHubPage> PageDefination<P> register(String type, String url, String name,
			Class<P> pageClass) {
		PageDefination<P> pd = new PageDefination<>(type, url, name, pageClass);
		definations.put(url, pd);
		return pd;
	}

	@Getter
	private String type;
	@Getter
	private String url;
	@Getter
	private String name;
	@Getter
	private Class<T> pageClass;
}
