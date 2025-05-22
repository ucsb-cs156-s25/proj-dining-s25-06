package edu.ucsb.cs156.dining.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class RoleInterceptorConfig implements WebMvcConfigurer {
    @Autowired
    RoleInterceptor roleAdminModeratorInterceptor;

    @Override //addinterctptors class runs on every request and checks the current user's roles in the data base 
    public void addInterceptors(InterceptorRegistry registry) { //runs on every request
        registry.addInterceptor(roleAdminModeratorInterceptor);
    }
}