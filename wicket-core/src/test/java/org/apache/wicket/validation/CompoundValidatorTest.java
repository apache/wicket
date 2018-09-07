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
import org.apache.wicket.validation.validator.RangeValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test for {@link CompoundValidator}.
 */
class CompoundValidatorTest
{
	/**
	 * WICKET-6482 delegate to nested behaviors
	 */
	@SuppressWarnings("unchecked")
	@Test
    void delegate() {
		CompoundValidator<String> compound = new CompoundValidator<>();
		
		compound.add(new IValidator<String>()
		{
			@Override
			public void validate(IValidatable<String> validatable)
			{
			}
		});
		
		RangeValidator<String> validator = Mockito.mock(RangeValidator.class);
		compound.add(validator);

		Component component = null;

		compound.bind(component);
		compound.onConfigure(component);
		compound.renderHead(component, null);
		compound.beforeRender(component);
		compound.onComponentTag(component, null);
		compound.afterRender(component);
		compound.onEvent(component, null);
		compound.onException(component, null);
		compound.onRemove(component);
		compound.detach(component);
		
		Mockito.verify(validator).bind(component);
		Mockito.verify(validator).onConfigure(component);
		Mockito.verify(validator).renderHead(component, null);
		Mockito.verify(validator).beforeRender(component);
		Mockito.verify(validator).onComponentTag(component, null);
		Mockito.verify(validator).afterRender(component);
		Mockito.verify(validator).onEvent(component, null);
		Mockito.verify(validator).onException(component, null);
		Mockito.verify(validator).onRemove(component);
		Mockito.verify(validator).detach(component);
	}
}