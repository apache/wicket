package wicked;

import java.io.OutputStream;
import java.util.Iterator;

/** repeater allows its immediate children to reuse its fragment */
public class Repeater extends Container {

	public Repeater(Container parent, String id) {
		super(parent, id);
	}

	@Override
	public void render(OutputStream stream) {
		Iterator<Component> it=getChildren();
		while (it.hasNext()) {
			Component child=it.next();
			child.render(stream);
		}
	}
	
	
	@Override
	protected String getMarkupRelativePath(String path) {
		// we cut out the immediate child's id out of the markup relative path which will
		// cause repeater's fragment to be reused instead

		String truePath = getParent().getMarkupRelativePath(path);
		
		String myPath=super.getMarkupRelativePath(getPath());
		
		String newPath=myPath;
		int divider=truePath.indexOf(":", newPath.length()+1);
		if (divider>=0) {
			newPath=newPath+truePath.substring(divider, truePath.length()-1);
		}
		
		return newPath;
	}
	

}
