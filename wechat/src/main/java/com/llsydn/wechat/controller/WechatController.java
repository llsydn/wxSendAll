package com.llsydn.wechat.controller;

import com.llsydn.wechat.service.WechatService;
import com.llsydn.wechat.wxservice.WechatHandler;
import com.llsydn.wechat.wxservice.WechatInterceptor;
import com.llsydn.wechat.wxservice.WechatMatcher;
import com.soecode.wxtools.api.IService;
import com.soecode.wxtools.api.WxConfig;
import com.soecode.wxtools.api.WxMessageRouter;
import com.soecode.wxtools.api.WxService;
import com.soecode.wxtools.bean.WxXmlMessage;
import com.soecode.wxtools.bean.WxXmlOutMessage;
import com.soecode.wxtools.util.xml.XStreamTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author lilinshen
 * @title 请填写标题
 * @description 请填写相关描述
 * @date 2019/1/23 11:00
 */
@RequestMapping(value = "/wx")
@Controller
public class WechatController {
    private IService iService = new WxService();

    @Autowired
    private WechatService wechatService;

    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request, String mediaId) {
        request.setAttribute("index", "index");
        return "index/index";
    }

    /**
     * 验证服务器地址是否有效
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @GetMapping
    @ResponseBody
    public String check(String signature, String timestamp, String nonce, String echostr) {
        if (iService.checkSignature(signature, timestamp, nonce, echostr)) {
            return echostr;
        }
        return null;
    }

    /**
     * 接收微信服务器发来的消息
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @PostMapping
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String timestamp = request.getParameter("timestamp");// 时间戳
        String nonce = request.getParameter("nonce");// 随机数

        // 创建一个路由器
        WxMessageRouter router = new WxMessageRouter(iService);
        // 获取encrypt_type 消息加解密方式标识
        String encrypt_type = request.getParameter("encrypt_type");
        try {
            if (!StringUtils.isEmpty(encrypt_type) && "aes".equals(encrypt_type)) {
                String msg_signature = request.getParameter("msg_signature");

                // 微信服务器推送过来的加密消息是XML格式。使用WxXmlMessage中的decryptMsg()解密得到明文。
                WxXmlMessage wx = WxXmlMessage.decryptMsg(request.getInputStream(), WxConfig.getInstance(), timestamp, nonce, msg_signature);
                System.out.println("消息：\n " + wx.toString());
                router.rule().matcher(new WechatMatcher()).interceptor(new WechatInterceptor()).handler(new WechatHandler()).end();
                WxXmlOutMessage xmlOutMsg = router.route(wx);
                if (xmlOutMsg != null) {
                    out.print(WxXmlOutMessage.encryptMsg(WxConfig.getInstance(), xmlOutMsg.toXml(), timestamp, nonce));// 返回给用户。
                }
            } else {
                // 微信服务器推送过来的是XML格式。
                WxXmlMessage wx = XStreamTransformer.fromXml(WxXmlMessage.class, request.getInputStream());
                System.out.println("消息：\n " + wx.toString());
                router.rule().matcher(new WechatMatcher()).interceptor(new WechatInterceptor()).handler(new WechatHandler()).end();
                // 把消息传递给路由器进行处理
                WxXmlOutMessage xmlOutMsg = router.route(wx);
                if (xmlOutMsg != null) {
                    // 因为是明文，所以不用加密，直接返回给用户
                    out.print(xmlOutMsg.toXml());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }


    /**
     * 群发图文信息
     *
     * @return
     */
    @RequestMapping(value = "/sendall")
    @ResponseBody
    public String sendMessageToAll() {
        wechatService.sendMessageToAll();
        return "sendall";
    }

}
