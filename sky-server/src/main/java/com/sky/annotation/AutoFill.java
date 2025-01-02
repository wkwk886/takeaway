package com.sky.annotation;
/*
自定义注解，用于进行公共字段的处理
 */

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//加在方法上面
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //指定操作数据库的函数的类型,在代码OperationType里已经写好了
    OperationType value();
}
