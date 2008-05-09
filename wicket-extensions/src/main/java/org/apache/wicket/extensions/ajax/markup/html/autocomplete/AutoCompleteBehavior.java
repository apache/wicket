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
package org.apache.wicket.extensions.ajax.markup.html.autocomplete;

import java.util.Iterator;

import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebResponse;


/**
 * This behavior builds on top of {@link AbstractAutoCompleteBehavior} by introducing the concept of
 * a {@link IAutoCompleteRenderer} to make response writing easier.
 * 
 * @param <T>
 * 
 * @see IAutoCompleteRenderer
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Janne Hietam&auml;ki (jannehietamaki)
 */
public abstract class AutoCompleteBehavior<T> extends AbstractAutoCompleteBehavior
{
	private static final long serialVersionUID = 1L;

	private final IAutoCompleteRenderer<T> renderer;

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 */
	public AutoCompleteBehavior(IAutoCompleteRenderer<T> renderer)
	{
		this(renderer, false);
	}


	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 * @param preselect
	 *            highlight/preselect the first item in the autocomplete list automatically
	 */
	public AutoCompleteBehavior(IAutoCompleteRenderer<T> renderer, boolean preselect)
	{
		this(renderer, new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Constructor
	 * 
	 * @param renderer
	 *            renderer that will be used to generate output
	 * @param settings
	 *            settings for the autocomplete list
	 */
	public AutoCompleteBehavior(IAutoCompleteRenderer<T> renderer, AutoCompleteSettings settings)
	{
		if (renderer == null)
		{
			throw new IllegalArgumentException("renderer cannot be null");
		}
		if (settings == null)
		{
			settings = new AutoCompleteSettings();
		}
		this.renderer = renderer;
		this.settings = settings;
	}


	@Override
	protected final void onRequest(final String val, RequestCycle requestCycle)
	{
		IRequestTarget target = new IRequestTarget()
		{

			public void respond(RequestCycle requestCycle)
			{

				WebResponse r = (WebResponse)requestCycle.getResponse();

				// Determine encoding
				final String encoding = Application.get()
					.getRequestCycleSettings()
					.getResponseRequestEncoding();
				r.setCharacterEncoding(encoding);
				r.setContentType("text/xml; charset=" + encoding);

				// Make sure it is not cached by a
				r.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
				r.setHeader("Cache-Control", "no-cache, must-revalidate");
				r.setHeader("Pragma", "no-cache");

				Iterator<T> comps = getChoices(val);
				renderer.renderHeader(r);
				while (comps.hasNext())
				{
					final T comp = comps.next();
					renderer.render(comp, r, val);
				}
				renderer.renderFooter(r);
			}

			public void detach(RequestCycle requestCycle)
			{
			}

		};
		requestCycle.setRequestTarget(target);
	}

	/**
	 * Callback method that should return an iterator over all possible choice objects. These
	 * objects will be passed to the renderer to generate output. Usually it is enough to return an
	 * iterator over strings.
	 * 
	 * @param input
	 *            current input
	 * @return iterator over all possible choice objects
	 */
	protected abstract Iterator<T> getChoices(String input);
}
