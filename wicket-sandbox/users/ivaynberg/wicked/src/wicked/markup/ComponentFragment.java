package wicked.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ComponentFragment extends Fragment implements IFragmentCollector {
	private Tag tag;
	private List<Fragment> fragments;
	
	public String getId() {
		return tag.getAttributes().get("wicket:id");
	}
	
	public List<Fragment> getFragments() {
		return fragments;
	}

	public void setFragments(List<Fragment> fragments) {
		this.fragments = fragments;
	}
	
	public void addFragment(Fragment frag) {
		if (fragments==null) {
			fragments=new ArrayList<Fragment>();
		}
		fragments.add(frag);
	}
	
	public ComponentFragment(Tag tag) {
		this.tag=tag;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
	public String toString() {
		StringBuilder builder=new StringBuilder(256);
		builder.append("<").append(tag.getName());
		for (Map.Entry<String,String> attr:tag.getAttributes().entrySet()) {
			builder.append(" ").append(attr.getKey())
			.append("=\"").append(attr.getValue()).append("\"");
		}
		
		if (tag.isEmpty()) {
			builder.append("/>");
		} else {
			builder.append(">");
			if (fragments!=null) {
				for (Fragment frag:fragments) {
					builder.append(frag.toString());
				}
			}
			builder.append("</").append(tag.getName()).append(">");
		}
		return builder.toString();
	}

	@Override
	public MarkupType getType() {
		return MarkupType.COMPONENT;
	}
	


}
