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
package org.apache.wicket.markup.head;

/**
 * A {@link org.apache.wicket.markup.head.HeaderItem} that supports <em>async</em>,
 * <em>defer</em> and <em>charset</em> attributes
 */
public abstract class AbstractJavaScriptReferenceHeaderItem extends JavaScriptHeaderItem
{
	private boolean async;
	private boolean defer;
	private String charset;

	/**
	 * Constructor.
	 *
	 * @param condition
	 *              The condition to use for Internet Explorer conditional comments. E.g. "IE 7".
	 *              {@code null} or empty string for no condition.
	 * @param defer
	 *              a flag indicating whether the execution of a script should be deferred (delayed)
	 *              until after the page has been loaded.
	 * @param charset
	 *              the charset to use when reading the script content
	 */
	protected AbstractJavaScriptReferenceHeaderItem(String condition, boolean defer, String charset)
	{
		super(condition);
		this.defer = defer;
		this.charset = charset;
	}

	/**
	 * @return if the script should be loaded and executed asynchronously
	 */
	public boolean isAsync()
	{
		return async;
	}

	public AbstractJavaScriptReferenceHeaderItem setAsync(boolean async)
	{
		this.async = async;
		return this;
	}

	/**
	 * @return if the execution of a script should be deferred (delayed) until after the page has been loaded.
	 */
	public boolean isDefer()
	{
		return defer;
	}

	public AbstractJavaScriptReferenceHeaderItem setDefer(boolean defer)
	{
		this.defer = defer;
		return this;
	}

	/**
	 * @return the optional value of the charset attribute of the script tag
	 */
	public String getCharset()
	{
		return charset;
	}

	public AbstractJavaScriptReferenceHeaderItem setCharset(String charset)
	{
		this.charset = charset;
		return this;
	}
}
