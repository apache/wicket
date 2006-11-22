/*
 * CPIntegerInfo.java
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
 * A class representing the CONSTANT_Integer constant pool type.
 *
 * @author Thomas Ball
 */
public final class CPIntegerInfo extends CPEntry {

    CPIntegerInfo(ConstantPool pool, int v) {
	super(pool);
        value = new Integer(v);
    }

    public final int getTag() {
	return ConstantPool.CONSTANT_Integer;
    }

    public String toString() {
        return getClass().getName() + ": value=" + value; //NOI18N
    }
}
