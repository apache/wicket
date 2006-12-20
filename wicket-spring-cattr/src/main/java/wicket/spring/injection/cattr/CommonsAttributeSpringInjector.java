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
package wicket.spring.injection.cattr;

import wicket.injection.ConfigurableInjector;
import wicket.injection.IFieldValueFactory;
import wicket.spring.ISpringContextLocator;

/**
 * Injector that injects classes based on {@link SpringBean} annotation
 * 
 * @author Karthik Gurumurthy
 */
public class CommonsAttributeSpringInjector extends ConfigurableInjector {

	/** value factory for creating field values. */
	IFieldValueFactory factory;

	/**
	 * Constructor
	 * 
	 * @param locator
	 *            spring context locator
	 */
	public CommonsAttributeSpringInjector(ISpringContextLocator locator) {
		initFactory(locator);
	}

	/**
	 * Initialize the factory.
	 * 
	 * @param locator
	 *            context locator
	 */
	private void initFactory(ISpringContextLocator locator) {
		factory = new CommonsAttributeProxyFieldValueFactory(locator);
	}

	/**
	 * @see wicket.injection.ConfigurableInjector#getFieldValueFactory()
	 */
	protected IFieldValueFactory getFieldValueFactory() {
		return factory;
	}

}
