package org.apache.wicket.protocol.https;

/**
 * Url scheme
 * 
 * @author igor
 */
enum Scheme {
	/** https */
	HTTPS {
		@Override
		public boolean usesStandardPort(HttpsConfig config)
		{
			return getPort(config) == 443;
		}

		@Override
		public int getPort(HttpsConfig config)
		{
			return config.getHttpsPort();
		}
	},
	/** http */
	HTTP {
		@Override
		public boolean usesStandardPort(HttpsConfig config)
		{
			return getPort(config) == 80;
		}

		@Override
		public int getPort(HttpsConfig config)
		{
			return config.getHttpPort();
		}
	},
	/** any, aka preserve current */
	ANY {
		@Override
		public String urlName()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isCompatibleWith(Scheme other)
		{
			return true;
		}

		@Override
		public boolean usesStandardPort(HttpsConfig config)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int getPort(HttpsConfig config)
		{
			throw new UnsupportedOperationException();
		}
	};


	/**
	 * @return scheme's url name
	 */
	public String urlName()
	{
		return name().toLowerCase();
	}

	/**
	 * Checks if two schemes are compatible. Compatible schemes do not require a redirect from the
	 * current scheme to the {@code other}.
	 * 
	 * @param other
	 * @return {@code true} iff the schemes are compatible.
	 */
	public boolean isCompatibleWith(Scheme other)
	{
		return this == other;
	}

	public abstract boolean usesStandardPort(HttpsConfig config);

	public abstract int getPort(HttpsConfig config);
}
