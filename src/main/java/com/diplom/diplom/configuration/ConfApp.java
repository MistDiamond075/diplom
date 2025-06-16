package com.diplom.diplom.configuration;

import com.diplom.diplom.configuration.filter.CookieFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@PropertySource("classpath:application.properties")
public class ConfApp {
    @Bean
    public FilterRegistrationBean<Filter> cookieFilterRegistration(CookieFilter cookieFilter) {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(cookieFilter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
