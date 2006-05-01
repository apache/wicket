/*
 * ClassFile.java
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.WeakHashMap;

/**
 * Class representing the name of a Java class.
 *
 * @author Thomas Ball
 */
public final class ClassName implements Comparable, Comparator, Serializable {

    static final long serialVersionUID = -8444469778945723553L;

    private final String type;
    private transient String internalName;
    private transient String externalName;
    private transient String packageName;
    private transient String simpleName;
    private transient int hash = -1;

    private final static WeakHashMap cache = new WeakHashMap();

    /**
     * Returns the ClassName object referenced by a class
     * type string (field descriptor), as defined in the 
     * JVM Specification, sections 4.3.2 and 4.2.  
     * <P>
     * Basically, the JVM Specification defines a class type 
     * string where the periods ('.') separating
     * a package name are replaced by forward slashes ('/').
     * Not documented in the second edition of the specification
     * is that periods separating inner and outer classes are
     * replaced with dollar signs ('$').  Array classes have one
     * or more left brackets ('[') prepending the class type.
     * For example:
     * <PRE><CODE>
     *   java.lang.String         java/lang/String
     *   java.util.HashMap.Entry  java/util/HashMap$Entry
     *   java.lang.Integer[]      [java/lang/Integer
     *   java.awt.Point[][]       [[java/awt/Point
     * </CODE><PRE>
     * <P>
     * This method also accepts type strings which contain with
     * 'L' and end with ';' characters.  This format is used
     * to reference a class in other type names, such as
     * method arguments.  These two characters are removed from the
     * type string.
     * <P>
     * Because ClassNames are immutable, multiple requests to
     * get the same type string may return identical object
     * references.  This cannot be assumed, however, and the
     * ClassName.equals() method should be used instead of
     * "==" to test for equality.
     *
     * @param classType  the class type string, as defined by the JVM spec.
     * @throws IllegalArgumentException if classType isn't of the correct
     *                   format.
     */
    public static ClassName getClassName(String classType) {
        // A null superclass name is supposed to be null, but may be
        // an empty string depending on the compiler.
        if (classType == null || classType.length() == 0)
	    return null;

        ClassName cn = getCacheEntry(classType);
	if (cn == null)
	    synchronized (cache) {
		cn = getCacheEntry(classType);
		if (cn == null) {
		    // check for valid class type
		    int i = classType.indexOf('L');
		    String _type;
		    char lastChar = classType.charAt(classType.length()-1);
		    if (i != -1 && lastChar == ';') {
                        // remove 'L' and ';' from type
			_type = classType.substring(i+1, classType.length()-1);
                        if (i > 0)
                            // add array prefix
                            _type = classType.substring(0, i) + _type;
			cn = getCacheEntry(_type);
			if (cn != null)
			    return cn;
		    } else {
			_type = classType;
		    }

		    cn = new ClassName(_type);
		    cache.put(_type, new WeakReference(cn));
		}
	    }
	return cn;
    }

    private static ClassName getCacheEntry(String key) {
	WeakReference ref = (WeakReference)cache.get(key);
	return ref != null ? (ClassName)ref.get() : null;
    }

    /**
     * Create a ClassName object via its internal type name, as
     * defined by the JVM spec.
     */
    private ClassName(String type) {
        this.type = type;

	// internal name is a type stripped of any array designators
	int i = type.lastIndexOf('[');
	internalName = (i > -1) ? type.substring(i+1) : type;
    }

    /**
     * Returns the type string of this class, as stored in the 
     * classfile (it's "raw" form).  For example, an array of
     * Floats would have a type of "[java/lang/Float".
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the "internal" classname, as defined by the JVM 
     * Specification, without any parameter or return type 
     * information.  For example, the name for the String
     * class would be "java/lang/String".  Inner classes are 
     * separated from their outer class with '$'; such as
     * "java/util/HashMap$Entry".  Array specifications are
     * stripped; use getType() instead.
     */
    public String getInternalName() {
        return internalName;
    }

    /**
     * Returns the "external" classname, as defined by the 
     * Java Language Specification, without any parameter
     * or return type information.  For example, the name for the
     * String class would be "java.lang.String".  Inner classes
     * are separated from their outer class with '.'; such as
     * "java.util.HashMap.Entry".  Arrays are shown as having one
     * or more "[]" characters behind the base classname, such
     * as "java.io.Files[]".
     */
    public String getExternalName() {
        return getExternalName(false);
    }

