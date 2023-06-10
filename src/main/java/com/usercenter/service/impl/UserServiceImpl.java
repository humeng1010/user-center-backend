package com.usercenter.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usercenter.entity.User;
import com.usercenter.mapper.UserMapper;
import com.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 用户服务实现类
 *
 * @author humeng
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-06-10 13:51:14
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final String SALT = "salt";
    private static final String USER_LOGIN_STATUS = "userLoginStatus";

    @Resource
    private HttpServletRequest httpServletRequest;


    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return -1L;
        }
        if (userAccount.length() < 4) {
            return -2L;
        }
        // 1.1密码长度8-18位
        if (userPassword.length() < 8
                || checkPassword.length() < 8
                || userPassword.length() > 18
                || checkPassword.length() > 18) {
            return -3L;
        }

        // 3.账户
        // 匹配特殊字符一次或多次
        if (userAccount.matches("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+")) {
            return -5L;
        }

        // 4.密码
        if (!Objects.equals(userPassword, checkPassword)) {
            return -6L;
        }
        // 4.2 密码必须包含至少一个数字、一个小写字母、一个大写字母和一个特殊字符,密码长度必须在 8 到 18 个字符之间。
        if (!userPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()-_+=\\\\[\\\\]\\\\{}|;:',.<>/?]).{8,18}$")) {
            return -7L;
        }

        // 2.账户不能重复
        // 2.1查数据库
        Long count = this.query().eq("userAccount", userAccount).count();
        if (count > 0) {
            // 被注册
            return -4L;
        }

        // 对密码进行加盐加密
        String encryptPassword = SaSecureUtil.md5(SALT + userPassword);

        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        this.save(user);
        return user.getId();
    }

    @Override
    public User doLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        // 1.1密码长度8-18位
        if (userPassword.length() < 8 || userPassword.length() > 18) {
            return null;
        }

        // 3.账户
        // 匹配特殊字符一次或多次
        if (userAccount.matches("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+")) {
            return null;
        }

        // 4.2 密码必须包含至少一个数字、一个小写字母、一个大写字母和一个特殊字符,密码长度必须在 8 到 18 个字符之间。
        if (!userPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()-_+=\\\\[\\\\]\\\\{}|;:',.<>/?]).{8,18}$")) {
            return null;
        }

        // 对密码进行加盐加密
        String encryptPassword = SaSecureUtil.md5(SALT + userPassword);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User loginUser = this.getOne(queryWrapper);

        if (Objects.isNull(loginUser)) {
            // 用户不存在
            log.info("user login failed, userAccount can not match userPassword");
            return null;
        }

        // 用户脱敏
        User safetyUser = new User();
        BeanUtils.copyProperties(loginUser, safetyUser, "userPassword", "updateTime", "isDelete");

        // 记录用户状态 session
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);

        return safetyUser;
    }
}




