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
package org.apache.wicket.injection.util;


/**
 * Object to test injection on
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class TestObject extends InternalTestObject
{
	private final MockDependency dependency3 = new MockDependency("dont-inject");

	private MockDependency dependency4;

	/**
	 * @return dependency4
	 */
	public MockDependency getDependency4()
	{
		return dependency4;
	}

	/**
	 * @return dependency3
	 */
	public MockDependency getDependency3()
	{
		return dependency3;
	}

}