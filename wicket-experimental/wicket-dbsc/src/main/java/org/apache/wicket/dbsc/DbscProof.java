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
package org.apache.wicket.dbsc;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;

/**
 * A DBSC proof: the JWT a browser signs with its session key, sent in the
 * {@code Secure-Session-Response} header.
 * <p>
 * Only {@code ES256} on P-256 and {@code RS256} with 2048 to 4096 bits are accepted. A proof
 * signed with {@code none} is refused, since it proves nothing, and so is one that lists critical
 * header extensions, none of which this class understands.
 */
final class DbscProof
{
	static final String ES256 = "ES256";

	static final String RS256 = "RS256";

	private static final String TYPE = "dbsc+jwt";

	private static final int MIN_RSA_BITS = 2048;

	private static final int MAX_RSA_BITS = 4096;

	private static final int MAX_RSA_EXPONENT_BITS = 32;

	private final JSONObject header;

	private final JSONObject payload;

	private final byte[] signingInput;

	private final byte[] signature;

	private DbscProof(JSONObject header, JSONObject payload, byte[] signingInput, byte[] signature)
	{
		this.header = header;
		this.payload = payload;
		this.signingInput = signingInput;
		this.signature = signature;
	}

	/**
	 * Parses a proof without checking its signature.
	 *
	 * @param jwt
	 *            the JWT in compact serialization
	 * @return the proof
	 * @throws InvalidProofException
	 *             if the JWT is malformed, not of type {@code dbsc+jwt}, signed with an algorithm
	 *             other than {@code ES256} or {@code RS256}, or has a {@code crit} header
	 */
	static DbscProof parse(String jwt) throws InvalidProofException
	{
		String[] parts = jwt.split("\\.", -1);
		if (parts.length != 3)
		{
			throw new InvalidProofException("A DBSC proof has three parts, not " + parts.length);
		}
		JSONObject header;
		JSONObject payload;
		byte[] signature;
		try
		{
			header = new JSONObject(decodeText(parts[0]));
			payload = new JSONObject(decodeText(parts[1]));
			signature = Base64.getUrlDecoder().decode(parts[2]);
		}
		catch (IllegalArgumentException | JSONException e)
		{
			throw new InvalidProofException("Malformed DBSC proof", e);
		}
		if (!TYPE.equals(header.optString("typ")))
		{
			throw new InvalidProofException("A DBSC proof has type " + TYPE);
		}
		String algorithm = header.optString("alg");
		if (!ES256.equals(algorithm) && !RS256.equals(algorithm))
		{
			throw new InvalidProofException("Unsupported algorithm '" + algorithm + "'");
		}
		if (header.has("crit"))
		{
			throw new InvalidProofException("A DBSC proof has no critical header extensions");
		}
		byte[] signingInput = (parts[0] + '.' + parts[1]).getBytes(StandardCharsets.US_ASCII);
		return new DbscProof(header, payload, signingInput, signature);
	}

	/**
	 * @return {@code ES256} or {@code RS256}
	 */
	String algorithm()
	{
		return header.getString("alg");
	}

	/**
	 * @return the {@code jti} claim, the challenge the proof answers, or {@code null}
	 */
	String challenge()
	{
		Object jti = payload.opt("jti");
		return jti instanceof String challenge ? challenge : null;
	}

	/**
	 * Reads the public key a registration proof carries.
	 *
	 * @return the key
	 * @throws InvalidProofException
	 *             if the proof carries no key or one that does not fit its algorithm
	 */
	PublicKey embeddedKey() throws InvalidProofException
	{
		// the draft puts the key in the header, its own example in the payload
		JSONObject jwk = header.optJSONObject("jwk");
		if (jwk == null)
		{
			jwk = payload.optJSONObject("jwk");
		}
		if (jwk == null)
		{
			throw new InvalidProofException("The proof carries no public key");
		}
		return toPublicKey(jwk, algorithm());
	}

	/**
	 * @param key
	 *            the session's public key
	 * @return whether the proof was signed with the private half of {@code key}
	 */
	boolean isSignedBy(PublicKey key)
	{
		try
		{
			Signature verifier = Signature.getInstance(
				ES256.equals(algorithm()) ? "SHA256withECDSAinP1363Format" : "SHA256withRSA");
			verifier.initVerify(key);
			verifier.update(signingInput);
			return verifier.verify(signature);
		}
		catch (GeneralSecurityException e)
		{
			return false;
		}
	}

	private static PublicKey toPublicKey(JSONObject jwk, String algorithm)
		throws InvalidProofException
	{
		try
		{
			String type = jwk.optString("kty");
			if (ES256.equals(algorithm) && "EC".equals(type) &&
				"P-256".equals(jwk.optString("crv")))
			{
				AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
				parameters.init(new ECGenParameterSpec("secp256r1"));
				ECParameterSpec curve = parameters.getParameterSpec(ECParameterSpec.class);
				ECPoint point = new ECPoint(unsigned(jwk.getString("x")),
					unsigned(jwk.getString("y")));
				if (!isOnCurve(point, curve.getCurve()))
				{
					throw new InvalidProofException("The public key is not a point on P-256");
				}
				return KeyFactory.getInstance("EC")
					.generatePublic(new ECPublicKeySpec(point, curve));
			}
			if (RS256.equals(algorithm) && "RSA".equals(type))
			{
				BigInteger modulus = unsigned(jwk.getString("n"));
				if (modulus.bitLength() < MIN_RSA_BITS || modulus.bitLength() > MAX_RSA_BITS)
				{
					throw new InvalidProofException("An RSA key needs " + MIN_RSA_BITS + " to " +
						MAX_RSA_BITS + " bits");
				}
				BigInteger exponent = unsigned(jwk.getString("e"));
				if (exponent.bitLength() > MAX_RSA_EXPONENT_BITS)
				{
					throw new InvalidProofException(
						"An RSA exponent has at most " + MAX_RSA_EXPONENT_BITS + " bits");
				}
				return KeyFactory.getInstance("RSA")
					.generatePublic(new RSAPublicKeySpec(modulus, exponent));
			}
		}
		catch (GeneralSecurityException | IllegalArgumentException | JSONException e)
		{
			throw new InvalidProofException("Malformed public key", e);
		}
		throw new InvalidProofException("The public key does not fit algorithm " + algorithm);
	}

	private static boolean isOnCurve(ECPoint point, EllipticCurve curve)
	{
		BigInteger p = ((ECFieldFp)curve.getField()).getP();
		BigInteger x = point.getAffineX();
		BigInteger y = point.getAffineY();
		if (x.compareTo(p) >= 0 || y.compareTo(p) >= 0)
		{
			return false;
		}
		BigInteger left = y.pow(2).mod(p);
		BigInteger right = x.pow(3).add(curve.getA().multiply(x)).add(curve.getB()).mod(p);
		return left.equals(right);
	}

	private static BigInteger unsigned(String base64Url)
	{
		return new BigInteger(1, Base64.getUrlDecoder().decode(base64Url));
	}

	private static String decodeText(String base64Url)
	{
		return new String(Base64.getUrlDecoder().decode(base64Url), StandardCharsets.UTF_8);
	}

	/**
	 * Thrown for a proof that cannot be used.
	 */
	static final class InvalidProofException extends Exception
	{
		private static final long serialVersionUID = 1L;

		InvalidProofException(String message)
		{
			super(message);
		}

		InvalidProofException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
