package com.dallaslu.geekhub.api.page;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dallaslu.geekhub.api.utils.ParseHelper;

import lombok.Getter;

@Getter
public class MoleculePost extends GeekHubPost {
	private String item;
	private double value;
	private double specialValue;
	private int count;
	private int seconds;
	private String[] luckyFloors;

	@Override
	public void parse(Document doc, String url) {
		super.parse(doc, url);
		Elements boxes = doc.select("main>div");
		Element articleE = boxes.get(0);
		Elements specialEs = articleE.select("div.border>div.flex>div");

		for (Element infoE : specialEs.get(0).select("div.flex")) {
			String text = infoE.text();
			if (text.matches("产品名称.*")) {
				this.item = text.replaceFirst("产品名称\\s*", "").trim();
			} else if (text.matches("分子价值.*")) {
				this.value = Double.parseDouble(text.replaceFirst("分子价值 ¥ \\s*", "").trim());
			} else if (text.matches("活动价格.*")) {
				this.specialValue = Double.parseDouble(text.replaceFirst("活动价格 ¥ \\s*", "").trim());
			} else if (text.matches("分子数量.*")) {
				this.count = Integer.parseInt(text.replaceFirst("分子数量 \\(x\\)\\s*", "").trim());
			}
		}

		int seconds = 0;
		{
			Element countDownE = articleE.selectFirst("#count-down");
			if (countDownE != null) {
				String dataSeconds = countDownE.attr("data-seconds");
				if (dataSeconds != null) {
					seconds = Integer.parseInt(dataSeconds);
				}
			}
		}

		String[] luckyFloors = new String[0];
		if (seconds == 0) {
			String oneLineHtml = doc.html().replace("\n", "");
			// TODO 优化避免 replaceAll("\n", "")
			Pattern p = Pattern.compile(
					"<span class=\"[\\w_\\-\\s]+\">分子楼层:</span>\\s*<span class=\"[\\w_\\-\\s]+?\">\\s*((?:\\d+, )*\\d+)\\s*</span>");
			Matcher m = p.matcher(oneLineHtml);
			if (m.find()) {
				String foo = m.group(1);
				luckyFloors = foo.split(", ");
				seconds = -1;
			}

			Pattern p2 = Pattern.compile("<div class=\"[\\w_\\-\\s]+\">\\s*抢分子已经?于\\s*(大约)?\\s*(\\d+\\s*小时前)结束");
			Matcher m2 = p2.matcher(oneLineHtml);
			if (m2.find()) {
				Date settlementTime = ParseHelper.parseDate(m2.group(2));
				seconds = (int) ((settlementTime.getTime() - System.currentTimeMillis()) / 1000L);
			}
		}

		this.seconds = seconds;
		this.luckyFloors = luckyFloors;
	}
}
