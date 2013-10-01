package org.apache.wicket.ajax.effects;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

public class Effects
{
	public static void replace(AjaxRequestTarget target, Component component)
	{
		replace(target, component, new SlideUp(), new SlideDown());
	}

	public static void replace(AjaxRequestTarget target, Component component, Effect in, Effect out)
	{
		Args.notNull(target, "target");
		Args.notNull(component, "component");
		Args.notNull(in, "in");
		Args.notNull(out, "out");

		component.add(new DisplayNoneBehavior());

		target.prependJavaScript(in.toJavaScript(component));

		target.add(component);

		target.appendJavaScript(out.toJavaScript(component));
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
