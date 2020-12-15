package com.dallaslu.geekhub.api.model;

import java.util.Date;

import com.dallaslu.geekhub.api.page.PageDefination;

import lombok.Data;

@Data
public class GeekHubPostItem {
	private String postId;
	private String poster;
	private String title;
	private int commentNum;
	private String lastReplier;
	private Date updateTime;
	private GeekHubClub club;
	private PageDefination<?> type;
	private boolean isTop;
}
