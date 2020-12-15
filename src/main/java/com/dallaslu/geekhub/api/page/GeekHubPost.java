package com.dallaslu.geekhub.api.page;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dallaslu.geekhub.api.model.AppendContent;
import com.dallaslu.geekhub.api.model.GeekHubClub;
import com.dallaslu.geekhub.api.model.GeekHubComment;
import com.dallaslu.geekhub.api.utils.ParseHelper;

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
	protected GeekHubClub club;
	protected List<GeekHubComment> comments;

	@Override
	public void parse(Document doc) {
		super.parse(doc);

		// poster
		Elements boxes = doc.select("main>div");
		Element articleE = boxes.get(0);
		Element posterLink = articleE.selectFirst("a[href^=/u/]");
		this.poster = ParseHelper.parseUserLink(posterLink);

		// club
		Element clubLink = articleE.selectFirst("a[href^=/club/]");
		this.club = ParseHelper.parseClubLink(clubLink);

		Elements allMetaEs = articleE.select("div.box div.meta>div");

		// Gbit, star
		Element posterMeta = allMetaEs.get(0);
		{
			String text = posterMeta.text();
			{
				Pattern p = Pattern.compile("Gbit\\s*(\\d+)");
				Matcher m = p.matcher(text);
				if (m.find()) {
					this.posterGbit = Integer.parseInt(m.group(1));
				}
			}
			{
				Pattern p = Pattern.compile("Star\\s*(\\d+)");
				Matcher m = p.matcher(text);
				if (m.find()) {
					this.posterStar = Integer.parseInt(m.group(1));
				}
			}
		}

		Element postMeta = allMetaEs.get(1);

		Elements postMetaSpanEs = postMeta.select("span");
		for (Element pMSE : postMetaSpanEs) {
			String text = pMSE.text();
			if (text.matches("发布于 \\d+.*")) {
				this.createdDate = ParseHelper.parseDate(text.replaceFirst("发布于\\s*", ""));
			} else if (text.matches("更新于 \\d+.*")) {
				this.updatedDate = ParseHelper.parseDate(text.replaceFirst("更新于\\s*", ""));
			} else if (text.matches("\\d+.*浏览")) {
				this.views = Integer.parseInt(text.replaceFirst("\\s*浏览$", ""));
			}
		}

		Element commentE = boxes.get(1);
		Elements commentElements = commentE.selectFirst("div.flex").select("div span");

		if (commentElements != null && commentElements.size() > 1) {
			Element commentStat = commentElements.get(1);
			if (commentStat.text().matches("\\d+\\s* 回复")) {
				this.commentNum = Integer.parseInt(commentStat.text().replaceAll("\\s*回复$", ""));
			}
		}
		comments = new ArrayList<>();
		Elements commentEs = commentE.select("div.comment-list");
		for (Element ce : commentEs) {
			GeekHubComment c = new GeekHubComment();
			String cid = ce.id();
			c.setId(cid.replaceFirst("comment_", ""));

			Element cMain = ce.selectFirst("div.flex>div.action-list-parent");

			Elements metas = cMain.select("div.flex>div.inline-flex");

			Element userLink = metas.get(0).selectFirst("a[href^=/u/]");
			c.setUser(ParseHelper.parseUserLink(userLink));

			{
				List<String> foo = new ArrayList<>();
				Elements metalEs = metas.get(0).select("span>img");
				for (Element mE : metalEs) {
					foo.add(mE.attr("title"));
				}
				c.setUserMedal(foo.toArray(new String[0]));
			}

			Elements metaSpanEs = metas.get(0).select("span");
			for (Element mSE : metaSpanEs) {
				Date createTime = ParseHelper.parseDate(mSE.text());
				if (createTime != null) {
					c.setCreateTime(createTime);
				} else if (mSE.text().matches("Gbit:\\s*\\d+")) {
					c.setUserGbit(Integer.parseInt(mSE.text().replaceFirst("Gbit:\\s*", "")));
				} else if (mSE.text().matches("STAR:\\s*\\d+")) {
					c.setUserStar(Integer.parseInt(mSE.text().replaceFirst("STAR:\\s*", "")));
				}
				// Gbit, star
			}

			for (Element sE : metas.get(1).select("span")) {
				if (sE.text().matches("#\\d+")) {
					c.setFloor(Integer.parseInt(sE.text().replaceFirst("#", "")));
					break;
				}
			}

			c.setStar(Integer.parseInt(metas.get(1).select("span.star-count.meta").text()));

			String content = cMain.selectFirst("div.mt-2.text-primary-700").select("span").text();
			if (content != null) {
				c.setContent(content.trim());
			}

			comments.add(c);
		}
	}

}
