package com.lsy.test;

import cn.hutool.core.io.resource.ResourceUtil;
import com.lsy.util.JsoupUtil;
import org.junit.Test;

public class HtmlToText {

    /**
     * 将html转为text
     */
    @Test
    public void test(){
        String html = ResourceUtil.readUtf8Str("test.html");
        String mainText = JsoupUtil.htmltoText(html, JsoupUtil.iCourtWhiteList());
        mainText = JsoupUtil.strFormat(mainText).replaceAll("( | )", "");
        System.out.println(mainText);
    }
}
