package com.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.usercenter.entity.User;

/**
 * 用户服务
 *
 * @author humeng
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-06-10 13:51:14
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户ID
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 登陆
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 返回脱敏后的用户信息
     */
    User doLogin(String userAccount, String userPassword);
}
