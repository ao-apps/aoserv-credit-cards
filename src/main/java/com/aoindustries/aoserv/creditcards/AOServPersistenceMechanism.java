/*
 * aoserv-credit-cards - Stores credit card processing data in the AOServ Platform.
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
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
 * along with aoserv-credit-cards.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.creditcards;

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.account.Account;
import com.aoindustries.aoserv.client.account.Administrator;
import com.aoindustries.aoserv.client.payment.CountryCode;
import com.aoindustries.aoserv.client.payment.Payment;
import com.aoindustries.aoserv.client.payment.Processor;
import com.aoindustries.collections.AoCollections;
import com.aoindustries.creditcards.AuthorizationResult;
import com.aoindustries.creditcards.CaptureResult;
import com.aoindustries.creditcards.CreditCard;
import com.aoindustries.creditcards.PersistenceMechanism;
import com.aoindustries.creditcards.TokenizedCreditCard;
import com.aoindustries.creditcards.Transaction;
import com.aoindustries.creditcards.TransactionRequest;
import com.aoindustries.creditcards.TransactionResult;
import com.aoindustries.net.Email;
import com.aoindustries.util.i18n.Money;
import com.aoindustries.validation.ValidationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stores the information in the AOServ Platform.  The principal sent in to the
 * methods should be an instance of {@link AOServConnectorPrincipal} and
 * any group should be a {@link AccountGroup}.
 *
 * All operations will be performed using the connector from the principal,
 * therefore the underlying AOServ security model will apply to these calls.
 *
 * @author  AO Industries, Inc.
 */
public class AOServPersistenceMechanism implements PersistenceMechanism {

	private static final AOServPersistenceMechanism instance = new AOServPersistenceMechanism();

	/**
	 * Only one instance is necessary since all calls are on the method
	 * parameter objects.
	 */
	public static AOServPersistenceMechanism getInstance() {
		return instance;
	}

	private AOServPersistenceMechanism() {
	}

	private static AOServConnector getAOServConnector(Principal principal) throws SQLException {
		if(principal==null) throw new SQLException("principal is null");
		if(!(principal instanceof AOServConnectorPrincipal)) throw new SQLException("principal is not a AOServConnectorPrincipal: "+principal.getName());
		return ((AOServConnectorPrincipal)principal).getAOServConnector();
	}

	private static String getPrincipalName(Principal principal) throws SQLException {
		if(principal==null) throw new SQLException("principal is null");
		if(!(principal instanceof AOServConnectorPrincipal)) throw new SQLException("principal is not a AOServConnectorPrincipal: "+principal.getName());
		return ((AOServConnectorPrincipal)principal).getPrincipalName();
	}

	private static Account getAccount(Group group) throws SQLException {
		if(group==null) throw new SQLException("group is null");
		if(!(group instanceof AccountGroup)) throw new SQLException("group is not an AccountGroup: " + group.getName());
		return ((AccountGroup)group).getAccount();
	}

	private static String getGroupName(Group group) throws SQLException {
		if(group==null) throw new SQLException("group is null");
		if(!(group instanceof AccountGroup)) throw new SQLException("group is not an AccountGroup: " + group.getName());
		return ((AccountGroup)group).getGroupName();
	}

