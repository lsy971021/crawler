package com.lsy.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsoupUtil {
    public static void extendFileAddClass(Element bodyEle, List<String> cssSelectorList) {
        //处理附件
        for (String cssItem : cssSelectorList) {
            Elements aFileEleList = bodyEle.select(cssItem);
            aFileEleList.removeAttr("class");
            aFileEleList.addClass("cDownFile");
        }
    }

    public static void extendFileAddClass(Elements bodyEle, List<String> cssSelectorList) {
        //处理附件
        for (String cssItem : cssSelectorList) {
            Elements aFileEleList = bodyEle.select(cssItem);
            aFileEleList.removeAttr("class");
            aFileEleList.addClass("cDownFile");
        }
    }

    static Pattern patternZeroRemove = Pattern.compile("<[\u4e00-\u9fa5]+[^>]*>");

    public static String encodeContent(String text) {
        //对"《最高人民法院关于适用<中华人民共和国民事诉讼法>的解释》"这种内容的<进行转意
        Matcher mZeroRemove = patternZeroRemove.matcher(text);
        while (mZeroRemove.find()) {
            String needRemove = mZeroRemove.group();
            String replacement = needRemove.replace("<", "&lt;").replace(">", "&gt;");
            text = text.replace(needRemove, replacement);
        }
        return text;
    }

    public static String decodeContent(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        text = text.replace("&lt;", "<").replace("&gt;", ">");
        return text;
    }

    /**
     * 去除标签
     * @param doc
     * @param whitelist
     * @return
     */
    public static String htmltoText(String doc, Whitelist whitelist) {
        doc = encodeContent(doc);
        StringBuilder textBuilder = new StringBuilder();
        String cleanStr = Jsoup.clean(doc, "", whitelist, new Document.OutputSettings().prettyPrint(false));
        traverse(Jsoup.parse(cleanStr), textBuilder, "", null);
        String rstText = textBuilder.toString();
        return decodeContent(rstText);
    }

    public static String htmltoText(String doc, String baseUri, Whitelist whitelist, List<String> attachHrefList) {
        doc = encodeContent(doc);
        StringBuilder textBuilder = new StringBuilder();
        String cleanStr = Jsoup.clean(doc, baseUri, whitelist, new Document.OutputSettings().prettyPrint(false));
        traverse(Jsoup.parse(cleanStr), textBuilder, baseUri, attachHrefList);
        String rstText = textBuilder.toString();
        return decodeContent(rstText);
    }

    public static void traverse(Node root, StringBuilder builder, String baseUri, List<String> attachHrefList) {
        Node node = root;
        int depth = 0;

        while (node != null) {
            node = head(node, depth, builder, baseUri, attachHrefList);
            /*if (node == null) {
                return;
            }*/
            if (node.childNodeSize() > 0) {
                node = node.childNode(0);
                depth++;
            } else {
                while (node.nextSibling() == null && depth > 0) {
                    tail(node, depth, builder);
                    node = node.parentNode();
                    depth--;
                }
                tail(node, depth, builder);
                if (node == root) {
                    break;
                }
                node = node.nextSibling();
            }
        }
    }

    private static Node head(Node node, int depth, StringBuilder builder, String baseUri, List<String> attachHrefList) {
        if ("table".equals(node.nodeName()) || "pre".equals(node.nodeName())) {
            for (String tagName : Arrays.asList("img", "a")) {
                Elements extendEles = ((Element) node).getElementsByTag(tagName);
                for (Element extItem : extendEles) {
                    String aClass = extItem.attr("class");
                    if (StringUtils.isNotEmpty(aClass) && aClass.contains("cDownFile")) {
                        extItem.attributes().forEach(attr -> {
                            if ("src".equals(attr.getKey()) || "href".equals(attr.getKey())) {
                                if (StringUtils.isNotEmpty(baseUri)) {
                                    attr.setValue(StringUtil.resolve(baseUri, attr.getValue().replace("\\", "/")));
                                } else {
                                    attr.setValue(attr.getValue().replace("\\", "/"));
                                }
                                if (attachHrefList != null) {
                                    attachHrefList.add(attr.getValue());
                                }
                            }
                        });
                    }
                }
            }
            builder.append(node.toString());
            if (CollectionUtils.isNotEmpty(node.childNodes())) {
                ((Element) node).empty();
            }
        } else if ("img".equals(node.nodeName()) || "a".equals(node.nodeName())) {
            String aClass = node.attr("class");
            if (StringUtils.isNotEmpty(aClass) && aClass.contains("cDownFile")) {
                node.attributes().forEach(attr -> {
                    if ("src".equals(attr.getKey()) || "href".equals(attr.getKey())) {
                        if (StringUtils.isNotEmpty(baseUri)) {
                            attr.setValue(StringUtil.resolve(baseUri, attr.getValue().replace("\\", "/")));
                        } else {
                            attr.setValue(attr.getValue().replace("\\", "/"));
                        }
                        if (attachHrefList != null) {
                            attachHrefList.add(attr.getValue());
                        }
                    }
                });
                builder.append(node.toString());
                if (CollectionUtils.isNotEmpty(node.childNodes())) {
                    ((Element) node).empty();
                }
            }
        } else {
            if (node.nodeName().equals("#text")) {
                builder.append(((TextNode) node).getWholeText());
            }
        }
        return node;
    }

    private static Node tail(Node node, int depth, StringBuilder builder) {
        if ("br".equals(node.nodeName()) || "p".equals(node.nodeName()) || "div".equals(node.nodeName()) || "tr".equals(node.nodeName())
                || "li".equals(node.nodeName()) || "ul".equals(node.nodeName())) {
            builder.append("\n");
        }
        return node;
    }

    public static Whitelist iCourtWhiteList() {
        return new Whitelist().addTags("p", "pre", "br", "table", "tr", "td", "div", "tbody", "tfoot", "thead", "a", "img")
                .addAttributes("img", "src", "class")
                .addAttributes("a", "href", "class")
                .addAttributes("td", "colspan", "rowspan", "height", "width");
    }

    public static Whitelist monitorWhiteList() {
        return new Whitelist().addTags("p", "br", "tr", "div", "ul", "li");
    }

    public static String strFormat(String sourceStr) {
        String textStr = sourceStr;
        if (textStr.contains("　")) {
            textStr = textStr.replace("　", " ");
        }
        if (textStr.contains("&nbsp;")) {
            textStr = textStr.replace("&nbsp;", " ");
        }
        //实现去掉换行周围不可见字符
        textStr = textStr.replaceAll("\\s*\\n\\s*", "\n");
        //去掉字符串开始和结尾位置不可见字符
        textStr = textStr.replaceAll("^\\s+|$\\s+", "");
        //去掉多重换行符
        textStr = textStr.replaceAll("\\n+", "\n");
        return textStr;
    }
}
