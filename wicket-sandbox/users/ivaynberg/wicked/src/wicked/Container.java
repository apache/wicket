package wicked;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wicked.markup.ComponentFragment;
import wicked.markup.Fragment;
import wicked.markup.Tag;

public class Container extends Component {

	Container(String id) {
		super(id);
	}

	public Container(Container parent, String id) {
		super(parent, id);
	}

	private Map<String, Component> idToComponent = new HashMap<String, Component>();

	void add(Component c, String id) {
		if (idToComponent.containsKey(id)) {
			throw new IllegalArgumentException("id already exists in container");
		}
		// DEBUG CHECK TO MAKE SURE MARKUP IS PRESENT
		c.getFragment();

		idToComponent.put(id, c);
	}

	@Override
	public void checkTag() {
	}

	public void render(OutputStream stream) {
		ComponentFragment frag = getFragment();
		Tag tag = frag.getTag();

		try {
			stream.write("<".getBytes());
			stream.write(tag.getName().getBytes());
			for (Map.Entry<String, String> attr : tag.getAttributes()
					.entrySet()) {
				stream.write(" ".getBytes());
				stream.write(attr.getKey().getBytes());
				stream.write("=\"".getBytes());
				stream.write(attr.getValue().getBytes());
				stream.write("\"".getBytes());
			}

			if (frag.getFragments() == null) {
				stream.write("/>".getBytes());
			} else {
				stream.write(">".getBytes());
				renderBody(stream, frag.getFragments());
				stream.write("</".getBytes());
				stream.write(tag.getName().getBytes());
				stream.write(">".getBytes());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void renderBody(OutputStream stream, List<Fragment> fragments) {
		if (fragments != null) {

			for (Fragment f : fragments) {
				switch (f.getType()) {
				case STATIC:
					try {
							stream.write(f.toString().getBytes());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					break;
				case COMPONENT:
					ComponentFragment cf=(ComponentFragment) f;
					Component comp=get(cf.getId());
					comp.render(stream);
					break;
				}
			}

		}
	}
	
	@Override
	public Component get(String path) {
		if (path==null||path.length()==0) {
			return this;
		}
		
		int col=path.indexOf(":");
		if (col<0) {
			return idToComponent.get(path); 
		}
		String first=path.substring(0, col);
		String rest=path.substring(col+1);
		
		Component next=idToComponent.get(first);
		return next.get(rest);
	}
	
}
