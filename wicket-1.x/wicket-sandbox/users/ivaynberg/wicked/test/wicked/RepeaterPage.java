package wicked;

import wicked.component.Label;

public class RepeaterPage extends Page {
	
	public RepeaterPage() {
		Repeater repeater=new Repeater(this, "repeater1");
		new Label(repeater, "0", "repeater1 label");
		new TestPanel3(repeater, "1");
	}
	
}
