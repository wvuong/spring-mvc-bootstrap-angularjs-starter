package com.willvuong.bootstrapper.mvcconfig;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlet.InstrumentedFilterContextListener;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.PingServlet;
import com.codahale.metrics.servlets.ThreadDumpServlet;
import com.opensymphony.sitemesh.webapp.SiteMeshFilter;
import com.willvuong.bootstrapper.filter.LogbackResponseServletFilter;
import com.willvuong.bootstrapper.filter.RequestMDCServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.util.EnumSet;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/25/13
 * Time: 9:13 AM
 */
public class ServletContextAutoConfigure implements WebApplicationInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ServletContextAutoConfigure.class);

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        // set up a metric registry instance just for jvm metrics
        MetricRegistry jvmMetrics = new MetricRegistry();
        jvmMetrics.register("memory", new MemoryUsageGaugeSet());
        jvmMetrics.register("gc", new GarbageCollectorMetricSet());
        jvmMetrics.register("threads", new ThreadStatesGaugeSet());
        jvmMetrics.register("fileDescriptors", new FileDescriptorRatioGauge());

        // handle listeners
        logger.info("autoconfiguring metrics servlet listener");
        servletContext.addListener(new MetricsServlet.ContextListener() {
            @Override
            protected MetricRegistry getMetricRegistry() {
                return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext).getBean(MetricRegistry.class);
            }
        });

        logger.info("autoconfiguring health check registry listener");
        servletContext.addListener(new HealthCheckServlet.ContextListener() {
            @Override
            protected HealthCheckRegistry getHealthCheckRegistry() {
                return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext).getBean(HealthCheckRegistry.class);
            }
        });

        logger.info("autoconfiguring instrumented filter listener");
        servletContext.addListener(new InstrumentedFilterContextListener() {
            @Override
            protected MetricRegistry getMetricRegistry() {
                return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext).getBean(MetricRegistry.class);
            }
        });

        // handle filters
        logger.info("autoconfiguring requestMDCServletFilter");
        addFilterIfDoesntExist(servletContext, "requestMDCServletFilter", RequestMDCServletFilter.class,
                null, true, "/*");

        logger.info("autoconfiguring metricFilter");
        addFilterIfDoesntExist(servletContext, "metricFilter", InstrumentedFilter.class,
                null, true, "/*");

        logger.info("autoconfiguring logbackResponseServletFilter");
        addFilterIfDoesntExist(servletContext, "logbackResponseServletFilter", LogbackResponseServletFilter.class,
                null, true, "/*");

        logger.info("autoconfiguring site mesh filter");
        addFilterIfDoesntExist(servletContext, "siteMeshFilter", SiteMeshFilter.class,
                null, true, "/*");

        // handle servlets
        logger.info("autoconfiguring metric servlet");
        addServletIfDoesntExist(servletContext, "metricsServlet", MetricsServlet.class, "/diagnostics/metrics");

        logger.info("autoconfiguring health check servlet");
        addServletIfDoesntExist(servletContext, "healthCheckServlet", HealthCheckServlet.class, "/diagnostics/health");

        logger.info("autoconfiguring ping servlet");
        addServletIfDoesntExist(servletContext, "pingServlet", PingServlet.class, "/diagnostics/ping");

        logger.info("autoconfiguring thread dump servlet");
        addServletIfDoesntExist(servletContext, "threadDumpServlet", ThreadDumpServlet.class, "/diagnostics/threads");

        logger.info("autoconfiguring jvm servlet");
        addServletIfDoesntExist(servletContext, "jvmServlet", new MetricsServlet(jvmMetrics), "/diagnostics/jvm");

        logger.info("all autoconfiguration injected into servlet context to be initialized when context starts.");
    }

    public void addFilterIfDoesntExist(final ServletContext context, final String name, final Class<? extends Filter> filterClass,
                                       final EnumSet<DispatcherType> dispatcherTypes, final boolean isMatchAfter,
                                       final String... urlPatterns) {

        FilterRegistration.Dynamic filter = context.addFilter(name, filterClass);
        // if filter already exists, addFilter() will return null
        if (filter != null) {
            filter.addMappingForUrlPatterns(dispatcherTypes, isMatchAfter, urlPatterns);
        }
    }

    public void addServletIfDoesntExist(final ServletContext context, final String name, final Class<? extends HttpServlet> servletClass,
                                       final String... urlPatterns) {

        ServletRegistration.Dynamic servlet = context.addServlet(name, servletClass);
        if (servlet != null) {
            servlet.addMapping(urlPatterns);
        }
    }

    public void addServletIfDoesntExist(final ServletContext context, final String name, final HttpServlet servlet,
                                        final String... urlPatterns) {

        ServletRegistration.Dynamic servletRegistration = context.addServlet(name, servlet);
        if (servletRegistration != null) {
            servletRegistration.addMapping(urlPatterns);
        }
    }
}
