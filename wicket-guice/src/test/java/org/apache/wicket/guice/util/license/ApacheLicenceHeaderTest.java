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
package org.apache.wicket.guice.util.license;


import org.apache.wicket.util.license.ApacheLicenseHeaderTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that the license headers are in place in this project. The tests are run from
 * {@link ApacheLicenseHeaderTestCase}, but you can add project specific tests here if needed.
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
		// addHeaders = true;
	}
	
	@BeforeEach
	@Override
	public void before()
	{
		super.before();
	}

	@Test
	@Override
	public void licenseHeaders()
	{
		super.licenseHeaders();
	}
	
}
