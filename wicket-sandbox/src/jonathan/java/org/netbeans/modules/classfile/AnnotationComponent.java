/*
 * AnnotationComponent.java
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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * AnnotationComponent:  a single annotation on a program element.
 *
 * @author  Thomas Ball
 */
public class AnnotationComponent {
    String name;
    ElementValue value;

    static AnnotationComponent load(DataInputStream in, ConstantPool pool,
				    boolean runtimeVisible) 
	throws IOException {
	int iName = in.readUnsignedShort();
	String name = ((CPName)pool.get(iName)).getName();
	ElementValue value = ElementValue.load(in, pool, runtimeVisible);
	return new AnnotationComponent(name, value);
    }

    AnnotationComponent(String name, ElementValue value) {
	this.name = name;
	this.value = value;
    }

    /**
     * Returns the name of this component.
     */
    public final String getName() {
	return name;
    }

    /**
     * Returns the value for this component.
     */
    public final ElementValue getValue() {
	return value;
    }

    public String toString() {
	return "name=" + name + ", value=" + value;
    }
}
