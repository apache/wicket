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
package org.apache.wicket.spring;

import org.apache.wicket.util.io.IClusterable;
import org.springframework.context.ApplicationContext;


/**
 * Interface representing object that can locate a spring context. The implementation should take up
 * little room when serialized.
 * 
 * SpringObjectLocator uses this interface to locate the spring context so that it in turn can
 * locate a bean.
 * 
 * Ideal implementations use a static lookup to locate the context.
 * 
 * For Example:
 * 
 * <pre>
 * class SpringContextLocator implements ISpringContextLocator
 * {
 * 	public ApplicationContext getSpringContext()
 * 	{
 * 		// MyApplication is the subclass of WebApplication used by the application
 * 		return ((MyApplication)Application.get()).getContext();
 * 	}
 * }
 * </pre>
 * 
 * @see SpringBeanLocator
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface ISpringContextLocator extends IClusterable
{
	/**
	 * Getter for spring application context
	 * 
	 * @return spring application context
	 */
	ApplicationContext getSpringContext();
}
