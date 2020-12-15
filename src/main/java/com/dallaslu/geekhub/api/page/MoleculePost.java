package com.dallaslu.geekhub.api.page;

import java.util.Map;

import lombok.Getter;

@Getter
public class MoleculePost extends GeekHubPost {

	public static final String ITEM = "产品名称";
	public static final String VALUE = "分子价值";
	public static final String SPECIAL_VALUE = "活动价格";
	public static final String COUNT = "分子数量";
	public static final String SECONDS = "剩余秒数";
	public static final String LUCKY_FLOORS = "分子楼层";

	private String item;
	private double value;
	private double specialValue;
	private int count;
	private int seconds;
	private String[] luckyFloors;

	public void fromInfo(Map<String, Object> info) {
		this.item = (String) info.get(ITEM);
		this.value = (double) info.get(VALUE);
		this.specialValue = (double) info.get(SPECIAL_VALUE);
		this.count = (int) info.get(COUNT);
		this.seconds = (int) info.get(SECONDS);
		this.luckyFloors = (String[]) info.get(LUCKY_FLOORS);
	}
}
