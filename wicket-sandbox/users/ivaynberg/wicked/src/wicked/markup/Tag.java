package wicked.markup;

import java.util.HashMap;
import java.util.Map;

public class Tag {
	private String name;
	private Map<String, String> attributes=new HashMap<String, String>();
	
	public Tag(String name) {
		setName(name);
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
}
