package com.usercenter.entity.request;


import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author humeng
 */
@Data
public class UserRegisterRequest {

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
