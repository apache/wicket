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
package org.apache.wicket.proxy.util;

/**
 * Tester object that is valid as long as equals/hashCode/toString have not been called on it.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IObjectMethodTester
{

	/**
	 * @return true if object is valid, false otherwise
	 */
	boolean isValid();

	/**
	 * Resets state of object back to valid
	 */
	void reset();

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	int hashCode();

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	String toString();

}