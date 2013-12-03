package com.willvuong.bootstrapper.filter;

import ch.qos.logback.classic.LoggerContext;
import com.willvuong.bootstrapper.util.ThreadLocalMemoryAppender;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: will
 * Date: 11/24/13
 * Time: 5:08 PM
 */
public class LogbackResponseServletFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LogbackResponseServletFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        boolean disabled = "true".equals(request.getParameter("disabled"));
        String url = request.getRequestURL().toString();
        boolean notHtml = url.endsWith(".js") || url.endsWith(".css");

        if (disabled || notHtml) {
            chain.doFilter(request, response);
            return;
        }

        CharResponseWrapper wrapper = new CharResponseWrapper(response);
        try {
            logger.trace("before doFilter()");
            chain.doFilter(request, wrapper);
        }
        finally {
            boolean doFilter = wrapper.getContentType() != null && wrapper.getContentType().contains("text/html");
            logger.debug("contentType: {}, filter this request? {}", wrapper.getContentType(), doFilter);

            if (!doFilter) {
                // do not modify response content
                return;
            }

            if (wrapper.toString() != null) {
                PrintWriter writer = response.getWriter();
                StringBuilder responseBody = new StringBuilder(wrapper.toString());
                logger.trace("responseBody before: {} bytes", responseBody.length());

                String encoded = ThreadLocalMemoryAppender.ThreadLocalHolder.getBufferAsJson(null, null);
                ThreadLocalMemoryAppender.ThreadLocalHolder.clearLoggedEvents();
                if (encoded != null) {
                    StringBuilder div = new StringBuilder(encoded);
                    logger.debug("html: {} bytes", div.length());

                    int pos = responseBody.lastIndexOf("</body>");
                    responseBody.insert(pos, div);
                    logger.trace("responseBody after: {} bytes", responseBody.length());
                    // logger.trace("{}", responseBody);
                }
                else {
                    logger.debug("no events to write");
                }

                response.setContentLength(responseBody.length());
                writer.write(responseBody.toString());
                writer.flush();

                logger.debug("response written: {} bytes", responseBody.length());
            }
        }

    }

    private class CharResponseWrapper extends HttpServletResponseWrapper {
        private CharArrayWriter output;
        private PrintWriter writer;

        private boolean getWriterCalled = false;
        private boolean getOutputStreamCalled = false;

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
            output = new CharArrayWriter();
        }

        public String toString() {
            if (writer != null) {
                return output.toString();
            }
            return null;
        }

        public PrintWriter getWriter() {
            if (writer != null) {
                return writer;
            }

            if (getOutputStreamCalled) {
                throw new IllegalStateException("getOutputStream() already called");
            }

            getWriterCalled = true;
            writer = new PrintWriter(output);
            return writer;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (getWriterCalled) {
                throw new IllegalStateException("getWriter() already called");
            }

            getOutputStreamCalled = true;
            return super.getOutputStream();
        }
    }
}
