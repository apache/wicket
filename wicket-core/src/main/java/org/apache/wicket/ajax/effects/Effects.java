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

		target.prependJavaScript(hide.toJavaScript(component));

		target.add(component);

		target.appendJavaScript(show.toJavaScript(component));
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
