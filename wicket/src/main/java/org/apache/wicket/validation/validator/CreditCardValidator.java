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

	/** The credit card number, which should be validated. */
	private String creditCardNumber = null;

	/** The ID which represents the credit card institute. */
	private int cardId = -1;

	/** */
	public static final int INVALID = -1;
	public static final int AMERICAN_EXPRESS = 0;
	public static final int CHINA_UNIONPAY = 1;
	public static final int DINERS_CLUB_CARTE_BLANCHE = 2;
	public static final int DINERS_CLUB_INTERNATIONAL = 3;
	public static final int DINERS_CLUB_US_AND_CANADA = 4;
	public static final int DISCOVER_CARD = 5;
	public static final int JCB = 6;
	public static final int LASER = 7;
	public static final int MAESTRO = 8;
	public static final int MASTERCARD = 9;
	public static final int SOLO = 10;
	public static final int SWITCH = 11;
	public static final int VISA = 12;
	public static final int VISA_ELECTRON = 13;

	private static final String[] creditCardNames = { "American Express", "China UnionPay",
			"Diners Club Carte Blanche", "Diners Club International", "Diners Club US & Canada",
			"Discover Card", "JCB", "Laser", "Maestro", "MasterCard", "Solo", "Switch", "Visa",
			"Visa Electron" };

	/**
	 * @see AbstractValidator#onValidate(IValidatable)
	 */
	@Override
	protected void onValidate(IValidatable<String> validatable)
	{
		creditCardNumber = validatable.getValue();
		if (!isLengthAndPrefixCorrect(creditCardNumber))
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
	 * alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid American Express
	 *         number. Else <code>FALSE</code> will be returned
	 */
	private boolean isAmericanExpress(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 15 &&
			(creditCardNumber.startsWith("34") || creditCardNumber.startsWith("37")))
		{
			if (isChecksumCorrect(creditCardNumber))
			{
				cardId = CreditCardValidator.AMERICAN_EXPRESS;
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
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() >= 16 && creditCardNumber.length() <= 19) &&
			(creditCardNumber.startsWith("622")))
		{
			int firstDigits = Integer.parseInt(creditCardNumber.substring(0, 5));
			if (firstDigits >= 622126 && firstDigits <= 622925)
			{
				cardId = CreditCardValidator.CHINA_UNIONPAY;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Diners Club Carte Blanche. A Diners Club Carte Blanche number
	 * has to start with a number between 300 and 305 and has to have a length of 14. The number has
	 * to be validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Diners Club Carte
	 *         Blanche number. Else <code>FALSE</code> will be returned
	 */
	private boolean isDinersClubCarteBlanche(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 14 && creditCardNumber.startsWith("30"))
		{
			int firstDigits = Integer.parseInt(creditCardNumber.substring(0, 3));
			if (firstDigits >= 300 && firstDigits <= 305 && isChecksumCorrect(creditCardNumber))
			{
				cardId = CreditCardValidator.DINERS_CLUB_CARTE_BLANCHE;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Diners Club International. A Diners Club International number
	 * has to start with the number 36 and has to have a length of 14. The number has to be
	 * validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Diners Club
	 *         International number. Else <code>FALSE</code> will be returned
	 */
	private boolean isDinersClubInternational(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 14 && creditCardNumber.startsWith("36") &&
			isChecksumCorrect(creditCardNumber))
		{
			cardId = CreditCardValidator.DINERS_CLUB_INTERNATIONAL;
			returnValue = true;
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Diners Club US & Canada. A Diners Club US & Canada number has
	 * to start with the number 54 or 55 and has to have a length of 16. The number has to be
	 * validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Diners Club US &
	 *         Canada number. Else <code>FALSE</code> will be returned
	 */
	private boolean isDinersClubUsAndCanada(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 &&
			(creditCardNumber.startsWith("54") || creditCardNumber.startsWith("55")) &&
			isChecksumCorrect(creditCardNumber))
		{
			cardId = CreditCardValidator.DINERS_CLUB_US_AND_CANADA;
			returnValue = true;
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Discover Card. A Discover Card number has to start with 6011,
	 * 622126-622925, 644-649 or 65 and has to have a length of 16. The number has to be validated
	 * with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Discover Card number.
	 *         Else <code>FALSE</code> will be returned
	 */
	private boolean isDiscoverCard(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
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
				cardId = CreditCardValidator.DISCOVER_CARD;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a JCB. A JCB number has to start with a number between 3528 and
	 * 3589 and has to have a length of 16. The number has to be validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid JCB number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isJCB(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && isChecksumCorrect(creditCardNumber))
		{
			int firstFourDigits = Integer.parseInt(creditCardNumber.substring(0, 4));
			if (firstFourDigits >= 3528 && firstFourDigits <= 3589)
			{
				cardId = CreditCardValidator.JCB;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Laser. A Laser number has to start with 6304, 6706, 6771 or
	 * 6709 and has to have a length between 16 and 19 digits. The number has to be validated with
	 * the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Laser number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isLaser(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() >= 16 && creditCardNumber.length() <= 19 &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("6304") || creditCardNumber.startsWith("6706") ||
				creditCardNumber.startsWith("6771") || creditCardNumber.startsWith("6709"))
			{
				cardId = CreditCardValidator.LASER;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Maestro. A Maestro number has to start with
	 * 5018,5020,5038,6304,6759,6761 or 6763 and has to have a length between 12 and 19 digits. The
	 * number has to be validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Maestro number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isMaestro(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() >= 12 && creditCardNumber.length() <= 19 &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("5018") || creditCardNumber.startsWith("5020") ||
				creditCardNumber.startsWith("5038") || creditCardNumber.startsWith("6304") ||
				creditCardNumber.startsWith("6759") || creditCardNumber.startsWith("6761") ||
				creditCardNumber.startsWith("6763"))
			{
				cardId = CreditCardValidator.MAESTRO;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Solo. A Solo number has to start with 6334 or 6767 and has to
	 * have a length of 16, 18 or 19 digits. The number has to be validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Solo number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isSolo(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() == 16 || creditCardNumber.length() == 18 || creditCardNumber.length() == 19) &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("6334") || creditCardNumber.startsWith("6767"))
			{
				cardId = CreditCardValidator.SOLO;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Switch. A Switch number has to start with
	 * 4903,4905,4911,4936,564182,633110,6333 or 6759 and has to have a length of 16, 18 or 19
	 * digits. The number has to be validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Switch number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isSwitch(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if ((creditCardNumber.length() == 16 || creditCardNumber.length() == 18 || creditCardNumber.length() == 19) &&
			isChecksumCorrect(creditCardNumber))
		{
			if (creditCardNumber.startsWith("4903") || creditCardNumber.startsWith("4905") ||
				creditCardNumber.startsWith("4911") || creditCardNumber.startsWith("4936") ||
				creditCardNumber.startsWith("564182") || creditCardNumber.startsWith("633110") ||
				creditCardNumber.startsWith("6333") || creditCardNumber.startsWith("6759"))
			{
				cardId = CreditCardValidator.SWITCH;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Visa. A Visa number has to start with a 4 and has to have a
	 * length of 13 or 16 digits. The number has to be validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Visa number. Else
	 *         <code>FALSE</code> will be returned
	 */
	private boolean isVisa(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 13 || creditCardNumber.length() == 16)
		{
			if (creditCardNumber.startsWith("4"))
			{
				cardId = CreditCardValidator.SWITCH;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Visa Electron. A Visa Electron number has to start with
	 * 417500,4917,4913,4508 or 4844 and has to have a length of 16 digits. The number has to be
	 * validated with the Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Visa Electron number.
	 *         Else <code>FALSE</code> will be returned
	 */
	private boolean isVisaElectron(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 &&
			(creditCardNumber.startsWith("417500") || creditCardNumber.startsWith("4917") ||
				creditCardNumber.startsWith("4913") || creditCardNumber.startsWith("4508") || creditCardNumber.startsWith("4844")))
		{
			cardId = CreditCardValidator.VISA_ELECTRON;
			returnValue = true;
		}

		return returnValue;
	}

	/**
	 * Check if the credit card is a Mastercard. A Mastercard number has to start with a number
	 * between 51 and 55 and has to have a length of 16. The number has to be validated with the
	 * Luhn alorithm.
	 * 
	 * @param creditCardNumber
	 *            the credit card number as a string
	 * @return <code>TRUE</code> if the credit card number seems to be a valid Mastercard number.
	 *         Else <code>FALSE</code> will be returned
	 */
	private boolean isMastercard(String creditCardNumber)
	{
		cardId = CreditCardValidator.INVALID;
		boolean returnValue = false;

		if (creditCardNumber.length() == 16 && isChecksumCorrect(creditCardNumber))
		{
			int firstTwoDigits = Integer.parseInt(creditCardNumber.substring(0, 2));
			if (firstTwoDigits >= 51 && firstTwoDigits <= 55)
			{
				cardId = CreditCardValidator.MASTERCARD;
				returnValue = true;
			}
		}

		return returnValue;
	}

	/**
	 * Just used for debugging purposes.<br>
	 * Due to re-branding (e.g. Switch was re-branded as Maestro in mid 2007) some rules might
	 * overlap, but those overlappings are not considered. So it might happen, that a Switch-card is
	 * identified as a Maestro. <br>
	 * So you shouldn't rely on the name which is returned here.
	 * 
	 * @return the name of the credit card if it could be determined, else an empty string
	 */
	private String getCardName()
	{
		return (cardId > -1 && cardId < creditCardNames.length ? creditCardNames[cardId] : "");
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
		String input = creditCardNumber;
		String numberToCheck = input.replaceAll("[ -]", "");
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
