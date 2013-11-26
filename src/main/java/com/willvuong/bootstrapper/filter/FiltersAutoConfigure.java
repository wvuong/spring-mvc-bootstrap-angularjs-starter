package com.willvuong.bootstrapper.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/25/13
 * Time: 9:13 AM
 */
public class FiltersAutoConfigure implements WebApplicationInitializer {

    private static final Logger logger = LoggerFactory.getLogger(FiltersAutoConfigure.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        logger.info("autoconfiguring requestMDCServletFilter");
        servletContext.addFilter("requestMDCServletFilter", RequestMDCServletFilter.class)
                .addMappingForUrlPatterns(null, true, "/*");

        logger.info("autoconfiguring logbackResponseServletFilter");
        servletContext.addFilter("logbackResponseServletFilter", LogbackResponseServletFilter.class)
                .addMappingForUrlPatterns(null, true, "/*");
    }
}
