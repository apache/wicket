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
package org.apache.wicket.examples.frames;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageMap;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * Body frame page for the frames example.
 * 
 * @author Eelco Hillenius
 */
public class BodyFrame extends WebPage<Void>
{
	/**
	 * Model that returns the url to the bookmarkable page that is set in the current frame target.
	 */
	private final class FrameModel implements IModel<CharSequence>
	{
		/**
		 * @see org.apache.wicket.model.IModel#getObject()
		 */
		public CharSequence getObject()
		{
			return RequestCycle.get().urlFor(PageMap.forName(RIGHT_FRAME_NAME),
				frameTarget.getFrameClass(), null);
		}

		/**
		 * @see org.apache.wicket.model.IModel#setObject(java.lang.Object)
		 */
		public void setObject(final CharSequence object)
		{
		}

		/**
		 * @see org.apache.wicket.model.IDetachable#detach()
		 */
		public void detach()
		{
		}
	}

	/** name for page map etc. */
	public static final String RIGHT_FRAME_NAME = "right";

	private final FrameTarget frameTarget = new FrameTarget(Page1.class);

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public BodyFrame()
	{
		// create a new page instance, passing this 'master page' as an argument
		LeftFrame leftFrame = new LeftFrame(this);
		// get the url to that page
		CharSequence leftFrameSrc = RequestCycle.get().urlFor(leftFrame);
		// and create a simple component that modifies it's src attribute to
		// hold the url to that frame
		WebComponent<?> leftFrameTag = new WebComponent<Void>("leftFrame");
		leftFrameTag.add(new AttributeModifier("src", new Model((Serializable)leftFrameSrc)));
		add(leftFrameTag);

		// make a simple component for the right frame tag
		WebComponent<?> rightFrameTag = new WebComponent<Void>("rightFrame");
		// and this time, set a model which retrieves the url to the currently
		// set frame class in the frame target
		rightFrameTag.add(new AttributeModifier("src", new FrameModel()));
		add(rightFrameTag);
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
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}