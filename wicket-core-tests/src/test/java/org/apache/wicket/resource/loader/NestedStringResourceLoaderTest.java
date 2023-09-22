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
package org.apache.wicket.resource.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.resource.loader.ClassStringResourceLoaderTest.MyValidator;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.junit.jupiter.api.Test;

/**
 * Tests the nested string resource loader
 */
class NestedStringResourceLoaderTest extends WicketTestCase
{

	/**
	 * Tests the nested string resource loader
	 */
	@Test
	void testNestedStrings(){
		List<IStringResourceLoader> loaders = tester.getApplication().getResourceSettings().getStringResourceLoaders();
		ClassStringResourceLoader classStringResourceLoader = new ClassStringResourceLoader(MyValidator.class);
		loaders.add(classStringResourceLoader);
		NestedStringResourceLoader nestedStringResourceLoader = new NestedStringResourceLoader(loaders,Pattern.compile("#\\(([^ ]*?)\\)"));
		loaders.clear();
		loaders.add(nestedStringResourceLoader);
		
		assertEquals("This is an assembled nested key.",
			nestedStringResourceLoader.loadStringResource((Component)null, "nested", null, null, null));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6393
	 */
	@Test
	void whenAnyKeyIsMissing_thenUseTheDefaultValue(){
		List<IStringResourceLoader> loaders = tester.getApplication().getResourceSettings().getStringResourceLoaders();
		ClassStringResourceLoader classStringResourceLoader = new ClassStringResourceLoader(NestedWithMissingKeyValidator.class);
		loaders.add(classStringResourceLoader);
		NestedStringResourceLoader nestedStringResourceLoader = new NestedStringResourceLoader(loaders,Pattern.compile("#\\(([^ ]*?)\\)"));
		loaders.clear();
		loaders.add(nestedStringResourceLoader);

		final String defaultValue = "default value";
		assertEquals(defaultValue, Localizer.get().getString("nested", null, defaultValue));
	}

	public static class NestedWithMissingKeyValidator implements IValidator<String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void validate(IValidatable<String> v)
		{
		}
	}
}
