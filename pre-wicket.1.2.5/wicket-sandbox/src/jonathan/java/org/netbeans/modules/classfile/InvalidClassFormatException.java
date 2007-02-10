/*
 * InvalidClassFormatException.java
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

import java.io.IOException;

/**
 * Exception thrown when a classfile with an invalid format is detected.
 *
 * @author Thomas Ball
 */
public class InvalidClassFormatException extends IOException {
	private static final long serialVersionUID = 1L;

	/**
     * Constructs an <code>InvalidClassFormatException</code> with 
     * <code>null</code> as its error detail message.
     */
    public InvalidClassFormatException() {
	super();
    }

    /**
     * Constructs an <code>InvalidClassFormatException</code> with the 
     * specified detail message. The error message string <code>s</code> 
     * can later be retrieved by the 
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    public InvalidClassFormatException(String s) {
	super(s);
    }
}

