package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @author: xiepanpan
 * @Date: 2020/4/15
 * @Description:
 */
public enum  LearningCode implements ResultCode {
    LEARNING_GETMEDIA_ERROR(false,23001,"获取学习地址失败！");

    boolean success;
    int code;
    String message;

    LearningCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    @Override
    public boolean success() {
        return false;
    }

    @Override
    public int code() {
        return 0;
    }

    @Override
    public String message() {
        return null;
    }
}
