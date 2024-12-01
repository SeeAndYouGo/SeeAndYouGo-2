package com.SeeAndYouGo.SeeAndYouGo.filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class CacheHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final ByteArrayInputStream inputStream;
    private final ServletInputStream servletInputStream;

    public CacheHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 요청 본문을 바이트 배열로 읽어들임
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = request.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        byte[] requestData = baos.toByteArray();
        inputStream = new ByteArrayInputStream(requestData);

        // ServletInputStream을 구현
        servletInputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0; // 더 이상 읽을 데이터가 없으면 true
            }

            @Override
            public boolean isReady() {
                return true; // 항상 준비 상태로 설정
            }

            @Override
            public void setReadListener(ReadListener listener) {
                // 비동기 처리를 위한 메서드, 필요에 따라 구현
            }
        };
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // ServletInputStream을 반환
        return servletInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // BufferedReader를 반환하여 요청 본문을 읽을 수 있게 함
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
