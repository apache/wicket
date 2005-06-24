package wicket.extensions.markup.html.navmenu.style.tabs;

import wicket.extensions.markup.html.navmenu.MenuRowStyle;
import wicket.markup.html.StaticResourceReference;

/**
 * Style that renders tabs.
 */
public final class TabsStyle
{
	/** the first level (level 0) of this style. */
	public static final MenuRowStyle LEVEL_0 = new Level0();

	/** the second level (level 1) of this style. */
	public static final MenuRowStyle LEVEL_1 = new Level1();

	/**
	 * Class for the first level (level 0) of this style.
	 */
	private final static class Level0 extends MenuRowStyle
	{
		static
		{
			new StaticResourceReference(TabsStyle.class, "folders/active.gif");
			new StaticResourceReference(TabsStyle.class, "folders/hover.gif");
			new StaticResourceReference(TabsStyle.class, "folders/selected.gif");
			new StaticResourceReference(TabsStyle.class, "folders/unselected.gif");
		}

		/**
		 * Construct.
		 */
		private Level0()
		{
			setStyleSheetResource(new StaticResourceReference(TabsStyle.class, "tabs-folders.css"));
			setCssClass("wicket-navmenu-tabs-level0");
		}
	}

	/**
	 * Class for the second level (level 1) of this style.
	 */
	private final static class Level1 extends MenuRowStyle
	{
		/**
		 * Construct.
		 */
		private Level1()
		{
			setStyleSheetResource(new StaticResourceReference(TabsStyle.class, "tabs-folders.css"));
			setCssClass("wicket-navmenu-tabs-level1");
		}
	}

	/**
	 * Hidden constructor; the public constants should be used instead.
	 */
	private TabsStyle()
	{
		super();
	}
}