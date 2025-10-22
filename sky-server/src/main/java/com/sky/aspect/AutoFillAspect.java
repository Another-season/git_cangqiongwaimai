package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Component
@Aspect
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始使用AOP自动填充公共字段");

        //获取当前切入点的数据库操作类型，通过连接点Joinpoint获取类签名，签名获取字节码文件，字节码文件存有类的所有信息获取注解
        //signature是父类，用于获取类的各类信息，MethodSignature是其子类，用于获取方法上的各类信息，子类中有获得方法注解的独有方法
        //因为需要调用子类独有的获取注解的方法因此将父类转为子类来调用子类独有的方法
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取需要增强的employee对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            log.info("当前参数为空，无需自动填充公共字段");
            return;
        }

        Object entity = args[0];

        //准备增强需要的数据
        LocalDateTime now = LocalDateTime.now();
        long currentId = BaseContext.getCurrentId();


        //根据数据库操作类型是Insert还是Update来动态添加符合的公共字段
        if(operationType == OperationType.INSERT) {
            //考虑开发中的参数多样性，因此用Object来接受参数，用这个对象获取他的CALSS字节码，在获取其get,set方法来动态赋值
            try {
                Method setCreateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTimeMethod.invoke(entity, now);
                setUpdateTimeMethod.invoke(entity, now);
                setCreateUserMethod.invoke(entity, currentId);
                setUpdateUserMethod.invoke(entity, currentId);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if(operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTimeMethod.invoke(entity, now);
                setUpdateUserMethod.invoke(entity, currentId);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

}
