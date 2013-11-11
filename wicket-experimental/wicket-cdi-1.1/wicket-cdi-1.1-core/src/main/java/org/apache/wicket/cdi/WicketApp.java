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
package org.apache.wicket.cdi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;
import javax.inject.Qualifier;

import org.apache.wicket.protocol.http.WebApplication;

/**
 * Bean Qualifier for Cdi enable WebApplication. This Qualifier allows for the
 * WebApplication to be named so that the CdiApplicationFactory can select the
 * WebApplication when multiple WebApplication exist in the ClassLoader. This
 * Annotation also marks the WebApplication as Dependent. This prevents the
 * WebApplication from being proxied, which will cause failures in an EE
 * container.
 * 
 * @author jsarman
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RUNTIME)
@Documented
@Dependent
@Typed(WebApplication.class)
public @interface WicketApp {
	String value() default "";
}
