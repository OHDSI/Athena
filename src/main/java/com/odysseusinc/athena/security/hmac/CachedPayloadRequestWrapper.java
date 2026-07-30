package com.odysseusinc.athena.security.hmac;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;

public class CachedPayloadRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] payload;

    CachedPayloadRequestWrapper(HttpServletRequest request, byte[] payload) {
        super(request);
        this.payload = payload;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ByteArrayServletInputStream(payload);
    }

    private static class ByteArrayServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteStream;

        public ByteArrayServletInputStream(byte[] payload) {
            byteStream = new ByteArrayInputStream(payload);
        }

        @Override
        public int read() {
            return byteStream.read();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
        }
    }
}