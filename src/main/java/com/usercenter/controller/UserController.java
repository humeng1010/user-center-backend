package com.usercenter.controller;

import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.constant.UserConstant;
import com.usercenter.entity.User;
import com.usercenter.entity.request.UserLoginRequest;
import com.usercenter.entity.request.UserRegisterRequest;
import com.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

import static com.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * 用户接口
 *
 * @author humeng
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest httpServletRequest;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (Objects.isNull(userRegisterRequest))
            return BaseResponse.error(ErrorCode.NULL_ERROR);

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword))
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);

        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        if (Objects.isNull(userLoginRequest))
            return BaseResponse.error(ErrorCode.NULL_ERROR);

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword))
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);

        return userService.doLogin(userAccount, userPassword);
    }

    @PostMapping("/out-login")
    public BaseResponse<String> userOutLogin() {
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATUS);
        return BaseResponse.ok("ok");
    }

    /**
     * 获取当前登陆的用户
     *
     * @return 登陆的用户
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentLoginUser() {
        User sessionCacheUser = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATUS);
        if (Objects.isNull(sessionCacheUser)) return null;
        // 防止数据库中的用户信息改变了,session缓存中的用户信息没有改变,造成的数据缓存不一致
        User databaseUser = userService.getById(sessionCacheUser.getId());
        User safetyUser = userService.getSafetyUser(databaseUser);
        return BaseResponse.ok(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(@RequestParam(value = "username", required = false) String username) {
        if (!isAdmin()) {
            return BaseResponse.error(ErrorCode.NO_AUTH_ERROR);
        }

        return userService.searchUsers(username);
    }


    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteUserById(@PathVariable("id") Long id) {
        if (!isAdmin()) {
            return BaseResponse.error(ErrorCode.NO_AUTH_ERROR);
        }
        if (id <= 0) return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        boolean remove = userService.removeById(id);
        if (!remove) {
            return BaseResponse.error(ErrorCode.SERVICE_ERROR);
        }
        return BaseResponse.ok("删除成功");
    }

    /**
     * 判断用户是否是管理员
     *
     * @return true: admin user; false: other user
     */
    private Boolean isAdmin() {
        User user = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATUS);
        return user != null && Objects.equals(user.getUserRole(), UserConstant.ADMIN_ROLE);
    }


}
