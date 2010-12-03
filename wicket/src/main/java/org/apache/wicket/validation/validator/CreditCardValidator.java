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
package org.apache.wicket.validation.validator;

import org.apache.wicket.validation.IValidatable;

/**
 * Checks if a credit card number is valid. The number will be checked for "American Express",
 * "China UnionPay", "Diners Club Carte Blanche", "Diners Club International",
 * "Diners Club US & Canada", "Discover Card", "JCB", "Laser", "Maestro", "MasterCard", "Solo",
 * "Switch", "Visa" and "Visa Electron". If none of those apply to the credit card number, the
 * credit card number is considered invalid.
 * 
 * <p>
 * Card prefixes and lengths have been taken from <a
 * href="http://en.wikipedia.org/w/index.php?title=Bank_card_number&oldid=322132931">Wikipedia</a>.
 * 
 * @author Johan Compagner
 * @author Joachim F. Rohde
 * @since 1.2.6
 */
public class CreditCardValidator extends AbstractValidator<String>
{
	private static final long serialVersionUID = 1L;

	/** */
	public static enum CreditCard {
		/** */
		INVALID(null),
		/** */
		AMERICAN_EXPRESS("American Express"),
		/** */
		CHINA_UNIONPAY("China UnionPay"),
		/** */
		DINERS_CLUB_CARTE_BLANCHE("Diners Club Carte Blanche"),
		/** */
		DINERS_CLUB_INTERNATIONAL("Diners Club International"),
		/** */
		DINERS_CLUB_US_AND_CANADA("Diners Club US & Canada"),
		/** */
		DISCOVER_CARD("Discover Card"),
		/** */
		JCB("JCB"),
		/** */
		LASER("Laser"),
		/** */
		MAESTRO("Maestro"),
		/** */
		MASTERCARD("MasterCard"),
		/** */
		SOLO("Solo"),
		/** */
		SWITCH("Switch"),
		/** */
		VISA("Visa"),
		/** */
		VISA_ELECTRON("Visa Electron");

		private final String name;

		CreditCard(String name)
		{
			this.name = name;
		}
	}

	/** The ID which represents the credit card institute. */
	private CreditCard cardId = CreditCard.INVALID;

	@Override
	protected void onValidate(IValidatable<String> validatable)
	{
		final String value = validatable.getValue();

		try
		{
			if (!isLengthAndPrefixCorrect(value))
			{
				error(validatable);
			}
		}
		catch (final NumberFormatException _)
		{
			error(validatable);
		}
	}

