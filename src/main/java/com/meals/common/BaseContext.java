package com.meals.common;

/**
 * LoginCheckfilterd的doFilter方法
 * EmployeeController的update方法
 * MyMetaObjectHandel的updateFill方法
 * 上面三个方法在执行更新时处于同一个线程
 * 所以此类作用范围是同一个线程内
 * 基于ThreadLocal封装类，用于保存setCurrentId和获取getCurrentId当前登录用户的id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();//因为id是Long型，所以泛型也是Long

    /**
     * 设置id值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取id值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
