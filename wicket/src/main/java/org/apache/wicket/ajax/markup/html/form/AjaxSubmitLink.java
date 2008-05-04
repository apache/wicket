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
package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.AbstractSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * A link that submits a form via ajax. Since this link takes the form as a constructor argument it
 * does not need to be inside form's component hierarchy.
 * 
 * <p/> It works by splitting the javascript/Ajax calls from the normal non-ajax requests by
 * generating:
 * 
 * <pre>
 * &lt;a href=&quot;normal action url&quot; onclick=&quot;ajax javascript script; return
 * false;&quot;&gt;link&lt;/a&gt;
 * </pre>
 * 
 * If/when javascript is turned off in the browser, or it doesn't support javascript, then the
 * browser will not respond to the onclick event, using the href directly. Wicket will then use a
 * normal request target, and call the serverside onClick with a null {@link AjaxRequestTarget}.
 * 
 * If javascript is enabled, Wicket will send an ajax request, and process it serverside with an
 * {@link AjaxRequestTarget} that is supplied to the server-side onClick method. The "return false"
 * in the &lt;a href&gt; onclick handler ensures the browser doesn't perform the normal request too.
 * 
 * The latter is nicely illustrated with this:
 * 
 * <pre>
 * &lt;a href=&quot;javascript:alert('href event handler');&quot;
 * onclick=&quot;alert('onclick event handler');&quot;&gt;clicking me gives two
 * alerts&lt;/a&gt;
 * 
 * &lt;a href=&quot;javascript:alert('href event handler');&quot;
 * onclick=&quot;alert('onclick event handler');return false;&quot;&gt;clicking me
 * gives only one alert&lt;/a&gt;
 * </pre>
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            The model object type
 */
public abstract class AjaxSubmitLink<T> extends AbstractSubmitLink<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public AjaxSubmitLink(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param form
	 */
	public AjaxSubmitLink(String id, final Form< ? > form)
	{
		super(id);

		add(new AjaxFormSubmitBehavior(form, "onclick")
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				AjaxSubmitLink.this.onSubmit(target, getForm());
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxSubmitLink.this.onError(target, getForm());
			}

			@Override
			protected CharSequence getEventHandler()
			{
				return new AppendingStringBuffer(super.getEventHandler()).append("; return false;");
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return AjaxSubmitLink.this.getAjaxCallDecorator();
			}

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				// write the onclick handler only if link is enabled
				if (isLinkEnabled())
				{
					super.onComponentTag(tag);
				}
			}
		});

	}

	/**
	 * Returns the {@link IAjaxCallDecorator} that will be used to modify the generated javascript.
	 * This is the preferred way of changing the javascript in the onclick handler
	 * 
	 * @return call decorator used to modify the generated javascript or null for none
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (isLinkEnabled())
		{
			if (tag.getName().toLowerCase().equals("a"))
			{
				tag.put("href", "#");
			}
		}
		else
		{
			disableLink(tag);
		}
	}

	/**
	 * Final implementation of the Button's onSubmit. AjaxSubmitLinks have there own onSubmit which
	 * is called.
	 * 
	 * @see org.apache.wicket.markup.html.form.Button#onSubmit()
	 */
	public final void onSubmit()
	{
	}

	/**
	 * Listener method invoked on form submit
	 * 
	 * @param target
	 * @param form
	 */
	protected abstract void onSubmit(AjaxRequestTarget target, Form< ? > form);

	/**
	 * Listener method invoked on form submit with errors
	 * 
	 * @param target
	 * @param form
	 * 
	 * TODO 1.3: Make abstract to be consistent with onsubmit()
	 */
	protected void onError(AjaxRequestTarget target, Form< ? > form)
	{

	}

}
