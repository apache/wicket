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
package org.apache.wicket.util.tester;

/**
 * A <code>Result</code> class.
 * 
 * @author unknown
 * @since 1.2.6
 */
public class Result
{
	public static final Result PASS = new Result(false);

	private final boolean failed;
	private final String message;

	public Result(boolean failed)
	{
		this(failed, "");
	}

	public Result(boolean failed, String message)
	{
		this.failed = failed;
		this.message = message;
	}

	/**
	 * Returns a <code>Result</code> which failed.
	 * 
	 * @param message
	 *            an error message
	 * @return a <code>Result</code> which failed
	 */
	public static Result fail(String message)
	{
		return new Result(true, message);
	}

	/**
	 * Returns a <code>Result</code> which passed.
	 * 
	 * @return a <code>Result</code> which passed
	 */
	public static Result pass()
	{
		return PASS;
	}

	/**
	 * Returns <code>true</code> if the <code>Result</code> was a failure.
	 * 
	 * @return <code>true</code> if the <code>Result</code> was a failure
	 */
	public boolean wasFailed()
	{
		return failed;
	}

	/**
	 * Retrieves the error message.
	 * 
	 * @return the error message
	 */
	public String getMessage()
	{
		return message;
	}
}
