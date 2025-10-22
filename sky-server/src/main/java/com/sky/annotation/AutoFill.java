package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//通过注解来实现AOP的切入点，在需要AOP增强的方法上加入该注解，来为每次update，insert的公共字段填写值，减少重复代码
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //声明属性，来指定数据库的操作类型，以便用在AOP增强时可以获得是对那种数据库属性的增强
    //未进行初始化以及default需要在使用注解时显示赋值
    OperationType value();
}
