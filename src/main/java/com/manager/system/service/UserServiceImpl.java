package com.manager.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manager.entry.common.CommonException;
import com.manager.entry.common.UserUtil;
import com.manager.entry.system.*;
import com.manager.system.dao.UserMapper;
import com.manager.system.dao.UserProjectMapper;
import com.manager.system.dao.UserRoleMapper;
import com.manager.util.Delete;
import com.manager.util.Message;
import com.manager.util.Role;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.Subject;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * UserService实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public int updatePassword(String oldPassword, String newPassword) throws Exception {

        // 长度小于6
        if ("".equals(newPassword.trim()) || newPassword.length() < 6) {
            throw new CommonException(Message.PASSWORD_LENGTH_SHORT);
        }

        User user = (User) SecurityUtils.getSubject().getPrincipal();
        User userDetail = userMapper.selectByPrimaryKey(user.getSs01Id());

        // 原始密码不匹配
        if (!oldPassword.equals(userDetail.getPassword())) {
            throw new CommonException(Message.PASSWORD_ERROR);
        }

        User updateUser = new User();
        UserUtil.updateData(updateUser);
        updateUser.setSs01Id(user.getSs01Id());
        updateUser.setPassword(newPassword);

        return userMapper.updateByPrimaryKeySelective(updateUser);
    }

    @Autowired
    private UserProjectMapper userProjectMapper;

    @Override
    public int updateUser(UserManager userManager) throws Exception {

        String userUid = userManager.getSs01Id();
        UserManager beforeUpdateUser = userMapper.selectUserManagerDetail(userUid);

        // 当前登录用户
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        // 当前时间 修改时间
        Date now = new Date();

        int count = 0;

        // 判断姓名和登录账号是否相同
        if (!userManager.getUserName().equals(beforeUpdateUser.getUserName())) {
            User user = new User();
            user.setSs01Id(userUid);
            user.setUserName(userManager.getUserName());
            user.setUserId(userManager.getUserId());
            user.setUpdateUser(currentUser.getSs01Id());
            user.setUpdateTime(now);
            count = userMapper.updateByPrimaryKeySelective(user);
        }

        // 判断角色是否改变
        String userRoleId = userManager.getUserRoleId();
        String beforeUpdateRoleId = beforeUpdateUser.getUserRole().getUserRoleId();
        if (!userRoleId.equals(beforeUpdateRoleId)) {
            // 删除所有角色
            userRoleMapper.deleteByUserUid(userUid);

            // 新增
            UserRole userRole = new UserRole();
            userRole.setSs0101Id(UUID.randomUUID().toString());
            userRole.setSs01Id(userUid);
            userRole.setUserRoleId(userRoleId);
            UserUtil.insertData(userRole);
            count = userRoleMapper.insertSelective(userRole);

        }

        // 判断项目
        String currentUserRoleId = currentUser.getUserRole().getUserRoleId();
        // 如果是全域管理员（有改变权利）且项目编码改变
        if (currentUserRoleId.equals(Role.SYSTEM) && !userManager.getProjectNo().equals(beforeUpdateUser.getProjectNo())) {
            UserProject userProject = new UserProject();
            userProject.setSs01Id(userUid);
            userProject.setOpUnit(userManager.getOpUnit());
            String projectNo = UserUtil.getProjectNo(userManager.getProjectNo());
            userProject.setProjectNo(projectNo);
            userProject.setUpdateUser(currentUser.getSs01Id());
            userProject.setUpdateTime(now);
            count = userProjectMapper.updateByUserUid(userProject);
        }


        return count;
    }

    @Override
    public IPage<UserManager> selectUserManagerList(Page<UserManager> page, UserManagerQuery query) {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        UserProject userProject = user.getUserProject();
        return userMapper.selectUserManagerList(page, query, userProject == null ? null : userProject.getProjectNo());
    }

    @Override
    public User selectUserByUserId(String userId) {
        return userMapper.selectUserByUserId(userId);
    }

    @Override
    public int updateStatus(String userUid, String status) throws Exception {
        return userMapper.updateStatus(status, userUid);
    }

    @Override
    public UserManager selectUserManagerDetail(String userUid) throws Exception {
        return userMapper.selectUserManagerDetail(userUid);
    }

    @Override
    public int addUser(UserManager userManager) throws Exception {

        // 校验userUid 是否存在
        User checkUser = userMapper.selectUserByUserId(userManager.getUserId());
        if (checkUser != null) {
            // 存在
            throw new CommonException(Message.EXIST_USER);
        }

        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        Date now = new Date();
        String newUserUid = UUID.randomUUID().toString();
        User user = new User(
                newUserUid,
                userManager.getUserId(),
                userManager.getUserName(),
                "123456",
                User.ENABLE,
                currentUser.getSs01Id(),
                now,
                currentUser.getSs01Id(),
                now,
                Delete.UN_DELETE);

        // 新增用户表
        int count = userMapper.insertSelective(user);

        // 新增角色  如果是项目管理员不能选择全域
        String currentUserRoleId = ((User) SecurityUtils.getSubject().getPrincipal()).getUserRole().getUserRoleId();
        String userRoleId =  userManager.getUserRoleId();
        if (currentUserRoleId.equals(Role.PROJECT) && userRoleId.equals(Role.SYSTEM)) {
            throw new CommonException(Message.ROLE_ERROR);
        }

        UserRole userRole = new UserRole(
                UUID.randomUUID().toString(),
                newUserUid,
                userRoleId,
                currentUser.getSs01Id(),
                now,
                currentUser.getSs01Id(),
                now,
                Delete.UN_DELETE);
        // 新增用户角色表
        count = userRoleMapper.insertSelective(userRole);

        // 全域管理员不添加项目
        if (!userRoleId.equals(Role.SYSTEM)) {
            String projectNo = UserUtil.getProjectNo(userManager.getProjectNo());
            UserProject userProject = new UserProject(
                    UUID.randomUUID().toString(),
                    newUserUid,
                    projectNo,
                    userManager.getOpUnit(),
                    currentUser.getSs01Id(),
                    now,
                    currentUser.getSs01Id(),
                    now,
                    Delete.UN_DELETE);
            // 新增用户项目关联
            count = userProjectMapper.insertSelective(userProject);
        }

        return count;
    }
}
