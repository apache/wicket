/*
 * Annotation.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
 * Annotation:  a single annotation on a program element.
 *
 * @author  Thomas Ball
 */
public class Annotation {
    ClassName type;
    AnnotationComponent[] components;
    boolean runtimeVisible;

    /**
     * Reads a classfile annotation section and adds its annotations to
     * a specified map.
     */
    static void load(DataInputStream in, ConstantPool pool, 
		     boolean visible, Map map) throws IOException {
	int nattrs = in.readUnsignedShort();
	for (int i = 0; i < nattrs; i++) {
	    Annotation ann = loadAnnotation(in, pool, visible);
	    map.put(ann.getType(), ann);
	}
    }

    static Annotation loadAnnotation(DataInputStream in, ConstantPool pool, 
				     boolean visible) throws IOException {
	final ClassName type;
	CPEntry entry = pool.get(in.readUnsignedShort());
	if (entry.getTag() == ConstantPool.CONSTANT_Class)
	    // 1.5 build 51 and earlier
	    type = ((CPClassInfo)entry).getClassName();
	else {
	    String s = ((CPName)entry).getName();
	    type = ClassName.getClassName(s);
	}
	int npairs = in.readUnsignedShort();
	List pairList = new ArrayList();
	for (int j = 0; j < npairs; j++)
	    pairList.add(AnnotationComponent.load(in, pool, visible));
	AnnotationComponent[] acs = 
	    new AnnotationComponent[pairList.size()];
	pairList.toArray(acs);
	return new Annotation(pool, type, acs, visible);
    }

    Annotation(ConstantPool pool, ClassName type, 
	       AnnotationComponent[] components, boolean runtimeVisible) {
	this.type = type;
	this.components = components;
	this.runtimeVisible = runtimeVisible;
    }

    /**
     * Returns the annotation type.
     */
    public final ClassName getType() {
	return type;
    }

    /**
     * Returns the named components for this annotation, as an
     * array of AnnotationComponents.
     */
    public final AnnotationComponent[] getComponents() {
	return (AnnotationComponent[])components.clone();
    }

    /**
     * Returns the named component for this annotation, or null if 
     * no component with that name exists.
     */
    public final AnnotationComponent getComponent(String name) {
	for (int i = 0; i < components.length; i++) {
	    AnnotationComponent comp = components[i];
	    if (comp.getName().equals(name))
		return comp;
	}
	return null;
    }

    /**
     * Returns true if this annotation is loaded by the Java Virtual
     * Machine to be available via the Java reflection facility.
     */
    public boolean isRuntimeVisible() {
	return runtimeVisible;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer("@");
	sb.append(type);
	sb.append(" runtimeVisible=");
	sb.append(runtimeVisible);
	int n = components.length;
	if (n > 0) {
	    sb.append(" { ");
	    for (int i = 0; i < n; i++) {
		sb.append(components[i]);
		if (i < n - 1)
		    sb.append(", ");
	    }
	    sb.append(" }");
	}
	return sb.toString();
    }
}
