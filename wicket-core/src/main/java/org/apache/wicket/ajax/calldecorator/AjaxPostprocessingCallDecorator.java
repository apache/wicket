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
package org.apache.wicket.ajax.calldecorator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;

/**
 * Ajax call decorator that decorates script after allowing the wrapped delegate decorator to
 * decorate it first.
 * 
 * @see IAjaxCallDecorator for notes on escaping quotes in scripts
 * 
 * @since 1.2
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxPostprocessingCallDecorator implements IAjaxCallDecorator, IAjaxCallDecoratorDelegate
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final IAjaxCallDecorator delegate;

	/**
	 * Construct.
	 * 
	 * @param delegate
	 *            wrapped delegate decorator
	 */
	public AjaxPostprocessingCallDecorator(IAjaxCallDecorator delegate)
	{
		this.delegate = delegate;
	}


	/** {@inheritDoc} */
	public final CharSequence decorateScript(Component component, CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateScript(component, script);
		return postDecorateScript(component, s);
	}

	/** {@inheritDoc} */
	public final CharSequence decorateOnSuccessScript(Component component, CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateOnSuccessScript(component,
			script);
		return postDecorateOnSuccessScript(component, s);
	}

	/** {@inheritDoc} */
	public final CharSequence decorateOnFailureScript(Component component, CharSequence script)
	{
		CharSequence s = (delegate == null) ? script : delegate.decorateOnFailureScript(component,
			script);
		return postDecorateOnFailureScript(component, s);
	}


	/**
	 * Decorates ajax call script
	 * 
	 * @param component
	 * @param script
	 * @return decorated script
	 */
	public CharSequence postDecorateScript(Component component, CharSequence script)
	{
		return script;
	}

	/**
	 * Decorates the success handling script
	 * 
	 * @param component
	 * @param script
	 * @return decorated script
	 */
	public CharSequence postDecorateOnSuccessScript(Component component, CharSequence script)
	{
		return script;
	}

	/**
	 * Decorates the failure handling script
	 * 
	 * @param component
	 * @param script
	 * @return decorated script
	 */
	public CharSequence postDecorateOnFailureScript(Component component, CharSequence script)
	{
		return script;
	}

	public IAjaxCallDecorator getDelegate()
	{
		return delegate;
	}
}
