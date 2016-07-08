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

import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.ClassStringResourceLoaderTest.MyValidator;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests the nested string resource loader
 */
public class NestedStringResourceLoaderTest extends WicketTestCase
{

	/**
	 * Tests the nested string resource loader
	 */
	@Test
	public void testNestedStrings(){
		List<IStringResourceLoader> loaders = tester.getApplication().getResourceSettings().getStringResourceLoaders();
		ClassStringResourceLoader classStringResourceLoader = new ClassStringResourceLoader(MyValidator.class);
		loaders.add(classStringResourceLoader);
		NestedStringResourceLoader nestedStringResourceLoader = new NestedStringResourceLoader(loaders,Pattern.compile("#\\(([^ ]*?)\\)"));
		loaders.clear();
		loaders.add(nestedStringResourceLoader);
		
		assertEquals("This is an assembled nested key.",
			nestedStringResourceLoader.loadStringResource((Component)null, "nested", null, null, null));
	}

}
