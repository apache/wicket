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
package org.apache.wicket.examples.forminput;

import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.SharedResources;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResource;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;


/**
 * Application class for form input example.
 * 
 * @author Eelco Hillenius
 */
public class FormInputApplication extends WicketExampleApplication
{
	/** Relevant locales wrapped in a list. */
	public static final List<Locale> LOCALES = Arrays.asList(Locale.ENGLISH,
		new Locale("nl", "NL"), Locale.GERMAN, Locale.SIMPLIFIED_CHINESE, Locale.JAPANESE,
		new Locale("pt", "BR"), new Locale("fa", "IR"), new Locale("da", "DK"),
		new Locale("th", "TH"), new Locale("ru"), new Locale("ko", "KR"));

	@Override
	public Class<? extends Page> getHomePage()
	{
		return FormInput.class;
	}

	@Override
	public Session newSession(Request request, Response response)
	{
		WebSession session = new WebSession(request);
		Locale locale = session.getLocale();
		if (!LOCALES.contains(locale))
		{
			session.setLocale(Locale.ENGLISH);
		}
		return session;
	}

	@Override
	protected void init()
	{
		super.init();

		getResourceSettings().setThrowExceptionOnMissingResource(false);

		// Chinese buttons
		Font font = new Font("SimSun", Font.BOLD, 16);
		DefaultButtonImageResource imgSave = new DefaultButtonImageResource("\u4FDD\u5B58");
		imgSave.setFont(font);
		DefaultButtonImageResource imgReset = new DefaultButtonImageResource("\u91CD\u7F6E");
		imgReset.setFont(font);

		SharedResources sharedResources = getSharedResources();
		sharedResources.add("save", Locale.SIMPLIFIED_CHINESE, imgSave);
		sharedResources.add("reset", Locale.SIMPLIFIED_CHINESE, imgReset);

		// Japanese buttons
		Font fontJa = new Font("Serif", Font.BOLD, 16);
		DefaultButtonImageResource imgSaveJa = new DefaultButtonImageResource("\u4fdd\u5b58");
		imgSaveJa.setFont(fontJa);
		DefaultButtonImageResource imgResetJa = new DefaultButtonImageResource(
			"\u30ea\u30bb\u30c3\u30c8");
		imgResetJa.setFont(fontJa);
		sharedResources.add("save", Locale.JAPANESE, imgSaveJa);
		sharedResources.add("reset", Locale.JAPANESE, imgResetJa);

		// Persian buttons
		Font fontFa = new Font("Serif", Font.BOLD, 16);
		Locale farsi = new Locale("fa", "IR");
		DefaultButtonImageResource imgSaveFa = new DefaultButtonImageResource(
			"\u0630\u062e\u064a\u0631\u0647");
		imgSaveFa.setFont(fontFa);
		DefaultButtonImageResource imgResetFa = new DefaultButtonImageResource(
			"\u0628\u0627\u0632\u0646\u0634\u0627\u0646\u064a");
		imgResetFa.setFont(fontFa);
		getSharedResources().add("save", farsi, imgSaveFa);
		getSharedResources().add("reset", farsi, imgResetFa);

	}
}
