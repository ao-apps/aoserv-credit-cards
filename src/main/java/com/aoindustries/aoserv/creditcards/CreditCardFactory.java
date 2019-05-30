/*
 * Copyright 2007-2009, 2015, 2016, 2018, 2019 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.creditcards;

import com.aoindustries.creditcards.CreditCard;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Creates instances of <code>CreditCard</code>s based on the AOServ object.
 *
 * @author  AO Industries, Inc.
 */
public class CreditCardFactory {

	/**
	 * Creates processor CreditCard beans from AOServ CreditCards.
	 */
	public static CreditCard getCreditCard(com.aoindustries.aoserv.client.payment.CreditCard creditCard) throws SQLException, IOException {
		Byte expirationMonth = creditCard.getExpirationMonth();
		Short expirationYear = creditCard.getExpirationYear();
		return new CreditCard(
			Integer.toString(creditCard.getPkey()),
			creditCard.getPrincipalName(),
			creditCard.getGroupName(),
			creditCard.getCreditCardProcessor().getProviderId(),
			creditCard.getProviderUniqueId(),
			null, // cardNumber
			creditCard.getCardInfo(),
			expirationMonth == null ? CreditCard.UNKNOWN_EXPRIATION_MONTH : expirationMonth, // TODO: 2.0: Nullable Byte
			expirationYear == null ? CreditCard.UNKNOWN_EXPRIATION_YEAR : expirationYear, // TODO: 2.0: Nullable Short
			null, // cardCode
			creditCard.getFirstName(),
			creditCard.getLastName(),
			creditCard.getCompanyName(),
			Objects.toString(creditCard.getEmail(), null),
			creditCard.getPhone(),
			creditCard.getFax(),
			creditCard.getCustomerId(),
			creditCard.getCustomerTaxId(),
			creditCard.getStreetAddress1(),
			creditCard.getStreetAddress2(),
			creditCard.getCity(),
			creditCard.getState(),
			creditCard.getPostalCode(),
			creditCard.getCountryCode().getCode(),
			creditCard.getDescription()
		);
	}

	private CreditCardFactory() {
		// Make no instances
	}
}
