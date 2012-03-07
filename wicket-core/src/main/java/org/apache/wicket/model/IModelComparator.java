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
package org.apache.wicket.model;

import org.apache.wicket.Component;
import org.apache.wicket.util.io.IClusterable;

/**
 * Implementations of this interface compare model object. The component is given so that a
 * developer can choose what the previous object is The default implementation for form components
 * is just component.getModelObject(); But developers can choose to keep the last rendered value for
 * that component and compare this value with the newObject. So that it doesn't overwrite values for
 * an object that was changed by another session if the current session didn't touch that specific
 * value.
 * 
 * @author jcompagner
 * @author Jonathan Locke
 * 
 */
public interface IModelComparator extends IClusterable
{
	/**
	 * @param component
	 *            The component which received the new object
	 * @param newObject
	 *            The newObject
	 * @return True if the previous components object is the same as the newObject.
	 */
	boolean compare(Component component, Object newObject);
}
