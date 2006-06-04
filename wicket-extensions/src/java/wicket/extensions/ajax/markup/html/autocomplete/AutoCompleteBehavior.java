/*
 * $Id: AbstractTime.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision: 5874 $ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
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
package wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.Response;

/**
 * This behavior builds on top of {@link AbstractAutoCompleteBehavior} by
 * introducing the concept of a {@link IAutoCompleteRenderer} to make response
 * writing easier.
 * 
 * @see IAutoCompleteRenderer
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public abstract class AutoCompleteBehavior extends AbstractAutoCompleteBehavior
{
	private static final long serialVersionUID = 1L;

	private final IAutoCompleteRenderer renderer;

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 */
	public AutoCompleteBehavior(IAutoCompleteRenderer renderer)
	{
		if (renderer == null)
		{
			throw new IllegalArgumentException("renderer cannot be null");
		}
		this.renderer = renderer;
	}


	@Override
	protected final void onRequest(final String val, RequestCycle requestCycle)
	{
		IRequestTarget target = new IRequestTarget()
		{
			public void respond(RequestCycle requestCycle)
			{
				Response r = requestCycle.getResponse();
				Iterator comps = getChoices(val);
				renderer.renderHeader(r);
				while (comps.hasNext())
				{
					final Object comp = comps.next();
					renderer.render(comp, r, val);
				}
				renderer.renderFooter(r);
			}

			public void detach(RequestCycle requestCycle)
			{
			}

			public Object getLock(RequestCycle requestCycle)
			{
				return requestCycle.getSession();
			}

		};
		requestCycle.setRequestTarget(target);
	}

	/**
	 * Callback method that should return an iterator over all possiblet
	 * choice objects. These objects will be passed to the renderer to generate
	 * output. Usually it is enough to return an iterator over strings.
	 * 
	 * @param input
	 *            current input
	 * @return iterator ver all possible choice objects
	 */
	protected abstract Iterator getChoices(String input);
}
