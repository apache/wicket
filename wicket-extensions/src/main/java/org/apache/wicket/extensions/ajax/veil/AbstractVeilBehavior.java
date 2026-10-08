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
package org.apache.wicket.extensions.ajax.veil;

import java.time.Duration;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;

/**
 * Base class of the behaviors that put a veil over a region of the page while Ajax requests are
 * in flight.
 * <p>
 * The veil appears as soon as a request is sent. It is transparent and swallows mouse clicks, so
 * the user cannot fire further requests or change what the pending one is about. Only if the
 * request is still running after {@link #getSpinnerDelay()} does the veil get the CSS class
 * {@code wicket-veil-busy}, which dims the region and shows a spinner; once shown, the spinner
 * stays for at least {@link #getMinimumSpinnerTime()}, so a response arriving just after it
 * appeared does not make it flicker. Both timings apply to the page veil and to local veils
 * alike, and can be changed per behavior with {@link #setSpinnerDelay(Duration)} and
 * {@link #setMinimumSpinnerTime(Duration)}. The veil does not intercept the keyboard.
 * <p>
 * The look comes from {@code wicket-veil.css} and can be overridden with the classes
 * {@code wicket-veil}, {@code wicket-veil-busy}, {@code wicket-veil-host} and
 * {@code wicket-veil-host-static}.
 * <p>
 * A request is left unveiled when it carries the extra parameter
 * {@value PageVeilBehavior#NO_VEIL_PARAMETER}, see {@link PageVeilBehavior#noVeil}.
 *
 * @see PageVeilBehavior
 * @see LocalVeilBehavior
 * @since 11.0.0
 */
public abstract class AbstractVeilBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	private static final ResourceReference JS = new JavaScriptResourceReference(
		AbstractVeilBehavior.class, "wicket-veil.js");

	private static final ResourceReference CSS = new CssResourceReference(
		AbstractVeilBehavior.class, "wicket-veil.css");

	private Duration spinnerDelay = Duration.ofMillis(300);

	private Duration minimumSpinnerTime = Duration.ofMillis(500);

	/**
	 * @return how long a request has to run before the spinner is shown; 300 ms by default
	 */
	protected Duration getSpinnerDelay()
	{
		return spinnerDelay;
	}

	/**
	 * Sets how long a request has to run before the spinner is shown. {@link Duration#ZERO}
	 * shows it as soon as the request is sent.
	 *
	 * @param spinnerDelay
	 *            the delay, not negative
	 * @return this, for chaining
	 */
	public AbstractVeilBehavior setSpinnerDelay(Duration spinnerDelay)
	{
		this.spinnerDelay = checkNotNegative(spinnerDelay, "spinnerDelay");
		return this;
	}

	/**
	 * @return how long the spinner stays at least, once it is shown; 500 ms by default
	 */
	protected Duration getMinimumSpinnerTime()
	{
		return minimumSpinnerTime;
	}

	/**
	 * Sets how long the spinner stays at least, once it is shown, even when the request is over
	 * sooner. {@link Duration#ZERO} removes it together with the request.
	 *
	 * @param minimumSpinnerTime
	 *            the minimum time, not negative
	 * @return this, for chaining
	 */
	public AbstractVeilBehavior setMinimumSpinnerTime(Duration minimumSpinnerTime)
	{
		this.minimumSpinnerTime = checkNotNegative(minimumSpinnerTime, "minimumSpinnerTime");
		return this;
	}

	private static Duration checkNotNegative(Duration duration, String name)
	{
		Args.notNull(duration, name);
		if (duration.isNegative())
		{
			throw new IllegalArgumentException(name + " must not be negative: " + duration);
		}
		return duration;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);

		CoreLibrariesContributor.contributeAjax(component.getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(CssHeaderItem.forReference(CSS));
		response.render(OnDomReadyHeaderItem.forScript(getInitScript(component)));
	}

	/**
	 * @param component
	 *            the component this behavior is bound to
	 * @return the script registering the veil with {@code Wicket.Veil}
	 */
	protected abstract CharSequence getInitScript(Component component);

	/**
	 * @return the timings as the options object {@code Wicket.Veil} expects
	 */
	protected final String getOptions()
	{
		return String.format(Locale.ROOT, "{\"delay\":%d,\"minimum\":%d}",
			getSpinnerDelay().toMillis(), getMinimumSpinnerTime().toMillis());
	}
}
