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

public class ShouldRenderPageAndWriteResponseVariations extends AbstractVariations
{
	VariationIterator<RenderPageRequestHandler.RedirectPolicy> redirectPolicy = VariationIterator.of(Variation.of(RenderPageRequestHandler.RedirectPolicy.class));
	VariationIterator<Boolean> ajax = VariationIterator.of(redirectPolicy, Variation.ofBoolean());
	VariationIterator<Boolean> onePassRender = VariationIterator.of(ajax,Variation.ofBoolean());
	VariationIterator<Boolean> redirectToRender = VariationIterator.of(onePassRender,Variation.ofBoolean());
	VariationIterator<Boolean> shouldPreserveClientUrl = VariationIterator.of(redirectToRender,Variation.ofBoolean());
	VariationIterator<Boolean> targetEqualsCurrentUrl = VariationIterator.of(shouldPreserveClientUrl,Variation.ofBoolean());
	VariationIterator<Boolean> newPageInstance = VariationIterator.of(targetEqualsCurrentUrl,Variation.ofBoolean());
	VariationIterator<Boolean> pageStateless = VariationIterator.of(newPageInstance,Variation.ofBoolean());

	VariationIterator<Boolean> last = pageStateless;

	@Override
	protected VariationIterator<?> last()
	{
		return last;
	}

	public boolean getResult()
	{
		TestPageRenderer renderer = new TestPageRenderer(null);
		renderer.ajax = ajax.current();
		renderer.onePassRender = onePassRender.current();
		renderer.redirectToRender = redirectToRender.current();
		renderer.redirectPolicy = redirectPolicy.current();
		renderer.shouldPreserveClientUrl = shouldPreserveClientUrl.current();
		renderer.newPageInstance = newPageInstance.current();
		renderer.pageStateless = pageStateless.current();

		return renderer.shouldRenderPageAndWriteResponse(null, Url.parse("test"),
			targetEqualsCurrentUrl.current() ? Url.parse("test") : Url.parse("test2"));
	}

	@Override
	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		toString(sb,"ajax",ajax);
		toString(sb,"onePassRender",onePassRender);
		toString(sb,"redirectToRender",redirectToRender);
		toString(sb,"redirectPolicy",redirectPolicy);
		toString(sb,"shouldPreserveClientUrl",shouldPreserveClientUrl);
		toString(sb,"targetEqualsCurrentUrl",targetEqualsCurrentUrl);
		toString(sb,"newPageInstance",newPageInstance);
		toString(sb,"pageStateless",pageStateless);
		return sb.toString();
	}


}
