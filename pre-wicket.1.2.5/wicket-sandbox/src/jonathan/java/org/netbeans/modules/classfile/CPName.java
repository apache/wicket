/*
 * CPName.java
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
 * The base class for all constant pool types which store strings.
 *
 * @author Thomas Ball
 */
abstract class CPName extends CPEntry {

    final static int INVALID_INDEX = -1;

    int index;
    private String name;

    CPName(ConstantPool pool,int index) {
	super(pool);
        this.index = index;
    }

    CPName(ConstantPool pool) {
	super(pool);
	index = INVALID_INDEX;
    }

    public String getName() {
	if (index == INVALID_INDEX) {
	    return null;
        }            
        if (name == null) {
            name = ((CPName)pool.cpEntries[index]).getName();
        }
        return name;
    }
    
    public Object getValue() {
        return getName();
    }

    void setNameIndex(int index) {
	this.index = index;
        name = null;
    }

    public String toString() {
	return getClass().getName() + ": name=" + 
	    (index == INVALID_INDEX ? "<unresolved>" :  //NOI18N
	     ((CPName)pool.cpEntries[index]).getName());
    }
}
