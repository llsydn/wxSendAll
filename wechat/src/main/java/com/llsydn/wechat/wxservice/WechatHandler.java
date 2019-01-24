package com.llsydn.wechat.wxservice;

import com.soecode.wxtools.api.IService;
import com.soecode.wxtools.api.WxMessageHandler;
import com.soecode.wxtools.bean.WxXmlMessage;
import com.soecode.wxtools.bean.WxXmlOutMessage;
import com.soecode.wxtools.exception.WxErrorException;

import java.util.Map;

/**
 * @author lilinshen
 * @title 请填写标题
 * @description 请填写相关描述
 * @date 2019/1/23 14:34
 */
public class WechatHandler implements WxMessageHandler {
    @Override
    public WxXmlOutMessage handle(WxXmlMessage wxMessage, Map<String, Object> context, IService iService)
            throws WxErrorException {
        //必须以build()作为结尾，否则不生效。
        WxXmlOutMessage xmlOutMsg = WxXmlOutMessage.TEXT().content("你好").toUser(wxMessage.getFromUserName()).fromUser(wxMessage.getToUserName()).build();
        return xmlOutMsg;
    }
}
