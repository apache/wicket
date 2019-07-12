package org.apache.wicket.util.value;

import java.util.Map;

public class HeaderItemAttributeMap extends AttributeMap {

    public HeaderItemAttributeMap() {
        super();
    }

    public HeaderItemAttributeMap(final Map<String, Object> map)
    {
        super(map);
    }

    public Object put(HeaderItemAttribute key, String value) {
        return super.put(key.getName(), value);
    }

    public Object add(HeaderItemAttribute key, String value) {
        return super.add(key.getName(), value);
    }

}
