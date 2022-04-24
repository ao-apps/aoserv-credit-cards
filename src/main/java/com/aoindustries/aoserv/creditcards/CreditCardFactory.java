/*
 * aoserv-credit-cards - Stores credit card processing data in the AOServ Platform.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2018, 2019, 2021, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-credit-cards.
 *
 * aoserv-credit-cards is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-credit-cards is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-credit-cards.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoindustries.aoserv.creditcards;

import com.aoapps.payments.CreditCard;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Creates instances of {@link CreditCard} based on {@link com.aoindustries.aoserv.client.payment.CreditCard the AOServ object}.
 *
 * @author  AO Industries, Inc.
 */
public final class CreditCardFactory {

  /** Make no instances. */
  private CreditCardFactory() {
    throw new AssertionError();
  }

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
        expirationMonth == null ? CreditCard.UNKNOWN_EXPIRATION_MONTH : expirationMonth, // TODO: 3.0: Nullable Byte
        expirationYear == null ? CreditCard.UNKNOWN_EXPIRATION_YEAR : expirationYear, // TODO: 3.0: Nullable Short
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
}
