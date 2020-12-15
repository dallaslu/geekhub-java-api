package com.dallaslu.geekhub.api.page;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dallaslu.geekhub.api.utils.ParseHelper;

import lombok.Getter;

@Getter
public class GeekHubPage {

	protected String title;
	protected int activities;
	protected int totalPage = 1;
	protected int notify;
	protected boolean logon;

	protected int myGbit;
	protected int myStar;
	protected double myScore;

	public void parse(Document doc) {

		Element titleP = doc.selectFirst("main>div>div.heading>p");
		if (titleP != null) {
			this.title = titleP.text();
		}

		Elements activitiesLinkSpan = doc.select("a[href=/activities]>span");
		if (activitiesLinkSpan != null && activitiesLinkSpan.size() >= 2) {
			String value = activitiesLinkSpan.get(1).text();
			if (value.matches("\\d+")) {
				this.activities = Integer.parseInt(value);
			}
		}

		// 分页
		for (Element pageNavE : doc.select("main nav>ul li a")) {
			if (pageNavE.text().matches("\\d+")) {
				int foo = Integer.parseInt(pageNavE.text());
				totalPage = Math.max(foo, totalPage);
			}
		}

		// 登录
		logon = ParseHelper.parseLogon(doc.html());
		// 提醒
		{
			Elements foo = doc.select("header a[href=/activities] span");
			if (foo != null && foo.size() > 1) {
				String bar = foo.get(1).text();
				notify = Integer.parseInt(bar);
			}
		}

		// 我的积分
		{
			Element block = doc.select("sidebar>div>div").get(1);
			Element valueBlock = block.select("div>div").get(1);
			Elements values = valueBlock.select("div");
			if (values.text().trim().matches("[\\d\\s\\.]+")) {
				myGbit = Integer.parseInt(values.get(0).text());
				myStar = Integer.parseInt(values.get(1).text());
				myScore = Double.parseDouble(values.get(2).text());
			}
		}
	}
}
