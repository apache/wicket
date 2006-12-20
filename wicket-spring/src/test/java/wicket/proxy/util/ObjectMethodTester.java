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
package wicket.proxy.util;

/**
 * @see IObjectMethodTester
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ObjectMethodTester implements IObjectMethodTester
{
	private boolean valid = true;

	/**
	 * Constructor
	 */
	public ObjectMethodTester()
	{
		valid = true;
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#isValid()
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#reset()
	 */
	public void reset()
	{
		valid = true;
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		valid = false;
		return super.equals(obj);
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#hashCode()
	 */
	public int hashCode()
	{
		valid = false;
		return super.hashCode();
	}

	/**
	 * @see wicket.proxy.util.IObjectMethodTester#toString()
	 */
	public String toString()
	{
		valid = false;
		return super.toString();
	}

}
