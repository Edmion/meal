package com.meals.filter;

import com.alibaba.fastjson.JSON;
import com.meals.common.BaseContext;
import com.meals.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckSerlvet",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;


        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("本次请求的路径是{}",requestURI);
        //2.判断本次请求是否需要处理
        //定义不需要处理的路径
        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                //对用户登陆操作放行
                "/user/login",
                "/user/sendMsg",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };
        boolean check = check(uris, requestURI);
        //3.如果不需要处理，直接放行
        if (check) {
            log.info("路径{}不需要拦截",requestURI);
            filterChain.doFilter(request,response);//chain.doFilter将请求转发给过滤器链下一个filter , 如果没有filter那就是你请求的资源
            return;
        }
        /**
         *  filterChain.doFilter：放行
         * 过滤器的作用就是之一就是在用户的请求到达servlet之前，拦截下来做预处理，处理之后便执行chain.doFilter(request, response)这个方法，
         * 如果还有别的过滤器，那么将处理好的请求传给下个过滤器，依此类推，当所有的过滤器都把这个请求处理好了之后，再将处理完的请求发给servlet；
         * 如果就这一个过滤器，那么就将处理好的请求直接发给servlet。
         */
        //4-1.判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，不需要拦截");
            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }
        //4-2.移动端判断用户是否登录
        if (request.getSession().getAttribute("user") != null){
            log.info("用户已登录，不需要拦截");
            Long id = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }
        //5.如果未登录则返回登录结果,通过输出流方式向客户端页面响应数据（先写到一个位置，再响应到前端）
        log.info("用户未登录，跳转到登录页面");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
        //因为writ()方法是需要一个字符串，所以需要将java对象转为Json字符串响应给客户端
        //这里前端的要求就是要后端给个信息，前端接受到这个信息来进行页面的跳转
    }


    /**
     * 将请求路径与不需要处理的路径进行匹配
     * @param uris
     * @param requestURI
     * @return
     */
    public boolean check(String[] uris,String requestURI){
        for (String uri : uris) {
            boolean match = PATH_MATCHER.match(uri, requestURI);//和不需要的拦截请求匹配上了，表示不需要拦截，返回true
            if (match) {
                return true;
            }

        }
        return false;//和不需要的拦截请求没有匹配上，表示需要拦截，返回flase
    }
     //log.info("拦截到的请求:{}",request.getRequestURI());//{}表示一个占位符，是新的拼接语法，代替+; /employee/logout
    //request.getRequestURL();http://localhost:8080/employee/logout
}
