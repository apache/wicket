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
package org.apache.wicket.util.value;

import org.apache.wicket.util.lang.Args;

/**
 * Some common header item attributes.
 * This is not a complete list of all possible attributes.
 */
public enum HeaderItemAttribute implements IAttributeMapKey
{
	ID("id"),
	TYPE("type"),
	// script (JavaScript) attributes see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script
	SCRIPT_SRC("src"),
	SCRIPT_DEFER("defer"),
	SCRIPT_ASYNC("async"),
	SCRIPT_NOMODULE("nomodule"),
	SCRIPT_REFERRERPOLICY("referrerpolicy"),
	// link (CSS) attributes
	LINK_HREF("href"),
	LINK_MEDIA("media"),
	LINK_REL("rel"),
	// Content Security Policy attributes, see https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
	// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/script-src
	// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/style-src
	CSP_NONCE("nonce"),
	// SRI Subresource integrity attributes, see https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity
	SRI_INTEGRITY("integrity"),
	SRI_CROSSORIGIN("crossorigin");
	private String name;

	HeaderItemAttribute(String name)
	{
		Args.notNull(name, "name");
		this.name = name;
	}

	public String getAttributeKey()
	{
		return name;
	}
}
