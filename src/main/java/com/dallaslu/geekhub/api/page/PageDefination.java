package com.dallaslu.geekhub.api.page;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PageDefination<T extends GeekHubPage> {

	private static Map<String, PageDefination<?>> definations = new HashMap<>();

	public static final PageDefination<GeekHubPost> DEFAULT_POST = register("Post", "posts", "话题", GeekHubPost.class);
	public static final PageDefination<GeekHubPostList> HOME = register("Home", "", "首页", GeekHubPostList.class);
	public static final PageDefination<GeekHubPost> SECOND_HAND = register("SecondHand", "second_hands", "二手",
			GeekHubPost.class);
	public static final PageDefination<GeekHubPost> AUCTION = register("Auction", "auctions", "拍卖", GeekHubPost.class);
	public static final PageDefination<MoleculePost> MOLECULE = register("Molecule", "molecules", "分子",
			MoleculePost.class);
	public static final PageDefination<GeekHubPost> GROUP_BUY = register("GroupBuy", "group_buys", "拼车",
			GeekHubPost.class);
	public static final PageDefination<GeekHubPost> PRODUCT = register("Product", "products", "商品", GeekHubPost.class);
	public static final PageDefination<GeekHubPost> SERVICE = register("Service", "services", "服务", GeekHubPost.class);
	public static final PageDefination<GeekHubPost> WORK = register("Work", "works", "产品", GeekHubPost.class);
	public static final PageDefination<GeekHubPost> CLUB = register("Club", "club", "小组", GeekHubPost.class);
	public static final PageDefination<GeekHubProductList> GBIT_STORE = register("GbitStore", "gbit_store", "福利社",
			GeekHubProductList.class);
	public static final PageDefination<GeekHubShop> SHOP = register("Shop", "shop", "商店", GeekHubShop.class);
	public static final PageDefination<GeekHubUserProfile> USER = register("User", "u", "用户资料",
			GeekHubUserProfile.class);

	public static <P extends GeekHubPage> PageDefination<P> register(String type, String slug, String name,
			Class<P> pageClass) {
		PageDefination<P> pd = new PageDefination<>(type, slug, name, pageClass);
		definations.put(slug, pd);
		return pd;
	}

	public static PageDefination<?> getPageDefinationBySlug(String slug) {
		return definations.get(slug);
	}

	@Getter
	private String type;
	@Getter
	private String slug;
	@Getter
	private String name;
	@Getter
	private Class<T> pageClass;
}
