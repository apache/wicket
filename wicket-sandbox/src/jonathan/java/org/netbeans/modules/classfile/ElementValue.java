/*
 * ElementValue.java
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
 * ElementValue:  the value portion of the name-value pair of a
 * single annotation element.
 *
 * @author  Thomas Ball
 */
public abstract class ElementValue {
    static ElementValue load(DataInputStream in, ConstantPool pool, 
			     boolean runtimeVisible) 
	throws IOException {
	char tag = (char)in.readByte();
	switch (tag) {
	case 'e': return loadEnumValue(in, pool);
	  case 'c': {
	      int classType = in.readUnsignedShort();
	      return new ClassElementValue(pool, classType);
	  }
	  case '@': {
	      Annotation value = 
		  Annotation.loadAnnotation(in, pool, runtimeVisible);
	      return new NestedElementValue(pool, value);
	  }
	  case '[': {
	      ElementValue[] values = new ElementValue[in.readUnsignedShort()];
	      for (int i = 0; i < values.length; i++)
		  values[i] = load(in, pool, runtimeVisible);
	      return new ArrayElementValue(pool, values);
	  }
	  default:
	      //assert "BCDFIJSZs".indexOf(tag) >= 0 : "invalid annotation tag";
	      return new PrimitiveElementValue(pool, in.readUnsignedShort());
	}
    }

    private static ElementValue loadEnumValue(DataInputStream in, 
					      ConstantPool pool) 
	throws IOException {
	int type = in.readUnsignedShort();
	CPEntry cpe = pool.get(type);
	if (cpe.getTag() == ConstantPool.CONSTANT_FieldRef) {
	    // workaround for 1.5 beta 1 and earlier builds
	    CPFieldInfo fe = (CPFieldInfo)cpe;
	    String enumType = fe.getClassName().getInternalName();
	    String enumName = fe.getFieldName();
	    return new EnumElementValue(enumType, enumName);
	} else {
	    int name = in.readUnsignedShort();
	    return new EnumElementValue(pool, type, name);
	}
    }

    /* Package-private constructor so that only classes in this
     * package can subclass.
     */
    ElementValue() {
    }
}
