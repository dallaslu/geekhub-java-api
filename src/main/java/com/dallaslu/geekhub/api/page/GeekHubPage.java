package com.dallaslu.geekhub.api.page;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.Getter;

@Getter
public class GeekHubPage {

	protected String title;
	protected int activities;

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
	}
}
