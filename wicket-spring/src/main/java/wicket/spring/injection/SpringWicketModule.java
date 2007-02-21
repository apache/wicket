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

import wicket.protocol.http.WebApplication;

/**
 * A spring integration module. This module takes care of configuring wicket to
 * allow injection of {@link SpringBean} annotated fields. To install the module do 
 * <code>
 * MyApplication extends WebApplication
 * {
 * ...
 * public void init() {
 *     new SpringWicketModule(this);
 * }
 * ...
 * }
 * </code>
 * 
 * @author ivaynberg
 * 
 */
public class SpringWicketModule
{
	/**
	 * Constructor
	 * 
	 * @param application
	 */
	public SpringWicketModule(WebApplication application)
	{
		application.addComponentInstantiationListener(new SpringComponentInjector(
				application));
	}

}
