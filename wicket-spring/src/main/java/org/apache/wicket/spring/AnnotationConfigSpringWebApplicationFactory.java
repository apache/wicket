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
package org.apache.wicket.spring;

import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * A specialization of {@link SpringWebApplicationFactory} that uses
 * {@link AnnotationConfigWebApplicationContext}
 *
 * <p>
 *
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;MyApplication&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.apache.wicket.protocol.http.WicketFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;org.apache.wicket.spring.AnnotationConfigSpringWebApplicationFactory&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
 *     &lt;param-value&gt;com.example.MySpringConfig&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * </p>
 */
public class AnnotationConfigSpringWebApplicationFactory extends SpringWebApplicationFactory {
    @Override
    protected ConfigurableWebApplicationContext newApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }
}
