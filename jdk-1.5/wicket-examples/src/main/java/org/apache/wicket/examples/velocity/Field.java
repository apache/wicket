/*
 * (c) 2007 Joost Technologies B.V. All rights reserved. This code contains
 * trade secrets of Joost Technologies B.V. and any unauthorized use or
 * disclosure is strictly prohibited.
 *
 * $Id$
 */
package org.apache.wicket.examples.velocity;

import java.io.Serializable;

/**
 * // TODO Describe the class or the interface here.
 */
public class Field implements Serializable
{
	private String fieldName;

	private int fieldSize;

	/**
	 * Construct.
	 * 
	 * @param fieldName
	 * @param fieldSize
	 */
	public Field(String fieldName, int fieldSize)
	{
		this.fieldName = fieldName;
		this.fieldSize = fieldSize;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public int getFieldSize()
	{
		return fieldSize;
	}

	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}

	public void setFieldSize(int fieldSize)
	{
		this.fieldSize = fieldSize;
	}
}
