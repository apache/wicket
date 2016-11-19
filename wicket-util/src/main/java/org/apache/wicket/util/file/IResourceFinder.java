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
package org.apache.wicket.util.file;

import org.apache.wicket.util.resource.IResourceStream;

/**
 * Knows how to find resources.
 * 
 * @author jcompagner
 */
@FunctionalInterface
public interface IResourceFinder
{
	/**
	 * Looks for a given pathname along this path
	 * 
	 * @param clazz
	 *            The class requesting the resource stream
	 * @param pathname
	 *            The filename with possible path
	 * @return The resource stream
	 */
	IResourceStream find(final Class<?> clazz, final String pathname);
}
