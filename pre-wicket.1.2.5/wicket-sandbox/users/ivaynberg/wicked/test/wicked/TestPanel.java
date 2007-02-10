package wicked;

import wicked.component.Label;

public class TestPanel extends Panel {

	public TestPanel(Container parent, String id) {
		super(parent, id);
		new Label(this, "label1", "this is label 1");
		new Label(this, "label2", "this is label 2");
		new TestPanel2(this, "testpanel2");
	}

}
