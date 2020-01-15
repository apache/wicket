package org.apache.wicket.csp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.Strings;

/**
 * An {@link IRequestCycleListener} that adds {@code Content-Security-Policy} and/or
 * {@code Content-Security-Policy-Report-Only} headers based on the supplied configuration.
 *
 * See also the {@code CSPSettingRequestCycleListenerTest}.
 *
 * Example usage:
 *
 * <pre>
 * {@code
 *      myApplication.getRequestCycleListeners().add(
 * 			new CSPSettingRequestCycleListener()
 * 				.addBlockingDirective(CSPDirective.DEFAULT_SRC, CSPDirectiveSrcValue.NONE)
 * 				.addBlockingDirective(CSPDirective.SCRIPT_SRC, CSPDirectiveSrcValue.SELF)
 * 				.addBlockingDirective(CSPDirective.IMG_SRC, CSPDirectiveSrcValue.SELF)
 * 				.addBlockingDirective(CSPDirective.FONT_SRC, CSPDirectiveSrcValue.SELF));
 *
 * 		 myApplication.getRequestCycleListeners().add(
 * 			new CSPSettingRequestCycleListener()
 * 				.addReportingDirective(CSPDirective.DEFAULT_SRC, CSPDirectiveSrcValue.NONE)
 * 				.addReportingDirective(CSPDirective.IMG_SRC, CSPDirectiveSrcValue.SELF)
 * 				.addReportingDirective(CSPDirective.FONT_SRC, CSPDirectiveSrcValue.SELF)
 * 				.addReportingDirective(CSPDirective.SCRIPT_SRC, CSPDirectiveSrcValue.SELF));
 * 	}
 * </pre>
 *
 * {@code frame-src} has been deprecated since CSP 2.0 and replaced by {@code child-src}. Some
 * browsers do not yet support {@code child-src} and expect {@code frame-src} instead. When
 * {@code child-src} is added, a matching {@code frame-src} is added automatically for
 * compatibility.
 *
 * @see "http://www.w3.org/TR/CSP2/"
 * @see "https://developer.mozilla.org/en-US/docs/Web/Security/CSP/CSP_policy_directives"
 *
 * @author Sven Haster
 * @author Emond Papegaaij
 */
public class CSPSettingRequestCycleListener implements IRequestCycleListener
{
	public static MetaDataKey<String> NONCE_KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	public static interface CSPRenderable
	{
		public String render(CSPSettingRequestCycleListener listener, RequestCycle cycle);
	}

	private static final class FixedCSPDirective implements CSPRenderable
	{
		private String value;

		public FixedCSPDirective(String value)
		{
			if (Strings.isEmpty(value))
				throw new IllegalArgumentException(
					"CSP directive cannot have empty or null values");
			this.value = value;
		}

		@Override
		public String render(CSPSettingRequestCycleListener listener, RequestCycle cycle)
		{
			return value;
		}
	}

	/**
	 * An enum holding the default values for -src directives including the mandatory single quotes
	 */
	public enum CSPDirectiveSrcValue implements CSPRenderable
	{
		NONE("'none'"),
		WILDCARD("*"),
		SELF("'self'"),
		UNSAFE_INLINE("'unsafe-inline'"),
		UNSAFE_EVAL("'unsafe-eval'"),
		STRICT_DYNAMIC("'strict-dynamic'"),
		NONCE("'nonce-%1$s'")
		{
			@Override
			public String render(CSPSettingRequestCycleListener listener, RequestCycle cycle)
			{
				return String.format(getValue(), listener.getNonce(cycle));
			}
		};

		private String value;

		private CSPDirectiveSrcValue(String value)
		{
			this.value = value;
		}

		@Override
		public String render(CSPSettingRequestCycleListener listener, RequestCycle cycle)
		{
			return value;
		}

		public String getValue()
		{
			return value;
		}
	}

	/**
	 * An enum representing the only possible values for the sandbox directive
	 */
	public enum CSPDirectiveSandboxValue implements CSPRenderable
	{
		ALLOW_FORMS("allow-forms"),
		ALLOW_SAME_ORIGIN("allow-same-origin"),
		ALLOW_SCRIPTS("allow-scripts"),
		ALLOW_TOP_NAVIGATION("allow-top-navigation"),
		EMPTY("");

		private String value;

		private CSPDirectiveSandboxValue(String value)
		{
			this.value = value;
		}

		public String getValue()
		{
			return value;
		}

		@Override
		public String render(CSPSettingRequestCycleListener listener, RequestCycle cycle)
		{
			return value;
		}
	}

