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
package org.apache.wicket.ajax.effects;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

/**
 * Helper class to replace component in Ajax responses with JavaScript animation effects..
 */
public class Effects
{
	/**
	 * Replaces a component by using 'slideUp' effect to hide the component
	 * and 'slideDown' to show it.
	 *
	 * @param target
	 *          The Ajax request handler
	 * @param component
	 *          The component to re-render
	 */
	public static void replace(AjaxRequestTarget target, Component component)
	{
		replace(target, component, new SlideUp(), new SlideDown());
	}

	/**
	 * Replaces a component by using the provided effects to hide and show the component
	 *
	 * @param target
	 *          The Ajax request handler
	 * @param component
	 *          The component to re-render
	 * @param hide
	 *          The effect that will hide the old component
	 * @param show
	 *          The effect that will show the new component
	 */
	public static void replace(AjaxRequestTarget target, Component component, Effect hide, Effect show)
	{
		Args.notNull(target, "target");
		Args.notNull(component, "component");
		Args.notNull(hide, "hide");
		Args.notNull(show, "show");

		component.add(new DisplayNoneBehavior());

		String markupId = component.getMarkupId();
		target.prependJavaScript(hide.setComponentMarkupId(markupId));

		target.add(component);

		target.appendJavaScript(show.setComponentMarkupId(markupId));
	}

	/*
	 * Effects provided by jQuery
	 */

	public static class SlideUp extends Effect
	{
		public SlideUp()
		{
			super("slideUp");
		}

		public SlideUp(Duration duration)
		{
			super("slideUp", duration);
		}
	}

	public static class SlideDown extends Effect
	{
		public SlideDown()
		{
			super("slideDown");
		}

		public SlideDown(Duration duration)
		{
			super("slideDown", duration);
		}
	}

	public static class FadeIn extends Effect
	{
		public FadeIn()
		{
			super("fadeIn");
		}

		public FadeIn(Duration duration)
		{
			super("fadeIn", duration);
		}
	}

	public static class FadeOut extends Effect
	{
		public FadeOut()
		{
			super("fadeOut");
		}

		public FadeOut(Duration duration)
		{
			super("fadeOut", duration);
		}
	}
}
