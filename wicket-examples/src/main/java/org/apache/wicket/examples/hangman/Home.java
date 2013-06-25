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
package org.apache.wicket.examples.hangman;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * The home page for the hangman application. Contains just a single hyperlink to the actual guess
 * page that runs the game.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class Home extends HangmanPage
{
	/**
	 * Create the home page.
	 * 
	 * @param parameters
	 *            The parameters for the page (not used)
	 */
	public Home(final PageParameters parameters)
	{
		getSession().bind();
		final String word = parameters.get("word").toOptionalString();
		if (word == null)
		{
			getGame().newGame(5, new WordGenerator());
		}
		else
		{
			getGame().newGame(5, new WordGenerator(new String[] { word }));
		}
		add(new BookmarkablePageLink<>("start", Guess.class));
	}
}
