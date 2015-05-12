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
package org.apache.wicket.ajax.attributes;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IComponentAwareHeaderContributor;
import org.apache.wicket.util.string.Strings;

/**
 * An adapter for implementations of IAjaxCallListener.
 *
 * @since 6.0
 */
public class AjaxCallListener implements IAjaxCallListener, IComponentAwareHeaderContributor
{
	private StringBuilder init;
	private StringBuilder success;
	private StringBuilder failure;
	private StringBuilder before;
	private StringBuilder beforeSend;
	private StringBuilder after;
	private StringBuilder complete;
	private StringBuilder precondition;

	/**
	 * Sets the JavaScript code that will be returned by {@link #getInitHandler(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param init
	 * 			the JavaScript code for the corresponding handler
	 * @return This
	 */
	public AjaxCallListener onInit(final CharSequence init)
	{
		if (Strings.isEmpty(init) == false)
		{
			if (this.init == null)
			{
				this.init = new StringBuilder();
			}
			this.init.append(init);
		}
		return this;
	}

	/**
	 * Sets the JavaScript code that will be returned by {@link #getBeforeHandler(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param before
	 * 			the JavaScript code for the corresponding handler
	 * @return This
	 */
	public AjaxCallListener onBefore(final CharSequence before)
	{
		if (Strings.isEmpty(before) == false)
		{
			if (this.before == null)
			{
				this.before = new StringBuilder();
			}
			this.before.append(before);
		}
		return this;
	}

	/**
	 * Sets the JavaScript code that will be returned by {@link #getBeforeSendHandler(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param beforeSend
	 * 			the JavaScript code for the corresponding handler
	 * @return This
	 */
	public AjaxCallListener onBeforeSend(final CharSequence beforeSend)
	{
		if (Strings.isEmpty(beforeSend) == false)
		{
			if (this.beforeSend == null)
			{
				this.beforeSend = new StringBuilder();
			}
			this.beforeSend.append(beforeSend);
		}
		return this;
	}
	
	/**
	 * Sets the JavaScript code that will be returned by {@link #getAfterHandler(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param after
	 * 			the JavaScript code for the corresponding handler
	 * @return This
	 */
	public AjaxCallListener onAfter(final CharSequence after)
	{
		if (Strings.isEmpty(after) == false)
		{
			if (this.after == null)
			{
				this.after = new StringBuilder();
			}
			this.after.append(after);
		}
		return this;
	}
	
	/**
	 * Sets the JavaScript code that will be returned by {@link #getSuccessHandler(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param success
	 * 			the JavaScript code for the corresponding handler
	 * @return This
	 */
	public AjaxCallListener onSuccess(final CharSequence success)
	{
		if (Strings.isEmpty(success) == false)
		{
			if (this.success == null)
			{
				this.success = new StringBuilder();
			}
			this.success.append(success);
		}
		return this;
	}

	/**
	 * Sets the JavaScript code that will be returned by {@link #getFailureHandler(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param failure
	 * 			the JavaScript code for the corresponding handler
	 * @return This
	 */
	public AjaxCallListener onFailure(final CharSequence failure)
	{
		if (Strings.isEmpty(failure) == false)
		{
			if (this.failure == null)
			{
				this.failure = new StringBuilder();
			}
			this.failure.append(failure);
		}
		return this;
	}

	/**
	 * Sets the JavaScript code that will be returned by {@link #getCompleteHandler(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param complete
	 * 			the JavaScript code for the corresponding handler
	 * @return This
	 */
	public AjaxCallListener onComplete(final CharSequence complete)
	{
		if (Strings.isEmpty(complete) == false)
		{
			if (this.complete == null)
			{
				this.complete = new StringBuilder();
			}
			this.complete.append(complete);
		}
		return this;
	}
	
	/**
	 * Sets the JavaScript code that will be returned by {@link #getPrecondition(Component)}.
	 * If this code was already set, the new one will be appended to the existing one.
	 * 
	 * @param precondition
	 * 			the JavaScript code for the precondition
	 * @return This
	 */
	public AjaxCallListener onPrecondition(final CharSequence precondition)
	{
		if (Strings.isEmpty(precondition) == false)
		{
			if (this.precondition == null)
			{
				this.precondition = new StringBuilder();
			}
			this.precondition.append(precondition);
		}
		return this;
	}

	@Override
	public CharSequence getSuccessHandler(Component component)
	{
		return success;
	}

	@Override
	public CharSequence getFailureHandler(Component component)
	{
		return failure;
	}

	/**
	 * TODO Wicket 7: pull up into IAjaxCallListener
	 */
	public CharSequence getInitHandler(Component component)
	{
		return init;
	}

	@Override
	public CharSequence getBeforeHandler(Component component)
	{
		return before;
	}

	@Override
	public CharSequence getBeforeSendHandler(Component component)
	{
		return beforeSend;
	}

	@Override
	public CharSequence getAfterHandler(Component component)
	{
		return after;
	}

	@Override
	public CharSequence getCompleteHandler(Component component)
	{
		return complete;
	}

	@Override
	public CharSequence getPrecondition(Component component)
	{
		return precondition;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
	}
}
