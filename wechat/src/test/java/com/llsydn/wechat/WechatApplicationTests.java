package com.llsydn.wechat;

import com.soecode.wxtools.api.IService;
import com.soecode.wxtools.api.WxService;
import com.soecode.wxtools.bean.WxQrcode;
import com.soecode.wxtools.bean.result.QrCodeResult;
import com.soecode.wxtools.exception.WxErrorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WechatApplicationTests {
    private IService iService = new WxService();

    @Test
    public void contextLoads() {

    }

    @Test
    public void testWxService() {
        WxQrcode code = new WxQrcode();
        code.setAction_name("QR_SCENE");
        code.setAction_info(new WxQrcode.WxQrActionInfo(new WxQrcode.WxQrActionInfo.WxScene("scene_id/str")));
        try {
            QrCodeResult result = iService.createQrCode(code);
            System.out.println(result.getUrl());
            File file = iService.downloadQrCode(new File("d://temp"), result.getTicket());
            System.out.println(file.getName());
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }

}

