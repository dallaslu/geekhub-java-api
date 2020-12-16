package com.dallaslu.geekhub.api.page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dallaslu.geekhub.api.model.GeekHubClub;
import com.dallaslu.geekhub.api.model.GeekHubPostItem;
import com.dallaslu.geekhub.api.utils.ParseHelper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class GeekHubPostList extends GeekHubPage {
	private List<GeekHubPostItem> posts = new ArrayList<>();

	@Override
	public void parse(Document doc, String url) {
		super.parse(doc, url);
		Elements mainArticles = doc.select("main article");
		for (Element article : mainArticles) {
			GeekHubPostItem post = parsePostItem(article);
			if (post != null) {
				posts.add(post);
			}
		}
	}

	public static GeekHubPostItem parsePostItem(Element article) {
		GeekHubPostItem post = new GeekHubPostItem();
		Element postLink = article.select("h3 a").get(0);

		post.setTitle(postLink.text());

		String link = postLink.attr("href");
		{
			Pattern p = Pattern.compile("/(.*)/(\\d+)");
			Matcher m = p.matcher(link);
			if (m.find()) {
				post.setPostId(m.group(2));
				PageDefination<?> pd = PageDefination.getPageDefinationBySlug(m.group(1));
				if (pd == null) {
					log.warn("未识别的 post type: " + m.group(1));
					return null;
				}
				post.setType(pd);
			}
		}
		// comments num
		{
			String foo = article.select("article>div").get(2).select("span").last().text().trim();
			if (foo.matches("\\d+")) {
				post.setCommentNum(Integer.parseInt(foo));
			}
		}
		Element meta = article.select("div.meta").get(0);
		Elements userLinks = meta.select("a[href^=/u/]");

		post.setPoster(userLinks.get(0).text().trim());
		if (post.getCommentNum() > 0) {
			post.setLastReplier(userLinks.get(1).text().trim());
		}
		// last update
		{
			Elements spans = meta.select("span");
			for (Element span : spans) {
				Date update = ParseHelper.parseDate(span.text());
				if (update != null) {
					post.setUpdateTime(update);
					break;
				}
			}
		}

		// isTop
		post.setTop(article.select("article>div").get(2).select("svg").size() > 0);
		// club
		{
			Element clubLink = article.selectFirst("a[href^=/club/]");
			GeekHubClub club = ParseHelper.parseClubLink(clubLink);
			post.setClub(club);
		}
		return post;
	}
}
