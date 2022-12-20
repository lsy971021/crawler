package com.lsy.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * 通用解析类
 */
public class CommonParseUtils {


    /**
     * @param contentEl 包含正文的element
     * @param url 详情URL
     * @return 解析后的正文
     */
    public static String getContent(Element contentEl, String url) {
        if (contentEl == null)
            return null;
        //添加img标签
        Elements imgEleList = contentEl.select("img");
        if (!imgEleList.isEmpty()) {
            imgEleList.removeAttr("class");
            imgEleList.addClass("cDownFile");
        }
        //添加a标签
        Elements aEleList = contentEl.select("a");
        if (!aEleList.isEmpty()) {
            aEleList.removeAttr("class");
            aEleList.addClass("cDownFile");
        }
        Elements mainAttachList = contentEl.getElementsByClass("cDownFile");
        // 修改标签格式
        for (Element el : mainAttachList) {
            //src 一般是 img 标签的属性 href 一般是 a 标签的属性
            String key = "src";
            String value = el.attr(key);
            if (StringUtils.isBlank(value)) {
                key = "href";
                value = el.attr(key);
            }

            //如果两个都不存在url则移除
            if (StringUtils.isBlank(value) || StringUtils.containsAny(value, "data:image/png", ".html", "javascript:window", "javascript:void(0)", "javascript:window.print()", "javascript:", ":void(0)") || value.equals("#")) {
                el.remove();
                continue;
            }

            if (StringUtils.isNotBlank(value) && ((value.startsWith("/") || value.startsWith("./") || !value.contains("http")))) {
                if (value.startsWith("/")) {
                    String[] split = url.split("/");
                    StringBuilder stringBuilder = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        if (count == 3)
                            break;
                        if (s.isEmpty()) {
                            stringBuilder.append("/");
                            count = count + 1;
                            continue;
                        }
                        count = count + 1;
                        stringBuilder.append(s).append("/");
                    }
                    stringBuilder.append(value.substring(1));
                    value = stringBuilder.toString();
                } else if (value.startsWith("./")) {
                    int index = url.lastIndexOf("/");
                    String uri = url.substring(0, index);
                    value = uri + value.substring(1);
                }
            }

            List<Attribute> attributes = el.attributes().asList();
            final String attrName = key;
            try {
                attributes.stream().filter(attr -> attr.getKey().equals(attrName)).findFirst().get().setValue(value);
            } catch (Throwable ignore) {
            }
        }

        Whitelist whitelist = JsoupUtil.iCourtWhiteList();
        String content = JsoupUtil.htmltoText(contentEl.outerHtml(), whitelist);
        content = content.replace("《br》", "\n");
        return JsoupUtil.strFormat(content);
    }



    /**
     * 根据公众号contentElement 获取正文部分文本
     *
     * @param contentElement
     */
    public static String getContent(Element contentElement) {
        if (contentElement == null)
            return null;
        //添加img标签
        Elements aFileEleList = contentElement.select("img");
        if (!aFileEleList.isEmpty()) {
            aFileEleList.removeAttr("class");
            aFileEleList.addClass("cDownFile");
        }

        Elements mainAttachList = contentElement.getElementsByClass("cDownFile");
        // 修改图片标签格式
        for (Element element : mainAttachList) {
            String attachUrl = element.attr("data-src");
            element.attr("src", attachUrl);
        }

        Whitelist whitelist = JsoupUtil.iCourtWhiteList();
        String content = JsoupUtil.htmltoText(contentElement.outerHtml(), whitelist);
        content = content.replace("《br》", "\n");
        return JsoupUtil.strFormat(content);
    }
}
