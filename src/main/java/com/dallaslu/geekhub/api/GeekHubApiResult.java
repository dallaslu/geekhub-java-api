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
	private boolean logon;
	private T content;
	private boolean deleted;
}
