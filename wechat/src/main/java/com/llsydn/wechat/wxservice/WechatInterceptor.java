package com.llsydn.wechat.wxservice;

import com.soecode.wxtools.api.IService;
import com.soecode.wxtools.api.WxConsts;
import com.soecode.wxtools.api.WxMessageInterceptor;
import com.soecode.wxtools.bean.WxXmlMessage;
import com.soecode.wxtools.exception.WxErrorException;

import java.util.Map;

/**
 * @author lilinshen
 * @title 请填写标题
 * @description 请填写相关描述
 * @date 2019/1/23 14:37
 */
public class WechatInterceptor implements WxMessageInterceptor {
    @Override
    public boolean intercept(WxXmlMessage wxMessage, Map<String, Object> context, IService iService) {
        //拦截所有非TEXT类型的消息,true通行；false拦截
        if (WxConsts.XML_MSG_TEXT.equals(wxMessage.getMsgType())) {
            return true;
        }
        return false;
    }
}
