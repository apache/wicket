package wicked.component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import wicked.Container;
import wicked.markup.Fragment;

public class Label extends Container {
	
	private String label;
	
	
	public Label(Container parent, String id, String label) {
		super(parent, id);
		this.label=label;
	}

	
	@Override
	public void renderBody(OutputStream stream, List<Fragment> fragments) {
		try {
			stream.write(label.getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
