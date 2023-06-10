package com.usercenter.service;

import com.usercenter.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@Slf4j
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testList() {
        List<User> users = userService.list();
        log.info("users: {}", users);
        Assertions.assertNotNull(users);
    }

    @Test
    public void testSave() {
        User user = new User();
        user.setUsername("test");
        user.setUserAccount("123");
        user.setAvatarUrl("http://wuluwulu.cn/upload/2023/04/IMG_9228.JPG");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean save = userService.save(user);
        Assertions.assertTrue(save);
        log.info("用户ID:{}", user.getId());
    }

    @Test
    void testUserRegister() {
        // 空值校验
        String userAccount = "";
        String userPassword = "";
        String checkPassword = "";
        Long res = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res);
        Assertions.assertEquals(-1L, res);

        // 用户名长度
        userAccount = "a";
        userPassword = "a";
        checkPassword = "a";
        Long res2 = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res2);
        Assertions.assertEquals(-2L, res2);

        // 密码长度校验
        userAccount = "tesst";
        userPassword = "111";
        checkPassword = "111";
        Long res3 = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res3);
        Assertions.assertEquals(-3L, res3);

        // 账户特殊字符
        userAccount = "*&{@*~~~````";
        userPassword = "111111111";
        checkPassword = "111111111";
        Long res4 = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res4);
        Assertions.assertEquals(-5L, res4);

        // 两次密码不一样
        userAccount = "tesstaaaaa";
        userPassword = "1111111111";
        checkPassword = "111111122222";
        Long res5 = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res5);
        Assertions.assertEquals(-6L, res5);

        // 密码简单
        userAccount = "tesstaaaaa";
        userPassword = "1111111111";
        checkPassword = "1111111111";
        Long res6 = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res6);
        Assertions.assertEquals(-7L, res6);

        // 账户相同
        userAccount = "xiaohuhuhu";
        userPassword = "Hu12345qwer...";
        checkPassword = "Hu12345qwer...";
        Long res7 = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res7);
        Assertions.assertEquals(-4L, res7);

        userAccount = "huhuhuhu";
        userPassword = "Hu12345qwer...";
        checkPassword = "Hu12345qwer...";
        Long res8 = userService.userRegister(userAccount, userPassword, checkPassword);
        System.out.println(res8);
        Assertions.assertEquals(3L, res8);


    }
}