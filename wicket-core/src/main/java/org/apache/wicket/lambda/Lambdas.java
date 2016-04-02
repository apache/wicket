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
package org.apache.wicket.lambda;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxClientInfoBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxNewWindowNotifyingBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.time.Duration;

/**
 * Convenience class for easy static importing of lambda factory methods in several components and behaviors.
 */
public class Lambdas
{
	/**
	 * Creates an {@link AbstractAjaxTimerBehavior} based on lambda expressions
	 * 
	 * @param interval
	 *            the interval of the timer
	 * @param onTimer
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AbstractAjaxTimerBehavior}
	 *
	 * @see AbstractAjaxTimerBehavior#onTimer(Duration, WicketConsumer)
	 */
	public static AbstractAjaxTimerBehavior onTimer(Duration interval,
		WicketConsumer<AjaxRequestTarget> onTimer)
	{
		return AbstractAjaxTimerBehavior.onTimer(interval, onTimer);
	}

	/**
	 * Creates an {@link AjaxClientInfoBehavior} based on lambda expressions
	 *
	 * @param onClientInfo
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link WebClientInfo}
	 * @return the {@link AjaxClientInfoBehavior}
	 *
	 * @see AjaxClientInfoBehavior#onClientInfo(WicketBiConsumer)
	 */
	public static AjaxClientInfoBehavior onClientInfo(
		WicketBiConsumer<AjaxRequestTarget, WebClientInfo> onClientInfo)
	{
		return AjaxClientInfoBehavior.onClientInfo(onClientInfo);
	}

	/**
	 * Creates an {@link AjaxEventBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onEvent
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxEventBehavior}
	 *
	 * @see AjaxEventBehavior#onEvent(org.apache.wicket.Component, org.apache.wicket.event.IEvent)
	 */
	public static AjaxEventBehavior onEvent(String eventName,
		WicketConsumer<AjaxRequestTarget> onEvent)
	{
		return AjaxEventBehavior.onEvent(eventName, onEvent);
	}

	/**
	 * Creates an {@link AjaxNewWindowNotifyingBehavior} based on lambda expressions
	 * 
	 * @param windowName
	 *            the window name
	 * @param onNewWindow
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxNewWindowNotifyingBehavior}
	 * 
	 * @see AjaxNewWindowNotifyingBehavior#onNewWindow(String, WicketConsumer)
	 */
	public static AjaxNewWindowNotifyingBehavior onNewWindow(String windowName,
		WicketConsumer<AjaxRequestTarget> onNewWindow)
	{
		return AjaxNewWindowNotifyingBehavior.onNewWindow(windowName, onNewWindow);
	}

	/**
	 * Creates an {@link AbstractAjaxTimerBehavior} based on lambda expressions
	 * 
	 * @param interval
	 *            the interval for the self update
	 * @param onTimer
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxSelfUpdatingTimerBehavior}
	 * 
	 * @see AjaxSelfUpdatingTimerBehavior#onSelfUpdate(Duration, WicketConsumer)
	 */
	public static AjaxSelfUpdatingTimerBehavior onSelfUpdate(Duration interval,
		WicketConsumer<AjaxRequestTarget> onTimer)
	{
		return AjaxSelfUpdatingTimerBehavior.onSelfUpdate(interval, onTimer);
	}

	/**
	 * Creates an {@link AjaxFormChoiceComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param onUpdateChoice
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormChoiceComponentUpdatingBehavior}
	 * 
	 * @see AjaxFormChoiceComponentUpdatingBehavior#onUpdateChoice(WicketConsumer)
	 */
	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(
		WicketConsumer<AjaxRequestTarget> onUpdateChoice)
	{
		return AjaxFormChoiceComponentUpdatingBehavior.onUpdateChoice(onUpdateChoice);
	}

	/**
	 * Creates an {@link AjaxFormChoiceComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param onUpdateChoice
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @param onError
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link RuntimeException}
	 * @return the {@link AjaxFormChoiceComponentUpdatingBehavior}
	 * 
	 * @see AjaxFormChoiceComponentUpdatingBehavior#onUpdateChoice(WicketConsumer, WicketBiConsumer)
	 */
	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(
		WicketConsumer<AjaxRequestTarget> onUpdateChoice,
		WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		return AjaxFormChoiceComponentUpdatingBehavior.onUpdateChoice(onUpdateChoice, onError);
	}

	/**
	 * Creates an {@link AjaxFormComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onUpdate
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormComponentUpdatingBehavior}
	 * 
	 * @see AjaxFormComponentUpdatingBehavior#onUpdate(String, WicketConsumer) 
	 */
	public static AjaxFormComponentUpdatingBehavior onUpdate(String eventName,
		WicketConsumer<AjaxRequestTarget> onUpdate)
	{
		return AjaxFormComponentUpdatingBehavior.onUpdate(eventName, onUpdate);
	}

	/**
	 * Creates an {@link AjaxFormComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onUpdate
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @param onError
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link RuntimeException}
	 * @return the {@link AjaxFormComponentUpdatingBehavior}
	 * 
	 * @see AjaxFormComponentUpdatingBehavior#onUpdate(String, WicketConsumer, WicketBiConsumer) 
	 */
	public static AjaxFormComponentUpdatingBehavior onUpdate(String eventName,
		WicketConsumer<AjaxRequestTarget> onUpdate,
		WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		return AjaxFormComponentUpdatingBehavior.onUpdate(eventName, onUpdate, onError);
	}

