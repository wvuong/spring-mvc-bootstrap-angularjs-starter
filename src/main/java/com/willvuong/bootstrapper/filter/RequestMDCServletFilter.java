package com.willvuong.bootstrapper.filter;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/25/13
 * Time: 8:46 AM
 */
public class RequestMDCServletFilter extends OncePerRequestFilter {
    public static final String REQUESTURL = "REQUESTURL";
    public static final String REQUESTURI = "REQUESTURI";
    public static final String REQUESTID = "REQUESTID";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MDC.put(REQUESTURL, request.getRequestURL().toString());
        MDC.put(REQUESTURI, request.getRequestURI());
        MDC.put(REQUESTID, RandomStringUtils.randomAlphanumeric(8));

        try {
            filterChain.doFilter(request, response);
        }
        finally {
            MDC.clear();
        }
    }
}
