/*
 * $Id: AbstractHeaderContributor.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat,
 * 20 May 2006) $
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
package wicket.behavior;

import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.IHeaderResponse;

/**
 * Behaviour that delegates header contribution to a number of other
 * contributors. It checks the contributions that were made in the same request
 * to avoid double contributions.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractHeaderContributor extends AbstractBehavior
		implements
			IHeaderContributor
{
	/**
	 * Construct.
	 */
	public AbstractHeaderContributor()
	{
	}

	/**
	 * Gets the header contributors for this behavior.
	 * 
	 * @return the header contributors; may return null if there are none
	 */
	public abstract IHeaderContributor[] getHeaderContributors();

	/**
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.Response)
	 */
	public final void renderHead(final IHeaderResponse response)
	{
		IHeaderContributor[] contributors = getHeaderContributors();
		// do nothing if we don't need to
		if (contributors == null)
		{
			return;
		}

		for (int i = 0; i < contributors.length; i++)
		{
			if (response.wasRendered(contributors[i]) == false) 
			{
				contributors[i].renderHead(response);
				response.markRendered(contributors[i]);
			}
		}
	}
}
