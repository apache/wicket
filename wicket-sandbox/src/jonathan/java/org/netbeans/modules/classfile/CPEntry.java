/*
 * CPEntry.java
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
 * A class representing an entry in a ConstantPool.
 *
 * @author Thomas Ball
 */
public abstract class CPEntry {

    ConstantPool pool;
    Object value;

    CPEntry(ConstantPool pool) {
	this.pool = pool;
    }
    
    void resolve(CPEntry[] pool) {
        // already resolved by default
    }

    /* The VM doesn't allow the next constant pool slot to be used
     * for longs and doubles. 
     */
    boolean usesTwoSlots() {
	return false;
    }
    
    public Object getValue() {
        return value;
    }

    /**
     * Returns the constant type value, or tag, as defined by
     * table 4.3 of the Java Virtual Machine specification.
     */
    public abstract int getTag();
}

