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
package org.apache.wicket.request;

/**
 * An interface that a Url can implement if it knows how to
 * render itself as full url or relative to a base url
 */
public interface IUrlRenderer
{
	/**
	 * Renders the passed url as full/absolute.
	 *
	 * @param url
	 *      the url to render as full
	 * @param baseUrl
	 *      the url of the currently rendered page
	 * @return The full url.
	 */
	String renderFullUrl(final Url url, Url baseUrl);

	/**
	 * Renders the passed url as relative to a base url.
	 *
	 * @param url
	 *      the url to render as relative
	 * @param baseUrl
	 *      the url of the currently rendered page
	 * @return The relative url.
	 */
	String renderRelativeUrl(final Url url, Url baseUrl);
}
