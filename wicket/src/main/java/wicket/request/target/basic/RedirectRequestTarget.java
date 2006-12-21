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
package wicket.request.target.basic;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.markup.html.pages.RedirectPage;

/**
 * A RequestTarget that will sent a redirect url to the browser. Use this if you 
 * want to direct the browser to some external URL, like Google etc, immediantly. 
 * or if you want to redirect to a Wicket page.
 * 
 * If you want to redirect with a delay the {@link RedirectPage} will do a meta
 * tag redirect with a delay.
 * 
 * @author jcompagner
 */
public class RedirectRequestTarget implements IRequestTarget
{

	private final String redirectUrl;

	/**
	 * Construct.
	 * @param redirectUrl
	 */
	public RedirectRequestTarget(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
		
	}
	
	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see wicket.IRequestTarget#getLock(wicket.RequestCycle)
	 */
	public Object getLock(RequestCycle requestCycle)
	{
		return null;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		requestCycle.getResponse().redirect(redirectUrl);
	}

}
