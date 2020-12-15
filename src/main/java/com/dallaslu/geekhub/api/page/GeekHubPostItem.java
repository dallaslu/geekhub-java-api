package com.dallaslu.geekhub.api.page;

import lombok.Data;

@Data
public class GeekHubPostItem {
	private String postId;
	private String poster;
	private String title;
	private int commentNum;
}
