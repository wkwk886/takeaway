package com.sky.aspect;

import ch.qos.logback.core.joran.spi.DefaultClass;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect//成为切面列
@Slf4j//log.in
@Component//作为bean,要放到容器里
public class AutoFillAspect {
    //定义切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")//在mapper下，且有annotation
    public void autoFillPointcut(){}

    // 前置通知 在通知中进行公共字段赋值
    @Before("autoFillPointcut()")
    //传入参数：连接点
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException {
        log.info("开始进行公共字段的自动填充");
        //获取当前被拦截的方法的数据库操作类型
        MethodSignature signature=(MethodSignature)joinPoint.getSignature();//要向下转型
        AutoFill autoFill=signature.getMethod().getAnnotation(AutoFill.class);//获得注解对象
        OperationType operationType=autoFill.value();

        //获取被拦截的方法的参数-实体对象
        Object[] args=joinPoint.getArgs();//会获得所有参数
        if (args==null || args.length==0){
            return;
        }

        Object entity=args[0];//接受第一位，是一个实体，例如employee

        //准备复制的数据：时间，id
        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();//localthread

        //根据不同的操作类型，用反射赋值
        if (operationType==OperationType.INSERT) {
            //四个公共字段都修改
            //todo 反射来赋值,获得set方法;还得再理解一下反射
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (operationType==OperationType.UPDATE){
            //两个公共字段
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
