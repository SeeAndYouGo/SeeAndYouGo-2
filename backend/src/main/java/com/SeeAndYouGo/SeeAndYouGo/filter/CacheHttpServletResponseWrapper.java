package com.SeeAndYouGo.SeeAndYouGo.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class CacheHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private ServletOutputStream servletOutputStream;
    private PrintWriter printWriter;

    private boolean isWriterUsed = false;
    private boolean isOutputStreamUsed = false;

    public CacheHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (isWriterUsed) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }
        isOutputStreamUsed = true;
        if (servletOutputStream == null) {
            servletOutputStream = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    byteArrayOutputStream.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener listener) {
                    // 필요 시 구현
                }
            };
        }
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (isOutputStreamUsed) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }
        isWriterUsed = true;
        if (printWriter == null) {
            printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, getCharacterEncoding()), true);
        }
        return printWriter;
    }

    public byte[] getResponseData() throws IOException {
        if (isWriterUsed) {
            printWriter.flush();
        } else if (isOutputStreamUsed) {
            servletOutputStream.flush();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public boolean isWriterUsed() {
        return isWriterUsed;
    }

    public boolean isOutputStreamUsed() {
        return isOutputStreamUsed;
    }
}
