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
package org.apache.wicket.extensions.markup.html.basic;

/**
 * An implementation of <code>ILinkRenderStrategy</code> transforms a link target (e.g. email
 * address) into a proper html link (e.g. &lta href="mailto:..."&gt;...&lt;/a&gt;)
 * 
 * @author Gerolf Seitz
 */
@FunctionalInterface
public interface ILinkRenderStrategy
{
	/**
	 * Uses the <code>linkTarget</code> to build a proper html link.
	 * 
	 * @param linkTarget
	 *            the target of the link.
	 * @return an html link.
	 */
	String buildLink(String linkTarget);
}
