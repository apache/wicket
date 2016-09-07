/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.core.util.lang;

import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;
import static java.lang.String.format;

import org.apache.wicket.core.util.lang.PropertyExpression.Property;

/**
 * EBNF like description of the property expression syntax <code>
 * 
 *  java letter				= "_" | "$" | "A" | "a" | "B" | "b" | (...) ;
 *  java letter or digit	= java letter | "0" | "1" | (...) ;
 *  char					= java letter or digit | "." | "(" | ")" | "[" | "]" | "!" | "@" | "#" | (...);
 *  index char				= char - "]"
 *  
 *  java identifier			= java letter , {java letter or digit}
 *  method sign				= "(" , ")"
 *  index					= "[" , index char , {index char} , "]" ;
 *  
 *  property				= java identifier , [ index | method sign ];
 *  property expression		= property , { "." , property expression } ;
 *  
 * </code>
 * 
 * @author Pedro Santos
 */
public class PropertyExpressionParser
{

	private static final char END_OF_EXPRESSION = (char)-1;
	private String text;
	private int currentPosition = 0;
	private int nextPosition = 1;
	private char currentToken;
	private char lookaheadToken;

	private char advance()
	{
		currentPosition = nextPosition;
		currentToken = lookaheadToken;

		nextPosition += 1;
		if (nextPosition >= text.length())
			lookaheadToken = END_OF_EXPRESSION;
		else
			lookaheadToken = text.charAt(nextPosition);
		return currentToken;
	}

	PropertyExpression parse(String text)
	{
		this.text = text;
		if (text == null || text.isEmpty())
		{
			throw new ParserException("No expression was given to be parsed.");
		}
		else if (text.length() == 1)
		{
			lookaheadToken = END_OF_EXPRESSION;
		}
		else
		{
			lookaheadToken = text.charAt(1);
		}
		currentToken = text.charAt(0);
		return expression();

	}

	private PropertyExpression expression()
	{
		PropertyExpression expression = new PropertyExpression();
		expression.property = property();
		switch (lookaheadToken)
		{
			case '.' :
				advance();// skips the dot
				advance();// advances to the next expression
				expression.next = expression();
				return expression;
			case END_OF_EXPRESSION :
				return expression;
			default :
				throw new ParserException("expecting a new expression but got: " + currentToken);
		}
	}

	private Property property()
	{
		Property property = new Property();
		if (currentToken == '[')
		{
			property.index = index();
			return property;
		}
		else
		{
			property.javaIdentifier = javaIdentifier();
			if (lookaheadToken == '[')
			{
				advance();// skips left bracket
				property.index = index();
			}
			else if (lookaheadToken == '(')
			{
				advance(); // skips left bracket
				property.hasMethodSign = methodSign();
			}
			return property;
		}
	}


	private CharSequence javaIdentifier()
	{
		if (!isJavaIdentifierStart(currentToken))
		{
			throw new ParserException("Expeting a java identifier but got a :" + currentToken);
		}
		int begin = currentPosition;
		while (isJavaIdentifierPart(lookaheadToken))
		{
			advance();
		}
		return text.substring(begin, nextPosition);
	}

	private CharSequence index()
	{
		advance();// escape bracket
		if (currentToken == ']')
		{
			throw new ParserException(
				format("Expecting a property index but found empty brakets: '%s<--'",
					text.substring(0, nextPosition)));
		}
		int begin = currentPosition;
		while (lookaheadToken != ']')
		{
			advance();
		}
		advance();// escape bracket
		return text.substring(begin, currentPosition);
	}

	private boolean methodSign()
	{
		if (lookaheadToken != ')')
		{
			throw new ParserException("expecting a method sign but got: " + currentToken);
		}
		advance();// skips right bracket
		return true;
	}
}