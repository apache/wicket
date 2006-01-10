package wicked.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Markup implements IFragmentCollector {
	private List<Fragment> fragments;

	private Map<String, Fragment> pathToFrag = new HashMap<String, Fragment>();

	public List<Fragment> getFragments() {
		return fragments;
	}

	public ComponentFragment getComponentFragment(String path) {
		Fragment frag = pathToFrag.get(path);
		if (frag == null) {
			throw new RuntimeException("component fragment for path [" + path
					+ "] not found in markup");
		}
		if (frag.getType() != MarkupType.COMPONENT) {
			throw new RuntimeException(
					"component fragment has invalid markup type ["
							+ frag.getType().toString() + "]");
		}
		
		return (ComponentFragment)frag;
	}

	public void setFragments(List<Fragment> fragments) {
		this.fragments = fragments;
	}

	public void addFragment(Fragment frag) {
		if (fragments == null) {
			fragments = new ArrayList<Fragment>();
		}
		fragments.add(frag);
	}

	public void init() {
		if (fragments != null) {

			for (Fragment frag : fragments) {
				if (frag.getType() == MarkupType.COMPONENT) {
					initHelper((ComponentFragment) frag, null);
				}
			}

		}
	}

	private void initHelper(ComponentFragment frag, String parentPath) {
		String path = (parentPath == null) ? frag.getId() : parentPath + ":"
				+ frag.getId();
		pathToFrag.put(path, frag);

		if (frag.getFragments() != null) {

			for (Fragment child : frag.getFragments()) {
				if (child.getType() == MarkupType.COMPONENT) {
					initHelper((ComponentFragment) child, path);
				}
			}
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(512);
		if (fragments != null) {
			for (Fragment frag : fragments) {
				builder.append(frag.toString());
			}
		}
		return builder.toString();
	}

}
