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
package org.apache.wicket.jmx.wrapper;

import java.io.UnsupportedEncodingException;

import org.apache.wicket.Application;
import org.apache.wicket.jmx.MarkupSettingsMBean;


/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class MarkupSettings implements MarkupSettingsMBean
{
	private final Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public MarkupSettings(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#getAutomaticLinking()
	 */
	@Override
	public boolean getAutomaticLinking()
	{
		return application.getMarkupSettings().getAutomaticLinking();
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#getCompressWhitespace()
	 */
	@Override
	public boolean getCompressWhitespace()
	{
		return application.getMarkupSettings().getCompressWhitespace();
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#getDefaultMarkupEncoding()
	 */
	@Override
	public String getDefaultMarkupEncoding()
	{
		return application.getMarkupSettings().getDefaultMarkupEncoding();
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#getStripComments()
	 */
	@Override
	public boolean getStripComments()
	{
		return application.getMarkupSettings().getStripComments();
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#getStripWicketTags()
	 */
	@Override
	public boolean getStripWicketTags()
	{
		return application.getMarkupSettings().getStripWicketTags();
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#setAutomaticLinking(boolean)
	 */
	@Override
	public void setAutomaticLinking(final boolean automaticLinking)
	{
		application.getMarkupSettings().setAutomaticLinking(automaticLinking);
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#setCompressWhitespace(boolean)
	 */
	@Override
	public void setCompressWhitespace(final boolean compressWhitespace)
	{
		application.getMarkupSettings().setCompressWhitespace(compressWhitespace);
	}

	/**
	 * @throws UnsupportedEncodingException
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#setDefaultMarkupEncoding(java.lang.String)
	 */
	@Override
	public void setDefaultMarkupEncoding(final String encoding) throws UnsupportedEncodingException
	{
		// test encoding is available
		"".getBytes(encoding);

		application.getMarkupSettings().setDefaultMarkupEncoding(encoding);
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#setStripComments(boolean)
	 */
	@Override
	public void setStripComments(final boolean stripComments)
	{
		application.getMarkupSettings().setStripComments(stripComments);
	}

	/**
	 * @see org.apache.wicket.jmx.MarkupSettingsMBean#setStripWicketTags(boolean)
	 */
	@Override
	public void setStripWicketTags(final boolean stripWicketTags)
	{
		application.getMarkupSettings().setStripWicketTags(stripWicketTags);
	}
}
