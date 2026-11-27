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
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;

import com.github.openjson.JSONObject;

/**
 * Plays the browser's part: holds a session key and signs DBSC proofs with it.
 */
final class TestBrowser
{
	private final KeyPair keys;

	private final String algorithm;

	private TestBrowser(KeyPair keys, String algorithm)
	{
		this.keys = keys;
		this.algorithm = algorithm;
	}

	static TestBrowser es256() throws GeneralSecurityException
	{
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec("secp256r1"));
		return new TestBrowser(generator.generateKeyPair(), DbscProof.ES256);
	}

	static TestBrowser rs256() throws GeneralSecurityException
	{
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return new TestBrowser(generator.generateKeyPair(), DbscProof.RS256);
	}

	/**
	 * @return a registration proof with the key in the JWT header, as the draft describes it
	 */
	String registrationProof(String challenge) throws GeneralSecurityException
	{
		return sign(header().put("jwk", jwk()), new JSONObject().put("jti", challenge));
	}

	/**
	 * @return a registration proof with the key in the payload, as the draft's example has it
	 */
	String registrationProofWithKeyInPayload(String challenge) throws GeneralSecurityException
	{
		return sign(header(), new JSONObject().put("jti", challenge).put("jwk", jwk()));
	}

	/**
	 * @return a registration proof that carries this browser's key but is signed by another one
	 */
	String registrationProofSignedBy(TestBrowser signer, String challenge)
		throws GeneralSecurityException
	{
		return signer.sign(header().put("jwk", jwk()), new JSONObject().put("jti", challenge));
	}

	String refreshProof(String challenge) throws GeneralSecurityException
	{
		return sign(header(), new JSONObject().put("jti", challenge));
	}

	/**
	 * @return an unsigned proof, with algorithm {@code none}
	 */
	static String unsignedProof(String challenge)
	{
		JSONObject header = new JSONObject().put("alg", "none").put("typ", "dbsc+jwt");
		return encode(header.toString()) + "." +
			encode(new JSONObject().put("jti", challenge).toString()) + ".";
	}

	private JSONObject header()
	{
		return new JSONObject().put("alg", algorithm).put("typ", "dbsc+jwt");
	}

	private JSONObject jwk()
	{
		if (keys.getPublic() instanceof ECPublicKey ec)
		{
			return new JSONObject().put("kty", "EC")
				.put("crv", "P-256")
				.put("x", encode(fixedLength(ec.getW().getAffineX(), 32)))
				.put("y", encode(fixedLength(ec.getW().getAffineY(), 32)));
		}
		RSAPublicKey rsa = (RSAPublicKey)keys.getPublic();
		return new JSONObject().put("kty", "RSA")
			.put("n", encode(unsigned(rsa.getModulus())))
			.put("e", encode(unsigned(rsa.getPublicExponent())));
	}

	private String sign(JSONObject header, JSONObject payload) throws GeneralSecurityException
	{
		String signingInput = encode(header.toString()) + "." + encode(payload.toString());
		Signature signer = Signature.getInstance(DbscProof.ES256.equals(algorithm)
			? "SHA256withECDSAinP1363Format" : "SHA256withRSA");
		signer.initSign(keys.getPrivate());
		signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
		return signingInput + "." + encode(signer.sign());
	}

	private static byte[] fixedLength(BigInteger value, int length)
	{
		byte[] bytes = unsigned(value);
		byte[] fixed = new byte[length];
		System.arraycopy(bytes, 0, fixed, length - bytes.length, bytes.length);
		return fixed;
	}

	private static byte[] unsigned(BigInteger value)
	{
		byte[] bytes = value.toByteArray();
		return bytes[0] == 0 ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
	}

	private static String encode(String text)
	{
		return encode(text.getBytes(StandardCharsets.UTF_8));
	}

	private static String encode(byte[] bytes)
	{
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