	@Override
	public String storeCreditCard(Principal principal, CreditCard creditCard) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			String principalName = getPrincipalName(principal);
			Account account = conn.getAccount().getAccount().get(Account.Name.valueOf(creditCard.getGroupName()));
			if(account == null) throw new SQLException("Unable to find Account: " + creditCard.getGroupName());
			Processor processor = conn.getPayment().getProcessor().get(creditCard.getProviderId());
			if(processor == null) throw new SQLException("Unable to find CreditCardProcessor: " + creditCard.getProviderId());
			CountryCode countryCode = conn.getPayment().getCountryCode().get(creditCard.getCountryCode());
			if(countryCode==null) throw new SQLException("Unable to find CountryCode: "+creditCard.getCountryCode());
			int pkey = account.addCreditCard(
				processor,
				creditCard.getGroupName(),
				creditCard.getMaskedCardNumber(),
				creditCard.getExpirationMonth(),
				creditCard.getExpirationYear(),
				creditCard.getProviderUniqueId(),
				creditCard.getFirstName(),
				creditCard.getLastName(),
				creditCard.getCompanyName(),
				Email.valueOf(creditCard.getEmail()),
				creditCard.getPhone(),
				creditCard.getFax(),
				creditCard.getCustomerId(),
				creditCard.getCustomerTaxId(),
				creditCard.getStreetAddress1(),
				creditCard.getStreetAddress2(),
				creditCard.getCity(),
				creditCard.getState(),
				creditCard.getPostalCode(),
				countryCode,
				principalName,
				creditCard.getComments(),
				creditCard.getCardNumber()
			);
			return Integer.toString(pkey);
		} catch(ValidationException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(), e);
		}
	}

	private static CreditCard newCreditCard(com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard) throws SQLException, IOException {
		Byte expirationMonth = aoservCreditCard.getExpirationMonth();
		Short expirationYear = aoservCreditCard.getExpirationYear();
		CountryCode countryCode = aoservCreditCard.getCountryCode();
		return new CreditCard(
			Integer.toString(aoservCreditCard.getPkey()),
			aoservCreditCard.getPrincipalName(),
			aoservCreditCard.getGroupName(),
			aoservCreditCard.getCreditCardProcessor().getProviderId(),
			aoservCreditCard.getProviderUniqueId(),
			null, // cardNumber
			aoservCreditCard.getCardInfo(),
			expirationMonth == null ? CreditCard.UNKNOWN_EXPIRATION_MONTH : expirationMonth, // TODO: 2.0: Make nullable Byte
			expirationYear == null ? CreditCard.UNKNOWN_EXPIRATION_YEAR : expirationYear, // TODO: 2.0: Make nullable Short
			null, // cardCode
			aoservCreditCard.getFirstName(),
			aoservCreditCard.getLastName(),
			aoservCreditCard.getCompanyName(),
			Objects.toString(aoservCreditCard.getEmail(), null),
			aoservCreditCard.getPhone(),
			aoservCreditCard.getFax(),
			aoservCreditCard.getCustomerId(),
			aoservCreditCard.getCustomerTaxId(),
			aoservCreditCard.getStreetAddress1(),
			aoservCreditCard.getStreetAddress2(),
			aoservCreditCard.getCity(),
			aoservCreditCard.getState(),
			aoservCreditCard.getPostalCode(),
			countryCode == null ? null : countryCode.getCode(),
			aoservCreditCard.getDescription()
		);
	}

	@Override
	public CreditCard getCreditCard(Principal principal, String persistenceUniqueId) throws SQLException {
		AOServConnector conn = getAOServConnector(principal);
		int id;
		try {
			id = Integer.parseInt(persistenceUniqueId);
		} catch(NumberFormatException e) {
			return null;
		}
		com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard;
		try {
			aoservCreditCard = conn.getPayment().getCreditCard().get(id);
			return aoservCreditCard == null ? null : newCreditCard(aoservCreditCard);
		} catch(IOException err) {
			throw new SQLException(err);
		}
	}

	@Override
	public Map<String, CreditCard> getCreditCards(Principal principal) throws SQLException {
		AOServConnector conn = getAOServConnector(principal);
		try {
			List<com.aoindustries.aoserv.client.payment.CreditCard> aoservCreditCards = conn.getPayment().getCreditCard().getRows();
			Map<String, CreditCard> map = AoCollections.newLinkedHashMap(aoservCreditCards.size());
			for(com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard : aoservCreditCards) {
				CreditCard copy = newCreditCard(aoservCreditCard);
				String persistenceUniqueId = copy.getPersistenceUniqueId();
				if(map.put(persistenceUniqueId, copy) != null) throw new SQLException("Duplicate persistenceUniqueId: " + persistenceUniqueId);
			}
			return map;
		} catch(IOException err) {
			throw new SQLException(err);
		}
	}

	@Override
	public Map<String, CreditCard> getCreditCards(Principal principal, String providerId) throws SQLException {
		AOServConnector conn = getAOServConnector(principal);
		try {
			Processor processor = conn.getPayment().getProcessor().get(providerId);
			if(processor == null) {
				return new LinkedHashMap<>();
			} else {
				List<com.aoindustries.aoserv.client.payment.CreditCard> aoservCreditCards = processor.getCreditCards();
				Map<String, CreditCard> map = AoCollections.newLinkedHashMap(aoservCreditCards.size());
				for(com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard : aoservCreditCards) {
					CreditCard copy = newCreditCard(aoservCreditCard);
					String providerUniqueId = copy.getProviderUniqueId();
					if(map.put(providerUniqueId, copy) != null) throw new SQLException("Duplicate providerUniqueId: " + providerUniqueId);
				}
				return map;
			}
		} catch(IOException err) {
			throw new SQLException(err);
		}
	}

	@Override
	public void updateCreditCard(
		Principal principal,
		CreditCard creditCard
	) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			int id = Integer.parseInt(creditCard.getPersistenceUniqueId());
			com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard = conn.getPayment().getCreditCard().get(id);
			if(aoservCreditCard == null) throw new SQLException("Unable to find CreditCard: " + id);
			CountryCode countryCode = conn.getPayment().getCountryCode().get(creditCard.getCountryCode());
			if(countryCode == null) throw new SQLException("Unable to find CountryCode: " + creditCard.getCountryCode());
			aoservCreditCard.update(
				creditCard.getMaskedCardNumber(),
				creditCard.getFirstName(),
				creditCard.getLastName(),
				creditCard.getCompanyName(),
				Email.valueOf(creditCard.getEmail()),
				creditCard.getPhone(),
				creditCard.getFax(),
				creditCard.getCustomerId(),
				creditCard.getCustomerTaxId(),
				creditCard.getStreetAddress1(),
				creditCard.getStreetAddress2(),
				creditCard.getCity(),
				creditCard.getState(),
				creditCard.getPostalCode(),
				countryCode,
				creditCard.getComments()
			);
		} catch(NumberFormatException err) {
			throw new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId(), err);
		} catch(ValidationException | IOException err) {
			throw new SQLException(err);
		}
	}

	@Override
	public void updateCardNumber(
		Principal principal,
		CreditCard creditCard,
		String cardNumber,
		byte expirationMonth,
		short expirationYear
	) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			int id = Integer.parseInt(creditCard.getPersistenceUniqueId());
			com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard = conn.getPayment().getCreditCard().get(id);
			if(aoservCreditCard == null) throw new SQLException("Unable to find CreditCard: " + id);
			aoservCreditCard.updateCardNumberAndExpiration(
				CreditCard.maskCreditCardNumber(cardNumber),
				cardNumber,
				expirationMonth,
				expirationYear
			);
		} catch(NumberFormatException err) {
			throw new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId(), err);
		} catch(IOException err) {
			throw new SQLException(err);
		}
	}

	@Override
	public void updateExpiration(
		Principal principal,
		CreditCard creditCard,
		byte expirationMonth,
		short expirationYear
	) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			int id = Integer.parseInt(creditCard.getPersistenceUniqueId());
			com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard = conn.getPayment().getCreditCard().get(id);
			if(aoservCreditCard == null) throw new SQLException("Unable to find CreditCard: " + id);
			aoservCreditCard.updateCardExpiration(
				expirationMonth,
				expirationYear
			);
		} catch(NumberFormatException err) {
			throw new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId(), err);
		} catch(IOException err) {
			throw new SQLException(err);
		}
	}

	@Override
	public void deleteCreditCard(Principal principal, CreditCard creditCard) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			int id = Integer.parseInt(creditCard.getPersistenceUniqueId());
			com.aoindustries.aoserv.client.payment.CreditCard aoservCreditCard = conn.getPayment().getCreditCard().get(id);
			if(aoservCreditCard == null) throw new SQLException("Unable to find CreditCard: " + id);
			aoservCreditCard.remove();
		} catch(NumberFormatException err) {
			throw new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId(), err);
		} catch(IOException e) {
			throw new SQLException(e.getLocalizedMessage(), e);
		}
	}

	private static Money getMoney(Currency currency, BigDecimal value) {
		return value == null ? null : new Money(currency, value);
	}

	@Override
	public String insertTransaction(Principal principal, Group group, Transaction transaction) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			String principalName = getPrincipalName(principal);
			Account account = getAccount(group);
			String groupName = getGroupName(group);
			String providerId = transaction.getProviderId();
			Processor processor = conn.getPayment().getProcessor().get(providerId);
			if(processor == null) throw new SQLException("Unable to find Processor: " + providerId);
			TransactionRequest transactionRequest = transaction.getTransactionRequest();
			CreditCard creditCard = transaction.getCreditCard();
			// Try to find the createdBy from the credit card persistence mechanism, otherwise default to current principal
			Administrator creditCardCreatedBy;
			Account ccAccount;
			{
				String ccPersistId = creditCard.getPersistenceUniqueId();
				if(ccPersistId==null || ccPersistId.length()==0) {
					creditCardCreatedBy = conn.getCurrentAdministrator();
					ccAccount = account;
				} else {
					int ccPersistIdInt = Integer.parseInt(ccPersistId);
					com.aoindustries.aoserv.client.payment.CreditCard storedCard = conn.getPayment().getCreditCard().get(ccPersistIdInt);
					if(storedCard == null) throw new SQLException("Unable to find CreditCard: " + ccPersistIdInt);
					creditCardCreatedBy = storedCard.getCreatedBy();
					if(creditCardCreatedBy==null) {
						// Might have been filtered - this is OK
						creditCardCreatedBy = conn.getCurrentAdministrator();
					}
					ccAccount = storedCard.getAccount();
				}
			}
			Byte expirationMonth = creditCard.getExpirationMonth(); // TODO: 2.0: Nullable Byte
			if(expirationMonth == CreditCard.UNKNOWN_EXPIRATION_MONTH) expirationMonth = null;
			Short expirationYear = creditCard.getExpirationYear(); // TODO: 2.0: Nullable Short
			if(expirationYear == CreditCard.UNKNOWN_EXPIRATION_YEAR) expirationYear = null;
			Currency currency = transactionRequest.getCurrency();
			int pkey = conn.getPayment().getPayment().addPayment(
				processor,
				account,
				groupName,
				transactionRequest.getTestMode(),
				transactionRequest.getDuplicateWindow(),
				transactionRequest.getOrderNumber(),
				new Money(currency, transactionRequest.getAmount()),
				getMoney(currency, transactionRequest.getTaxAmount()),
				transactionRequest.getTaxExempt(),
				getMoney(currency, transactionRequest.getShippingAmount()),
				getMoney(currency, transactionRequest.getDutyAmount()),
				transactionRequest.getShippingFirstName(),
				transactionRequest.getShippingLastName(),
				transactionRequest.getShippingCompanyName(),
				transactionRequest.getShippingStreetAddress1(),
				transactionRequest.getShippingStreetAddress2(),
				transactionRequest.getShippingCity(),
				transactionRequest.getShippingState(),
				transactionRequest.getShippingPostalCode(),
				transactionRequest.getShippingCountryCode(),
				transactionRequest.getEmailCustomer(),
				Email.valueOf(transactionRequest.getMerchantEmail()),
				transactionRequest.getInvoiceNumber(),
				transactionRequest.getPurchaseOrderNumber(),
				transactionRequest.getDescription(),
				creditCardCreatedBy,
				creditCard.getPrincipalName(),
				ccAccount,
				creditCard.getGroupName(),
				creditCard.getProviderUniqueId(),
				creditCard.getMaskedCardNumber(),
				expirationMonth,
				expirationYear,
				creditCard.getFirstName(),
				creditCard.getLastName(),
				creditCard.getCompanyName(),
				Email.valueOf(creditCard.getEmail()),
				creditCard.getPhone(),
				creditCard.getFax(),
				creditCard.getCustomerId(),
				creditCard.getCustomerTaxId(),
				creditCard.getStreetAddress1(),
				creditCard.getStreetAddress2(),
				creditCard.getCity(),
				creditCard.getState(),
				creditCard.getPostalCode(),
				creditCard.getCountryCode(),
				creditCard.getComments(),
				System.currentTimeMillis(),
				principalName
			);
			return Integer.toString(pkey);
		} catch(ValidationException | IOException err) {
			throw new SQLException(err);
		}
	}

	/**
	 * Stores the results of a sale transaction:
	 * <ol>
	 *   <li>authorizationResult</li>
	 *   <li>captureTime</li>
	 *   <li>capturePrincipalName</li>
	 *   <li>captureResult</li>
	 *   <li>status</li>
	 * </ol>
	 *
	 * The current status must be PROCESSING or AUTHORIZED.
	 */
	@Override
	public void saleCompleted(Principal principal, Transaction transaction) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			String providerId = transaction.getProviderId();
			Processor processor = conn.getPayment().getProcessor().get(providerId);
			if(processor == null) throw new SQLException("Unable to find Processor: " + providerId);
			// Get the stored creditCardTransaction
			int ccTransactionId = Integer.parseInt(transaction.getPersistenceUniqueId());
			Payment ccTransaction = conn.getPayment().getPayment().get(ccTransactionId);
			if(ccTransaction == null) throw new SQLException("Unable to find Payment: " + ccTransactionId);
			if(
				!ccTransaction.getStatus().equals(Transaction.Status.PROCESSING.name())
				&& !ccTransaction.getStatus().equals(Transaction.Status.AUTHORIZED.name())
			) {
				throw new SQLException(
					"CreditCardTransaction #" + ccTransactionId + " must have status "
					+ Transaction.Status.PROCESSING.name()
					+ " or "
					+ Transaction.Status.AUTHORIZED.name()
					+ ", its current status is " + ccTransaction.getStatus()
				);
			}

			AuthorizationResult authorizationResult = transaction.getAuthorizationResult();
			TransactionResult.CommunicationResult authorizationCommunicationResult = authorizationResult.getCommunicationResult();
			TransactionResult.ErrorCode authorizationErrorCode = authorizationResult.getErrorCode();
			AuthorizationResult.ApprovalResult approvalResult = authorizationResult.getApprovalResult();
			AuthorizationResult.DeclineReason declineReason = authorizationResult.getDeclineReason();
			AuthorizationResult.ReviewReason reviewReason = authorizationResult.getReviewReason();
			AuthorizationResult.CvvResult cvvResult = authorizationResult.getCvvResult();
			AuthorizationResult.AvsResult avsResult = authorizationResult.getAvsResult();

			CaptureResult captureResult = transaction.getCaptureResult();
			TransactionResult.CommunicationResult captureCommunicationResult = captureResult.getCommunicationResult();
			TransactionResult.ErrorCode captureErrorCode = captureResult.getErrorCode();

			TokenizedCreditCard tokenizedCreditCard = authorizationResult.getTokenizedCreditCard();
			ccTransaction.saleCompleted(
				authorizationCommunicationResult==null ? null : authorizationCommunicationResult.name(),
				authorizationResult.getProviderErrorCode(),
				authorizationErrorCode==null ? null : authorizationErrorCode.name(),
				authorizationResult.getProviderErrorMessage(),
				authorizationResult.getProviderUniqueId(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getProviderReplacementMaskedCardNumber(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getReplacementMaskedCardNumber(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getProviderReplacementExpiration(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getReplacementExpirationMonth(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getReplacementExpirationYear(),
				authorizationResult.getProviderApprovalResult(),
				approvalResult==null ? null : approvalResult.name(),
				authorizationResult.getProviderDeclineReason(),
				declineReason==null ? null : declineReason.name(),
				authorizationResult.getProviderReviewReason(),
				reviewReason==null ? null : reviewReason.name(),
				authorizationResult.getProviderCvvResult(),
				cvvResult==null ? null : cvvResult.name(),
				authorizationResult.getProviderAvsResult(),
				avsResult==null ? null : avsResult.name(),
				authorizationResult.getApprovalCode(),
				transaction.getCaptureTime()==-1 ? null : new Timestamp(transaction.getCaptureTime()),
				transaction.getCapturePrincipalName(),
				captureCommunicationResult==null ? null : captureCommunicationResult.name(),
				captureResult.getProviderErrorCode(),
				captureErrorCode==null ? null : captureErrorCode.name(),
				captureResult.getProviderErrorMessage(),
				captureResult.getProviderUniqueId(),
				transaction.getStatus().name()
			);
		} catch(IOException err) {
			throw new SQLException(err);
		}
	}

	/**
	 * Stores the results of an authorize transaction:
	 * <ol>
	 *   <li>authorizationResult</li>
	 *   <li>status</li>
	 * </ol>
	 *
	 * The current status must be PROCESSING.
	 */
	@Override
	public void authorizeCompleted(Principal principal, Transaction transaction) throws SQLException {
		try {
			AOServConnector conn = getAOServConnector(principal);
			String providerId = transaction.getProviderId();
			Processor processor = conn.getPayment().getProcessor().get(providerId);
			if(processor == null) throw new SQLException("Unable to find Processor: " + providerId);
			// Get the stored creditCardTransaction
			int ccTransactionId = Integer.parseInt(transaction.getPersistenceUniqueId());
			Payment ccTransaction = conn.getPayment().getPayment().get(ccTransactionId);
			if(ccTransaction == null) throw new SQLException("Unable to find Payment: " + ccTransactionId);
			if(!ccTransaction.getStatus().equals(Transaction.Status.PROCESSING.name())) throw new SQLException("CreditCardTransaction #"+ccTransactionId+" must have status "+Transaction.Status.PROCESSING.name()+", its current status is "+ccTransaction.getStatus());

			AuthorizationResult authorizationResult = transaction.getAuthorizationResult();
			TransactionResult.CommunicationResult authorizationCommunicationResult = authorizationResult.getCommunicationResult();
			TransactionResult.ErrorCode authorizationErrorCode = authorizationResult.getErrorCode();
			AuthorizationResult.ApprovalResult approvalResult = authorizationResult.getApprovalResult();
			AuthorizationResult.DeclineReason declineReason = authorizationResult.getDeclineReason();
			AuthorizationResult.ReviewReason reviewReason = authorizationResult.getReviewReason();
			AuthorizationResult.CvvResult cvvResult = authorizationResult.getCvvResult();
			AuthorizationResult.AvsResult avsResult = authorizationResult.getAvsResult();

			TokenizedCreditCard tokenizedCreditCard = authorizationResult.getTokenizedCreditCard();
			ccTransaction.authorizeCompleted(
				authorizationCommunicationResult==null ? null : authorizationCommunicationResult.name(),
				authorizationResult.getProviderErrorCode(),
				authorizationErrorCode==null ? null : authorizationErrorCode.name(),
				authorizationResult.getProviderErrorMessage(),
				authorizationResult.getProviderUniqueId(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getProviderReplacementMaskedCardNumber(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getReplacementMaskedCardNumber(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getProviderReplacementExpiration(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getReplacementExpirationMonth(),
				tokenizedCreditCard == null ? null : tokenizedCreditCard.getReplacementExpirationYear(),
				authorizationResult.getProviderApprovalResult(),
				approvalResult==null ? null : approvalResult.name(),
				authorizationResult.getProviderDeclineReason(),
				declineReason==null ? null : declineReason.name(),
				authorizationResult.getProviderReviewReason(),
				reviewReason==null ? null : reviewReason.name(),
				authorizationResult.getProviderCvvResult(),
				cvvResult==null ? null : cvvResult.name(),
				authorizationResult.getProviderAvsResult(),
				avsResult==null ? null : avsResult.name(),
				authorizationResult.getApprovalCode(),
				transaction.getStatus().name()
			);
		} catch(IOException err) {
			throw new SQLException(err);
		}
	}

	@Override
	public void voidCompleted(Principal principal, Transaction transaction) throws SQLException {
		AOServConnector conn = getAOServConnector(principal);

		throw new RuntimeException("TODO: Implement method");
	}
}
