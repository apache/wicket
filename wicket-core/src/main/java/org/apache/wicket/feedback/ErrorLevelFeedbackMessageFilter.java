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

/**
 * Filter for accepting feedback messages with minimum level.
 * 
 * @author Jonathan Locke
 */
public class ErrorLevelFeedbackMessageFilter implements IFeedbackMessageFilter
{
	private static final long serialVersionUID = 1L;

	/** The minimum error level */
	private final int minimumErrorLevel;

	/**
	 * Filters messages with an greater or equals level than minimumErrorLevel.
	 * 
	 * @param minimumErrorLevel
	 *            The component to filter on, the constraints can be: <br />
	 *            {@link org.apache.wicket.feedback.FeedbackMessage#SUCCESS} <br />
	 *            {@link org.apache.wicket.feedback.FeedbackMessage#UNDEFINED} <br />
	 *            {@link org.apache.wicket.feedback.FeedbackMessage#DEBUG} <br />
	 *            {@link org.apache.wicket.feedback.FeedbackMessage#INFO} <br />
	 *            {@link org.apache.wicket.feedback.FeedbackMessage#WARNING} <br />
	 *            {@link org.apache.wicket.feedback.FeedbackMessage#ERROR} <br />
	 *            {@link org.apache.wicket.feedback.FeedbackMessage#FATAL} <br />
	 */
	public ErrorLevelFeedbackMessageFilter(int minimumErrorLevel)
	{
		this.minimumErrorLevel = minimumErrorLevel;
	}

	/**
	 * @see org.apache.wicket.feedback.IFeedbackMessageFilter#accept(org.apache.wicket.feedback.FeedbackMessage)
	 */
	@Override
	public boolean accept(FeedbackMessage message)
	{
		return message.isLevel(minimumErrorLevel);
	}
}
