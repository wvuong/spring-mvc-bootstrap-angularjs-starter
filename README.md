spring-mvc-bootstrap-angularjs-starter
======================================

The purpose of this is to serve as a project base that has the following things built in:
* Maven project structure and configuration
* Spring MVC and default mappings
* Bootstrap resources and starter page
* AngularJS resources and starter page
* Application defaults like logging, exception handling, and login.

So clone it and get started.

Main components
----------------------
* Build and tooling via [Maven 3.1+](http://maven.apache.org)
* Servlet 3.x and [Spring Framework 3.2.x](http://docs.spring.io/spring/docs/3.2.5.RELEASE/spring-framework-reference/htmlsingle/)
* [SiteMesh 2.4](http://wiki.sitemesh.org/display/sitemesh/Home) to decorate layouts
* [Bootstrap 3.x](http://getbootstrap.com/) styling and [AngularJS](http://angularjs.org/)
* Logging via [SLF4J](http://www.slf4j.org/) and [Logback](http://logback.qos.ch/)
* Devopsy monitoring things via [Metrics](http://metrics.codahale.com/)

How it is configured
----------------------
Maven is configured to compile for JDK7 so make sure that you have your `JAVA_HOME` configured correctly (you can check this also by running `mvn --version`).

Let's get this thing running:

```
mvn jetty:run
```

The root of all configuration starts in the `src/main/webapp/WEB-INF/web.xml` file.  The web.xml uses the `ContextLoaderListener` to initialize the Spring application context via `@Configuration` annotation scanning (the `contextClass` and `contextConfigLocation` context-params).  In this case, the web.xml's `contextConfigLocation` context-param directs the `ContextLoaderListener` to load the main/parent application context in the annotated class `com.willvuong.bootstrapper.config.AppConfiguration`. 

``` xml
<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>com.willvuong.bootstrapper.config.AppConfiguration</param-value>
</context-param>
```

The `AppConfiguration` class should use `@Import` and/or `@ComponentScan` annotations to import other `@Configuration` classes or annotated `@Component`-ish classes.

The Spring MVC context is configured via the `DispatcherServlet`'s `contextConfigLocation` init-param value:

``` xml
<servlet>
		<servlet-name>appServlet</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextClass</param-name>
            <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
        </init-param>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>com.willvuong.bootstrapper.mvcconfig.MvcConfiguration</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
		<async-supported>true</async-supported>
</servlet>
```

The `MvcConfiguration` class configures the Spring MVC framework with some "classic" defaults and already has a `@ComponentScan` annotation for mapping annotated `@Controller` classes in the `com.willvuong.bootstrapper.controllers` package.

Let's talk about the "classic" defaults:
* The `DispatcherServlet` is configured to map to all requests within the servlet context via `/*`.
* `MvcConfiguration` is configured to route all `/resources/**` requests to static resources located in `src/main/webapp/resources`.
* All unmatched requests are finally routed to the [default servlet handler](http://docs.spring.io/spring/docs/3.2.x/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurerAdapter.html#configureDefaultServletHandling(org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer)).

If you paid attention to the console output during `mvn jetty:run`, you might have seen a few lines that begin with "autoconfiguring blah blah...".  These log statements originate from the class  `com.willvuong.bootstrapper.mvcconfig.ServletContextAutoConfigure` which does some additional configuration of the servlet context.
* A utility filter `com.willvuong.bootstrapper.filter.RequestMDCServletFilter` is added to the servlet context.  Its main purpose is to set details from the current `HttpServletRequest` (request URL, request URI, and generate a unique logging ID for the scope of the current request) into the SLF4J MDC.
* Configure SiteMesh to decorate all responses except for static resources in `/resources/**`.  SiteMesh is configured by `src/main/webapp/WEB-INF/decorators.xml`.
* Configure Metrics instrumentation and reporting via Metrics servlets.

Another thing to pay attention to is the Logback configuration (via `src/main/resources/logback.xml`).
* Logback is started in debug mode and will refresh its configuration every 30 seconds.
* It will log to the console in color for readability.  (If you are running in a terminal emulator without color, you can disable colors like `mvn jetty:run -Dnocolor=true`.)
* Log messages are very detailed and include the current request URI and request ID.  This helps to map request handlers to log messages.

Open up a web browser and navigate to `localhost:8080` and click on the currently running servlet context (if you didn't change the pom.xml artifactId yet, it will be "spring-mvc-bootstrap-angularjs-starter").
* The web page you should see is mapped by controller `com.willvuong.bootstrapper.controllers.HomeController` which all it does is forward to `/WEB-INF/views/angular-index.jsp`.
* JSP `/WEB-INF/views/angular-index.jsp` is located at `src/main/webapp/WEB-INF/views/angular-index.jsp`.  It references JavaScript and Angular resources in `/resources/` (`src/main/webapp/resources`).
* The response is decorated by SiteMesh by `src/main/webapp/WEB-INF/decorators/default.jsp`.  This JSP decorator wraps angular-index.jsp in Bootstrap CSS styling in `/resources/` (`src/main/webapp/resources`).

Metrics reporting is exposed via:
* Application metrics: {contextpath}/diagnostics/metrics
* JVM metrics: {contextpath}/diagnostics/jvm
* Thread dump: {contextpath}/diagnostics/threads
* Health checks: {contextpath}/diagnostics/health
* Ping: {contextpath}/diagnostics/ping

To kill the Jetty server, use `Control-c`.

Hot development mode (aka class reloading)
----------------------
You could just reload the servlet context in the Jetty command line after every recompile by hitting enter (at least until you run out of heap space!) but that really sucks most times.  (Also a reminder that you don't have to reload the servlet context for JSP or static resource changes as they are LIVE since `mvn jetty:run` is serving directly out of `src/main/webapp`.)

If you have [JRebel](http://zeroturnaround.com/software/jrebel/), you should already be good to go via IDE plugin or [attaching the JRebel agent to the JVM](http://manuals.zeroturnaround.com/jrebel/standalone/launch-quick-start.html).

Otherwise, the [Spring Loaded](https://github.com/spring-projects/spring-loaded) project can serve as a poor man's JRebel in a pinch.  The Spring Loaded jar is located in the `tools` directory and all of the configuration is in the pom.xml (jetty-maven-plugin and the debug profile).

To start your app in Jetty with Spring Loaded: 
`mvn jetty:run-forked`

To start your app in Jetty with Spring Loaded and JDWP on port `5005`:
`mvn jetty:run-forked -P debug`

Packaging for deployment
----------------------
TODO
