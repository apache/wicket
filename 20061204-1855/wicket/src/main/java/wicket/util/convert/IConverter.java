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
package wicket.util.convert;

import java.io.Serializable;

/**
 * General purpose data type converter.  An object that implements this
 * interface can convert objects from one class to another.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public interface IConverter extends ILocalizable, Serializable
{
	/**
	 * Converts the given value to class c.
	 * 
	 * @param value
	 *            The value to convert
	 * @param c
	 *            The class of object to convert to
	 * @return The converted value
	 */
	Object convert(Object value, Class c);
}