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
package org.apache.wicket;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.convert.IConverter;

/**
 * Locates the proper converter instance for a given type. Classes that implement this interface
 * must return the right converter for the given class type. Instances are created by
 * {@link IConverterLocator}, which can be configured using
 * {@link Application#newConverterLocator()}.
 * 
 * @see ConverterLocator
 * 
 * @author jcompagner
 * 
 *         The converter object type
 */
public interface IConverterLocator extends IClusterable
{
	/**
	 * Returns the Converter for the class that is given.
	 * 
	 * @param <C>
	 *            The object to convert from and to String
	 * @param type
	 * @return The converter for the given type.
	 */
	public <C> IConverter<C> getConverter(Class<C> type);
}
