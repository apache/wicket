/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload2.jaksrvlt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;


import org.apache.commons.fileupload2.FileUploadBase;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ReadListener;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

public class MockJakSrvltHttpRequest implements HttpServletRequest {

    private final InputStream mRequestData;

    private long length;

    private final String mStrContentType;

    private int readLimit = -1;

    private final Map<String, String> mHeaders = new java.util.HashMap<>();

    /**
     * Creates a new instance with the given request data
     * and content type.
     */
    public MockJakSrvltHttpRequest(
            final byte[] requestData,
            final String strContentType) {
        this(new ByteArrayInputStream(requestData),
                requestData.length, strContentType);
    }

    /**
     * Creates a new instance with the given request data
     * and content type.
     */
    public MockJakSrvltHttpRequest(
            final InputStream requestData,
            final long requestLength,
            final String strContentType) {
        mRequestData = requestData;
        length = requestLength;
        mStrContentType = strContentType;
        mHeaders.put(FileUploadBase.CONTENT_TYPE, strContentType);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    @Override
    public String getAuthType() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    @Override
    public Cookie[] getCookies() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(String)
     */
    @Override
    public long getDateHeader(final String arg0) {
        return 0;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeader(String)
     */
    @Override
    public String getHeader(final String headerName) {
        return mHeaders.get(headerName);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaders(String)
     */
    @Override
    public Enumeration<String> getHeaders(final String arg0) {
        // todo - implement
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        // todo - implement
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(String)
     */
    @Override
    public int getIntHeader(final String arg0) {
        return 0;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    @Override
    public String getMethod() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    @Override
    public String getPathTranslated() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    @Override
    public String getContextPath() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    @Override
    public String getQueryString() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    @Override
    public String getRemoteUser() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(String)
     */
    @Override
    public boolean isUserInRole(final String arg0) {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    @Override
    public String getRequestedSessionId() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    @Override
    public String getServletPath() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    @Override
    public HttpSession getSession(final boolean arg0) {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    @Override
    public HttpSession getSession() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     * @deprecated
     */
    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    /**
     * @see javax.servlet.ServletRequest#getAttribute(String)
     */
    @Override
    public Object getAttribute(final String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#setCharacterEncoding(String)
     */
    @Override
    public void setCharacterEncoding(final String arg0)
        throws UnsupportedEncodingException {
    }

    /**
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        int iLength;

        if (null == mRequestData) {
            iLength = -1;
        } else {
            if (length > Integer.MAX_VALUE) {
                throw new RuntimeException("Value '" + length + "' is too large to be converted to int");
            }
            iLength = (int) length;
        }
        return iLength;
    }

    /**
     * For testing attack scenarios in SizesTest.
     */
    public void setContentLength(final long length) {
        this.length = length;
    }

    /**
     * @see javax.servlet.ServletRequest#getContentType()
     */
    @Override
    public String getContentType() {
        return mStrContentType;
    }

    /**
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new MyServletInputStream(mRequestData, readLimit);
    }

    /**
     * Sets the read limit. This can be used to limit the number of bytes to read ahead.
     *
     * @param readLimit the read limit to use
     */
    public void setReadLimit(final int readLimit) {
        this.readLimit = readLimit;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameter(String)
     */
    @Override
    public String getParameter(final String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterValues(String)
     */
    @Override
    public String[] getParameterValues(final String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    @Override
    public String getProtocol() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getScheme()
     */
    @Override
    public String getScheme() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getServerName()
     */
    @Override
    public String getServerName() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalName() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    @Override
    public int getServerPort() {
        return 0;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getLocalPort() {
        return 0;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public int getRemotePort() {
        return 0;
    }

    /**
     * @see javax.servlet.ServletRequest#getReader()
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    @Override
    @SuppressWarnings("javadoc") // This is a Servlet 2.4 method
    public String getLocalAddr() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#setAttribute(String, Object)
     */
    @Override
    public void setAttribute(final String arg0, final Object arg1) {
    }

    /**
     * @see javax.servlet.ServletRequest#removeAttribute(String)
     */
    @Override
    public void removeAttribute(final String arg0) {
    }

    /**
     * @see javax.servlet.ServletRequest#getLocale()
     */
    @Override
    public Locale getLocale() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#getLocales()
     */
    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        return false;
    }

    /**
     * @see javax.servlet.ServletRequest#getRequestDispatcher(String)
     */
    @Override
    public RequestDispatcher getRequestDispatcher(final String arg0) {
        return null;
    }

    private static class MyServletInputStream
        extends jakarta.servlet.ServletInputStream {

        private final InputStream in;
        private final int readLimit;

        /**
         * Creates a new instance, which returns the given
         * streams data.
         */
        public MyServletInputStream(final InputStream pStream, final int readLimit) {
            in = pStream;
            this.readLimit = readLimit;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (readLimit > 0) {
                return in.read(b, off, Math.min(readLimit, len));
            }
            return in.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(final ReadListener readListener) {
            throw new IllegalStateException("Not implemented");
        }

    }

    @Override
    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public ServletContext getServletContext() {
        final HttpSession session = getSession();
        if (session == null) {
            return null;
        }
      return session.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse)
            throws IllegalStateException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(final String username, final String password) throws ServletException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void logout() throws ServletException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(final String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) throws IOException, ServletException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public String getRealPath(final String path) {
        return null;
    }
}
