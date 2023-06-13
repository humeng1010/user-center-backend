package com.usercenter.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.entity.User;
import com.usercenter.mapper.UserMapper;
import com.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.usercenter.common.ErrorCode.*;
import static com.usercenter.constant.UserConstant.SALT;
import static com.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

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


    @Resource
    private HttpServletRequest httpServletRequest;


    @Override
    public BaseResponse<Long> userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }
        // 1.1密码长度8-18位
        if (userPassword.length() < 8
                || checkPassword.length() < 8
                || userPassword.length() > 18
                || checkPassword.length() > 18) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }

        // 3.账户
        // 匹配特殊字符一次或多次
        if (userAccount.matches("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+")) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }

        // 4.密码
        if (!Objects.equals(userPassword, checkPassword)) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }
        // TODO 方便测试环境
        // 4.2 密码必须包含至少一个数字、一个小写字母、一个大写字母和一个特殊字符,密码长度必须在 8 到 18 个字符之间。
        // if (!userPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()-_+=\\\\[\\\\]\\\\{}|;:',.<>/?]).{8,18}$")) {
        //     return BaseResponse.error(50006, "密码过于简单");
        // }

        // 2.账户不能重复
        // 2.1查数据库
        Long count = this.query().eq("userAccount", userAccount).count();
        if (count > 0) {
            // 被注册
            return BaseResponse.error(ACCOUNT_REPEAT);
        }

        // 对密码进行加盐加密
        String encryptPassword = SaSecureUtil.md5(SALT + userPassword);

        // 插入数据
        User user = new User();
        user.setUsername("momo_" + IdUtil.nanoId(5));
        user.setAvatarUrl("https://img.alicdn.com/bao/uploaded/i1/232692832/O1CN01XERLVq1Wn6Sq5ufB4_!!232692832.jpg_400x400q90");
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        this.save(user);
        return BaseResponse.ok(user.getId());
    }

    @Override
    public BaseResponse<User> doLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return BaseResponse.error(PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            return null;
        }
        // 1.1密码长度8-18位
        if (userPassword.length() < 8 || userPassword.length() > 18) {
            return BaseResponse.error(PARAMS_ERROR);
        }

        // 3.账户
        // 匹配特殊字符一次或多次
        if (userAccount.matches("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+")) {
            return BaseResponse.error(PARAMS_ERROR);
        }

        // TODO 方便测试环境
        // 4.2 密码必须包含至少一个数字、一个小写字母、一个大写字母和一个特殊字符,密码长度必须在 8 到 18 个字符之间。
        // if (!userPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()-_+=\\\\[\\\\]\\\\{}|;:',.<>/?]).{8,18}$")) {
        //     return BaseResponse.error(50014, "密码过于简单");
        // }

        // 对密码进行加盐加密
        String encryptPassword = SaSecureUtil.md5(SALT + userPassword);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User databaseUser = this.getOne(queryWrapper);

        if (Objects.isNull(databaseUser)) {
            // 用户不存在
            log.info("user login failed, userAccount can not match userPassword");
            return BaseResponse.error(LOGIN_ERROR);
        }

        // 用户脱敏
        User safetyUser = getSafetyUser(databaseUser);

        // 记录用户状态 session
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);

        return BaseResponse.ok(safetyUser, "登录成功");
    }

    @Override
    public User getSafetyUser(User loginUser) {
        User safetyUser = new User();
        BeanUtils.copyProperties(loginUser, safetyUser, "userPassword", "updateTime", "isDelete");
        return safetyUser;
    }

    @Override
    public BaseResponse<List<User>> searchUsers(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(username), User::getUsername, username);

        List<User> list = this.list(queryWrapper);
        List<User> users = list.stream().map(this::getSafetyUser).collect(Collectors.toList());

        return BaseResponse.ok(users);
    }
}




