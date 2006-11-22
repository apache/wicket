package wicked;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import wicked.markup.ComponentFragment;
import wicked.markup.Markup;
import wicked.markup.Tag;

public class Component {

	private Container parent;

	private String id;

	// needed for end case(the page), there might be a cleaner way
	Component(String id) {
		this.id = id;
	}

	public Component(Container parent, String id) {
		if (parent == null || id == null || id.trim().length() == 0) {
			throw new IllegalArgumentException();
		}
		this.parent = parent;
		this.id = id;
		parent.add(this, id);
		checkTag();
	}

	public Container getParent() {
		return parent;
	}

	public String getPath() {
		StringBuilder builder = new StringBuilder();
		for (Component c = this; c != null && c.parent != null; c = c
				.getParent()) {
			if (builder.length() > 0) {
				builder.insert(0, ':');
			}
			builder.insert(0, c.getId());
		}
		return builder.toString();
	}

	public ComponentFragment getFragment() {
		return getFragment(getMarkupRelativePath());
	}

	protected ComponentFragment getFragment(String markupRelativePath) {
		return parent.getFragment(markupRelativePath);

	}

	public Page getPage() {
		Component c = this;
		while (c.parent != null) {
			c = c.parent;
		}
		return (Page) c;
	}

	private void renderTagOpening(OutputStream stream) {
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void renderOpenTag(OutputStream stream) {
		renderTagOpening(stream);
		try {
			stream.write(">".getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void renderCloseTag(OutputStream stream) {
		ComponentFragment frag = getFragment();
		Tag tag = frag.getTag();
		try {
			stream.write("</".getBytes());
			stream.write(tag.getName().getBytes());
			stream.write(">".getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void renderEmptyTag(OutputStream stream) {
		renderTagOpening(stream);
		try {
			stream.write("/>".getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void render(OutputStream stream) {
		Tag tag = getFragment().getTag();
		if (tag.isEmpty()) {
			renderEmptyTag(stream);
		} else {
			renderOpenTag(stream);
			renderCloseTag(stream);
		}

	}

	public void checkTag() {
		if (getFragment().getFragments() != null) {
			throw new RuntimeException("component has markup but it should not");
		}
	}

	public Component get(String path) {
		if (path == null || path.length() == 0) {
			return this;
		} else {
			throw new RuntimeException(
					"component is not a container so it cannot contain other components");
		}
	}

	public String getId() {
		return id;
	}

	protected Markup getMarkup() {
		return parent.getMarkup();
	}

	protected String getMarkupRelativePath() {
		return getMarkupRelativePath(getPath());
	}

	String getMarkupRelativePath(String path) {
		return parent.getMarkupRelativePath(path);
	}

}
