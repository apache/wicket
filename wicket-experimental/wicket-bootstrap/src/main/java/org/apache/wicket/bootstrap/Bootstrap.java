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
package org.apache.wicket.bootstrap;

import java.util.Arrays;

import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Bootstrap extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	private static final Bootstrap instance = new Bootstrap();

	public static Bootstrap get() {
		return instance;
	}

	public static void renderHead(IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(Bootstrap.get()));
	}

	private Bootstrap() {
		super(Bootstrap.class, "js/bootstrap.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(JavaScriptHeaderItem
				.forReference(WicketEventJQueryResourceReference.get()),
				CssHeaderItem.forReference(new CssResourceReference(
						Bootstrap.class, "css/bootstrap.css")), CssHeaderItem
						.forReference(new CssResourceReference(Bootstrap.class,
								"css/bootstrap-responsive.css")));
	}
}
