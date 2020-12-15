package com.dallaslu.geekhub.api;

import lombok.Builder;
import lombok.Getter;

/**
 * 页面结果
 */
@Getter
@Builder
public class GeekHubApiResult<T> {
	private boolean success;
	private String raw;
	private int totalPage;
	private boolean logon;
	private int myGbit;
	private int myStar;
	private int myScore;
	private int notify;
	private T content;
	private boolean deleted;
}
