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

import java.util.Objects;

import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.html.CrossOrigin;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.value.AttributeMap;

/**
 * A {@link org.apache.wicket.markup.head.HeaderItem} that renders a JavaScript reference.
 */
public abstract class AbstractJavaScriptReferenceHeaderItem extends JavaScriptHeaderItem implements ISubresourceHeaderItem
{
	private boolean async;
	private boolean defer;
	private String charset;
	private CrossOrigin crossOrigin;
	private String integrity;

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

	@Override
	public CrossOrigin getCrossOrigin()
	{
		return crossOrigin;
	}
	
	@Override
	public AbstractJavaScriptReferenceHeaderItem setCrossOrigin(CrossOrigin crossOrigin)
	{
		this.crossOrigin = crossOrigin;
		return this;
	}
	
	@Override
	public String getIntegrity()
	{
		return integrity;
	}
	
	@Override
	public AbstractJavaScriptReferenceHeaderItem setIntegrity(String integrity)
	{
		this.integrity = integrity;
		return this;
	}

	protected final void internalRenderJavaScriptReference(Response response, String url)
	{
		Args.notEmpty(url, "url");

		AttributeMap attributes = new AttributeMap();
		attributes.putAttribute(JavaScriptUtils.ATTR_TYPE, "text/javascript");
		attributes.putAttribute(JavaScriptUtils.ATTR_ID, getId());
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_DEFER, defer);
		// XXX this attribute is not necessary for modern browsers
		attributes.putAttribute("charset", charset);
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_ASYNC, async);
		attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_SRC, url);
		attributes.putAttribute(JavaScriptUtils.ATTR_CSP_NONCE, getNonce());
		attributes.putAttribute(JavaScriptUtils.ATTR_CROSS_ORIGIN, getCrossOrigin() == null ? null : getCrossOrigin().getRealName());
		attributes.putAttribute(JavaScriptUtils.ATTR_INTEGRITY, getIntegrity());
		JavaScriptUtils.writeScript(response, attributes);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractJavaScriptReferenceHeaderItem that = (AbstractJavaScriptReferenceHeaderItem) o;
		return async == that.async &&
				defer == that.defer &&
				Objects.equals(charset, that.charset);
	}

	@Override
	public int hashCode() {
		// Not using `Objects.hash` for performance reasons
		int result = super.hashCode();
		result = 31 * result + (async ? 1 : 0);
		result = 31 * result + (defer ? 1 : 0);
		result = 31 * result + (charset != null ? charset.hashCode() : 0);
		return result;
	}
}
