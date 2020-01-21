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
package org.apache.wicket.csp;

import static org.apache.wicket.csp.CSPDirective.CHILD_SRC;
import static org.apache.wicket.csp.CSPDirective.DEFAULT_SRC;
import static org.apache.wicket.csp.CSPDirective.FRAME_SRC;
import static org.apache.wicket.csp.CSPDirective.REPORT_URI;
import static org.apache.wicket.csp.CSPDirective.SANDBOX;
import static org.apache.wicket.csp.CSPDirectiveSandboxValue.ALLOW_FORMS;
import static org.apache.wicket.csp.CSPDirectiveSandboxValue.EMPTY;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.NONE;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.SELF;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.WILDCARD;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CSPSettingRequestCycleListenerTest extends WicketTestCase
{
	private WicketTester wicketTester;

	@BeforeEach
	public void setUp()
	{
		wicketTester = new WicketTester(MockHomePage.class);
	}

	@Test
	public void testNullSrcInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, (String) null);
		});
	}

	@Test
	public void testEmptySrcInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, "");
		});
	}

	/**
	 * A value for any of the -src directives can be a number of predefined values (for most of them
	 * you can use {@link CSPDirectiveSrcValue}) or a correct URI.
	 */
	@Test
	public void testInvalidSrcInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, "abc?^()-_\'xyz");
		});
	}

	/**
	 * If {@code 'none'} is used for any of the -src directives, it must be the only value for that
	 * directive.
	 */
	@Test
	public void testMultipleSrcInputWithNoneIsRejected1()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, SELF, NONE);
		});
	}

	/**
	 * If {@code 'none'} is used for any of the -src directives, it must be the only value for that
	 * directive.
	 */
	@Test
	public void testMultipleSrcInputWithNoneIsRejected2()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, NONE, SELF);
		});
	}

	/**
	 * If {@code *} (asterisk) is used for any of the -src directives, it must be the only value for
	 * that directive.
	 */
	@Test
	public void testMultipleSrcInputWithStarIsRejected1()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		cspListener.blocking().add(DEFAULT_SRC, SELF);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, WILDCARD);
		});
	}

	/**
	 * If {@code *} (asterisk) is used for any of the -src directives, it must be the only value for
	 * that directive.
	 */
	@Test
	public void testMultipleSrcInputWithStarIsRejected2()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		cspListener.blocking().add(DEFAULT_SRC, WILDCARD);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, SELF);
		});
	}

	@Test
	public void testWrongSrcInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(DEFAULT_SRC, ALLOW_FORMS);
		});
	}

	@Test
	public void testWrongSandboxInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(SANDBOX, SELF);
		});
	}

	@Test
	public void testNullSandboxInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(SANDBOX, (String) null);
		});
	}

	@Test
	public void testEmptySandboxInputIsAccepted()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		cspListener.blocking().add(SANDBOX, CSPDirectiveSandboxValue.EMPTY);
	}

	@Test
	public void testInvalidSandboxInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(SANDBOX, "abcxyz");
		});
	}

	@Test
	public void testMultipleSandboxInputWithEmptyStringIsRejected1()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		cspListener.blocking().add(SANDBOX, ALLOW_FORMS);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(SANDBOX, EMPTY);
		});
	}

	@Test
	public void testMultipleSandboxInputWithEmptyStringIsRejected2()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		cspListener.blocking().add(SANDBOX, EMPTY);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(SANDBOX, ALLOW_FORMS);
		});
	}

	@Test
	public void testNullReportUriInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(REPORT_URI, (String) null);
		});
	}

	@Test
	public void testEmptyReportUriInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(REPORT_URI, "");
		});
	}

	@Test
	public void testInvalidReportUriInputIsRejected()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			cspListener.blocking().add(REPORT_URI, "abc?^()-_\'xyz");
		});
	}

	@Test
	public void testAllCSPSrcDefaultEnumsAreSetCorrectly() throws NoSuchAlgorithmException
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());

		final int cspDirectiveCount = CSPDirective.values().length;
		final int cspDirectiveSrcValueCount = CSPDirectiveSrcValue.values().length;
		for (int i = 0; i < Math.max(cspDirectiveCount, cspDirectiveSrcValueCount); i++)
		{
			final CSPDirective cspDirective = CSPDirective.values()[i % cspDirectiveCount];
			// FRAME-SRC wordt al gezet door de aanroep voor CHILD-SRC
			if (!FRAME_SRC.equals(cspDirective) && cspDirective.getValue().endsWith("-src"))
			{
				final CSPDirectiveSrcValue cspDirectiveValue =
					CSPDirectiveSrcValue.values()[i % cspDirectiveSrcValueCount];
				cspListener.blocking().add(cspDirective, cspDirectiveValue);

				cspListener.reporting().add(cspDirective, cspDirectiveValue);
			}
		}

		List<String> headerErrors = checkHeaders(cspListener);

		if (!headerErrors.isEmpty())
		{
			Assertions.fail(headerErrors.toString());
		}
	}

	@Test
	public void testCSPReportUriDirectiveSetCorrectly()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		cspListener.blocking().add(REPORT_URI, "http://report.example.com");
		cspListener.reporting().add(REPORT_URI, "/example-report-uri");

		List<String> headerErrors = checkHeaders(cspListener);

		if (!headerErrors.isEmpty())
		{
			Assertions.fail(headerErrors.toString());
		}
	}

	@Test
	public void testCSPSandboxDirectiveSetCorrectly()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		final int cspSandboxDirectiveValueCount = CSPDirectiveSandboxValue.values().length;
		for (int i = 0; i < cspSandboxDirectiveValueCount; i++)
		{
			final CSPDirectiveSandboxValue cspDirectiveValue = CSPDirectiveSandboxValue.values()[i];
			if (cspDirectiveValue.equals(CSPDirectiveSandboxValue.EMPTY))
				continue;

			cspListener.blocking().add(SANDBOX, cspDirectiveValue);
			cspListener.reporting().add(SANDBOX, cspDirectiveValue);
		}

		List<String> headerErrors = checkHeaders(cspListener);

		if (!headerErrors.isEmpty())
		{
			Assertions.fail(headerErrors.toString());
		}
	}

	/**
	 * Not all browsers support child-src and still expect frame-src. This test asserts that
	 * frame-src is added when child-src is added.
	 */
	@Test
	public void testChildSrcDirectiveAlsoSetsFrameSrcDirective()
	{
		ContentSecurityPolicyEnforcer cspListener =
			new ContentSecurityPolicyEnforcer(tester.getApplication());
		cspListener.blocking().add(CHILD_SRC, SELF);
		cspListener.reporting().add(CHILD_SRC, SELF);
		List<String> headerErrors = checkHeaders(cspListener);

		if (!headerErrors.isEmpty())
		{
			Assertions.fail(headerErrors.toString());
		}
	}

	private List<String> checkHeaders(ContentSecurityPolicyEnforcer cspListener)
	{
		List<String> headerErrors = new ArrayList<>();
		wicketTester.getRequestCycle().getListeners().add(cspListener);
		wicketTester.executeUrl("/");
		String cspHeaderValue =
			wicketTester.getLastResponse().getHeader(CSPHeaderMode.BLOCKING.getHeader());
		String cspReportingHeaderValue =
			wicketTester.getLastResponse().getHeader(CSPHeaderMode.REPORT_ONLY.getHeader());

		if (cspHeaderValue == null)
		{
			headerErrors.add(String.format("Header %s expected but either not present or empty",
				CSPHeaderMode.BLOCKING.getHeader()));
		}
		if (cspReportingHeaderValue == null)
		{
			headerErrors.add(String.format("Header %s expected but either not present or empty",
				CSPHeaderMode.REPORT_ONLY.getHeader()));
		}

		if (!headerErrors.isEmpty())
		{
			return headerErrors;
		}

		List<String> headerValueErrors = new ArrayList<>();
		List<String> blockingHeaderValueErrors = checkCSPHeaderValues(cspHeaderValue);
		List<String> reportingHeaderValueErrors = checkCSPHeaderValues(cspReportingHeaderValue);

		if (!blockingHeaderValueErrors.isEmpty())
		{
			headerValueErrors.add("Blocking-mode CSP header value issues: "
				+ blockingHeaderValueErrors.stream().collect(Collectors.joining("; ")) + ".");
		}
		if (!reportingHeaderValueErrors.isEmpty())
		{
			headerValueErrors.add("Reporting-mode CSP header value issues: "
				+ reportingHeaderValueErrors.stream().collect(Collectors.joining("; ")) + ".");
		}
		return headerValueErrors;
	}

	private List<String> checkCSPHeaderValues(String cspHeaderValue)
	{
		Set<String> directiveValues = Stream.of(CSPDirective.values())
			.map(CSPDirective::getValue)
			.collect(Collectors.toSet());
		Set<String> directiveSrcValues = Stream.of(CSPDirectiveSrcValue.values())
			.map(CSPDirectiveSrcValue::getValue)
			.collect(Collectors.toSet());
		Set<String> directiveSandboxValues = Stream.of(CSPDirectiveSandboxValue.values())
			.map(CSPDirectiveSandboxValue::getValue)
			.collect(Collectors.toSet());

		final List<String> errors = new ArrayList<>();
		String[] directives = cspHeaderValue.split(";");
		boolean hasChildSrc = false, hasFrameSrc = false;
		for (String directive : directives)
		{
			directive = directive.trim();
			String[] values = directive.split("\\s");
			String directiveName = values[0];
			if (!directiveValues.contains(directiveName))
			{
				errors.add(
					String.format("Directive %s is not a valid directive name", directiveName));
			}
			else
			{
				if (CSPDirective.fromValue(directiveName).equals(FRAME_SRC))
				{
					hasFrameSrc = true;
				}
				if (CSPDirective.fromValue(directiveName).equals(CHILD_SRC))
				{
					hasChildSrc = true;
				}
				for (int i = 1; i < values.length; i++)
				{
					final String trimmedValue = values[i].trim();
					final boolean isValidDefaultSrcValue =
						directiveSrcValues.contains(trimmedValue);
					final boolean isValidDefaultSandboxValue =
						directiveSandboxValues.contains(trimmedValue);
					if (!(isValidDefaultSrcValue || isValidDefaultSandboxValue
						|| isValidDirectiveValue(trimmedValue)))
					{
						errors.add(
							String.format("Value %s is not a valid directive value", trimmedValue));
					}
				}
			}
		}

		if (hasFrameSrc != hasChildSrc)
		{
			String presentDirective = hasFrameSrc ? FRAME_SRC.getValue() : CHILD_SRC.getValue();
			String notPresentDirective = !hasFrameSrc ? FRAME_SRC.getValue() : CHILD_SRC.getValue();
			errors.add(String.format("Directive %s present without directive %s for fallback",
				presentDirective, notPresentDirective));
		}

		return errors;
	}

	// @see: http://content-security-policy.com/#source_list
	private boolean isValidDirectiveValue(String directiveValue)
	{
		if ("*".equals(directiveValue))
			return true;
		else if ("data:".equals(directiveValue) || "https:".equals(directiveValue))
			return true;

		// strip off "*." for "*.example.com" so we can check "example.com" to be a valid
		// URI.
		if (directiveValue.startsWith("*."))
			directiveValue = directiveValue.substring(2);
		try
		{
			new URI(directiveValue);
			return true;
		}
		catch (URISyntaxException ignored)
		{
			// fall through
		}

		return false;
	}
}
