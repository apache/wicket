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
package javax.servlet.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

/**
 * A temporary class used until all dependencies provide releases based on jakarta.** APIs
 */
public interface HttpServletRequest extends jakarta.servlet.http.HttpServletRequest {

    class Impl implements HttpServletRequest {

        private final jakarta.servlet.http.HttpServletRequest delegate;

        public Impl(jakarta.servlet.http.HttpServletRequest delegate)
        {
            this.delegate = delegate;
        }
        
        @Override
        public jakarta.servlet.http.HttpServletRequest getDelegate() {
            return delegate;
        }
    }

    jakarta.servlet.http.HttpServletRequest getDelegate();

    @Override
    default String getAuthType() {
        return getDelegate().getAuthType();
    }

    @Override
    default Cookie[] getCookies() {
        return getDelegate().getCookies();
    }

    @Override
    default long getDateHeader(String name) {
        return getDelegate().getDateHeader(name);
    }

    @Override
    default String getHeader(String name) {
        return getDelegate().getHeader(name);
    }

    @Override
    default Enumeration<String> getHeaders(String name) {
        return getDelegate().getHeaders(name);
    }

    @Override
    default Enumeration<String> getHeaderNames() {
        return getDelegate().getHeaderNames();
    }

    @Override
    default int getIntHeader(String name) {
        return getDelegate().getIntHeader(name);
    }

    @Override
    default String getMethod() {
        return getDelegate().getMethod();
    }

    @Override
    default String getPathInfo() {
        return getDelegate().getPathInfo();
    }

    @Override
    default String getPathTranslated() {
        return getDelegate().getPathTranslated();
    }

    @Override
    default String getContextPath() {
        return getDelegate().getContextPath();
    }

    @Override
    default String getQueryString() {
        return getDelegate().getQueryString();
    }

    @Override
    default String getRemoteUser() {
        return getDelegate().getRemoteUser();
    }

    @Override
    default boolean isUserInRole(String role) {
        return getDelegate().isUserInRole(role);
    }

    @Override
    default Principal getUserPrincipal() {
        return getDelegate().getUserPrincipal();
    }

    @Override
    default String getRequestedSessionId() {
        return getDelegate().getRequestedSessionId();
    }

    @Override
    default String getRequestURI() {
        return getDelegate().getRequestURI();
    }

    @Override
    default StringBuffer getRequestURL() {
        return getDelegate().getRequestURL();
    }

    @Override
    default String getServletPath() {
        return getDelegate().getServletPath();
    }

    @Override
    default HttpSession getSession(boolean create) {
        return getDelegate().getSession(create);
    }

    @Override
    default HttpSession getSession() {
        return getDelegate().getSession();
    }

    @Override
    default String changeSessionId() {
        return getDelegate().changeSessionId();
    }

    @Override
    default boolean isRequestedSessionIdValid() {
        return getDelegate().isRequestedSessionIdValid();
    }

    @Override
    default boolean isRequestedSessionIdFromCookie() {
        return getDelegate().isRequestedSessionIdFromCookie();
    }

    @Override
    default boolean isRequestedSessionIdFromURL() {
        return getDelegate().isRequestedSessionIdFromURL();
    }

    @Override
    default boolean isRequestedSessionIdFromUrl() {
        return getDelegate().isRequestedSessionIdFromURL();
    }

    @Override
    default boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return getDelegate().authenticate(response);
    }

    @Override
    default void login(String username, String password) throws ServletException {
        getDelegate().login(username, password);
    }

    @Override
    default void logout() throws ServletException {
        getDelegate().logout();
    }

    @Override
    default Collection<Part> getParts() throws IOException, ServletException {
        return getDelegate().getParts();
    }

    @Override
    default Part getPart(String name) throws IOException, ServletException {
        return getDelegate().getPart(name);
    }

    @Override
    default <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return getDelegate().upgrade(handlerClass);
    }

    @Override
    default Object getAttribute(String name) {
        return getDelegate().getAttribute(name);
    }

    @Override
    default Enumeration<String> getAttributeNames() {
        return getDelegate().getAttributeNames();
    }

    @Override
    default String getCharacterEncoding() {
        return getDelegate().getCharacterEncoding();
    }

    @Override
    default void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        getDelegate().setCharacterEncoding(env);
    }

    @Override
    default int getContentLength() {
        return getDelegate().getContentLength();
    }

    @Override
    default long getContentLengthLong() {
        return getDelegate().getContentLengthLong();
    }

    @Override
    default String getContentType() {
        return getDelegate().getContentType();
    }

    @Override
    default ServletInputStream getInputStream() throws IOException {
        return getDelegate().getInputStream();
    }

    @Override
    default String getParameter(String name) {
        return getDelegate().getParameter(name);
    }

    @Override
    default Enumeration<String> getParameterNames() {
        return getDelegate().getParameterNames();
    }

    @Override
    default String[] getParameterValues(String name) {
        return getDelegate().getParameterValues(name);
    }

    @Override
    default Map<String, String[]> getParameterMap() {
        return getDelegate().getParameterMap();
    }

    @Override
    default String getProtocol() {
        return getDelegate().getProtocol();
    }

    @Override
    default String getScheme() {
        return getDelegate().getScheme();
    }

    @Override
    default String getServerName() {
        return getDelegate().getScheme();
    }

    @Override
    default int getServerPort() {
        return getDelegate().getServerPort();
    }

    @Override
    default BufferedReader getReader() throws IOException {
        return getDelegate().getReader();
    }

    @Override
    default String getRemoteAddr() {
        return getDelegate().getRemoteAddr();
    }

    @Override
    default String getRemoteHost() {
        return getDelegate().getRemoteHost();
    }

    @Override
    default void setAttribute(String name, Object o) {
        getDelegate().setAttribute(name, o);
    }

    @Override
    default void removeAttribute(String name) {
        getDelegate().removeAttribute(name);
    }

    @Override
    default Locale getLocale() {
        return getDelegate().getLocale();
    }

    @Override
    default Enumeration<Locale> getLocales() {
        return getDelegate().getLocales();
    }

    @Override
    default boolean isSecure() {
        return getDelegate().isSecure();
    }

    @Override
    default RequestDispatcher getRequestDispatcher(String path) {
        return getDelegate().getRequestDispatcher(path);
    }

    @Override
    default String getRealPath(String path) {
        return getDelegate().getRealPath(path);
    }

    @Override
    default int getRemotePort() {
        return getDelegate().getRemotePort();
    }

    @Override
    default String getLocalName() {
        return getDelegate().getLocalName();
    }

    @Override
    default String getLocalAddr() {
        return getDelegate().getLocalAddr();
    }

    @Override
    default int getLocalPort() {
        return getDelegate().getLocalPort();
    }

    @Override
    default ServletContext getServletContext() {
        return getDelegate().getServletContext();
    }

    @Override
    default AsyncContext startAsync() throws IllegalStateException {
        return getDelegate().startAsync();
    }

    @Override
    default AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return getDelegate().startAsync(servletRequest, servletResponse);
    }

    @Override
    default boolean isAsyncStarted() {
        return getDelegate().isAsyncStarted();
    }

    @Override
    default boolean isAsyncSupported() {
        return getDelegate().isAsyncSupported();
    }

    @Override
    default AsyncContext getAsyncContext() {
        return getDelegate().getAsyncContext();
    }

    @Override
    default DispatcherType getDispatcherType() {
        return getDelegate().getDispatcherType();
    }
}
