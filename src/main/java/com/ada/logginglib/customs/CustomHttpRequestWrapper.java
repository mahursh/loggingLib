//package com.ada.logginglib.customs;
//
//import jakarta.servlet.ReadListener;
//import jakarta.servlet.ServletInputStream;
//import jakarta.servlet.ServletOutputStream;
//import jakarta.servlet.WriteListener;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletRequestWrapper;
//
//import java.io.*;
//
//
//public class CustomHttpRequestWrapper extends HttpServletRequestWrapper {
//
//    private final ByteArrayInputStream inputStream = new ByteArrayInputStream();
//    private final BufferedReader getReader = new BufferedReader();
//
//    CustomHttpRequestWrapper(HttpServletRequest request){
//        super(request);
//    }
//
//    @Override
//    public ServletInputStream getInputStream() throws IOException {
//        return new ServletInputStream() {
//            @Override
//            public int read() throws IOException {
//                return 0;
//            }
//
//            @Override
//            public int readLine(byte[] b, int off, int len) throws IOException {
//                return super.readLine(b, off, len);
//            }
//
//            @Override
//            public boolean isFinished() {
//                return false;
//            }
//
//            @Override
//            public boolean isReady() {
//                return false;
//            }
//
//
//            @Override
//            public void setReadListener(ReadListener readListener) {
//
//            }
//
//
//        };
//    }
//
//    @Override
//    public BufferedReader getReader() throws IOException {
//        return printWriter;
//    }
//
//    public byte[] getRequestData() throws IOException {
//        printWriter.flush();
//        return outputStream.toByteArray();
//    }
//}
