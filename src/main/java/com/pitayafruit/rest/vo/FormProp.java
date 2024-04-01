package com.pitayafruit.rest.vo;

import java.io.Serializable;
import lombok.Data;

/**
 * FormProps 表单属性.
 */
@Data
public class FormProp implements Serializable {

    /**
     * id.
     */
    private String id;

    /**
     * 属性名称.
     */
    private String name;

    /**
     * 属性变量.
     */
    private String variable;

    /**
     * 变量类型.
     */
    private String type;

    /**
     * 值.
     */
    private String value;

    /**
     * 表单配置.
     */
    private FormConfig config;

    public FormConfig getConfig() {
        return config;
    }
}
