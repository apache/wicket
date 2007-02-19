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
package wicket.spring.injection;

import wicket.Application;
import wicket.IInitializer;
import wicket.protocol.http.WebApplication;

/**
 * A spring integration module. This module takes care of configuring wicket to
 * allow injection of {@link SpringBean} annotated fields
 * 
 * @author ivaynberg
 * 
 */
public class SpringModule implements IInitializer
{

	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	public void init(Application application)
	{
		if (!(application instanceof WebApplication))
		{
			throw new IllegalStateException(
					"This initializer can only be used with applications that extend "
							+ WebApplication.class.getName());
		}

		application.addComponentInstantiationListener(new SpringComponentInjector(
				(WebApplication) application));
	}

}
