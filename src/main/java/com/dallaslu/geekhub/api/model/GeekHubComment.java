package com.dallaslu.geekhub.api.model;

import java.util.Date;

import lombok.Data;

@Data
public class GeekHubComment {
	private int floor;
	private int star;
	private int replyTo;
	private String id;
	private String user;
	private String[] userMedal;
	private String userRole;
	private String content;
	private Date createTime;
	private int userGbit;
	private int userStar;
	private int commentStar;
	private String via;
}
