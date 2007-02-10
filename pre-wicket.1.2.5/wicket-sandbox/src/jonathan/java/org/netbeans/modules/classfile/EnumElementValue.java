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
 * EnumElementValue:  a single annotation on a program element for
 * those annotations that are enum constants.  
 *
 * @author  Thomas Ball
 */
public final class EnumElementValue extends ElementValue {
    String enumType;
    String enumName;

    EnumElementValue(ConstantPool pool, int iEnumType, int iEnumName) {
	enumType = ((CPName)pool.get(iEnumType)).getName();
	enumName = ((CPName)pool.get(iEnumName)).getName();
    }

    // for 1.5 beta1 classfile incompatibility
    EnumElementValue(String type, String name) {
	enumType = type;
	enumName = name;
    }

    /**
     * Returns the enum type as a string, rather than a ClassName.
     * This is necessary because an enum may have a primitive type.
     */
    public final String getEnumType() {
	return enumType;
    }

    /**
     * Returns the name of the enum constant for this annotation
     * component.
     */
    public final String getEnumName() {
	return enumName;
    }

    public String toString() {
	return enumType + "." + enumName;
    }
}
