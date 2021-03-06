package com.manager.system.service;

import com.manager.entry.system.UserRole;
import com.manager.system.dao.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserRoleServiceImpl implements UserRoleService{

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public UserRole selectUserRoleByUserUid(String userUid) {
        return userRoleMapper.selectUserRoleByUserUid(userUid);
    }
}
