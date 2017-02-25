package org.apache.wicket.core.util.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.wicket.core.util.parser.PropertyExpression.BeanProperty;
import org.apache.wicket.core.util.parser.PropertyExpression.JavaProperty;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PropertyExpressionParserTest
{

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private PropertyExpressionParser parser = new PropertyExpressionParser();

	@Test
	public void shouldParsePropertyExpressions()
	{
		PropertyExpression expression = parser.parse("person");
		assertThat(expression.index, nullValue());
		assertThat(expression.beanProperty, nullValue());
		assertThat(expression.javaProperty, is(new JavaProperty("person", null, false)));
		assertThat(expression.next, nullValue());
	}

	@Test
	public void shouldParsePropertyExpressionStartingWithDigits()
	{
		PropertyExpression expression = parser.parse("1person");
		assertThat(expression.index, nullValue());
		assertThat(expression.beanProperty, is(new BeanProperty("1person", null)));
		assertThat(expression.javaProperty, nullValue());
		assertThat(expression.next, nullValue());
	}

	@Test
	public void shouldParseShortPropertyExpressions()
	{
		PropertyExpression expression = parser.parse("a");
		assertThat(expression.index, nullValue());
		assertThat(expression.beanProperty, nullValue());
		assertThat(expression.javaProperty, is(new JavaProperty("a", null, false)));
		assertThat(expression.next, nullValue());
	}

	@Test
	public void shouldParseIndexedPropertyExpressions()
	{
		PropertyExpression expression = parser.parse("person[age]");
		assertThat(expression.index, nullValue());
		assertThat(expression.beanProperty, nullValue());
		assertThat(expression.javaProperty, is(new JavaProperty("person", "age", false)));
		assertThat(expression.next, nullValue());
	}

	@Test
	public void shouldParseMethodExpressions()
	{
		PropertyExpression expression = parser.parse("person()");
		assertThat(expression.index, nullValue());
		assertThat(expression.beanProperty, nullValue());
		assertThat(expression.javaProperty, is(new JavaProperty("person", null, true)));
		assertThat(expression.next, nullValue());
	}

	@Test
	public void shouldParsePropertyExpressionsWithSpaceInMethod()
	{
		final PropertyExpression parse = parser.parse("person( )");
		assertThat(parse.javaProperty, is(new JavaProperty("person", null, true)));
	}

	@Test
	public void shouldParseIndexExpressions()
	{
		PropertyExpression expression = parser.parse("[person#name]");
		assertThat(expression.index, is("person#name"));
		assertThat(expression.javaProperty, nullValue());
		assertThat(expression.next, nullValue());
	}

	@Test
	public void shouldParseChainedPropertyExpressions()
	{
		PropertyExpression expression = parser.parse("person.child");
		assertThat(expression.javaProperty, is(new JavaProperty("person", null, false)));
		assertThat(expression.next.javaProperty, is(new JavaProperty("child", null, false)));
	}

	@Test
	public void shouldParseShortChainedPropertyExpressions()
	{
		PropertyExpression expression = parser.parse("a.b");
		assertThat(expression.javaProperty, is(new JavaProperty("a", null, false)));
		assertThat(expression.next.javaProperty, is(new JavaProperty("b", null, false)));
	}

	@Test
	public void shouldParseChainedIndexedPropertyExpressions()
	{
		PropertyExpression expression = parser.parse("person[1].child");
		assertThat(expression.javaProperty, is(new JavaProperty("person", "1", false)));
		assertThat(expression.next.javaProperty, is(new JavaProperty("child", null, false)));
	}

	@Test
	public void shouldParseChainedMethodExpressions()
	{
		PropertyExpression expression = parser.parse("person().child");
		assertThat(expression.javaProperty, is(new JavaProperty("person", null, true)));
		assertThat(expression.next.javaProperty, is(new JavaProperty("child", null, false)));
	}

	@Test
	public void shouldParseChainedIndexExpressions()
	{
		PropertyExpression expression = parser.parse("[person].child");
		assertThat(expression.index, is("person"));
		assertThat(expression.next.javaProperty, is(new JavaProperty("child", null, false)));
	}

	@Test
	public void shouldParseDeeperChainedPropertyExpressions()
	{
		PropertyExpression expression = parser.parse("person.child.name");
		assertThat(expression.javaProperty, is(new JavaProperty("person", null, false)));
		assertThat(expression.next.javaProperty, is(new JavaProperty("child", null, false)));
		assertThat(expression.next.next.javaProperty, is(new JavaProperty("name", null, false)));
	}


	@Test
	public void shouldCheckExpressions()
	{
		expectedException.expect(ParserException.class);
		expectedException.expectMessage("No expression was given to be parsed.");
		parser.parse("");
	}

	@Test
	public void shouldFailParsePropertyExpressionsWithSpace()
	{
		expectedException.expect(ParserException.class);
		expectedException.expectMessage(
			"Expecting a new expression but got the invalid character ' ' at: 'per <--'");
		parser.parse("per son");
	}

	@Test
	public void shouldReportEmptyIndexBrackets()
	{
		expectedException.expect(ParserException.class);
		expectedException
			.expectMessage("Expecting a property index but found empty brakets: 'person[]<--'");
		parser.parse("person[]");
	}

	@Test
	public void shouldReportMethodsCantHaveParameters()
	{
		expectedException.expect(ParserException.class);
		expectedException.expectMessage(
			"The expression can't have method parameters: 'repository.getPerson(<--'");
		parser.parse("repository.getPerson(filter)");
	}

	@Test
	public void shouldFailParseInvalidBeanProperty()
	{
		expectedException.expect(ParserException.class);
		expectedException.expectMessage(
			"Expecting a new expression but got the invalid character '#' at: 'repository.prop#<--'");
		parser.parse("repository.prop#name");
	}

	@Test
	public void shouldFailParseMethodsStartingWithInvalidCharacter()
	{
		expectedException.expect(ParserException.class);
		expectedException
			.expectMessage("Expecting a valid method name but got: 'repository.0method(<--'");
		parser.parse("repository.0method()");
	}

}
