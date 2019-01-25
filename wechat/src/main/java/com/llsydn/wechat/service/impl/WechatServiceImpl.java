package com.llsydn.wechat.service.impl;

import com.llsydn.wechat.entity.TradeTemplate;
import com.llsydn.wechat.service.WechatService;
import com.soecode.wxtools.api.IService;
import com.soecode.wxtools.api.WxConsts;
import com.soecode.wxtools.api.WxService;
import com.soecode.wxtools.bean.*;
import com.soecode.wxtools.bean.result.SenderResult;
import com.soecode.wxtools.bean.result.TemplateSenderResult;
import com.soecode.wxtools.bean.result.WxMediaUploadResult;
import com.soecode.wxtools.exception.WxErrorException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lilinshen
 * @title 请填写标题
 * @description 请填写相关描述
 * @date 2019/1/23 11:04
 */
@Service
public class WechatServiceImpl implements WechatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WechatServiceImpl.class);
    private IService iService = new WxService();
    @Value("${spring.profiles.active}")
    private String active;

    /**
     * 群发图文信息
     *
     * @return
     */
    @Override
    public boolean sendMessageToAll() {
        boolean flag = false;
        try {
            List<WxNewsInfo> wxNewsInfoList = new ArrayList<>();
            String resoucePath = ResourceUtils.getURL("classpath:").getPath() + "static/";

            // 1.上传封面图片
            WxNewsInfo wxNewsInfo = new WxNewsInfo();
            try {
                File file = new File(resoucePath + "qunianxiatian.jpg");
                WxMediaUploadResult result = iService.uploadMedia(WxConsts.MEDIA_IMAGE, file, null);
                if (!StringUtils.isEmpty(result.getMedia_id())) {
                    wxNewsInfo.setThumb_media_id(result.getMedia_id());
                }
                System.out.println(result.getMedia_id());
            } catch (WxErrorException e) {
                LOGGER.info("----------上传封面发生错误:{}----------" + e.getMessage());
            }
            // 2.封装其他信息
            wxNewsInfo.setAuthor("llsydn");
            // wxNewsInfo.setDigest("为深入学习习近平总书记在庆祝改革开放40周年大会上的重要讲话...");//图文消息的描述，如本字段为空，则默认抓取正文前64个字
            wxNewsInfo.setTitle("怎样解读《去年夏天》这首歌？");//图文消息的标题
            wxNewsInfo.setContent_source_url("jingyan.baidu.com/article/9c69d48fda35da13c9024e23.html");//在图文消息页面点击“阅读原文”后的页面
            wxNewsInfo.setShow_cover_pic(0); //是否显示封面，1为显示，0为不显示
            wxNewsInfo.setNeed_open_comment(1); //Uint32 是否打开评论，0不打开，1打开
            wxNewsInfo.setOnly_fans_can_comment(1); //Uint32 是否粉丝才可评论，0所有人可评论，1粉丝才可评论

            String content = readFileToString(resoucePath + "qunianxiatian.html");
            content = handStringImg(content);
            wxNewsInfo.setContent(content);//图文消息页面的内容，支持HTML标签
            wxNewsInfoList.add(wxNewsInfo);

            // 3.上传群发图文素材
            String mediaId = iService.addNewsMedia(wxNewsInfoList);
            // String mediaId = "rD00SW7wAfsG1KoJxcbEs81Rd7yiJfDAYtHT3wSHdxo"; // （中山）
            // String mediaId = "Aq5btHcFc2cKaC_gvFGfW48pGEHOrv9P7bSrKaAEe0E"; // （去年夏天）
            LOGGER.info("----------上传图文素材的mediaId:{}----------" + mediaId);

            // 4.群发图文信息（测试账号只能群发预览的图文信息，可以群发文本信息）
            // 群发文本内容
            if (!"dev".equals(active)) {
                WxTagSender wxTagSender = new WxTagSender();
                SenderFilter filter = new SenderFilter();
                filter.setIs_to_all(true); //用于设定是否向全部用户发送，值为true或false，选择true该消息群发给所有用户，选择false可根据tag_id发送给指定群组的用户
                filter.setTag_id(1); //群发到的标签的tag_id，参见用户管理中用户分组接口，若is_to_all值为true，可不填写tag_id
                wxTagSender.setFilter(filter);
                wxTagSender.setMsgtype("text");
                wxTagSender.setText(new SenderContent.Text("xxx是小傻逼"));
                try {
                    SenderResult result = iService.sendAllByTag(wxTagSender);
                    System.out.println(result.toString());
                } catch (WxErrorException e) {
                    e.printStackTrace();
                }
            }

            // 群发预览图文信息
            PreviewSender previewSender = new PreviewSender();
            // TODO 需要修改为自己的openid
            previewSender.setTouser("owuCb1byYreqo9BEUwgVgJ1_0EPI");
            // previewSender.setTouser("op5v856uhhacVQiDAkRrr1PQTE-k");
            SenderContent.Media media = new SenderContent.Media();
            media.setMedia_id(mediaId);
            previewSender.setMpnews(media);
            previewSender.setMsgtype("mpnews");
            try {
                SenderResult result = iService.sendAllPreview(previewSender);
                System.out.println(result.toString());
            } catch (WxErrorException e) {
                e.printStackTrace();
            }

            // 群发图文信息（测试账号没有权限）
            if (!"dev".equals(active)) {
                WxTagSender wxTagSender1 = new WxTagSender();
                SenderFilter filter1 = new SenderFilter();
                filter1.setIs_to_all(true);
                filter1.setTag_id(1);
                wxTagSender1.setFilter(filter1);
                wxTagSender1.setMsgtype("mpnews");
                media = new SenderContent.Media();
                media.setMedia_id(mediaId);
                wxTagSender1.setMpnews(media);
                try {
                    SenderResult result = iService.sendAllByTag(wxTagSender1);
                    System.out.println(result.toString());
                } catch (WxErrorException e) {
                    e.printStackTrace();
                }
            }

            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 发送模板信息
     *
     * @return
     */
    @Override
    public boolean sendTemplate() {
        // 测试数据
        TradeTemplate tradeTemplate = new TradeTemplate();
        Map<String, String> tempData = new HashMap<>();
        tempData.put("value", "杨先生");
        tempData.put("color", "#173177");
        tradeTemplate.setUser(tempData);

        tempData = new HashMap<>();
        tempData.put("value", "0426");
        tempData.put("color", "#173177");
        tradeTemplate.setNumber(tempData);

        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日 HH时mm分");
        String dateString = formatter.format(currentTime);
        tempData = new HashMap<>();
        tempData.put("value", dateString);
        tempData.put("color", "#173177");
        tradeTemplate.setDate(tempData);

        tempData = new HashMap<>();
        tempData.put("value", "人民币250.00元");
        tempData.put("color", "#173177");
        tradeTemplate.setAmountText(tempData);

        tempData = new HashMap<>();
        tempData.put("value", "190428.00");
        tempData.put("color", "#173177");
        tradeTemplate.setMoney(tempData);

        TemplateSender sender = new TemplateSender();
        // TODO 需要修改为自己的openid
        sender.setTouser("owuCb1byYreqo9BEUwgVgJ1_0EPI");
        // sender.setTouser("owuCb1fE2hF6SfCY5wJCWvUNwJlo");
        // TODO 需要修改为自己的TemplateId
        sender.setTemplate_id("-ogvOODkWu8TXzj9HuTN6Vu_ySQaTuIHop3U-bvALvQ");
        sender.setData(tradeTemplate);
        sender.setUrl("www.taobao.com");
        try {
            TemplateSenderResult result = iService.templateSend(sender);
            System.out.println(result.toString());
        } catch (WxErrorException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 读取文件并转换位字符串
     *
     * @param filepath
     * @return
     */
    private String readFileToString(String filepath) {
        String result = null;
        try {
            StringBuffer stringBuffer = new StringBuffer();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath));
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) {
                stringBuffer.append(temp.trim());
            }
            result = stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    /**
     * 处理字符串中img标签数据
     *
     * @param content
     * @return
     */
    private String handStringImg(String content) {
        Element doc = Jsoup.parseBodyFragment(content).body();
        Elements pngs = doc.select("img");
        for (Element element : pngs) {
            String imgUrl = element.attr("src");
            // 下载资源到本地
            String filepath = downloadPicture(imgUrl);
            if (!StringUtils.isEmpty(filepath)) {
                try {
                    // 上传图片变成腾讯域名下的图片
                    WxMediaUploadResult result = iService.imageDomainChange(new File(filepath));
                    System.out.println(result.getUrl());
                    imgUrl = result.getUrl();
                    // 删除文件
                    deleteFile(filepath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                element.attr("src", imgUrl);
            }
        }
        return doc.toString();
    }

    /**
     * 下载网络图片到本地
     *
     * @param urlList
     * @return
     */
    private String downloadPicture(String urlList) {
        String filepath = null;
        URL url = null;
        try {
            url = new URL(urlList);
            String resoucePath = ResourceUtils.getURL("classpath:").getPath() + "static/";
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            String imageName = resoucePath + "image/" + System.currentTimeMillis() + ".jpg";
            File file = new File(resoucePath + "image");
            if (!file.exists()) {
                file.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
            filepath = imageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filepath;
    }

    /**
     * 删除文件
     *
     * @param fileName
     * @return
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
