package com.dallaslu.geekhub.api.page;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class GeekHubUserProfile extends GeekHubPage {
	private String telegram;

	@Override
	public void parse(Document doc) {
		super.parse(doc);
		Element userInfoE = doc.selectFirst("main>div.box");
		if (userInfoE == null) {
			return;
		}
		Elements profileEs = userInfoE.select("div.box>div.border-color>div.border-color>div");
		if (profileEs.size() < 2) {
			return;
		}
		Element profileSocialE = profileEs.get(1);
		for (Element ps : profileSocialE.select("div.flex")) {
			String text = ps.text();
			log.debug("user page: " + text);
			if (text.matches("\\s*Telegram\\s*.*")) {
				telegram = text.replaceFirst("\\s*Telegram\\s*", "").trim();
			} else if (text.matches("\\s*Telegram\\s*https://t\\.me/.*")) {
				telegram = text.replaceFirst("\\s*Telegram\\s*https://t\\.me/", "").trim();
			} else if (text.matches("\\s*Telegram\\s*@.*")) {
				telegram = text.replaceFirst("\\s*Telegram\\s*@", "").trim();
			}
		}
	}
}
