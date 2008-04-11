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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;

/**
 * Optional interface for validators ({@link IValidator} and {@link IFormValidator}) that can
 * react when validators are added to a <code>Component</code>.
 * <p>
 * Implementation note: currently we keep this case simple, stupid, and non-generic. In future
 * versions we may revisit this and support removal events (<i>IF</i> removal of validators is
 * ever allowed, which justifies its own discussion). Also, we may look at whether this is a common
 * event to support for {@link IBehavior}s as well. This raises additional questions that need to
 * be answered, hence we'll start by supporting just the use case when validators are added to forms
 * or form components.
 * 
 * @author Eelco Hillenius
 * @since 1.3
 */
public interface IValidatorAddListener extends IClusterable
{
	/**
	 * Called right after a validator is added to a {@link Form} or {@link FormComponent}. A common
	 * use case for implementing this interface is for validators to add behaviors to implement
	 * client-side validation capabilities, e.g. through JavaScript, Ajax or just by adding a simple
	 * attribute modifier that sets a "maxlength" attribute.
	 * 
	 * @param component
	 *            a <code>Component</code> to which the validator was just added
	 */
	void onAdded(Component component);
}
