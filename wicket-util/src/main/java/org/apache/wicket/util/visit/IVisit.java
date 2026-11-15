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
package org.apache.wicket.util.visit;

/**
 * Allows visitors to control the visit/traversal
 * 
 * @author igor.vaynberg
 * 
 * @param <R>
 *            type of object the visitor is expected to return, if none use {@link Void}
 */
public interface IVisit<R>
{
	/**
	 * Stops the visit/traversal
	 */
	void stop();

	/**
	 * Stops the visit/traversal and returns {@code result}
	 * 
	 * @param result
	 */
	void stop(R result);

	/**
	 * Prevents the visitor from visiting any children of the object currently visited
	 */
	void dontGoDeeper();
}
