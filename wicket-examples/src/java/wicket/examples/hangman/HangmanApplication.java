/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.examples.hangman;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import wicket.ISessionFactory;
import wicket.Session;
import wicket.examples.WicketExampleApplication;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.markup.html.image.resource.ImageResource;

/**
 * Class defining the main Game application.
 * 
 * @author Chris Turner
 * @author Jonathan Locke
 */
public class HangmanApplication extends WicketExampleApplication
{
	/** Map from letter to image */
	private final Map letterToImage = new HashMap();

	/**
	 * Create the hangman application.
	 */
	public HangmanApplication()
	{
		getPages().setHomePage(Home.class);
	}

	/**
	 * @param letter
	 *            The letter
	 * @return The image resource for the letter
	 */
	public ImageResource imageForLetter(final Letter letter)
	{
		DefaultButtonImageResource image = (DefaultButtonImageResource)letterToImage.get(letter);
		if (image == null)
		{
			image = new DefaultButtonImageResource(30, 30, letter.asString());
			if (letter.isGuessed())
			{
				image.setColor(Color.GRAY);
			}
			letterToImage.put(letter, image);
		}
		return image;
	}

	/**
	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
	 */
	public ISessionFactory getSessionFactory()
	{
		return new ISessionFactory()
		{
			public Session newSession()
			{
				return new HangmanSession(HangmanApplication.this);
			}
		};
	}
}
