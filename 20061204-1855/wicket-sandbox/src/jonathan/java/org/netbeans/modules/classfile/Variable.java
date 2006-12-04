/*
 * Variable.java
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

import java.io.*;

/**
 * A Java field.  Unfortunately, the word "field" is generally used in 
 * the Java documentation to mean either a variable, or both variables
 * and methods.  This class only describes variables.
 *
 * @author  Thomas Ball
 */
public final class Variable extends Field {

    private Object constValue;
    
    static Variable[] loadFields(DataInputStream in, ConstantPool pool,
                                 ClassFile cls) 
      throws IOException {
          int count = in.readUnsignedShort();
          Variable[] variables = new Variable[count];
          for (int i = 0; i < count; i++)
              variables[i] = new Variable(in, pool, cls);
          return variables;
    }
    
    /** Creates new Variable */
    Variable(DataInputStream in, ConstantPool pool, ClassFile cls) 
	throws IOException {
        super(in, pool, cls, false);
    }

    /**
     * Returns true if the variable is a constant; that is, a final
     * static variable.
     * @see #getConstantValue
     */
    public final boolean isConstant() {
        return attributes.get("ConstantValue") != null;//NOI18N
    }

    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @deprecated replaced by <code>Object getConstantValue()</code>.
     */
    public final Object getValue() {
	return getConstantValue();
    }
    
    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @see #isConstant
     */
    public final Object getConstantValue() {
	if (constValue == null) {
	    DataInputStream in = attributes.getStream("ConstantValue"); // NOI18N
	    if (in != null) {
		try {
		    int index = in.readUnsignedShort();
		    CPEntry cpe = classFile.constantPool.get(index);
		    constValue = cpe.getValue();
		} catch (IOException e) {
		    System.err.println("invalid ConstantValue attribute");
		}
	    }
	}
        return constValue;
    }
    
    /**
     * Return a string in the form "<type> <name>".  Class types
     * are shown in a "short" form; i.e. "Object" instead of
     * "java.lang.Object"j.
     *
     * @return string describing the variable and its type.
     */
    public final String getDeclaration() {
	StringBuffer sb = new StringBuffer();
	sb.append(CPFieldMethodInfo.getSignature(getDescriptor(), false));
	sb.append(' ');
	sb.append(getName());
	return sb.toString();
    }

    /**
     * Returns true if this field defines an enum constant.
     */
    public final boolean isEnumConstant() {
	return (access & Access.ENUM) == Access.ENUM;
    }
            
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        if (isConstant()) {
	    sb.append(", const value="); //NOI18N
	    sb.append(getValue());
	}
        return sb.toString();
    }
}