    /**
     * Returns the "external" classname, as defined by the 
     * Java Language Specification, without any parameter
     * or return type information.  For example, the name for the
     * String class would be "java.lang.String".  Inner classes
     * are separated from their outer class with '.'; such as
     * "java.util.HashMap.Entry".  Unless suppressed, arrays are 
     * shown as having one or more "[]" characters behind the 
     * base classname, such as "java.io.Files[]".
     */
    public String getExternalName(boolean suppressArrays) {
        initExternalName();
        int i;
        if (suppressArrays && (i = externalName.indexOf('[')) != -1)
	    return externalName.substring(0, i);
        return externalName;
    }
    
    private synchronized void initExternalName() {
        if (externalName == null) 
            externalName = externalizeClassName();
    }

    /**
     * Return the package portion of this classname.
     */
    public synchronized String getPackage() {
        if (packageName == null) {
            int i = internalName.lastIndexOf('/');
            packageName = (i != -1) ? 
                internalName.substring(0, i).replace('/', '.') : "";
        }
	return packageName;
    }

    /**
     * Returns the classname without any package specification.
     */
    public String getSimpleName() {
	if (simpleName == null) {
	    String pkg = getPackage();
	    int i = pkg.length();
	    String extName = getExternalName();
	    if (i == 0)
		simpleName = extName;  // no package
	    else
		simpleName = extName.substring(i + 1);
	}
	return simpleName;
    }

    public boolean equals(Object obj) {
        if (this == obj)
	    return true;
	return (obj instanceof ClassName) ? 
	  (type.equals(((ClassName)obj).type)) : false;
    }

    /**
     * Compares this ClassName to another Object.  If the Object is a 
     * ClassName, this function compares the ClassName's types.  Otherwise,
     * it throws a <code>ClassCastException</code>.
     *
     * @param   obj the <code>Object</code> to be compared.
     * @return  the value <code>0</code> if the argument is a string
     *		lexicographically equal to this string; a value less than
     *		<code>0</code> if the argument is a string lexicographically 
     *		greater than this string; and a value greater than
     *		<code>0</code> if the argument is a string lexicographically
     *		less than this string.
     * @exception <code>ClassCastException</code> if the argument is not a
     *		  <code>ClassName</code>. 
     * @see     java.lang.Comparable
     */
    public int compareTo(Object obj) {
        // If obj isn't a ClassName, the correct ClassCastException
        // will be thrown by the cast.
        return type.compareTo(((ClassName)obj).type);
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
     * @throws ClassCastException if the arguments' types prevent them from
     * 	       being compared by this Comparator.
     */
    public int compare(Object o1, Object o2) {
        return ((ClassName)o1).compareTo(o2);
    }

    public int hashCode() {
        if (hash == -1)
	    hash = type.hashCode();
	return hash;
    }

    public String toString() {
        return getExternalName();
    }

    // Called from synchronization block, do not call out!
    private String externalizeClassName() {
        StringBuffer sb = new StringBuffer(type);
	int arrays = 0;
	boolean atBeginning = true;
	int length = sb.length();
	for (int i = 0; i < length; i++) {
	    char ch = sb.charAt(i);
	    switch (ch) {
	      case '[': 
		if (atBeginning)
		    arrays++; 
		break;

	      case '/':
	      case '$':
		sb.setCharAt(i, '.');
		atBeginning = false;
		break;

	      default:
		atBeginning = false;
	    }
	}

	if (arrays > 0) {
	    sb.delete(0, arrays);
	    for (int i = 0; i < arrays; i++)
	      sb.append("[]");
	}

        return sb.toString();
    }
    
    /**
     * Empties the cache -- used by unit tests.
     */
    static void clearCache() {
        cache.clear();
    }

    /**
     * Suppress multiple instances of the same type, as well as any
     * immutability attacks (unlikely as that might be).  For more information
     * on this technique, check out Effective Java, Item 57, by Josh Bloch.
     */
    private Object readResolve() throws ObjectStreamException {
        return getClassName(internalName);
    }
}
