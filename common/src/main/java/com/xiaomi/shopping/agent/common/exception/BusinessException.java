package com.xiaomi.shopping.agent.common.exception;

import lombok.Getter;

/**
 * 业务异常基类（全局统一异常）。
 * <p>
 * 子节点抛出业务异常，由主 Agent / 全局异常处理器统一捕获转译。
 *
 * @author liyunyi
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
