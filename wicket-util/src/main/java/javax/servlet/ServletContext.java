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
package javax.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

/**
 * A temporary class used until all dependencies provide releases based on jakarta.** APIs
 */
public interface ServletContext extends jakarta.servlet.ServletContext {

    class Impl implements ServletContext {

        private final jakarta.servlet.ServletContext delegate;

        public Impl(jakarta.servlet.ServletContext delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public jakarta.servlet.ServletContext getDelegate() {
            return delegate;
        }
    }

    jakarta.servlet.ServletContext getDelegate();

    @Override
    default String getContextPath() {
        return getDelegate().getContextPath();
    }

    @Override
    default jakarta.servlet.ServletContext getContext(String uripath) {
        return getDelegate().getContext(uripath);
    }

    @Override
    default int getMajorVersion() {
        return getDelegate().getMajorVersion();
    }

    @Override
    default int getMinorVersion() {
        return getDelegate().getMinorVersion();
    }

    @Override
    default int getEffectiveMajorVersion() {
        return getDelegate().getEffectiveMajorVersion();
    }

    @Override
    default int getEffectiveMinorVersion() {
        return getDelegate().getEffectiveMinorVersion();
    }

    @Override
    default String getMimeType(String file) {
        return getDelegate().getMimeType(file);
    }

    @Override
    default Set<String> getResourcePaths(String path) {
        return getDelegate().getResourcePaths(path);
    }

    @Override
    default URL getResource(String path) throws MalformedURLException {
        return getDelegate().getResource(path);
    }

    @Override
    default InputStream getResourceAsStream(String path) {
        return getDelegate().getResourceAsStream(path);
    }

    @Override
    default RequestDispatcher getRequestDispatcher(String path) {
        return getDelegate().getRequestDispatcher(path);
    }

    @Override
    default RequestDispatcher getNamedDispatcher(String name) {
        return getDelegate().getNamedDispatcher(name);
    }

    @Override
    default Servlet getServlet(String name) throws ServletException {
        return getDelegate().getServlet(name);
    }

    @Override
    default Enumeration<Servlet> getServlets() {
        return getDelegate().getServlets();
    }

    @Override
    default Enumeration<String> getServletNames() {
        return getDelegate().getServletNames();
    }

    @Override
    default void log(String msg) {
        getDelegate().log(msg);
    }

    @Override
    default void log(Exception exception, String msg) {
        getDelegate().log(exception, msg);
    }

    @Override
    default void log(String message, Throwable throwable) {
        getDelegate().log(message, throwable);
    }

    @Override
    default String getRealPath(String path) {
        return getDelegate().getRealPath(path);
    }

    @Override
    default String getServerInfo() {
        return getDelegate().getServerInfo();
    }

    @Override
    default String getInitParameter(String name) {
        return getDelegate().getInitParameter(name);
    }

    @Override
    default Enumeration<String> getInitParameterNames() {
        return getDelegate().getInitParameterNames();
    }

    @Override
    default boolean setInitParameter(String name, String value) {
        return getDelegate().setInitParameter(name, value);
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
    default void setAttribute(String name, Object object) {
        getDelegate().setAttribute(name, object);
    }

    @Override
    default void removeAttribute(String name) {
        getDelegate().removeAttribute(name);
    }

    @Override
    default String getServletContextName() {
        return getDelegate().getServletContextName();
    }

    @Override
    default ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return getDelegate().addServlet(servletName, className);
    }

    @Override
    default ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return getDelegate().addServlet(servletName, servlet);
    }

    @Override
    default ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return getDelegate().addServlet(servletName, servletClass);
    }

    @Override
    default ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return getDelegate().addJspFile(servletName, jspFile);
    }

    @Override
    default <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return getDelegate().createServlet(clazz);
    }

    @Override
    default ServletRegistration getServletRegistration(String servletName) {
        return getDelegate().getServletRegistration(servletName);
    }

    @Override
    default Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return getDelegate().getServletRegistrations();
    }

    @Override
    default FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return getDelegate().addFilter(filterName, className);
    }

    @Override
    default FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return getDelegate().addFilter(filterName, filter);
    }

    @Override
    default FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return getDelegate().addFilter(filterName, filterClass);
    }

    @Override
    default <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return getDelegate().createFilter(clazz);
    }

    @Override
    default FilterRegistration getFilterRegistration(String filterName) {
        return getDelegate().getFilterRegistration(filterName);
    }

    @Override
    default Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return getDelegate().getFilterRegistrations();
    }

    @Override
    default SessionCookieConfig getSessionCookieConfig() {
        return getDelegate().getSessionCookieConfig();
    }

    @Override
    default void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        getDelegate().setSessionTrackingModes(sessionTrackingModes);
    }

    @Override
    default Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return getDelegate().getDefaultSessionTrackingModes();
    }

    @Override
    default Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return getDelegate().getEffectiveSessionTrackingModes();
    }

    @Override
    default void addListener(String className) {
        getDelegate().addListener(className);
    }

    @Override
    default <T extends EventListener> void addListener(T t) {
        getDelegate().addListener(t);
    }

    @Override
    default void addListener(Class<? extends EventListener> listenerClass) {
        getDelegate().addListener(listenerClass);
    }

    @Override
    default <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return getDelegate().createListener(clazz);
    }

    @Override
    default JspConfigDescriptor getJspConfigDescriptor() {
        return getDelegate().getJspConfigDescriptor();
    }

    @Override
    default ClassLoader getClassLoader() {
        return getDelegate().getClassLoader();
    }

    @Override
    default void declareRoles(String... roleNames) {
        getDelegate().declareRoles(roleNames);
    }

    @Override
    default String getVirtualServerName() {
        return getDelegate().getVirtualServerName();
    }

    @Override
    default int getSessionTimeout() {
        return getDelegate().getSessionTimeout();
    }

    @Override
    default void setSessionTimeout(int sessionTimeout) {
        getDelegate().setSessionTimeout(sessionTimeout);
    }

    @Override
    default String getRequestCharacterEncoding() {
        return getDelegate().getRequestCharacterEncoding();
    }

    @Override
    default void setRequestCharacterEncoding(String encoding) {
        getDelegate().setRequestCharacterEncoding(encoding);
    }

    @Override
    default String getResponseCharacterEncoding() {
        return getDelegate().getResponseCharacterEncoding();
    }

    @Override
    default void setResponseCharacterEncoding(String encoding) {
        getDelegate().setResponseCharacterEncoding(encoding);
    }
}
