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

import java.awt.Color;

import org.apache.wicket.markup.html.image.resource.DefaultButtonImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Primitives;


/**
 * Model for a letter in the game of hangman
 * 
 * @author Jonathan Locke
 */
public class Letter implements IClusterable
{
	/** True if the letter has been guessed */
	private boolean guessed;

	/** The letter */
	private final char letter;

	/**
	 * Constructor
	 * 
	 * @param letter
	 *            The letter
	 */
	public Letter(final char letter)
	{
		this.letter = letter;
	}

	/**
	 * @return This letter as a string
	 */
	public String asString()
	{
		return Character.toString(letter);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof Letter)
		{
			final Letter that = (Letter)object;
			return that.letter == letter && that.guessed == guessed;
		}
		return false;
	}

	static String PARAMETER_GUESSED = "guessed";
	static String PARAMETER_LETTER = "letter";

	static ResourceReference LETTER_RESOURCE_REFERENCE = new ResourceReference(Letter.class,
		"letter")
	{
		/**
		 * @see org.apache.wicket.request.resource.ResourceReference#getResource()
		 */
		@Override
		public IResource getResource()
		{
			return new ButtonResource();
		}
	};

	/**
	 * Simple resource implementation that checks for "guessed" parameter and delegates to
	 * {@link DefaultButtonImageResource}.
	 * 
	 * @author Matej Knopp
	 */
	private static class ButtonResource implements IResource
	{
		/**
		 * @see org.apache.wicket.request.resource.IResource#respond(org.apache.wicket.request.resource.IResource.Attributes)
		 */
		public void respond(Attributes attributes)
		{
			// request parameter for the resource
			boolean guessed = attributes.getParameters()
				.get(PARAMETER_GUESSED)
				.toBoolean(false);
			String letter = attributes.getParameters()
				.get(PARAMETER_LETTER)
				.toString();

			// delegate to another resource
			DefaultButtonImageResource buttonResource = new DefaultButtonImageResource(30, 30,
				letter);

			if (guessed)
			{
				buttonResource.setColor(Color.GRAY);
			}
			buttonResource.respond(attributes);
		}
	}

	/**
	 * Guess this letter
	 */
	public void guess()
	{
		guessed = true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Primitives.hashCode(letter << (guessed ? 1 : 0));
	}

	/**
	 * @return Returns the isGuessed.
	 */
	public boolean isGuessed()
	{
		return guessed;
	}

	/**
	 * Resets this letter into the default state
	 */
	public void reset()
	{
		guessed = false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[Letter letter = " + letter + ", guessed = " + guessed + "]";
	}
}
