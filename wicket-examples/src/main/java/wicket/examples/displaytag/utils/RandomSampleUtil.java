/*
 * $Id: RandomSampleUtil.java 5394 2006-04-16 13:36:52 +0000 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 13:36:52 +0000 (Sun, 16 Apr
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.displaytag.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Utility class used to get random word and sentences used in examples.
 * 
 * @author fgiust (wicket.examples.wicket.examples.displaytag)
 */
public final class RandomSampleUtil
{

	/**
	 * list of words.
	 */
	private static final String[] words = new String[] { "Lorem", "ipsum", "dolor", "sit", "amet",
			"consetetur", "sadipscing", "elitr", "sed", "diam", "nonumy", "eirmod", "tempor",
			"invidunt", "ut", "labore", "et", "dolore", "magna", "aliquyam", "erat", "sed", "diam",
			"voluptua", "At", "vero", "eos", "et", "accusam", "et", "justo", "duo", "dolores",
			"et", "ea", "rebum", "Stet", "clita", "kasd", "gubergren", "no", "sea", "takimata",
			"sanctus", "est" };

	/**
	 * random number producer.
	 */
	private static final Random random = new Random();

	/**
	 * utility class, don't instantiate.
	 */
	private RandomSampleUtil()
	{
		super();
	}

	/**
	 * returns a random word.
	 * 
	 * @return random word
	 */
	public static String getRandomWord()
	{
		return words[random.nextInt(words.length)];
	}

	/**
	 * returns a random sentence.
	 * 
	 * @param wordNumber
	 *            number of word in the sentence
	 * @return random sentence made of <code>wordNumber</code> words
	 */
	public static String getRandomSentence(final int wordNumber)
	{
		StringBuffer buffer = new StringBuffer(wordNumber * 12);

		int j = 0;
		while (j < wordNumber)
		{
			buffer.append(getRandomWord());
			buffer.append(" ");
			j++;
		}
		return buffer.toString();
	}

	/**
	 * returns a random email.
	 * 
	 * @return random email
	 */
	public static String getRandomEmail()
	{
		return getRandomWord() + "@" + getRandomWord() + ".com";
	}

	/**
	 * Returns a random date.
	 * 
	 * @return random date
	 */
	public static Date getRandomDate()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 365 - random.nextInt(730));
		return calendar.getTime();
	}

	/**
	 * Returns a random truth value.
	 * 
	 * @return Random boolean
	 */
	public static boolean getRandomBoolean()
	{
		return random.nextBoolean();
	}
}
