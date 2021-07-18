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
package org.apache.wicket.bean.validation;

import java.lang.annotation.Annotation;

import jakarta.validation.constraints.Size;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * An object that can modify a component's markup tag during render based on values of a constraint
 * annotation. An example would be a modifier that adds the {@code maxlen} attribute to the tag
 * given a {@link Size} annotation.
 * 
 * @author igor
 * 
 */
@FunctionalInterface
public interface ITagModifier<T extends Annotation>
{
	/**
	 * Modify the tag
	 * 
	 * @param component
	 *            component the tag belongs to
	 * @param tag
	 *            markup tag to be modified
	 * @param annotation
	 *            constraint annotation
	 */
	void modify(FormComponent<?> component, ComponentTag tag, T annotation);
	
	ITagModifier<?> NO_OP = (component, tag, annotation) -> {
	};
}