	/** An enum holding the possible CSP Directives */
	public enum CSPDirective
	{
		DEFAULT_SRC("default-src"),
		SCRIPT_SRC("script-src"),
		STYLE_SRC("style-src"),
		IMG_SRC("img-src"),
		CONNECT_SRC("connect-src"),
		FONT_SRC("font-src"),
		OBJECT_SRC("object-src"),
		MANIFEST_SRC("manifest-src"),
		MEDIA_SRC("media-src"),
		CHILD_SRC("child-src"),
		FRAME_ANCESTORS("frame-ancestors"),
		@Deprecated
		/** @deprecated Gebruik CHILD-SRC, deze zet ook automatisch FRAME-SRC. */
		FRAME_SRC("frame-src"),
		SANDBOX("sandbox")
		{
			@Override
			protected void checkValueForDirective(CSPRenderable value,
					List<CSPRenderable> existingDirectiveValues)
			{
				if (!existingDirectiveValues.isEmpty())
				{
					if (CSPDirectiveSandboxValue.EMPTY.equals(value))
					{
						throw new IllegalArgumentException(
							"A sandbox directive can't contain an empty string if it already contains other values ");
					}
					if (existingDirectiveValues.contains(CSPDirectiveSandboxValue.EMPTY))
					{
						throw new IllegalArgumentException(
							"A sandbox directive can't contain other values if it already contains an empty string");
					}
				}

				if (!(value instanceof CSPDirectiveSandboxValue))
				{
					throw new IllegalArgumentException(
						"A sandbox directive can only contain values from CSPDirectiveSandboxValue or be empty");
				}
			}
		},
		REPORT_URI("report-uri")
		{
			@Override
			protected void checkValueForDirective(CSPRenderable value,
					List<CSPRenderable> existingDirectiveValues)
			{
				if (!existingDirectiveValues.isEmpty())
				{
					throw new IllegalArgumentException(
						"A report-uri directive can only contain one uri");
				}
				if (!(value instanceof FixedCSPDirective))
				{
					throw new IllegalArgumentException(
						"A report-uri directive can only contain an URI");
				}
				try
				{
					new URI(value.render(null, null));
				}
				catch (URISyntaxException urise)
				{
					throw new IllegalArgumentException("Illegal URI for report-uri directive",
						urise);
				}
			}
		};

		private String value;

		private CSPDirective(String value)
		{
			this.value = value;
		}

		public String getValue()
		{
			return value;
		}

		protected void checkValueForDirective(CSPRenderable value,
				List<CSPRenderable> existingDirectiveValues)
		{
			if (!existingDirectiveValues.isEmpty())
			{
				if (CSPDirectiveSrcValue.WILDCARD.equals(value)
					|| CSPDirectiveSrcValue.NONE.equals(value))
				{
					throw new IllegalArgumentException(
						"A -src directive can't contain an * or a 'none' if it already contains other values ");
				}
				if (existingDirectiveValues.contains(CSPDirectiveSrcValue.WILDCARD)
					|| existingDirectiveValues.contains(CSPDirectiveSrcValue.NONE))
				{
					throw new IllegalArgumentException(
						"A -src directive can't contain other values if it already contains an * or a 'none'");
				}
			}

			if (value instanceof CSPDirectiveSrcValue)
			{
				return;
			}

			if (value instanceof CSPDirectiveSandboxValue)
			{
				throw new IllegalArgumentException(
					"A -src directive can't contain any of the sandbox directive values");
			}

			String strValue = value.render(null, null);
			if ("data:".equals(strValue) || "https:".equals(strValue))
			{
				return;
			}

			// strip off "*." so "*.example.com" becomes "example.com" and we can check if
			// it
			// is a valid uri
			if (strValue.startsWith("*."))
			{
				strValue = strValue.substring(2);
			}

			try
			{
				new URI(strValue);
			}
			catch (URISyntaxException urise)
			{
				throw new IllegalArgumentException("Illegal URI for -src directive", urise);
			}
		}

		/**
		 * @return The CSPDirective constant whose value-parameter equals the input-parameter or
		 *         {@code null} if none can be found.
		 */
		public static CSPDirective fromValue(String value)
		{
			if (Strings.isEmpty(value))
				return null;
			for (int i = 0; i < values().length; i++)
			{
				if (value.equals(values()[i].getValue()))
					return values()[i];
			}
			return null;
		}
	}

	private enum CSPHeaderMode
	{
		BLOCKING("Content-Security-Policy"),
		REPORT_ONLY("Content-Security-Policy-Report-Only");

		private final String header;

		private CSPHeaderMode(String header)
		{
			this.header = header;
		}

		public String getHeader()
		{
			return header;
		}

		public String getLegacyHeader()
		{
			return "X-" + getHeader();
		}
	}

	public class CSPConfiguration
	{
		private CSPHeaderMode mode;

		private Map<CSPDirective, List<CSPRenderable>> directives =
			new EnumMap<>(CSPDirective.class);

