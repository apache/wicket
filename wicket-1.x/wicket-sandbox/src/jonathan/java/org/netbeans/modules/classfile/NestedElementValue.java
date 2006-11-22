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
 * NestedElementValue:  an annotation on a program element that is
 * another annotation.  The value for this annotation is the
 * nested AnnotationComponent.
 *
 * @author  Thomas Ball
 */
public final class NestedElementValue extends ElementValue {
    Annotation value;

    NestedElementValue(ConstantPool pool, Annotation value) {
	this.value = value;
    }

    /**
     * Returns the value of this component, which is an Annotation.
     */
    public final Annotation getNestedValue() {
	return value;
    }

    public String toString() {
	return "nested value=" + value;
    }
}
