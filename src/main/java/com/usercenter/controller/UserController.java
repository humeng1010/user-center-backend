package com.usercenter.controller;

import cn.dev33.satoken.util.SaResult;
import com.usercenter.constant.UserConstant;
import com.usercenter.entity.User;
import com.usercenter.entity.request.UserLoginRequest;
import com.usercenter.entity.request.UserRegisterRequest;
import com.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
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
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (Objects.isNull(userRegisterRequest)) return null;

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) return null;

        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        if (Objects.isNull(userLoginRequest)) return null;

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) return null;

        return userService.doLogin(userAccount, userPassword);
    }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam("username") String username) {
        if (!isAdmin()) {
            return Collections.emptyList();
        }
        return userService.searchUsers(username);
    }


    @DeleteMapping("/{id}")
    public SaResult deleteUserById(@PathVariable("id") Long id) {
        if (!isAdmin()) {
            return SaResult.error("对不起,您不是管理员,没有权限进行该操作");
        }
        if (id <= 0) return SaResult.error("id不合法");
        boolean remove = userService.removeById(id);
        if (!remove) {
            return SaResult.error("删除失败");
        }
        return SaResult.ok("删除成功");
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
