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
package org.apache.wicket.cdi;

import java.util.Arrays;

import org.apache.wicket.util.license.ApacheLicenseHeaderTestCase;

/**
 * Test that the license headers are in place in this project. The tests are run
 * from {@link ApacheLicenseHeaderTestCase}, but you can add project specific
 * tests here if needed.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public class ApacheLicenceHeaderTest extends ApacheLicenseHeaderTestCase
{
	/**
	 * Construct.
	 */
	public ApacheLicenceHeaderTest()
	{
		xmlIgnore.add(".settings");
		xmlIgnore.add("src/main/resources/META-INF/beans.xml");
		xmlIgnore.add("src/test/java/simplelogger.properties");
		xmlPrologIgnore = Arrays.asList("src");
	}
}
