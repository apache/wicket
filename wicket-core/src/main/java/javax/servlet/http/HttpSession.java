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

import java.util.Enumeration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionContext;

/**
 * A temporary class used until all dependencies provide releases based on jakarta.** APIs
 */
public interface HttpSession extends jakarta.servlet.http.HttpSession {
    
    class Impl implements javax.servlet.http.HttpSession {

        private final jakarta.servlet.http.HttpSession delegate;

        public Impl(jakarta.servlet.http.HttpSession delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public jakarta.servlet.http.HttpSession getDelegate() {
            return delegate;
        }
    }

    jakarta.servlet.http.HttpSession getDelegate();

    @Override
    default long getCreationTime() {
        return getDelegate().getCreationTime();
    }

    @Override
    default String getId() {
        return getDelegate().getId();
    }

    @Override
    default long getLastAccessedTime() {
        return getDelegate().getLastAccessedTime();
    }

    @Override
    default ServletContext getServletContext() {
        return getDelegate().getServletContext();
    }

    @Override
    default void setMaxInactiveInterval(int interval) {
        getDelegate().setMaxInactiveInterval(interval);
    }

    @Override
    default int getMaxInactiveInterval() {
        return getDelegate().getMaxInactiveInterval();
    }

    @Override
    default HttpSessionContext getSessionContext() {
        return getDelegate().getSessionContext();
    }

    @Override
    default Object getAttribute(String name) {
        return getDelegate().getAttribute(name);
    }

    @Override
    default Object getValue(String name) {
        return getDelegate().getValue(name);
    }

    @Override
    default Enumeration<String> getAttributeNames() {
        return getDelegate().getAttributeNames();
    }

    @Override
    default String[] getValueNames() {
        return getDelegate().getValueNames();
    }

    @Override
    default void setAttribute(String name, Object value) {
        getDelegate().setAttribute(name, value);
    }

    @Override
    default void putValue(String name, Object value) {
        getDelegate().putValue(name, value);
    }

    @Override
    default void removeAttribute(String name) {
        getDelegate().removeAttribute(name);
    }

    @Override
    default void removeValue(String name) {
        getDelegate().removeValue(name);
    }

    @Override
    default void invalidate() {
        getDelegate().invalidate();
    }

    @Override
    default boolean isNew() {
        return getDelegate().isNew();
    }
}
