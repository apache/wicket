/*
 * AttributeMap.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.*;
import java.util.*;

/**
 * Class representing a map of classfile attributes.  The
 * keys for this map are the names of the attributes (as Strings,
 * not constant pool indexes).  The values are byte arrays that
 * hold the contents of the attribute.
 *
 * Note: if a ClassFile is created with includeCode parameter set to
 * false, then no AttributeMaps for the classfile's methods will
 * have Code attributes.
 *
 * @author Thomas Ball
 */
public final class AttributeMap {

    Map map;

    /**
     * Load zero or more attributes from a class, field or method.
     */
    static AttributeMap load(DataInputStream in, ConstantPool pool) 
      throws IOException {
	return load(in, pool, false);
    }

    static AttributeMap load(DataInputStream in, ConstantPool pool,
			     boolean includeCode) throws IOException {        
        int count = in.readUnsignedShort();
        Map map = new HashMap(count + 1, (float)1.0);
        for (int i = 0; i < count; i++) {
	    Object o = pool.get(in.readUnsignedShort());
            if (!(o instanceof CPUTF8Info))
                throw new InvalidClassFormatException();
	    CPUTF8Info entry = (CPUTF8Info)o;
	    String name = entry.getName();
	    int len = in.readInt();
	    if (!includeCode && name.equals("Code")) {
		int n;
		while ((n = (int)in.skip(len)) > 0 && n < len)
		    len -= n;
	    } else {
		byte[] attr = new byte[len];
		in.readFully(attr);
		map.put(name, attr);
	    }
        }
	return new AttributeMap(map);
    }

    AttributeMap(Map attributes) {
	this.map = attributes;
    }

    DataInputStream getStream(String name) {
	byte[] attr = (byte[])map.get(name);
	return attr != null ? 
	    new DataInputStream(new ByteArrayInputStream(attr)) : null;	    
    }

    /**
     * Returns an array containing the bytes in a specified attribute.
     * If the attribute exists but doesn't have any length (such as
     * the <code>Deprecated</code> attribute), then a zero-length
     * array is returned.  If the attribute doesn't exist, then null 
     * is returned.
     */
    byte[] get(String name) {
	return (byte[])map.get(name);
    }

    /**
     * Returns the number of attributes in this map.
     */
    public int size() {
	return map.size();
    }

    /**
     * Returns true if no attributes exist in this map.
     */
    public boolean isEmpty() {
	return map.isEmpty();
    }

    /**
     * Returns true if an attribute of the specified name exists in this map.
     */
    public boolean containsAttribute(String key) {
	return map.containsKey(key);
    }

    /**
     * Returns a set of names of all of the attributes in this map.
     */
    public Set keySet() {
	return map.keySet();
    }

}
