package wicked;

import java.io.OutputStream;

import wicked.markup.ComponentFragment;
import wicked.markup.Markup;

public class Page extends Container {
	private static int idCounter=0;
	
	public Page() {
		super(String.valueOf(idCounter++));
	}
	
	@Override
	public ComponentFragment getFragment(String markupRelativePath) {
		Markup markup=getMarkup();
		return markup.getComponentFragment(markupRelativePath);
	}
	
	@Override
	public void render(OutputStream stream) {
		renderBody(stream, getMarkup().getFragments());
	}
	
	@Override
	protected Markup getMarkup() {
		return Application.get().getMarkup(this);
	}
	
	@Override
	String getMarkupRelativePath(String path) {
		return path;
	}
}
