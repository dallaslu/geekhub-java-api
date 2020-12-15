package com.dallaslu.geekhub.api.utils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import com.dallaslu.geekhub.api.CsrfData;
import com.dallaslu.geekhub.api.model.GeekHubClub;

public class ParseHelper {
	public static String parseUserLink(Element userLink) {
		return userLink.attr("href").replaceFirst("/u/", "");
	}

	public static GeekHubClub parseClubLink(Element clubLink) {
		if (clubLink == null) {
			return null;
		}
		GeekHubClub club = new GeekHubClub();
		club.setSlug(clubLink.attr("href").replaceFirst("/club/", ""));
		club.setName(clubLink.text());
		return club;
	}

	public static Date parseDate(String text) {
		Pattern p = Pattern.compile("(\\d+)\\s*个?(分钟|小时|天|月|年)前");
		Matcher m = p.matcher(text);
		long value = 0L;
		int n = 0;
		if (m.find()) {
			n = Integer.parseInt(m.group(1));
			String foo = m.group(2);
			switch (foo) {
			case "秒":
				value = 1L;
				break;
			case "分钟":
				value = 60L;
				break;
			case "小时":
				value = 3600L;
				break;
			case "天":
				value = 3600L * 24;
				break;
			case "星期":
			case "周":
				value = 3600L * 24 * 7;
				break;
			case "月":
				value = 3600L * 24 * 30;
				break;
			case "年":
				value = 3600L * 24 * 365;
				break;
			default:
			}
		}
		Date date = new Date(System.currentTimeMillis() - value * n * 1000L);
		return date;
	}

	public static CsrfData parseCsrfData(String html) {
		CsrfData cd = new CsrfData();
		{
			Pattern p = Pattern.compile("<meta name=\"csrf-param\" content=\"(.*?)\" />");
			Matcher matcher = p.matcher(html);
			if (matcher.find()) {
				cd.setCsrfParam(matcher.group(1));
			}
		}
		{
			Pattern p = Pattern.compile("<meta name=\"csrf-token\" content=\"(.*?)\" />");
			Matcher matcher = p.matcher(html);
			if (matcher.find()) {
				cd.setCsrfToken(matcher.group(1));
			}
		}
		return cd;
	}
}
