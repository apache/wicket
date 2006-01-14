package wicked;

import wicked.component.Label;

public class TestPage extends Page {
	public TestPage() {
		Label label=new Label(this, "feedback", "hello there i am label 1");
		new TestPanel(this, "testpanel");
		
		
		Container container1=new Container(this, "container1");
		new Label(container1, "label", "label in container1");
		new TestPanel3(container1, "panel3");
	}
}
