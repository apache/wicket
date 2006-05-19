/*
 * $Id$ $Revision$ $Date:
 * 2005-03-31 15:26:49 +0200 (Do, 31 Mrz 2005) $
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Model for a word in the game of Game.
 * 
 * @author Jonathan Locke
 */
public class Word implements Serializable
{
	/** The word */
	final List letters = new ArrayList();

	/**
	 * Constructor
	 * 
	 * @param word
	 *            The word
	 */
	public Word(final String word)
	{
		for (int i = 0; i < word.length(); i++)
		{
			letters.add(new Letter(word.charAt(i)));
		}
	}

	/**
	 * @return This word
	 */
	public String asString()
	{
		return asString(false);
	}

	/**
	 * @param hideUnguessed
	 *            True if unguessed letters should be hidden
	 * @return This word as a String
	 */
	public String asString(final boolean hideUnguessed)
	{
		final StringBuffer buffer = new StringBuffer();
		for (Iterator iterator = letters.iterator(); iterator.hasNext();)
		{
			final Letter letter = (Letter)iterator.next();
			if (hideUnguessed)
			{
				buffer.append(letter.isGuessed() ? letter.asString() : "_");
			}
			else
			{
				buffer.append(letter.asString());
			}
		}
		return buffer.toString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object object)
	{
		if (object instanceof Word)
		{
			final Word that = (Word)object;
			return this.asString().equalsIgnoreCase(that.asString());
		}
		return false;
	}

	/**
	 * @param letter
	 *            The letter to guess
	 * @return True if guess was correct
	 */
	public boolean guess(final Letter letter)
	{
		boolean correct = false;
		for (Iterator iterator = letters.iterator(); iterator.hasNext();)
		{
			final Letter current = (Letter)iterator.next();
			if (current.equals(letter))
			{
				current.guess();
				correct = true;
			}
		}
		letter.guess();
		return correct;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return asString().hashCode();
	}

	/**
	 * @return True if the word has been guessed
	 */
	public boolean isGuessed()
	{
		for (final Iterator iterator = letters.iterator(); iterator.hasNext();)
		{
			final Letter letter = (Letter)iterator.next();
			if (!letter.isGuessed())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Word letters = " + letters + "]";
	}
}
