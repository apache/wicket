/*
 * $Id: ContainerFeedbackMessageFilter.java 5528 2006-04-26 12:49:13 +0000 (Wed,
 * 26 Apr 2006) eelco12 $ $Revision$ $Date: 2006-04-26 12:49:13 +0000
 * (Wed, 26 Apr 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.feedback;

import wicket.MarkupContainer;

/**
 * Filter for child-of relationship
 * 
 * @author Jonathan Locke
 * @author Johan Compagner
 */
public class ContainerFeedbackMessageFilter implements IFeedbackMessageFilter
{
	private static final long serialVersionUID = 1L;

	private final MarkupContainer container;

	/**
	 * Constructor
	 * 
	 * @param container
	 *            The container that message reporters must be a child of
	 */
	public ContainerFeedbackMessageFilter(MarkupContainer container)
	{
		if (container == null)
		{
			throw new IllegalArgumentException("container must be not null");
		}
		this.container = container;
	}

	/**
	 * @see wicket.feedback.IFeedbackMessageFilter#accept(wicket.feedback.FeedbackMessage)
	 */
	public boolean accept(FeedbackMessage message)
	{
		if (message.getReporter() == null)
		{
			return false;
		}
		else
		{
			return container.contains(message.getReporter(), true);
		}
	}
}
