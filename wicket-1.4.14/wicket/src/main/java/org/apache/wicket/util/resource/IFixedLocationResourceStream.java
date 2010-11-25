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
package org.apache.wicket.util.resource;

/**
 * Non-dynamic resource streams point to fixed locations, like a file or a url. Such resources
 * should implement this interface and provide clients with information about the resource location,
 * such that the client is able to deduct e.g. an extension or URL schema.
 * 
 * @author eelcohillenius
 */
public interface IFixedLocationResourceStream
{
	/**
	 * @return The fixed location as a string, e.g. the file name or the URL. Return null to avoid
	 *         caching the markup.
	 */
	String locationAsString();
}
