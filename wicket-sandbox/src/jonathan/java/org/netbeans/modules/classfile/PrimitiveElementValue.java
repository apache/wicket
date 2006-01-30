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
 * A PrimitiveElementValue is the value portion of an annotation component
 * that has a primitive type or String constant.  Its value
 * is a constant pool entry of the same type as the primitive; 
 * for example, an int constant would have a value of type CPIntegerInfo.
 *
 * @author  Thomas Ball
 */
public final class PrimitiveElementValue extends ElementValue {
    CPEntry value;

    PrimitiveElementValue(ConstantPool pool, int iValue) {
	this.value = pool.get(iValue);
    }

    /**
     * Returns the value of this component, as a constant pool entry.
     */
    public final CPEntry getValue() {
	return value;
    }

    public String toString() {
	return "const=" + value.getValue();
    }
}
