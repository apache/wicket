/*
 * $Id$ $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.hangman;

import java.awt.Color;
import java.io.Serializable;

import wicket.Application;
import wicket.Resource;
import wicket.SharedResource;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.util.lang.Primitives;

/**
 * Model for a letter in the game of hangman
 * 
 * @author Jonathan Locke
 */
public class Letter implements Serializable
{
	/** True if the letter has been guessed */
	private boolean isGuessed;

	/** The letter */
	private char letter;

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
	 * @param application
	 *            Application where shared resources are stored
	 * @param enabled
	 *            True to get the enabled resource, false to get the disabled
	 *            resource
	 * @return Shared image resource
	 */
	public SharedResource getImage(final Application application, final boolean enabled)
	{
		// Lazy loading of shared resource
		final String sharedResourceName = asString() + (enabled ? "_enabled" : "_disabled");
		Resource resource = application.getResource(Application.class, sharedResourceName, null, null);
		if (resource == null)
		{
			DefaultButtonImageResource buttonResource = new DefaultButtonImageResource(30, 30,
					asString());
			if (!enabled)
			{
				buttonResource.setColor(Color.GRAY);
			}
			application.addResource(sharedResourceName, buttonResource);
			resource = buttonResource;
		}
		return new SharedResource(sharedResourceName);
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
	public boolean equals(final Object object)
	{
		if (object instanceof Letter)
		{
			Letter that = (Letter)object;
			return that.letter == this.letter && that.isGuessed == this.isGuessed;
		}
		return false;
	}

	/**
	 * Guess this letter
	 */
	public void guess()
	{
		this.isGuessed = true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return Primitives.hashCode(letter << (isGuessed ? 1 : 0));
	}

	/**
	 * @return Returns the isGuessed.
	 */
	public boolean isGuessed()
	{
		return isGuessed;
	}

	/**
	 * Resets this letter into the default state
	 */
	public void reset()
	{
		this.isGuessed = false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Letter letter = " + letter + ", guessed = " + isGuessed + "]";
	}
}
