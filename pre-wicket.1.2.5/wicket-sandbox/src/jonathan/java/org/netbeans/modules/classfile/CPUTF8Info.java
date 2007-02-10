/*
 * CPUTFInfo.java
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

/**
 * A class representing the CONSTANT_Utf8 constant pool type.
 *
 * @author Thomas Ball
 */
public final class CPUTF8Info extends CPName {
    String name;
    byte[] utf;

    CPUTF8Info(ConstantPool pool, String name) {
	super(pool);
	this.name = name;
    }

    CPUTF8Info(ConstantPool pool, byte[] utf) {
	super(pool);
	this.utf = utf;
    }

    public final String getName() {
	if (name == null) {
	    name = ConstantPoolReader.readUTF(utf, utf.length);
	    utf = null;
	}
	return name;
    }

    public final Object getValue() {
        return getName();
    }

    public final int getTag() {
	return ConstantPool.CONSTANT_Utf8;
    }

    public String toString() {
	return getClass().getName() + ": name=" + getName(); //NOI18N
    }
}
