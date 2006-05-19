/*
 * CPNameAndTypeInfo.java
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
 * A class representing the CONSTANT_NameAndType constant pool type.
 *
 * @author Thomas Ball
 */
public class CPNameAndTypeInfo extends CPEntry {
    int iName;
    int iDesc;

    CPNameAndTypeInfo(ConstantPool pool,int iName,int iDesc) {
	super(pool);
        this.iName = iName;
        this.iDesc = iDesc;
    }
    
    protected CPNameAndTypeInfo(ConstantPool pool) {
        super(pool);
        iName = CPName.INVALID_INDEX;
        iDesc = CPName.INVALID_INDEX;
    }

    public final String getName() {
	return ((CPName)pool.cpEntries[iName]).getName();
    }

    void setNameIndex(int index) {
	iName = index;
    }

    public final String getDescriptor() {
	return ((CPName)pool.cpEntries[iDesc]).getName();
    }

    void setDescriptorIndex(int index) {
	iDesc = index;
    }

    public int getTag() {
	return ConstantPool.CONSTANT_NameAndType;
    }

    public String toString() {
        return getClass().getName() + ": name=" + getName() + //NOI18N
            ", descriptor=" + getDescriptor(); //NOI18N
    }
}
