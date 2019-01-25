package com.llsydn.wechat.entity;

import lombok.Data;

import java.util.Map;

/**
 * @author lilinshen
 * @title 交易提醒模板
 * @description 请填写相关描述
 * @date 2019/1/25 10:50
 */
@Data
public class TradeTemplate {
    private Map<String, String> user;
    private Map<String, String> number;
    private Map<String, String> date;
    private Map<String, String> amountText;
    private Map<String, String> money;
}
