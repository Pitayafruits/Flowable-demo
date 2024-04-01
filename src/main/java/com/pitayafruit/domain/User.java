package com.pitayafruit.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;


@Data
@TableName("sys_user")
public class User implements Serializable {

    /**
     * 用户id.
     */
    private Long id;

    /**
     * 用户昵称.
     */
    private String nickName;

    /**
     * 职位
     */
    private String position;
}
