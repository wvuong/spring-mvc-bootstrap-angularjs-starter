package com.willvuong.bootstrapper.util;

import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/23/13
 * Time: 5:04 PM
 */
public class ThreadLocalMemoryAppender extends AppenderBase<ILoggingEvent> {

    private static ThreadLocal<List<ILoggingEvent>> threadLocal = new ThreadLocal<List<ILoggingEvent>>() {
        @Override
        protected List<ILoggingEvent> initialValue() {
            return Lists.newArrayList();
        }
    };

    private static Encoder<ILoggingEvent> encoder;


    @Override
    protected void append(ILoggingEvent eventObject) {
        threadLocal.get().add(eventObject);
    }

    @Override
    public void start() {
        if (encoder == null) {
            addInfo("encoder not configured, falling back to default HTMLLayout encoder");

            HTMLLayout layout = new HTMLLayout();
            layout.setContext(context);
            layout.start();

            LayoutWrappingEncoder<ILoggingEvent> lwe = new LayoutWrappingEncoder<>();
            lwe.setLayout(layout);
            lwe.setContext(context);
            this.encoder = lwe;
        }

        encoder.start();

        super.start();
    }

    @Override
    public void stop() {
        if (encoder != null) {
            try {
                encoder.close();
                encoder.stop();
            } catch (IOException e) {
                addError("while closing encoder", e);
            }
        }

        super.stop();
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> e) {
        encoder = e;
    }

    public static void resetBuffer() {
        threadLocal.get().clear();
    }

    public static List<ILoggingEvent> getBufferAsList() {
        if (encoder == null) {
            System.err.println("Trying to read ThreadLocalMemoryAppender event buffer without initializing.  Please check Logback config.");
            return null;
        }

        synchronized (threadLocal) {
            return Collections.unmodifiableList(threadLocal.get());
        }
    }

    public static String getBufferAsEncodedString() {
        if (encoder == null) {
            System.err.println("Trying to read ThreadLocalMemoryAppender event buffer without initializing.  Please check Logback config.");
            return null;
        }

        synchronized (threadLocal) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<ILoggingEvent> list = threadLocal.get();

            try {
                encoder.init(out);
                for (ILoggingEvent e : list) {
                    encoder.doEncode(e);
                }
                encoder.close();
            }
            catch (IOException ex) {
                throw Throwables.propagate(ex);
            }

            return out.toString();
        }
    }
}
