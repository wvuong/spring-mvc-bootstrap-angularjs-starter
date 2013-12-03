package com.willvuong.bootstrapper.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/23/13
 * Time: 11:49 PM
 */
public class ThreadLocalMemoryAppenderTest {

    private Logger rootLogger = null;
    private LoggerContext loggerContext = null;

    @Before
    public void setup() {
        rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        loggerContext = rootLogger.getLoggerContext();
        // we are not interested in auto-configuration
        loggerContext.reset();
    }

    @Test
    public void testEventLoggingAndReset() {
        // set up
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%-5level [%thread]: %message%n");
        encoder.start();

        ThreadLocalMemoryAppender appender = new ThreadLocalMemoryAppender();
        appender.setContext(loggerContext);
        appender.start();
        rootLogger.addAppender(appender);

        // log
        rootLogger.debug("Message 1");
        rootLogger.warn("Message 2");

        // verify
        List<ILoggingEvent> buffer = ThreadLocalMemoryAppender.ThreadLocalHolder.getLoggedEvents();
        assertThat(buffer.size(), is(2));

        ThreadLocalMemoryAppender.ThreadLocalHolder.clearLoggedEvents();
        buffer = ThreadLocalMemoryAppender.ThreadLocalHolder.getLoggedEvents();
        assertThat(buffer.size(), is(0));
    }

    @Test
    public void testMultipleThreads() {
        // set up
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%-5level [%thread]: %message%n");
        encoder.start();

        ThreadLocalMemoryAppender appender = new ThreadLocalMemoryAppender();
        appender.setContext(loggerContext);
        appender.start();
        rootLogger.addAppender(appender);

        Thread thread1 = new Thread() {
            private Logger logger = (Logger) LoggerFactory.getLogger("thread1");

            @Override
            public void run() {
                logger.debug("Message 1");
                logger.warn("Message 2");

                // verify
                List<ILoggingEvent> buffer = ThreadLocalMemoryAppender.ThreadLocalHolder.getLoggedEvents();
                assertThat(buffer.size(), is(2));

                ThreadLocalMemoryAppender.ThreadLocalHolder.clearLoggedEvents();
                buffer = ThreadLocalMemoryAppender.ThreadLocalHolder.getLoggedEvents();
                assertThat(buffer.size(), is(0));
            }
        };

        Thread thread2 = new Thread() {
            private Logger logger = (Logger) LoggerFactory.getLogger("thread2");

            @Override
            public void run() {
                logger.debug("Message 1");
                logger.warn("Message 2");

                // verify
                List<ILoggingEvent> buffer = ThreadLocalMemoryAppender.ThreadLocalHolder.getLoggedEvents();
                assertThat(buffer.size(), is(2));

                ThreadLocalMemoryAppender.ThreadLocalHolder.clearLoggedEvents();
                buffer = ThreadLocalMemoryAppender.ThreadLocalHolder.getLoggedEvents();
                assertThat(buffer.size(), is(0));
            }
        };

        thread1.start();
        thread2.start();
    }

    @Test
    public void testDefaultEncoder() {
        // set up
        ThreadLocalMemoryAppender appender = new ThreadLocalMemoryAppender();
        appender.setContext(loggerContext);
        appender.start();
        rootLogger.addAppender(appender);

        // log
        Logger logger = (Logger) LoggerFactory.getLogger("test");
        logger.debug("Message 1");
        logger.warn("Message 2");

        String s = ThreadLocalMemoryAppender.ThreadLocalHolder.getBufferAsJson(null, null);
        System.out.println(s);
        /*
        assertThat(s, containsString("<html>"));
        assertThat(s, containsString("</html>"));
        assertThat(s, containsString("Message 1"));
        assertThat(s, containsString("Message 2"));
        */
    }
}
