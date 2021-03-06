package com.manager.system.controller;

import com.manager.entry.common.ResultEntry;
import com.manager.entry.system.User;
import com.manager.system.service.SystemService;
import com.manager.util.Message;
import com.manager.util.ResultUtil;
import com.manager.util.Role;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

/**
 * 描述
 *
 * @author nosign
 * @date 2019/08/16
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private SystemService systemService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResultEntry Login(@RequestParam @NotBlank(message = "用户名不能为空") String username,
                             @RequestParam @NotBlank(message = "密码不能为空") String password,
                             @RequestParam @NotBlank(message = "登录环境不能为空") String type)
    {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, type);
        // 登录
        subject.login(token);

        // 获取user对象
        User user = (User)SecurityUtils.getSubject().getPrincipal();
        // 过期时间
        SecurityUtils.getSubject().getSession().setTimeout(24 * 60 * 60 * 1000l);

        return ResultUtil.success(Message.LOGIN_SUCCESS, user);
    }

    /**
     * 上传文件
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @RequiresRoles(value = {Role.SYSTEM, Role.PROJECT}, logical = Logical.OR)
    public ResultEntry upload(@RequestParam("file")MultipartFile multipartFile,
                              @RequestParam(required = false, value = "code") String code) throws Exception{
        String url = systemService.upload(multipartFile, code);
        return ResultUtil.success(Message.INSERT_SUCCESS, url);
    }
}
