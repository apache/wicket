/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.HashSet;
import java.util.Set;

import wicket.Response;
import wicket.markup.html.IHeaderContributor;

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
	 * thread local for the entries that were processed during the current
	 * request.
	 */
	private static final ThreadLocal processedEntries = new ThreadLocal();

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
	public final void renderHead(final Response response)
	{
		IHeaderContributor[] contributors = getHeaderContributors();
		// do nothing if we don't need to
		if (contributors == null)
		{
			return;
		}

		// get the processed entries for this request
		Set entries = (Set)processedEntries.get();

		int len = contributors.length;
		// were any contributors set?
		if (entries == null)
		{
			entries = new HashSet(len);
			processedEntries.set(entries);
		}

		for (int i = 0; i < len; i++)
		{
			if (!entries.contains(contributors[i]))
			{
				// not yet printed for this request: print it
				contributors[i].renderHead(response);
				entries.add(contributors[i]);
			}
			// else the reference was already printed out: ignore it
		}
	}

	/**
	 * @see wicket.behavior.AbstractBehavior#cleanup()
	 */
	public final void cleanup()
	{
		// clean up thread
		processedEntries.set(null);
		super.cleanup();
	}
}
