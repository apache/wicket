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
package wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.protocol.http.WebResponse;

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


	protected final void onRequest(final String val, RequestCycle requestCycle)
	{
		IRequestTarget target = new IRequestTarget()
		{

			public void respond(RequestCycle requestCycle)
			{
				
				WebResponse r = (WebResponse)requestCycle.getResponse();
				
				// Determine encoding
				final String encoding = Application.get().getRequestCycleSettings().getResponseRequestEncoding();
				r.setCharacterEncoding(encoding);
				r.setContentType("text/xml; charset=" + encoding);

				// Make sure it is not cached by a
				r.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
				r.setHeader("Cache-Control", "no-cache, must-revalidate");
				r.setHeader("Pragma", "no-cache");

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
