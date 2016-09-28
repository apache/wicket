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

import org.apache.wicket.Component;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.junit.Test;

/**
 * 
 */
public class ClassStringResourceLoaderTest extends WicketTestCase
{

	/**
	 * 
	 */
	@Test
	public void validator1()
	{
		ClassStringResourceLoader loader = new ClassStringResourceLoader(MyValidator.class);
		tester.getApplication().getResourceSettings().getStringResourceLoaders().add(loader);

		assertEquals("${label} is invalid",
			loader.loadStringResource((Component)null, "error", null, null, null));
	}

	/**
	 * 
	 */
	public static class MyValidator implements IValidator<String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void validate(IValidatable<String> v)
		{

		}
	}
}
