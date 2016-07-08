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
package org.apache.wicket.markup.html;

import org.apache.wicket.request.resource.PackageResource;


/**
 * Guards lazily loaded {@link PackageResource package resources} against unwanted access.
 * 
 * @author Eelco Hillenius
 */
public interface IPackageResourceGuard
{

	/**
	 * Whether the package resource that can be reached using the provided parameters may be
	 * accessed.
	 * 
	 * @param absolutePath
	 *            The absolute path, starting from the class root (packages are separated with
	 *            forward slashes instead of dots).
	 * 
	 * @return True if access is permitted, false otherwise
	 */
	boolean accept(final String absolutePath);
}
