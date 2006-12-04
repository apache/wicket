package wicked.markup;

import java.util.HashMap;
import java.util.Map;

public class Tag {
	private String name;
	private Map<String, String> attributes=new HashMap<String, String>();
	private boolean empty=false;
	
	public Tag(String name) {
		setName(name);
	}

	public Tag(String name, Map<String,String> attrs) {
		setName(name);
		attributes=attrs;
	}

	public Tag(String name, Map<String,String> attrs, boolean empty) {
		setName(name);
		attributes=attrs;
		this.empty=empty;
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

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
	
	
}
