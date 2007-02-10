package wicked;

import wicked.component.Label;

public class TestPanel3 extends Panel {

	public TestPanel3(Container parent, String id) {
		super(parent, id);
		new Label(this, "label1", "this is label 1 in TestPanel3");
	}

}
