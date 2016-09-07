package org.apache.wicket.core.util.lang;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.wicket.core.util.lang.ParserException;
import org.apache.wicket.core.util.lang.PropertyExpression;
import org.apache.wicket.core.util.lang.PropertyExpressionParser;
import org.apache.wicket.core.util.lang.PropertyExpression.Property;
import org.hamcrest.CoreMatchers;
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
		PropertyExpression parse = parser.parse("a");
		assertThat(parse.property, is(new Property("a", null, false)));
		assertThat(parse.next, CoreMatchers.nullValue());
	}

	@Test
	public void shouldParseShortPropertyExpressions()
	{
		PropertyExpression parse = parser.parse("person");
		assertThat(parse.property, is(new Property("person", null, false)));
		assertThat(parse.next, CoreMatchers.nullValue());
	}

	@Test
	public void shouldParseIndexedPropertyExpressions()
	{
		PropertyExpression parse = parser.parse("person[age]");
		assertThat(parse.property, is(new Property("person", "age", false)));
		assertThat(parse.next, CoreMatchers.nullValue());
	}

	@Test
	public void shouldParseMethodExpressions()
	{
		PropertyExpression parse = parser.parse("person()");
		assertThat(parse.property, is(new Property("person", null, true)));
		assertThat(parse.next, CoreMatchers.nullValue());
	}

	@Test
	public void shouldParseIndexExpressions()
	{
		PropertyExpression parse = parser.parse("[person#name]");
		assertThat(parse.property, is(new Property(null, "person#name", false)));
		assertThat(parse.next, CoreMatchers.nullValue());
	}

	@Test
	public void shouldParseChainedPropertyExpressions()
	{
		PropertyExpression parse = parser.parse("person.child");
		assertThat(parse.property, is(new Property("person", null, false)));
		assertThat(parse.next.property, is(new Property("child", null, false)));
	}

	@Test
	public void shouldParseShortChainedPropertyExpressions()
	{
		PropertyExpression parse = parser.parse("a.b");
		assertThat(parse.property, is(new Property("a", null, false)));
		assertThat(parse.next.property, is(new Property("b", null, false)));
	}

	@Test
	public void shouldParseChainedIndexedPropertyExpressions()
	{
		PropertyExpression parse = parser.parse("person[1].child");
		assertThat(parse.property, is(new Property("person", "1", false)));
		assertThat(parse.next.property, is(new Property("child", null, false)));
	}

	@Test
	public void shouldParseChainedMethodExpressions()
	{
		PropertyExpression parse = parser.parse("person().child");
		assertThat(parse.property, is(new Property("person", null, true)));
		assertThat(parse.next.property, is(new Property("child", null, false)));
	}

	@Test
	public void shouldParseDeeperChainedPropertyExpressions()
	{
		PropertyExpression parse = parser.parse("person.child.name");
		assertThat(parse.property, is(new Property("person", null, false)));
		assertThat(parse.next.property, is(new Property("child", null, false)));
		assertThat(parse.next.next.property, is(new Property("name", null, false)));
	}


	@Test
	public void shouldCheckExpressions()
	{
		expectedException.expect(ParserException.class);
		expectedException.expectMessage("No expression was given to be parsed.");
		parser.parse("");
	}

	@Test
	public void shouldReportEmptyIndexBrackets()
	{
		expectedException.expect(ParserException.class);
		expectedException
			.expectMessage("Expecting a property index but found empty brakets: 'person[]<--'");
		parser.parse("person[]");
	}

}
