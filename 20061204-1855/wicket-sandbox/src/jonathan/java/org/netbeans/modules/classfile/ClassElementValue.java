/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

/**
 * ClassElementValue:  the value part of a single element for
 * those annotations that are a class type.
 *
 * @author  Thomas Ball
 */
public final class ClassElementValue extends ElementValue {
    String name;

    ClassElementValue(ConstantPool pool, int iValue) {
	// getName() works for either the old CPClassInfo or now
	// CPUTF8Info entries, changed after 1.5 beta 1.
	this.name = ((CPName)pool.get(iValue)).getName();
    }

    /**
     * Returns the value of this component, as a class constant pool entry.
     */
    public final ClassName getClassName() {
	return ClassName.getClassName(name);
    }

    public String toString() {
	return "class=" + name;
    }
}
