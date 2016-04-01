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

import java.util.UUID;

import org.apache.wicket.Component;
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
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

/**
 * Helper class to create components based on lambda expressions. In each component that are
 * returned of the methods of this class, there are static helper methods, so that they can be used
 * for lambdas.
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
	 */
	public static AbstractAjaxTimerBehavior onTimer(Duration interval,
		WicketConsumer<AjaxRequestTarget> onTimer)
	{
		Args.notNull(onTimer, "onTimer");

		return new AbstractAjaxTimerBehavior(interval)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				onTimer.accept(target);
			}
		};
	}

	/**
	 * Creates an {@link AjaxClientInfoBehavior} based on lambda expressions
	 * 
	 * @param onClientInfo
	 *            the {@link WicketBiConsumer} which accepts the {@link AjaxRequestTarget} and the
	 *            {@link WebClientInfo}
	 * @return the {@link AjaxClientInfoBehavior}
	 */
	public static AjaxClientInfoBehavior onClientInfo(
		WicketBiConsumer<AjaxRequestTarget, WebClientInfo> onClientInfo)
	{
		Args.notNull(onClientInfo, "onClientInfo");

		return new AjaxClientInfoBehavior()
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onClientInfo(AjaxRequestTarget target, WebClientInfo clientInfo)
			{
				onClientInfo.accept(target, clientInfo);
			}
		};
	}

	/**
	 * Creates an {@link AjaxEventBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onEvent
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxEventBehavior}
	 */
	public static AjaxEventBehavior onEvent(String eventName,
		WicketConsumer<AjaxRequestTarget> onEvent)
	{
		Args.notNull(onEvent, "onEvent");

		return new AjaxEventBehavior(eventName)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onEvent.accept(target);
			}
		};
	}

	/**
	 * Creates an {@link AjaxNewWindowNotifyingBehavior} based on lambda expressions
	 * 
	 * @param windowName
	 *            the window name
	 * @param onNewWindow
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxNewWindowNotifyingBehavior}
	 */
	public static AjaxNewWindowNotifyingBehavior onNewWindow(String windowName,
		WicketConsumer<AjaxRequestTarget> onNewWindow)
	{
		Args.notNull(onNewWindow, "onNewWindow");

		if (Strings.isEmpty(windowName))
		{
			windowName = UUID.randomUUID().toString();
		}

		return new AjaxNewWindowNotifyingBehavior(windowName)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onNewWindow(AjaxRequestTarget target)
			{
				onNewWindow.accept(target);
			}
		};
	}

	/**
	 * Creates an {@link AbstractAjaxTimerBehavior} based on lambda expressions
	 * 
	 * @param interval
	 *            the interval for the self update
	 * @param onTimer
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AbstractAjaxTimerBehavior}
	 */
	public static AjaxSelfUpdatingTimerBehavior onSelfUpdate(Duration interval,
		WicketConsumer<AjaxRequestTarget> onTimer)
	{
		Args.notNull(onTimer, "onTimer");

		return new AjaxSelfUpdatingTimerBehavior(interval)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target)
			{
				onTimer.accept(target);
			}
		};
	}

	/**
	 * Creates an {@link AjaxFormChoiceComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param onUpdateChoice
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormChoiceComponentUpdatingBehavior}
	 */
	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(
		WicketConsumer<AjaxRequestTarget> onUpdateChoice)
	{
		Args.notNull(onUpdateChoice, "onUpdateChoice");
		return new AjaxFormChoiceComponentUpdatingBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdateChoice.accept(target);
			}
		};
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
	 */
	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(
		WicketConsumer<AjaxRequestTarget> onUpdateChoice,
		WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		Args.notNull(onUpdateChoice, "onUpdateChoice");
		Args.notNull(onError, "onError");
		return new AjaxFormChoiceComponentUpdatingBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdateChoice.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e)
			{
				onError.accept(target, e);
			}
		};
	}

	/**
	 * Creates an {@link AjaxFormComponentUpdatingBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onUpdate
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormComponentUpdatingBehavior}
	 */
	public static AjaxFormComponentUpdatingBehavior onUpdate(String eventName,
		WicketConsumer<AjaxRequestTarget> onUpdate)
	{
		Args.notNull(onUpdate, "onUpdate");

		return new AjaxFormComponentUpdatingBehavior(eventName)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdate.accept(target);
			}
		};
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
	 */
	public static AjaxFormComponentUpdatingBehavior onUpdate(String eventName,
		WicketConsumer<AjaxRequestTarget> onUpdate,
		WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		Args.notNull(onUpdate, "onUpdate");
		Args.notNull(onError, "onError");

		return new AjaxFormComponentUpdatingBehavior(eventName)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdate.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e)
			{
				onError.accept(target, e);
			}
		};
	}

	/**
	 * Creates an {@link AjaxFormSubmitBehavior} based on lambda expressions
	 * 
	 * @param eventName
	 *            the event name
	 * @param onSubmit
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxFormSubmitBehavior}
	 */
	public static AjaxFormSubmitBehavior onSubmit(String eventName,
		WicketConsumer<AjaxRequestTarget> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxFormSubmitBehavior(eventName)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				onSubmit.accept(target);
			}
		};
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
	 */
	public static AjaxFormSubmitBehavior onSubmit(String eventName,
		WicketConsumer<AjaxRequestTarget> onSubmit, WicketConsumer<AjaxRequestTarget> onError)
	{
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxFormSubmitBehavior(eventName)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				onSubmit.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				onError.accept(target);
			}
		};
	}

	/**
	 * Creates an {@link OnChangeAjaxBehavior} based on lambda expressions
	 * 
	 * @param onChange
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link OnChangeAjaxBehavior}
	 */
	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange)
	{
		Args.notNull(onChange, "onChange");

		return new OnChangeAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onChange.accept(target);
			}
		};
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
	 */
	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange,
		WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		Args.notNull(onChange, "onChange");
		Args.notNull(onError, "onError");

		return new OnChangeAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onChange.accept(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, RuntimeException e)
			{
				onError.accept(target, e);
			}
		};
	}

	/**
	 * Creates an {@link AjaxLink} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the ajax link
	 * @param onClick
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxLink}
	 */
	public static <T> AjaxLink<T> ajaxLink(String id, WicketConsumer<AjaxRequestTarget> onClick)
	{
		Args.notNull(onClick, "onClick");

		return new AjaxLink<T>(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onClick.accept(target);
			}
		};
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
	 */
	public static AjaxButton ajaxButton(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxButton(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				onSubmit.accept(target, form);
			}
		};
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
	 */
	public static AjaxButton ajaxButton(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onError)
	{
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxButton(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				onSubmit.accept(target, form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				onError.accept(target, form);
			}
		};
	}

	/**
	 * Creates an {@link AjaxCheckBox} based on lambda expressions
	 * 
	 * @param id
	 *            the id of ajax check box
	 * @param onUpdate
	 *            the {@link WicketConsumer} which accepts the {@link AjaxRequestTarget}
	 * @return the {@link AjaxCheckBox}
	 */
	public static AjaxCheckBox ajaxCheckBox(String id, WicketConsumer<AjaxRequestTarget> onUpdate)
	{
		Args.notNull(onUpdate, "onUpdate");

		return new AjaxCheckBox(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onUpdate(AjaxRequestTarget target)
			{
				onUpdate.accept(target);
			}
		};
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
	 */
	public static AjaxSubmitLink ajaxSubmitLink(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxSubmitLink(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				onSubmit.accept(target, form);
			}
		};
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
	 */
	public static AjaxSubmitLink ajaxSubmitLink(String id,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit,
		WicketBiConsumer<AjaxRequestTarget, Form<?>> onError)
	{
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxSubmitLink(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				onSubmit.accept(target, form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				onError.accept(target, form);
			}
		};
	}

	/**
	 * Creates a {@link Link} based on lambda expressions
	 * 
	 * @param id
	 *            the id of the link
	 * @param onClick
	 *            the {@link WicketConsumer} which accepts the {@link Void}
	 * @return the {@link Link}
	 */
	public static <T> Link<T> link(String id, WicketConsumer<Void> onClick)
	{
		Args.notNull(onClick, "onClick");

		return new Link<T>(id)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				onClick.accept((Void)null);
			}
		};
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
	 */
	public static Behavior onTag(WicketConsumer<ComponentTag> onTagConsumer)
	{
		Args.notNull(onTagConsumer, "onTagConsumer");

		return new Behavior()
		{
			@Override
			public void onComponentTag(Component component, ComponentTag tag)
			{
				super.onComponentTag(component, tag);
				onTagConsumer.accept(tag);
			}
		};
	}
}
