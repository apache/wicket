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
package org.apache.wicket.request.handler.render;

import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.Url;

public class ShouldRedirectToTargetUrl extends AbstractVariations
{

	private VariationIterator<Boolean> ajax = VariationIterator.of(Variation.ofBoolean());
	private VariationIterator<RenderPageRequestHandler.RedirectPolicy> redirectPolicy = VariationIterator.of(ajax, Variation.of(RenderPageRequestHandler.RedirectPolicy.class));
	private VariationIterator<Boolean> redirectToRender = VariationIterator.of(redirectPolicy, Variation.ofBoolean());
	private VariationIterator<Boolean> targetEqualsCurrentUrl = VariationIterator.of(redirectToRender, Variation.ofBoolean());
	private VariationIterator<Boolean> newPageInstance = VariationIterator.of(targetEqualsCurrentUrl, Variation.ofBoolean());
	private VariationIterator<Boolean> pageStateless = VariationIterator.of(newPageInstance, Variation.ofBoolean());
	private VariationIterator<Boolean> sessionTemporary = VariationIterator.of(pageStateless, Variation.ofBoolean());

	private VariationIterator<Boolean> last = sessionTemporary;

	@Override
	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		toString(sb,"ajax",ajax);
		toString(sb,"redirectPolicy",redirectPolicy);
		toString(sb,"redirectToRender",redirectToRender);
		toString(sb,"targetEqualsCurrentUrl",targetEqualsCurrentUrl);
		toString(sb,"newPageInstance",newPageInstance);
		toString(sb,"pageStateless",pageStateless);
		toString(sb,"sessionTemporary",sessionTemporary);
		return sb.toString();
	}

	@Override
	protected VariationIterator<?> last()
	{
		return last;
	}

	@Override
	public boolean getResult()
	{
		TestPageRenderer renderer = new TestPageRenderer(null);
		renderer.ajax = ajax.current();
		renderer.redirectPolicy = redirectPolicy.current();
		renderer.redirectToRender = redirectToRender.current();
		renderer.newPageInstance = newPageInstance.current();
		renderer.pageStateless = pageStateless.current();
		renderer.sessionTemporary = sessionTemporary.current();


		return renderer.shouldRedirectToTargetUrl(null, Url.parse("test1"),
			targetEqualsCurrentUrl.current() ? Url.parse("test1") : Url.parse("test2"));
	}
}
