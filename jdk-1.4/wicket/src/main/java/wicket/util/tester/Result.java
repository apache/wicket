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
package wicket.util.tester;

/**
 *
 */
public class Result
{
	private static final Result PASS = new Result(false);
	private final boolean failed;
	private final String message;

	private Result(boolean failed)
	{
		this.failed = failed;
		this.message = "";
	}

	private Result(boolean failed, String message)
	{
		this.failed = failed;
		this.message = message;
	}

	/**
	 * @param message the error message for the user
	 * @return a Result which failed
	 */
	static Result fail(String message)
	{
		return new Result(true, message);
	}

	/**
	 * @return a Result which passed
	 */
	static Result pass()
	{
		return PASS;
	}

	/**
	 * @return true if the result was failed
	 */
	public boolean wasFailed()
	{
		return failed;
	}

	/**
	 * @return the error message for the user
	 */
	public String getMessage()
	{
		return message;
	}
}
