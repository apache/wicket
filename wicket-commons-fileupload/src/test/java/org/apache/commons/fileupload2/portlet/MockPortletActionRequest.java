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
package org.apache.commons.fileupload2.portlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

import org.apache.commons.fileupload2.FileUploadBase;

/**
 * Mock class for tests. Implements an {@link ActionRequest}.
 *
 * @see PortletFileUploadTest
 * @since 1.4
 */
@SuppressWarnings("rawtypes") // because of the portlet ActionRequest API does not use generics
public class MockPortletActionRequest implements ActionRequest {

    private final Hashtable<String, Object> attributes = new Hashtable<>();

    private final Map<String, String> parameters = new HashMap<>();

    private String characterEncoding;
    private final int length;
    private final String contentType;
    private final InputStream requestData;

    public MockPortletActionRequest(final byte[] requestData, final String contentType) {
        this(new ByteArrayInputStream(requestData), requestData.length, contentType);
    }

    public MockPortletActionRequest(final ByteArrayInputStream byteArrayInputStream,
                                    final int requestLength, final String contentType) {
        this.requestData = byteArrayInputStream;
        length = requestLength;
        this.contentType = contentType;
        attributes.put(FileUploadBase.CONTENT_TYPE, contentType);
    }

    @Override
    public Object getAttribute(final String key) {
        return attributes.get(key);
    }

    @Override
    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public Enumeration getLocales() {
        return Collections.enumeration(Arrays.asList(Locale.getAvailableLocales()));
    }

    @Override
    public String getParameter(final String key) {
        return parameters.get(key);
    }

    @Override
    public Map getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(final String arg0) {
        return null;
    }

    @Override
    public PortalContext getPortalContext() {
        return null;
    }

    @Override
    public PortletMode getPortletMode() {
        return null;
    }

    @Override
    public PortletSession getPortletSession() {
        return null;
    }

    @Override
    public PortletSession getPortletSession(final boolean arg0) {
        return null;
    }

    @Override
    public PortletPreferences getPreferences() {
        return null;
    }

    @Override
    public Enumeration getProperties(final String arg0) {
        return null;
    }

    @Override
    public String getProperty(final String arg0) {
        return null;
    }

    @Override
    public Enumeration getPropertyNames() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getResponseContentType() {
        return null;
    }

    @Override
    public Enumeration getResponseContentTypes() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public WindowState getWindowState() {
        return null;
    }

    @Override
    public boolean isPortletModeAllowed(final PortletMode arg0) {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isUserInRole(final String arg0) {
        return false;
    }

    @Override
    public boolean isWindowStateAllowed(final WindowState arg0) {
        return false;
    }

    @Override
    public void removeAttribute(final String key) {
        attributes.remove(key);
    }

    @Override
    public void setAttribute(final String key, final Object value) {
        attributes.put(key, value);
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public int getContentLength() {
        return length;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getPortletInputStream() throws IOException {
        return requestData;
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException, IOException {
        return null;
    }

    @Override
    public void setCharacterEncoding(final String characterEncoding) throws UnsupportedEncodingException {
        this.characterEncoding = characterEncoding;
    }

}
