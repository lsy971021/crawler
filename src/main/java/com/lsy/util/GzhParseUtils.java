package com.lsy.util;

import com.lsy.constant.GeneralConstant;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 公众号html解析工具类
 */
public class GzhParseUtils {
    // 地域正则
    private static final Pattern provincePattern = Pattern.compile("provinceName: '\\S+'");

    private static final Pattern fromPattern = Pattern.compile("来源：\\S+");

    private static final Pattern authorPattern = Pattern.compile("var author = \"\\S+\";");
    //符号过滤
    private static final String regEx = "来源|\\pP|\\pS|\\s+";


    /**
     * 公众号标题解析
     *
     * @param html
     * @return
     */
    public static String getTitle(String html) {
        if (StringUtils.isBlank(html))
            return null;
        String title = null;
        Document document = Jsoup.parse(html);
        Element titleEl = document.getElementById("activity-name");
        title = titleEl != null ? titleEl.text().trim() : null;
        return title;
    }

    /**
     * 公众号作者解析
     *
     * @param html
     * @return
     */
    public static String getAuthor(String html) {
        if (StringUtils.isBlank(html))
            return null;
        String author = null;
        Document document = Jsoup.parse(html);
        Matcher m = authorPattern.matcher(html);
        while (m.find()) {
            author = m.group(0);
        }
        if (author != null) {
            author = author.replace("var author = \"", "");
            author = author.substring(0, author.length() - 2).trim();
        }
        if (StringUtils.isBlank(author)) {
            //文章作者
            Elements authorElements = document.getElementsByTag("meta");
            for (Element authorElement : authorElements) {
                String name = authorElement.attr("name");
                if (name.equals("author")) {
                    author = authorElement.attr("content");
                    if (author != null) {
                        author = author.trim().replaceAll(GeneralConstant.BLANK, "、");
                        author = author.trim().replaceAll(GeneralConstant.VERTICAL, "、");
                    }
                }
            }
        }
        return author;
    }


    /**
     * 公众号正文解析
     *
     * @param html
     * @return
     */
    public static String getContent(String html) {
        if (StringUtils.isBlank(html))
            return null;
        html = html.replaceAll("<[\\s]*?br[^>]*>", "《br》");
        Document document = Jsoup.parse(html);
        return CommonParseUtils.getContent(document.getElementById("js_content"));
    }

    /**
     * 公众号地域解析
     *
     * @param html
     * @return
     */
    public static String getProvince(String html) {
        if (StringUtils.isBlank(html))
            return null;
        String province = null;
        try {
            Matcher matcher = provincePattern.matcher(html);
            while (matcher.find()) {
                province = matcher.group(0);
            }
            if (province != null) {
                String replace = province.replace("provinceName: '", "");
                province = replace.substring(0, replace.length() - 2);
                String[] allProvince = GeneralConstant.allProvince;
                for (int i = 0; i < allProvince.length; i++) {
                    String p = allProvince[i];
                    if (p.contains(province) || province.contains(p)) {
                        province = p;
                        break;
                    }
                }

            }
        } catch (Exception e) {
            return null;
        }
        return province;
    }

    /**
     * 公众号机构解析
     *
     * @param html
     * @return
     */
    public static String noticeOrgan(String html) {
        if (StringUtils.isBlank(html))
            return null;
        Document document = Jsoup.parse(html);

        String noticeOrgan = null;
        try {
            Matcher matcher = fromPattern.matcher(document.text());
            while (matcher.find()) {
                noticeOrgan = matcher.group(0);
            }
            if (noticeOrgan != null) {
                noticeOrgan = noticeOrgan.replaceAll(regEx, "").trim();
            }
            if (noticeOrgan == null) {
                Matcher m = authorPattern.matcher(html);
                while (m.find()) {
                    noticeOrgan = m.group(0);
                }
                if (noticeOrgan != null) {
                    noticeOrgan = noticeOrgan.replace("var author = \"", "");
                    noticeOrgan = noticeOrgan.substring(0, noticeOrgan.length() - 2).trim();
                }
            }
            if (noticeOrgan != null) {
                Element js_name = document.getElementById("js_name");
                noticeOrgan = js_name.text().trim().replace(GeneralConstant.BLANK, "");
            }
        } catch (Throwable throwable) {
        }
        return noticeOrgan;
    }

}
