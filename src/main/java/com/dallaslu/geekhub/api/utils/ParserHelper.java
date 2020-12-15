package com.dallaslu.geekhub.api.utils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserHelper {
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
}
