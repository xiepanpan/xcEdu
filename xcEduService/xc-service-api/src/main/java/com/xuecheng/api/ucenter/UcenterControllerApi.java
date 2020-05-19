package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author: xiepanpan
 * @Date: 2020/4/17
 * @Description:
 */
@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {

    @ApiOperation("根据用户账号查询用户信息")
    XcUserExt getUserext(String username);
}
