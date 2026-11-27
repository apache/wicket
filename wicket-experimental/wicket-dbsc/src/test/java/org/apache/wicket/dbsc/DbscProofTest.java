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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

class DbscProofTest
{
	@Test
	void registrationProofCarriesItsKey() throws Exception
	{
		DbscProof proof = DbscProof.parse(TestBrowser.es256().registrationProof("challenge"));

		assertEquals(DbscProof.ES256, proof.algorithm());
		assertEquals("challenge", proof.challenge());
		assertTrue(proof.isSignedBy(proof.embeddedKey()));
	}

	@Test
	void tamperedProofFailsVerification() throws Exception
	{
		String jwt = TestBrowser.es256().registrationProof("challenge");
		String[] parts = jwt.split("\\.");
		DbscProof original = DbscProof.parse(jwt);
		String tampered = parts[0] + "." + encode(new JSONObject().put("jti", "other").toString()) +
			"." + parts[2];

		DbscProof proof = DbscProof.parse(tampered);

		assertFalse(proof.isSignedBy(original.embeddedKey()));
	}

	@Test
	void rejectsAnotherType()
	{
		String jwt = encode(new JSONObject().put("alg", "ES256").put("typ", "JWT").toString()) +
			"." + encode("{}") + ".AA";

		assertThrows(DbscProof.InvalidProofException.class, () -> DbscProof.parse(jwt));
	}

	@Test
	void rejectsMalformedJwt()
	{
		assertThrows(DbscProof.InvalidProofException.class, () -> DbscProof.parse("a.b"));
		assertThrows(DbscProof.InvalidProofException.class, () -> DbscProof.parse("!.!.!"));
	}

	@Test
	void rejectsAPointOffTheCurve() throws Exception
	{
		JSONObject jwk = new JSONObject().put("kty", "EC")
			.put("crv", "P-256")
			.put("x", encode(new byte[] { 1 }))
			.put("y", encode(new byte[] { 1 }));

		DbscProof proof = DbscProof.parse(unsigned("ES256", jwk));

		assertThrows(DbscProof.InvalidProofException.class, proof::embeddedKey);
	}

	@Test
	void rejectsAShortRsaKey() throws Exception
	{
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(1024);
		RSAPublicKey key = (RSAPublicKey)generator.generateKeyPair().getPublic();
		JSONObject jwk = new JSONObject().put("kty", "RSA")
			.put("n", encode(key.getModulus().toByteArray()))
			.put("e", encode(key.getPublicExponent().toByteArray()));

		DbscProof proof = DbscProof.parse(unsigned("RS256", jwk));

		assertThrows(DbscProof.InvalidProofException.class, proof::embeddedKey);
	}

	@Test
	void rejectsALongRsaKey() throws Exception
	{
		JSONObject jwk = new JSONObject().put("kty", "RSA")
			.put("n", encode(BigInteger.ONE.shiftLeft(4096).setBit(0).toByteArray()))
			.put("e", "AQAB");

		DbscProof proof = DbscProof.parse(unsigned("RS256", jwk));

		assertThrows(DbscProof.InvalidProofException.class, proof::embeddedKey);
	}

	@Test
	void rejectsALongRsaExponent() throws Exception
	{
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		RSAPublicKey key = (RSAPublicKey)generator.generateKeyPair().getPublic();
		JSONObject jwk = new JSONObject().put("kty", "RSA")
			.put("n", encode(key.getModulus().toByteArray()))
			.put("e", encode(BigInteger.ONE.shiftLeft(32).setBit(0).toByteArray()));

		DbscProof proof = DbscProof.parse(unsigned("RS256", jwk));

		assertThrows(DbscProof.InvalidProofException.class, proof::embeddedKey);
	}

	@Test
	void rejectsCriticalHeaderExtensions()
	{
		String jwt = encode(new JSONObject().put("alg", "ES256")
			.put("typ", "dbsc+jwt")
			.put("crit", new JSONArray().put("exp"))
			.toString()) + "." + encode("{}") + ".AA";

		assertThrows(DbscProof.InvalidProofException.class, () -> DbscProof.parse(jwt));
	}

	@Test
	void rejectsAKeyThatDoesNotFitTheAlgorithm() throws Exception
	{
		JSONObject jwk = new JSONObject().put("kty", "RSA").put("n", "AQAB").put("e", "AQAB");

		DbscProof proof = DbscProof.parse(unsigned("ES256", jwk));

		assertThrows(DbscProof.InvalidProofException.class, proof::embeddedKey);
	}

	private static String unsigned(String algorithm, JSONObject jwk)
	{
		JSONObject header = new JSONObject().put("alg", algorithm)
			.put("typ", "dbsc+jwt")
			.put("jwk", jwk);
		return encode(header.toString()) + "." + encode("{\"jti\":\"c\"}") + ".AA";
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
