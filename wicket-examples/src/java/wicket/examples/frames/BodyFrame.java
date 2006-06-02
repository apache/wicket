/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.frames;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.RequestCycle;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebPage;
import wicket.model.Model;
import wicket.request.IRequestCodingStrategy;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.PageRequestTarget;

/**
 * Body frame page for the frames example.
 * 
 * @author Eelco Hillenius
 */
public class BodyFrame extends WebPage
{
	private final FrameTarget frameTarget = new FrameTarget(Page1.class);

	/**
	 * Model that returns the url to the bookmarkable page that is set in the
	 * current frame target.
	 */
	private final class FrameModel extends Model
	{
		/**
		 * @see wicket.model.Model#getObject(wicket.Component)
		 */
		@Override
		public Object getObject(Component component)
		{
			RequestCycle cycle = getRequestCycle();
			IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
			return encoder.encode(cycle, new BookmarkablePageRequestTarget("rightFrame",
					frameTarget.getFrameClass()));
		}
	}

	/**
	 * Constructor
	 */
	public BodyFrame()
	{
		RequestCycle cycle = getRequestCycle();

		// create a new page instance, passing this 'master page' as an argument
		LeftFrame leftFrame = new LeftFrame(this);
		// get the url to that page
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		CharSequence leftFrameSrc = encoder.encode(cycle, new PageRequestTarget(leftFrame));
		// and create a simple component that modifies it's src attribute to
		// hold the url to that frame
		WebComponent leftFrameTag = new WebComponent(this, "leftFrame");
		leftFrameTag.add(new AttributeModifier("src", new Model<CharSequence>(leftFrameSrc)));

		// make a simple component for the right frame tag
		WebComponent rightFrameTag = new WebComponent(this, "rightFrame");
		// and this time, set a model which retrieves the url to the currently
		// set frame class in the frame target
		rightFrameTag.add(new AttributeModifier("src", new FrameModel()));
	}

	/**
	 * Gets frameTarget.
	 * 
	 * @return frameTarget
	 */
	public FrameTarget getFrameTarget()
	{
		return frameTarget;
	}

	/**
	 * @see wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}