	/**
	 * Checks if the credit card number can be determined as a valid number.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number could be determined as a valid number,
	 *         else <code>FALSE</code> is returned
	 */
	private boolean isLengthAndPrefixCorrect(String creditCardNumber)
	{
		if (creditCardNumber != null)
		{
			// strip spaces and dashes
			creditCardNumber = creditCardNumber.replaceAll("[ -]", "");
		}

		// the length of the credit card number has to be between 12 and 19.
		// else the number is invalid.
		if (creditCardNumber != null && creditCardNumber.length() >= 12 &&
			creditCardNumber.length() <= 19)
		{
			if (isAmericanExpress(creditCardNumber))
			{
				return true;
			}
			else if (isChinaUnionPay(creditCardNumber))
			{
				return true;
			}
			else if (isDinersClubCarteBlanche(creditCardNumber))
			{
				return true;
			}
			else if (isDinersClubInternational(creditCardNumber))
			{
				return true;
			}
			else if (isDinersClubUsAndCanada(creditCardNumber))
			{
				return true;
			}
			else if (isDiscoverCard(creditCardNumber))
			{
				return true;
			}
			else if (isJCB(creditCardNumber))
			{
				return true;
			}
			else if (isLaser(creditCardNumber))
			{
				return true;
			}
			else if (isMaestro(creditCardNumber))
			{
				return true;
			}
			else if (isMastercard(creditCardNumber))
			{
				return true;
			}
			else if (isSolo(creditCardNumber))
			{
				return true;
			}
			else if (isSwitch(creditCardNumber))
			{
				return true;
			}
			else if (isVisa(creditCardNumber))
			{
				return true;
			}
			else if (isVisaElectron(creditCardNumber))
			{
				return true;
			}
			else if (isUnknown(creditCardNumber))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Can be used (subclassed) to extend the test with a credit card not yet known by the
	 * validator.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid American Express
	 *         number. Else <code>FALSE</code> will be returned
	 */
	protected boolean isUnknown(String creditCardNumber)
	{
		return false;
	}

	/**
	 * Check if the credit card is an American Express. An American Express number has to start with
	 * 34 or 37 and has to have a length of 15. The number has to be validated with the Luhn
	 * algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid American Express
	 *         number. Else <code>FALSE</code> will be returned
	 */
	private boolean isAmericanExpress(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 15 &&
			(creditCardNumber.startsWith("34") || creditCardNumber.startsWith("37")))
		{
			if (isChecksumCorrect(creditCardNumber))
			{
				cardId = CreditCard.AMERICAN_EXPRESS;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a China UnionPay. A China UnionPay number has to start with 622
	 * (622126-622925) and has to have a length between 16 and 19. No further validation takes
	 * place.<br/>
	 * <br/>
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid China UnionPay
	 *         number. Else <code>FALSE</code> will be returned.
	 */
	private boolean isChinaUnionPay(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() >= 16 && creditCardNumber.length() <= 19) &&
			(creditCardNumber.startsWith("622")))
		{
			int firstDigits = Integer.parseInt(creditCardNumber.substring(0, 5));
			if (firstDigits >= 622126 && firstDigits <= 622925)
			{
				cardId = CreditCard.CHINA_UNIONPAY;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Diners Club Carte Blanche. A Diners Club Carte Blanche number
	 * has to start with a number between 300 and 305 and has to have a length of 14. The number has
	 * to be validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Diners Club Carte
	 *         Blanche number. Else <code>FALSE</code> will be returned
	 */
	private boolean isDinersClubCarteBlanche(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 14 && creditCardNumber.startsWith("30"))
		{
			int firstDigits = Integer.parseInt(creditCardNumber.substring(0, 3));
			if (firstDigits >= 300 && firstDigits <= 305 && isChecksumCorrect(creditCardNumber))
			{
				cardId = CreditCard.DINERS_CLUB_CARTE_BLANCHE;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Diners Club International. A Diners Club International number
	 * has to start with the number 36 and has to have a length of 14. The number has to be
	 * validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Diners Club
	 *         International number. Else <code>FALSE</code> will be returned
	 */
	private boolean isDinersClubInternational(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 14 && creditCardNumber.startsWith("36") &&
			isChecksumCorrect(creditCardNumber))
		{
			cardId = CreditCard.DINERS_CLUB_INTERNATIONAL;
			returnValue = true;
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Diners Club US & Canada. A Diners Club US & Canada number has
	 * to start with the number 54 or 55 and has to have a length of 16. The number has to be
	 * validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Diners Club US &
	 *         Canada number. Else <code>FALSE</code> will be returned
	 */
	private boolean isDinersClubUsAndCanada(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 &&
			(creditCardNumber.startsWith("54") || creditCardNumber.startsWith("55")) &&
			isChecksumCorrect(creditCardNumber))
		{
			cardId = CreditCard.DINERS_CLUB_US_AND_CANADA;
			returnValue = true;
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Discover Card. A Discover Card number has to start with 6011,
	 * 622126-622925, 644-649 or 65 and has to have a length of 16. The number has to be validated
	 * with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Discover Card number.
	 *         Else <code>FALSE</code> will be returned
	 */
	private boolean isDiscoverCard(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && creditCardNumber.startsWith("6") &&
			isChecksumCorrect(creditCardNumber))
		{
			int firstThreeDigits = Integer.parseInt(creditCardNumber.substring(0, 3));
			int firstSixDigits = Integer.parseInt(creditCardNumber.substring(0, 6));
			if (creditCardNumber.startsWith("6011") || creditCardNumber.startsWith("65") ||
				(firstThreeDigits >= 644 && firstThreeDigits <= 649) ||
				(firstSixDigits >= 622126 && firstSixDigits <= 622925))
			{
				cardId = CreditCard.DISCOVER_CARD;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a JCB. A JCB number has to start with a number between 3528 and
	 * 3589 and has to have a length of 16. The number has to be validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid JCB number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isJCB(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && isChecksumCorrect(creditCardNumber))
		{
			int firstFourDigits = Integer.parseInt(creditCardNumber.substring(0, 4));
			if (firstFourDigits >= 3528 && firstFourDigits <= 3589)
			{
				cardId = CreditCard.JCB;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Laser. A Laser number has to start with 6304, 6706, 6771 or
	 * 6709 and has to have a length between 16 and 19 digits. The number has to be validated with
	 * the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Laser number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isLaser(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() >= 16 && creditCardNumber.length() <= 19 &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("6304") || creditCardNumber.startsWith("6706") ||
				creditCardNumber.startsWith("6771") || creditCardNumber.startsWith("6709"))
			{
				cardId = CreditCard.LASER;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Maestro. A Maestro number has to start with
	 * 5018,5020,5038,6304,6759,6761 or 6763 and has to have a length between 12 and 19 digits. The
	 * number has to be validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Maestro number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isMaestro(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() >= 12 && creditCardNumber.length() <= 19 &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("5018") || creditCardNumber.startsWith("5020") ||
				creditCardNumber.startsWith("5038") || creditCardNumber.startsWith("6304") ||
				creditCardNumber.startsWith("6759") || creditCardNumber.startsWith("6761") ||
				creditCardNumber.startsWith("6763"))
			{
				cardId = CreditCard.MAESTRO;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Solo. A Solo number has to start with 6334 or 6767 and has to
	 * have a length of 16, 18 or 19 digits. The number has to be validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Solo number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isSolo(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() == 16 || creditCardNumber.length() == 18 || creditCardNumber.length() == 19) &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("6334") || creditCardNumber.startsWith("6767"))
			{
				cardId = CreditCard.SOLO;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Switch. A Switch number has to start with
	 * 4903,4905,4911,4936,564182,633110,6333 or 6759 and has to have a length of 16, 18 or 19
	 * digits. The number has to be validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Switch number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isSwitch(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() == 16 || creditCardNumber.length() == 18 || creditCardNumber.length() == 19) &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("4903") || creditCardNumber.startsWith("4905") ||
				creditCardNumber.startsWith("4911") || creditCardNumber.startsWith("4936") ||
				creditCardNumber.startsWith("564182") || creditCardNumber.startsWith("633110") ||
				creditCardNumber.startsWith("6333") || creditCardNumber.startsWith("6759"))
			{
				cardId = CreditCard.SWITCH;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Visa. A Visa number has to start with a 4 and has to have a
	 * length of 13 or 16 digits. The number has to be validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Visa number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isVisa(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 13 || creditCardNumber.length() == 16)
		{
			if (creditCardNumber.startsWith("4"))
			{
				cardId = CreditCard.SWITCH;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Visa Electron. A Visa Electron number has to start with
	 * 417500,4917,4913,4508 or 4844 and has to have a length of 16 digits. The number has to be
	 * validated with the Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Visa Electron number.
	 *         Else <code>FALSE</code> will be returned
	 */
	private boolean isVisaElectron(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 &&
			(creditCardNumber.startsWith("417500") || creditCardNumber.startsWith("4917") ||
				creditCardNumber.startsWith("4913") || creditCardNumber.startsWith("4508") || creditCardNumber.startsWith("4844")))
		{
			cardId = CreditCard.VISA_ELECTRON;
			returnValue = true;
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Mastercard. A Mastercard number has to start with a number
	 * between 51 and 55 and has to have a length of 16. The number has to be validated with the
	 * Luhn algorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Mastercard number.
	 *         Else <code>FALSE</code> will be returned
	 */
	private boolean isMastercard(String creditCardNumber)
	{
		cardId = CreditCard.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && isChecksumCorrect(creditCardNumber))
		{
			int firstTwoDigits = Integer.parseInt(creditCardNumber.substring(0, 2));
			if (firstTwoDigits >= 51 && firstTwoDigits <= 55)
			{
				cardId = CreditCard.MASTERCARD;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Calculates the checksum of a credit card number using the Luhn algorithm (the so-called
	 * "mod 10" algorithm).
	 * 
	 * @param creditCardNumber
	 *            the credit card number for which the checksum should be calculated
	 * @return <code>TRUE</code> if the checksum for the given credit card number is valid, else
	 *         return <code>FALSE</code>
	 * @see <a href="http://en.wikipedia.org/wiki/Luhn_algorithm">Wikipedie - Luhn algorithm</a>
	 */
	private boolean isChecksumCorrect(String creditCardNumber)
	{
		String numberToCheck = creditCardNumber.replaceAll("[ -]", "");
		int nulOffset = '0';
		int sum = 0;
		for (int i = 1; i <= numberToCheck.length(); i++)
		{
			int currentDigit = numberToCheck.charAt(numberToCheck.length() - i) - nulOffset;
			if ((i % 2) == 0)
			{
				currentDigit *= 2;
				currentDigit = currentDigit > 9 ? currentDigit - 9 : currentDigit;
				sum += currentDigit;
			}
			else
			{
				sum += currentDigit;
			}
		}

		return (sum % 10) == 0;
	}
}
