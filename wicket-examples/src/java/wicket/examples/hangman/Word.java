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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Model for a word in the game of Game.
 * 
 * @author Jonathan Locke
 */
public class Word
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
	 * @param letter
	 *            The letter to guess
	 * @return True if guess was correct
	 */
	public boolean guess(final Letter letter)
	{
		letter.guess();
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
		return correct;
	}

	/**
	 * @return True if the word has been guessed
	 */
	public boolean isGuessed()
	{
		for (Iterator iterator = letters.iterator(); iterator.hasNext();)
		{
			Letter letter = (Letter)iterator.next();
			if (!letter.isGuessed())
			{
				return false;
			}
		}
		return true;
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
			Letter letter = (Letter)iterator.next();
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
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[Word letters = " + letters + "]";
	}
}
