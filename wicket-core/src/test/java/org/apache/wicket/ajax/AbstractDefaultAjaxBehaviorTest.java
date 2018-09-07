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
package org.apache.wicket.ajax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxAttributeName;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for AbstractDefaultAjaxBehavior
 * 
 * @since 6.0
 */
class AbstractDefaultAjaxBehaviorTest
{
	/**
	 * Checks the generated JSON for Ajax's attributes
	 */
	@Test
	void renderAjaxAttributes()
	{
		AjaxRequestAttributes attributes = new AjaxRequestAttributes();
		attributes.getExtraParameters().put("param1", 123);
		attributes.getExtraParameters().put("param2", Locale.CANADA_FRENCH);

		AjaxCallListener listener = new AjaxCallListener();
		listener.onPrecondition("return somePrecondition();");
		listener.onBefore("alert('Before!');");
		listener.onAfter("alert('After!');");
		listener.onSuccess("alert('Success!');");
		listener.onFailure("alert('Failure!');");
		listener.onComplete("alert('Complete!');");
		attributes.getAjaxCallListeners().add(listener);

		Component component = Mockito.mock(Component.class);
		AbstractDefaultAjaxBehavior behavior = new AbstractDefaultAjaxBehavior()
		{
			@Override
			protected void respond(AjaxRequestTarget target)
			{
			}

			@Override
			public CharSequence getCallbackUrl()
			{
				return "some/url";
			}
		};
		behavior.bind(component);

		CharSequence json = behavior.renderAjaxAttributes(component, attributes);

		String expected = "{\"" +
			AjaxAttributeName.URL + "\":\"some/url\",\"" +
			AjaxAttributeName.BEFORE_HANDLER +
				"\":[function(attrs){alert('Before!');}],\"" +
			AjaxAttributeName.AFTER_HANDLER + "\":[function(attrs){alert('After!');}],\"" +
			AjaxAttributeName.SUCCESS_HANDLER +
				"\":[function(attrs, jqXHR, data, textStatus){alert('Success!');}],\"" +
			AjaxAttributeName.FAILURE_HANDLER +
				"\":[function(attrs, jqXHR, errorMessage, textStatus){alert('Failure!');}],\"" +
			AjaxAttributeName.COMPLETE_HANDLER +
				"\":[function(attrs, jqXHR, textStatus){alert('Complete!');}],\"" +
			AjaxAttributeName.PRECONDITION +
				"\":[function(attrs){return somePrecondition();}],\"" +
			AjaxAttributeName.EXTRA_PARAMETERS +
			"\":[{\"name\":\"param1\",\"value\":123},{\"name\":\"param2\",\"value\":\"fr_CA\"}]" +
			"}";

		assertEquals(expected, json);
	}
}
