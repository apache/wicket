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
package org.apache.wicket.application;

import java.net.URL;
import java.util.Iterator;

/**
 * An interface to code which finds classes and resources
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public interface IClassResolver
{
	/**
	 * Resolves a class by name (which may or may not involve loading it; thus the name class
	 * *resolver* not *loader*).
	 * 
	 * @param classname
	 *            Fully qualified classname to find
	 * @return Class
	 * @throws ClassNotFoundException
	 */
	Class<?> resolveClass(final String classname) throws ClassNotFoundException;


	/**
	 * Tries to load all the resources by the name that is given.
	 * 
	 * @param name
	 * @return iterator over matching resources
	 */
	Iterator<URL> getResources(String name);

	/**
	 * Returns the {@link ClassLoader} to be used for resolving classes
	 *
	 * @return the {@link ClassLoader} to be used for resolving classes
	 */
	ClassLoader getClassLoader();
}