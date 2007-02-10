/*
 * InnerClass.java
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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An InnerClass attribute of a classfile.
 *
 * @author  Thomas Ball
 */
public final class InnerClass {

    ClassName name;
    ClassName outerClassName;
    String simpleName;
    int access;
    
    static InnerClass[] loadInnerClasses(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int n = in.readUnsignedShort();
        InnerClass[] innerClasses = new InnerClass[n];
        for (int i = 0; i < n; i++)
            innerClasses[i] = new InnerClass(in, pool);
        return innerClasses;
    }

    InnerClass(DataInputStream in, ConstantPool pool) 
      throws IOException {
        loadInnerClass(in, pool);
    }

    private void loadInnerClass(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int index = in.readUnsignedShort();
        name = (index > 0) ? pool.getClass(index).getClassName() : null;
        index = in.readUnsignedShort();
	outerClassName = (index > 0) ? pool.getClass(index).getClassName() : null;
        index = in.readUnsignedShort();
        if (index > 0) {
            CPUTF8Info entry = (CPUTF8Info)pool.get(index);
            simpleName = entry.getName();
        }
        access = in.readUnsignedShort();
    }

    /** Returns the name of this class, including its package (if any).
     * If the compiler didn't define this value, the string 
     * "<not defined>" is returned.
     * @return the name of this class.
     */    
    public final ClassName getName() {
        return name;
    }
    
    /** Returns the name of the enclosing outer class, including 
     *  its package (if any).  
     * @return the name of this class, or null if not available.
     */    
    public final ClassName getOuterClassName() {
        return outerClassName;
    }

    /**
     * Returns the original simple name as given in the source code.
     * If this is an anonymous class, null is returned instead.
     * @return the simple name of this class, or null if anonymous.
     */
    public final String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns the access flags of this class.
     */
    public final int getAccess() {
        return access;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("innerclass=");
        sb.append(name);
        if (simpleName != null) {
            sb.append(" (");
            sb.append(simpleName);
            sb.append(')');
        }
        sb.append(", outerclass=");
        sb.append(outerClassName);
        return sb.toString();
    }
}
