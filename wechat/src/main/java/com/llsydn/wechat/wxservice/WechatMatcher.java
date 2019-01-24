package com.llsydn.wechat.wxservice;

import com.soecode.wxtools.api.WxMessageMatcher;
import com.soecode.wxtools.bean.WxXmlMessage;
import com.soecode.wxtools.util.StringUtils;

/**
 * @author lilinshen
 * @title 请填写标题
 * @description 请填写相关描述
 * @date 2019/1/23 14:33
 */
public class WechatMatcher implements WxMessageMatcher {
    @Override
    public boolean match(WxXmlMessage message) {
        if(StringUtils.isNotEmpty(message.getContent())){
            if(message.getContent().equals("你好")){
                return true;
            }
        }
        return false;
    }
}
