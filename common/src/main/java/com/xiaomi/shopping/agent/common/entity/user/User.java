package com.xiaomi.shopping.agent.common.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户表（t_user）。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends LogicDeleteEntity {

    /** 用户编码（业务唯一） */
    private String userCode;

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 角色：admin/user */
    private String role;

    /** 头像 URL */
    private String avatar;
}
