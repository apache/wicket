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
package org.apache.wicket.validation;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.validation.IFormValidator;

/**
 * Validator that provides a behavior, e.g. for rendering client-side or ajax
 * validation. This interface can be implemented by either
 * {@link IValidator validators} or {@link IFormValidator form validators}.
 */
public interface IBehaviorProvider extends IClusterable
{
	/**
	 * Gets behavior for validation. This method is called right after the
	 * validator is added to a form or form component. The resulting behavior
	 * will be added to the component. Note that after the behavior is added, it
	 * will just lead its own life; this method will not be invoked anymore.
	 * 
	 * @param component
	 *            component currently using the validator
	 * @return The behavior, which can be used for rendering e.g. javascript or
	 *         ajax validation. If this returns null, it will be ignored.
	 */
	IBehavior newValidationBehavior(Component component);
}
