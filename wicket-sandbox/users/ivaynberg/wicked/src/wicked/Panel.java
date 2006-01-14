package wicked;

import wicked.markup.ComponentFragment;
import wicked.markup.Markup;

public class Panel extends Container {

	public Panel(Container parent, String id) {
		super(parent, id);
	}

	@Override
	public ComponentFragment getFragment() {
		Markup pmarkup=getParent().getMarkup();
		String path=getParent().getMarkupRelativePath(getPath());
		ComponentFragment outside=pmarkup.getComponentFragment(path);
		ComponentFragment panel=new ComponentFragment(outside.getTag());
		
		Markup markup=Application.get().getMarkup(this);
		panel.setFragments(markup.getFragments());
		
		return panel;
	}
	
	@Override
	protected ComponentFragment getFragment(String path) {
		String panelRelativePath=path.substring(getPath().length()+1);
		return getMarkup().getComponentFragment(panelRelativePath);
	}
	
	@Override
	protected Markup getMarkup() {
		Markup markup=Application.get().getMarkup(this);
		return markup;
	}
	
	@Override
	protected String getMarkupRelativePath(String path) {
		return path.substring(getPath().length()+1);
	}
	
}
