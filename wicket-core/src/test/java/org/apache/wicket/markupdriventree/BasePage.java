package org.apache.wicket.markupdriventree;

import org.apache.wicket.Auto;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markupdriventree.components.ComponentA;
import org.apache.wicket.markupdriventree.components.ComponentB;
import org.apache.wicket.markupdriventree.components.ComponentC;

public class BasePage extends WebPage {

	@Auto
	ComponentA a;
	
	@Auto
	ComponentB b;
	
	@Auto
	ComponentC c;
	
	public BasePage() 
	{
		super();

		a = new ComponentA("a");
		b = new ComponentB("b");
		c = new ComponentC("c");
	}
}
