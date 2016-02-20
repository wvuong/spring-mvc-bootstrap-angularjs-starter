package com.willvuong.bootstrapper.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/23/13
 * Time: 5:04 PM
 */
public class ThreadLocalMemoryAppender extends AppenderBase<ILoggingEvent> {

    private String htmlBefore = "";
    private String htmlAfter = "";

    // for json output
    private boolean jsonOutput = false;

    @Override
    protected void append(ILoggingEvent eventObject) {
        ThreadLocalHolder.appendLoggedEvent(eventObject);
    }

    @Override
    public void start() {
        addInfo("htmlBefore=" + htmlBefore);
        addInfo("htmlAfter=" + htmlAfter);

        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    public void setJson(boolean jsonOutput) {
        this.jsonOutput = jsonOutput;
    }

    public String getHtmlBefore() {
        return this.htmlBefore;
    }

    public void setHtmlBefore(String htmlBefore) {
        this.htmlBefore = htmlBefore;
    }

    public String getHtmlAfter() {
        return htmlAfter;
    }

    public void setHtmlAfter(String htmlAfter) {
        this.htmlAfter = htmlAfter;
    }


    public static abstract class ThreadLocalHolder {
        private static ThreadLocal<List<ILoggingEvent>> threadLocal = new ThreadLocal<List<ILoggingEvent>>() {
            @Override
            protected List<ILoggingEvent> initialValue() {
                return Lists.newArrayList();
            }
        };

        private static ThreadLocal<ByteArrayOutputStream> output = new ThreadLocal<ByteArrayOutputStream>() {
            @Override
            protected ByteArrayOutputStream initialValue() {
                return new ByteArrayOutputStream(2048);
            }
        };

        public static List<ILoggingEvent> getLoggedEvents() {
            return threadLocal.get();
        }

        public static void appendLoggedEvent(ILoggingEvent event) {
            threadLocal.get().add(event);
        }

        public static void clearLoggedEvents() {
            threadLocal.get().clear();
        }

        public static String getBufferAsJson(String htmlBefore, String htmlAfter) {
            synchronized (threadLocal) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    ByteArrayOutputStream out = output.get();
                    out.reset();

                    if (htmlBefore != null) {
                        out.write(htmlBefore.getBytes());
                    }

                    out.write("<script type=\"text/javascript\">".getBytes());
                    out.write("var logged = ".getBytes());
                    mapper.writeValue(out, threadLocal.get());
                    out.write(";".getBytes());
                    out.write("</script>".getBytes());

                    if (htmlAfter != null) {
                        out.write(htmlAfter.getBytes());
                    }

                    return out.toString();
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
    }
}
