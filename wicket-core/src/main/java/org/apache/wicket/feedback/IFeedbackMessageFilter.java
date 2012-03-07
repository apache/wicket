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
package org.apache.wicket.feedback;

import org.apache.wicket.util.io.IClusterable;

/**
 * Interface for filtering feedback messages.
 * 
 * @author Jonathan Locke
 */
public interface IFeedbackMessageFilter extends IClusterable
{
	/**
	 * Filter that returns simply all available messages.
	 */
	public static final IFeedbackMessageFilter ALL = new IFeedbackMessageFilter()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean accept(FeedbackMessage message)
		{
			return true;
		}
	};

	/**
	 * Filter that does not match any message
	 */
	public static final IFeedbackMessageFilter NONE = new IFeedbackMessageFilter()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean accept(FeedbackMessage message)
		{
			return false;
		}
	};

	/**
	 * @param message
	 *            The message to test for inclusion
	 * @return True if the message should be included, false to exclude it
	 */
	boolean accept(FeedbackMessage message);
}