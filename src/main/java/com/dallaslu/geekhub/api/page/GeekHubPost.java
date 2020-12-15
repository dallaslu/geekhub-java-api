package com.dallaslu.geekhub.api.page;

import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dallaslu.geekhub.api.model.AppendContent;

import lombok.Getter;

@Getter
public class GeekHubPost extends GeekHubPage {
	protected String poster;
	protected int posterGbit;
	protected int posterStar;
	protected Date createdDate;
	protected Date updatedDate;
	protected int views;
	protected int star;
	protected int commentNum;
	protected String content;
	protected List<AppendContent> appendContents;

	@Override
	public void parse(Document doc) {
		super.parse(doc);
		Elements boxes = doc.select("main>div");
		Element articleE = boxes.get(0);
		Element posterLink = articleE.selectFirst("a[href^=/u/]");
		this.poster = parseUserLink(posterLink);
	}

	private String parseUserLink(Element userLink) {
		return userLink.attr("href").replaceFirst("/u/", "");
	}
}
