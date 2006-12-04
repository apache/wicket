/*
 * CPMethodInfo.java
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
 * A class representing the CONSTANT_Methodref constant pool type.
 *
 * @author Thomas Ball
 */
public class CPMethodInfo extends CPFieldMethodInfo {
    CPMethodInfo(ConstantPool pool,int iClass,int iNameAndType) {
        super(pool, iClass, iNameAndType);
    }

    public final String getMethodName() {
	return getFieldName();
    }

    /**
     * Get method name and signature, such as "void setBar(Bar)".
     */
    public final String getFullMethodName() {
        return getFullMethodName(getMethodName(), getDescriptor());
    }
    
    static String getFullMethodName(String name, String signature) {
        StringBuffer sb = new StringBuffer();
        int index = signature.indexOf(')');
        String params = signature.substring(1, index);
        
        if (!name.equals("<init>") && !name.equals("<clinit>")) {
            String ret = signature.substring(index + 1);
            ret = CPFieldMethodInfo.getSignature(ret, false);
            if (ret.length() > 0) {
                sb.append(ret);
                sb.append(' ');
            }
        }
        sb.append(name);
        sb.append('(');
        index = 0;
        int paramsLength = params.length();
        while (index < paramsLength) {
            StringBuffer p = new StringBuffer();
            char ch = params.charAt(index++);
            while (ch == '[') {
                p.append(ch);
                ch = params.charAt(index++);
            }
            p.append(ch);
            if (ch == 'L')
                do {
                    ch = params.charAt(index++);
                    p.append(ch);
                } while (ch != ';');
            sb.append(CPFieldMethodInfo.getSignature(p.toString(), false));
            if (index < paramsLength)
                sb.append(',');
        }
        sb.append(')');
        return sb.toString();
    }

    public int getTag() {
	return ConstantPool.CONSTANT_MethodRef;
    }
}