	/**
	 * Creates an {@link AjaxFormSubmitBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onSubmit
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormSubmitBehavior}
	 * 
	 * @see AjaxFormSubmitBehavior#onSubmit(String, WicketConsumer)
	 */
	public static AjaxFormSubmitBehavior onSubmit(String eventName,
		WicketConsumer<AjaxRequestTarget> onSubmit)
	{
		return AjaxFormSubmitBehavior.onSubmit(eventName, onSubmit);
	}

	/**
	 * Creates an {@link AjaxFormSubmitBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onSubmit
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @param onError
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormSubmitBehavior}
	 * 
	 * @see AjaxFormSubmitBehavior#onSubmit(String, WicketConsumer, WicketConsumer)
	 */
	public static AjaxFormSubmitBehavior onSubmit(String eventName,
		WicketConsumer<AjaxRequestTarget> onSubmit, WicketConsumer<AjaxRequestTarget> onError)
	{
		return AjaxFormSubmitBehavior.onSubmit(eventName, onSubmit, onError);
	}

	/**
	 * Creates an {@link OnChangeAjaxBehavior} based on lambda expressions
	 * 
	 * @param onChange
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link OnChangeAjaxBehavior}
	 * 
	 * @see OnChangeAjaxBehavior#onChange(WicketConsumer)
	 */
	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange)
	{
		return OnChangeAjaxBehavior.onChange(onChange);
	}

	/**
	 * Creates an {@link OnChangeAjaxBehavior} based on lambda expressions
	 * 
	 * @param onChange
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @param onError
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link RuntimeException}
	 * @return the {@link OnChangeAjaxBehavior}
	 * 
	 * @see OnChangeAjaxBehavior#onChange(WicketConsumer, WicketBiConsumer)
	 */
	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange,
		WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		return OnChangeAjaxBehavior.onChange(onChange, onError);
	}

	/**
	 * Creates a {@link Behavior} that uses the given {@link WicketConsumer consumer}
	 * to do something with the component's tag.
	 *
	 * <p>
	 *     Usage:<br/>
	 *     <code>component.add(onTag(tag -> tag.put(key, value)));</code>
	 * </p>
	 *
	 * @param onTagConsumer
	 *              the {@link WicketConsumer} that accepts the {@link ComponentTag}
	 * @return The created behavior
	 * 
	 * @see Behavior#onTag(WicketConsumer)
	 */
	public static Behavior onTag(WicketConsumer<ComponentTag> onTagConsumer)
	{
		return Behavior.onTag(onTagConsumer);
	}

	/**
	 * Creates an {@link AjaxLink} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax link
	 * @param onClick
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxLink}
	 * 
	 * @see AjaxLink#onClick(String, WicketConsumer)
	 */
	public static <T> AjaxLink<T> ajaxLink(String id, WicketConsumer<AjaxRequestTarget> onClick)
	{
		return AjaxLink.onClick(id, onClick);
	}

	/**
	 * Creates an {@link AjaxButton} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax button
	 * @param onSubmit
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link Form}
	 * @return the {@link AjaxButton}
	 * 
	 * @see AjaxButton#onSubmit(String, WicketBiConsumer)
	 */
	public static AjaxButton ajaxButton(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit)
	{
		return AjaxButton.onSubmit(id, onSubmit);
	}

	/**
	 * Creates an {@link AjaxButton} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax button
	 * @param onSubmit
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link Form}
	 * @param onError
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link Form}
	 * @return the {@link AjaxButton}
	 * 
	 * @see AjaxButton#onSubmit(String, WicketBiConsumer, WicketBiConsumer)
	 */
	public static AjaxButton ajaxButton(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onError)
	{
		return AjaxButton.onSubmit(id, onSubmit, onError);
	}

	/**
	 * Creates an {@link AjaxCheckBox} based on lambda expressions
	 * 
	 * @param id
	 *            the id of ajax check box
	 * @param onUpdate
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxCheckBox}
	 * 
	 * @see AjaxCheckBox#onUpdate(String, WicketConsumer)
	 */
	public static AjaxCheckBox ajaxCheckBox(String id, WicketConsumer<AjaxRequestTarget> onUpdate)
	{
		return AjaxCheckBox.onUpdate(id, onUpdate);
	}

	/**
	 * Creates an {@link AjaxSubmitLink} based on lambda expressions
	 * 
	 * @param id
	 *            the id of ajax submit link
	 * @param onSubmit
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link Form}
	 * @return the {@link AjaxSubmitLink}
	 * 
	 * @see AjaxSubmitLink#onSubmit(String, WicketBiConsumer)
	 */
	public static AjaxSubmitLink ajaxSubmitLink(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit)
	{
		return AjaxSubmitLink.onSubmit(id, onSubmit);
	}

	/**
	 * Creates an {@link AjaxSubmitLink} based on lambda expressions
	 * 
	 * @param id
	 *            the id of ajax submit link
	 * @param onSubmit
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link Form}
	 * @param onError
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link Form}
	 * @return the {@link AjaxSubmitLink}
	 * 
	 * @see AjaxSubmitLink#onSubmit(String, WicketBiConsumer, WicketBiConsumer)
	 */
	public static AjaxSubmitLink ajaxSubmitLink(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onError)
	{
		return AjaxSubmitLink.onSubmit(id,  onSubmit,  onError);
	}

	/**
	 * Creates a {@link Link} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the link
	 * @param onClick
	 *            the {@link WicketConsumer} which accepts the {@link Void}
	 * @return the {@link Link}
	 * 
	 * @see Link#onClick(String, WicketConsumer)
	 */
	public static <T> Link<T> link(String id, WicketConsumer<Void> onClick)
	{
		return Link.onClick(id, onClick);
	}
}
