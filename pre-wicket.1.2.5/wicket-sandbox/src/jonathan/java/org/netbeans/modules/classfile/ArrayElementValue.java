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
 * ArrayElementValue:  the value portion of an annotation element that 
 * is an array of ElementValue instances.
 *
 * @author  Thomas Ball
 */
public final class ArrayElementValue extends ElementValue {
    ElementValue[] values;

    ArrayElementValue(ConstantPool pool, ElementValue[] values) {
	this.values = values;
    }

    /**
     * Returns the set of ElementValue instances for this component.
     */
    public final ElementValue[] getValues() {
	return (ElementValue[])values.clone();
    }

    public String toString() {
	StringBuffer sb = new StringBuffer("[");
	int n = values.length;
	for (int i = 0; i < n; i++) {
	    sb.append(values[i]);
	    if ((i + 1) < n)
		sb.append(',');
	}
	sb.append(']');
	return sb.toString();
    }
}
