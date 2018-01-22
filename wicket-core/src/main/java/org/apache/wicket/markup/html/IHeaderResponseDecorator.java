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
package org.apache.wicket.markup.html;

import org.apache.wicket.markup.head.IHeaderResponse;

/**
 * Setting an IHeaderResponseDecorator on an application allows you to wrap any {@link IHeaderResponse}
 * created by Wicket in a separate implementation that adds functionality to it when used by all
 * {@link IHeaderContributor} components or behaviors.
 * <p>
 * Everywhere that Wicket creates an instance of IHeaderResponse, it will call to your application
 * and give it the opportunity to decorate that IHeaderResponse before using it.
 * 
 * @see IHeaderResponse
 * @see DecoratingHeaderResponse
 * @author Jeremy Thomerson
 */
public interface IHeaderResponseDecorator
{

	/**
	 * The method that does the decorating of the IHeaderResponse.
	 * 
	 * @param response
	 *            the original response created by Wicket
	 * @return the response to be used by IHeaderContributors
	 */
	IHeaderResponse decorate(IHeaderResponse response);
}
