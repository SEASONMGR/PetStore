package org.example.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户（表 account）。admin=true 表示管理员，用于后台管理入口判定。
 */
@Data
@TableName("account")
public class Account {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private String address;

    private String city;

    private String state;

    private String zip;

    private String country;

    /** ACTIVE / DISABLED */
    private String status;

    /** 是否管理员 */
    private Boolean admin;

    private LocalDateTime createdAt;
}