		private boolean addLegacyHeaders = false;

		private CSPConfiguration(CSPHeaderMode mode)
		{
			this.mode = mode;
		}

		public CSPHeaderMode getMode()
		{
			return mode;
		}

		/**
		 * True when legacy headers should be added.
		 * 
		 * @return True when legacy headers should be added.
		 */
		public boolean isAddLegacyHeaders()
		{
			return addLegacyHeaders;
		}

		/**
		 * Enable legacy {@code X-Content-Security-Policy} headers for older browsers, such as IE.
		 * 
		 * @param addLegacyHeaders
		 *            True when the legacy headers should be added.
		 * @return {@code this} for chaining
		 */
		public CSPConfiguration setAddLegacyHeaders(boolean addLegacyHeaders)
		{
			this.addLegacyHeaders = addLegacyHeaders;
			return this;
		}

		public CSPConfiguration addDirective(CSPDirective directive, CSPDirectiveSrcValue... values)
		{
			for (CSPDirectiveSrcValue value : values)
			{
				doAddDirective(directive, value);
			}
			return this;
		}

		/**
		 * Adds any of the default values to the sandbox directive for the 'blocking' CSP header.
		 * Use {@link #addBlockingDirective(CSPDirective, String...)} with the sandbox
		 * {@link CSPDirective} and a single empty string (<em>not</em> {@code null}) to add the
		 * empty sandbox directive.
		 */
		public CSPConfiguration addDirective(CSPDirective sandboxDirective,
				CSPDirectiveSandboxValue... values)
		{
			for (CSPDirectiveSandboxValue value : values)
			{
				doAddDirective(sandboxDirective, value);
			}
			return this;
		}

		/**
		 * Adds any value to a directive for the 'blocking' CSP header. Use
		 * {@link #addBlockingDirective(CSPDirective, CSPDirectiveSandboxValue...)} and
		 * {@link #addBlockingDirective(CSPDirective, CSPDirectiveSrcValue...)} for the default
		 * values for the sandbox and -src directives.
		 */
		public CSPConfiguration addDirective(CSPDirective directive, String... values)
		{
			for (String value : values)
			{
				doAddDirective(directive, new FixedCSPDirective(value));
			}
			return this;
		}

		public boolean isSet()
		{
			return !directives.isEmpty();
		}

		private CSPConfiguration doAddDirective(CSPDirective directive, CSPRenderable value)
		{
			// Add backwards compatible frame-src
			// see http://caniuse.com/#feat=contentsecuritypolicy2
			if (CSPDirective.CHILD_SRC.equals(directive))
			{
				doAddDirective(CSPDirective.FRAME_SRC, value);
			}
			List<CSPRenderable> values =
				directives.computeIfAbsent(directive, x -> new ArrayList<>());
			directive.checkValueForDirective(value, values);
			values.add(value);
			return this;
		}

		// @returns "key1 value1a value1b; key2 value2a; key3 value3a value3b value3c"
		public String renderHeaderValue(RequestCycle cycle)
		{
			return directives.entrySet()
				.stream()
				.map(e -> e.getKey().getValue() + " "
					+ e.getValue()
						.stream()
						.map(r -> r.render(CSPSettingRequestCycleListener.this, cycle))
						.collect(Collectors.joining(" ")))
				.collect(Collectors.joining("; "));
		}
	}

	private Function<Integer, byte[]> randomSupplier;

	private Map<CSPHeaderMode, CSPConfiguration> configs = new HashMap<>();

	public CSPSettingRequestCycleListener()
	{
	}

	public CSPSettingRequestCycleListener(Function<Integer, byte[]> randomSupplier)
	{
		this.randomSupplier = randomSupplier;
	}

	public CSPConfiguration blocking()
	{
		return configs.computeIfAbsent(CSPHeaderMode.BLOCKING, CSPConfiguration::new);
	}

	public CSPConfiguration reporting()
	{
		return configs.computeIfAbsent(CSPHeaderMode.REPORT_ONLY, CSPConfiguration::new);
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
	{
		WebResponse webResponse = (WebResponse) cycle.getResponse();
		configs.values().stream().filter(CSPConfiguration::isSet).forEach(config -> {
			String headerValue = config.renderHeaderValue(cycle);
			webResponse.setHeader(config.getMode().getHeader(), headerValue);
			if (config.isAddLegacyHeaders())
				webResponse.setHeader(config.getMode().getLegacyHeader(), headerValue);
		});
	}

	public String getNonce(RequestCycle cycle)
	{
		String nonce = cycle.getMetaData(NONCE_KEY);
		if (nonce == null)
		{
			nonce = Base64.getEncoder().encodeToString(randomSupplier.apply(12));
			cycle.setMetaData(NONCE_KEY, nonce);
		}
		return nonce;
	}
}
