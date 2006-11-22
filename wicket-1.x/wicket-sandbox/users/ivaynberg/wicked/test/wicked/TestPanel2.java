package wicked;

import wicked.component.Label;

public class TestPanel2 extends Panel {

	public TestPanel2(Container parent, String id) {
		super(parent, id);
		new Label(this, "label1", "this is label 1 in TestPanel2");
		new TestPanel3(this, "testpanel3");
		new TestPanel3(this, "testpanel3-2");
	}

}
