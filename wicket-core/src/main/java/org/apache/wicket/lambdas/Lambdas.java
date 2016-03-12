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
package org.apache.wicket.lambdas;

import java.util.UUID;

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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

/**
 *
 */
public class Lambdas
{
	public static AbstractAjaxTimerBehavior onTimer(Duration interval, WicketConsumer<AjaxRequestTarget> onTimer)
	{
		Args.notNull(onTimer, "onTimer");

		return new AbstractAjaxTimerBehavior(interval)
		{
			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				onTimer.accept(target);
			}
		};
	}

	public static AjaxClientInfoBehavior onClientInfo(WicketBiConsumer<AjaxRequestTarget, WebClientInfo> onClientInfo)
	{
		Args.notNull(onClientInfo, "onClientInfo");

		return new AjaxClientInfoBehavior()
		{
			@Override
			protected void onClientInfo(AjaxRequestTarget target, WebClientInfo clientInfo)
			{
				onClientInfo.accept(target, clientInfo);
			}
		};
	}

	public static AjaxEventBehavior onEvent(String eventName, WicketConsumer<AjaxRequestTarget> onEvent)
	{
		Args.notNull(onEvent, "onEvent");

		return new AjaxEventBehavior(eventName)
		{
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onEvent.accept(target);
			}
		};
	}

	public static AjaxNewWindowNotifyingBehavior onNewWindow(String windowName, WicketConsumer<AjaxRequestTarget> onNewWindow)
	{
		Args.notNull(onNewWindow, "onNewWindow");

		if (Strings.isEmpty(windowName))
		{
			windowName = UUID.randomUUID().toString();
		}

		return new AjaxNewWindowNotifyingBehavior(windowName)
		{
			@Override
			protected void onNewWindow(AjaxRequestTarget target)
			{
				onNewWindow.accept(target);
			}
		};
	}

	public static AbstractAjaxTimerBehavior onSelfUpdate(Duration interval, WicketConsumer<AjaxRequestTarget> onTimer)
	{
		Args.notNull(onTimer, "onTimer");

		return new AjaxSelfUpdatingTimerBehavior(interval)
		{
			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target)
			{
				onTimer.accept(target);
			}
		};
	}


	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(WicketConsumer<AjaxRequestTarget> onUpdateChoice) {
		Args.notNull(onUpdateChoice, "onUpdateChoice");
		return new AjaxFormChoiceComponentUpdatingBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdateChoice.accept(target);
			}
		};
	}

	public static AjaxFormChoiceComponentUpdatingBehavior onUpdateChoice(WicketConsumer<AjaxRequestTarget> onUpdateChoice,
	                                                                     WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError) {
		Args.notNull(onUpdateChoice, "onUpdateChoice");
		Args.notNull(onError, "onError");
		return new AjaxFormChoiceComponentUpdatingBehavior()
		{
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


	public static AjaxFormComponentUpdatingBehavior onUpdate(String eventName, WicketConsumer<AjaxRequestTarget> onUpdate)
	{
		Args.notNull(onUpdate, "onUpdate");

		return new AjaxFormComponentUpdatingBehavior(eventName)
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onUpdate.accept(target);
			}
		};
	}

	public static AjaxFormComponentUpdatingBehavior onUpdate(String eventName,
	                                                         WicketConsumer<AjaxRequestTarget> onUpdate,
	                                                         WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		Args.notNull(onUpdate, "onUpdate");
		Args.notNull(onError, "onError");

		return new AjaxFormComponentUpdatingBehavior(eventName)
		{
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

	public static AjaxFormSubmitBehavior onSubmit(String eventName, WicketConsumer<AjaxRequestTarget> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxFormSubmitBehavior(eventName)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				onSubmit.accept(target);
			}
		};
	}

	public static AjaxFormSubmitBehavior onSubmit(String eventName,
	                                              WicketConsumer<AjaxRequestTarget> onSubmit,
	                                              WicketConsumer<AjaxRequestTarget> onError) {
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxFormSubmitBehavior(eventName)
		{
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


	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange)
	{
		Args.notNull(onChange, "onChange");

		return new OnChangeAjaxBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				onChange.accept(target);
			}
		};
	}

	public static OnChangeAjaxBehavior onChange(WicketConsumer<AjaxRequestTarget> onChange,
	                                            WicketBiConsumer<AjaxRequestTarget, RuntimeException> onError)
	{
		Args.notNull(onChange, "onChange");
		Args.notNull(onError, "onError");

		return new OnChangeAjaxBehavior()
		{
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

	public static <T> AjaxLink<T> ajaxLink(String id, WicketConsumer<AjaxRequestTarget> onClick)
	{
		Args.notNull(onClick, "onClick");

		return new AjaxLink<T>(id)
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onClick.accept(target);
			}
		};
	}


	public static AjaxButton ajaxButton(String id, WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxButton(id)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				onSubmit.accept(target, form);
			}
		};
	}

	public static AjaxButton ajaxButton(String id, WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit,
	                                    WicketBiConsumer<AjaxRequestTarget, Form<?>> onError)
	{
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxButton(id)
		{
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

	public static AjaxCheckBox ajaxCheckBox(String id, WicketConsumer<AjaxRequestTarget> onUpdate)
	{
		Args.notNull(onUpdate, "onUpdate");

		return new AjaxCheckBox(id)
		{
			@Override
			public void onUpdate(AjaxRequestTarget target)
			{
				onUpdate.accept(target);
			}
		};
	}


	public static AjaxSubmitLink ajaxSubmitLink(String id, WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit)
	{
		Args.notNull(onSubmit, "onSubmit");

		return new AjaxSubmitLink(id)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				onSubmit.accept(target, form);
			}
		};
	}

	public static AjaxSubmitLink ajaxSubmitLink(String id,
	                                            WicketBiConsumer<AjaxRequestTarget, Form<?>> onSubmit,
	                                            WicketBiConsumer<AjaxRequestTarget, Form<?>> onError)
	{
		Args.notNull(onSubmit, "onSubmit");
		Args.notNull(onError, "onError");

		return new AjaxSubmitLink(id)
		{
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

	public static <T> Link<T> link(String id, WicketConsumer<Void> onClick)
	{
		Args.notNull(onClick, "onClick");

		return new Link<T>(id)
		{
			@Override
			public void onClick()
			{
				onClick.accept((Void)null);
			}
		};
	}
}
