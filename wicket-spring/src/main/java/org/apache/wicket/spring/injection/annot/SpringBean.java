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
package org.apache.wicket.spring.injection.annot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to tag a field as a placeholder for a spring bean.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
public @interface SpringBean {
	/**
	 * Optional attribute for specifying the name of the bean. If not specified, the bean will be
	 * looked up by the type of the field with the annotation.
	 * 
	 * @return name attr
	 */
	String name() default "";


	/**
	 * Optional attribute for specifying if bean is required or not.
	 * 
	 * @return {@code false} if the bean is optional. Default: {@code true}.
	 */
	boolean required() default true;
}
