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
import wicket.Page;
import wicket.RequestCycle;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebPage;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.protocol.http.WebRequest;
import wicket.request.target.PageRequestTarget;

/**
 * Body frame page for the frames example.
 * 
 * @author Eelco Hillenius
 */
public class BodyFrame extends WebPage
{
	private final FrameTarget frameTarget = new FrameTarget(Page1.class.getName());

	/**
	 * Constructor
	 */
	public BodyFrame()
	{
		LeftFrame leftFrame = new LeftFrame(this);

		String leftFrameSrc = urlForNotYetRenderedPage(leftFrame);

		WebComponent leftFrameTag = new WebComponent("leftFrame");
		leftFrameTag.add(new AttributeModifier("src", new Model(leftFrameSrc)));
		add(leftFrameTag);

		WebComponent rightFrameTag = new WebComponent("rightFrame");
		IModel rightFrameModel = new Model()
		{
			public Object getObject(Component component)
			{
				WebRequest webRequest = (WebRequest)getRequest();
				String pathTo = webRequest.getContextPath() + webRequest.getServletPath();
				String url = pathTo + "?bookmarkablePage=" + frameTarget.getFrameClass()
						+ "&pagemap=rightFrame";
				return url;
			}
		};
		rightFrameTag.add(new AttributeModifier("src", rightFrameModel));
		add(rightFrameTag);
	}

	/**
	 * This is a trick to get the URL for the given page without having the need
	 * to render that page first. NOT RECOMMENDED FOR NORMAL USE, AS
	 * {@link wicket.Session#touch(Page)} IS A INTERNAL METHOD.
	 * 
	 * @param page
	 *            the page to get the url to
	 * @return the url to the page
	 */
	private String urlForNotYetRenderedPage(Page page)
	{
		// add the page to the session so that it will be available for
		// rendering
		getSession().touch(page);
		RequestCycle requestCycle = getRequestCycle();
		return requestCycle.getProcessor().getRequestCodingStrategy().encode(requestCycle,
				new PageRequestTarget(page));
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
	public boolean isVersioned()
	{
		return false;
	}
}