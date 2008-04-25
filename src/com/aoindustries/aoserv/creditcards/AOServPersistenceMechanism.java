package com.aoindustries.aoserv.creditcards;

/*
 * Copyright 2007 by AO Industries, Inc.,
 * 816 Azalea Rd, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.Business;
import com.aoindustries.aoserv.client.BusinessAdministrator;
import com.aoindustries.aoserv.client.CountryCode;
import com.aoindustries.aoserv.client.CreditCardProcessor;
import com.aoindustries.aoserv.client.CreditCardTransaction;
import com.aoindustries.creditcards.AuthorizationResult;
import com.aoindustries.creditcards.CaptureResult;
import com.aoindustries.creditcards.CreditCard;
import com.aoindustries.creditcards.PersistenceMechanism;
import com.aoindustries.creditcards.Transaction;
import com.aoindustries.creditcards.TransactionRequest;
import com.aoindustries.creditcards.TransactionResult;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Stores the information in the AOServ system.  The principal sent in to the
 * methods should be an instance of <code>AOServConnectorPrincipal</code> and
 * any group should be a <code>BusinessGroup</code>.
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

    private static Business getBusiness(Group group) throws SQLException {
        if(group==null) throw new SQLException("group is null");
        if(!(group instanceof BusinessGroup)) throw new SQLException("group is not a BusinessGroup: "+group.getName());
        return ((BusinessGroup)group).getBusiness();
    }

    private static String getGroupName(Group group) throws SQLException {
        if(group==null) throw new SQLException("group is null");
        if(!(group instanceof BusinessGroup)) throw new SQLException("group is not a BusinessGroup: "+group.getName());
        return ((BusinessGroup)group).getGroupName();
    }

    @Override
    public String storeCreditCard(Principal principal, CreditCard creditCard, Locale userLocale) throws SQLException {
        AOServConnector conn = getAOServConnector(principal);
        String principalName = getPrincipalName(principal);
        Business business = conn.businesses.get(creditCard.getGroupName());
        if(business==null) throw new SQLException("Unable to find Business: "+creditCard.getGroupName());
        CreditCardProcessor processor = conn.creditCardProcessors.get(creditCard.getProviderId());
        if(processor==null) throw new SQLException("Unable to find CreditCardProcessor: "+creditCard.getProviderId());
        CountryCode countryCode = conn.countryCodes.get(creditCard.getCountryCode());
        if(countryCode==null) throw new SQLException("Unable to find CountryCode: "+creditCard.getCountryCode());
        int pkey = business.addCreditCard(
            processor,
            creditCard.getGroupName(),
            creditCard.getMaskedCardNumber(),
            creditCard.getProviderUniqueId(),
            creditCard.getFirstName(),
            creditCard.getLastName(),
            creditCard.getCompanyName(),
            creditCard.getEmail(),
            creditCard.getPhone(),
            creditCard.getFax(),
            creditCard.getCustomerTaxId(),
            creditCard.getStreetAddress1(),
            creditCard.getStreetAddress2(),
            creditCard.getCity(),
            creditCard.getState(),
            creditCard.getPostalCode(),
            countryCode,
            principalName,
            creditCard.getComments(),
            creditCard.getCardNumber(),
            creditCard.getExpirationMonth(),
            creditCard.getExpirationYear()
        );
        return Integer.toString(pkey);
    }

    @Override
    public void updateCardNumber(Principal principal, CreditCard creditCard, String maskedCardNumber, String cardNumber, byte expirationMonth, short expirationYear, Locale userLocale) throws SQLException {
        try {
            AOServConnector conn = getAOServConnector(principal);
            int pkey = Integer.parseInt(creditCard.getPersistenceUniqueId());
            com.aoindustries.aoserv.client.CreditCard aoservCreditCard = conn.creditCards.get(pkey);
            if(aoservCreditCard==null) throw new SQLException("Unable to find CreditCard: "+pkey);
            aoservCreditCard.updateCardNumberAndExpiration(maskedCardNumber, cardNumber, expirationMonth, expirationYear);
        } catch(NumberFormatException err) {
            SQLException sqlErr = new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId());
            sqlErr.initCause(err);
            throw sqlErr;
        } catch(IOException err) {
            SQLException sqlErr = new SQLException();
            sqlErr.initCause(err);
            throw sqlErr;
        }
    }

    @Override
    public void updateExpiration(Principal principal, CreditCard creditCard, byte expirationMonth, short expirationYear, Locale userLocale) throws SQLException {
        try {
            AOServConnector conn = getAOServConnector(principal);
            int pkey = Integer.parseInt(creditCard.getPersistenceUniqueId());
            com.aoindustries.aoserv.client.CreditCard aoservCreditCard = conn.creditCards.get(pkey);
            if(aoservCreditCard==null) throw new SQLException("Unable to find CreditCard: "+pkey);
            aoservCreditCard.updateCardExpiration(expirationMonth, expirationYear);
        } catch(NumberFormatException err) {
            SQLException sqlErr = new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId());
            sqlErr.initCause(err);
            throw sqlErr;
        } catch(IOException err) {
            SQLException sqlErr = new SQLException();
            sqlErr.initCause(err);
            throw sqlErr;
        }
    }

    @Override
    public void deleteCreditCard(Principal principal, CreditCard creditCard, Locale userLocale) throws SQLException {
        try {
            AOServConnector conn = getAOServConnector(principal);
            int pkey = Integer.parseInt(creditCard.getPersistenceUniqueId());
            com.aoindustries.aoserv.client.CreditCard aoservCreditCard = conn.creditCards.get(pkey);
            if(aoservCreditCard==null) throw new SQLException("Unable to find CreditCard: "+pkey);
            aoservCreditCard.remove();
        } catch(NumberFormatException err) {
            SQLException sqlErr = new SQLException("Unable to convert providerUniqueId to pkey: "+creditCard.getPersistenceUniqueId());
            sqlErr.initCause(err);
            throw sqlErr;
        }
    }

    @Override
    public String insertTransaction(Principal principal, Group group, Transaction transaction, Locale userLocale) throws SQLException {
        try {
            AOServConnector conn = getAOServConnector(principal);
            String principalName = getPrincipalName(principal);
            Business business = getBusiness(group);
            String groupName = getGroupName(group);
            String providerId = transaction.getProviderId();
            CreditCardProcessor processor = conn.creditCardProcessors.get(providerId);
            if(processor==null) throw new SQLException("Unable to find CreditCardProcessor: "+providerId);
            TransactionRequest transactionRequest = transaction.getTransactionRequest();
            CreditCard creditCard = transaction.getCreditCard();
            // Try to find the createdBy from the credit card persistence mechanism, otherwise default to current principal
            BusinessAdministrator creditCardCreatedBy;
            Business ccBusiness;
            {
                String ccPersistId = creditCard.getPersistenceUniqueId();
                if(ccPersistId==null || ccPersistId.length()==0) {
                    creditCardCreatedBy = conn.getThisBusinessAdministrator();
                    ccBusiness = business;
                } else {
                    int ccPersistIdInt = Integer.parseInt(ccPersistId);
                    com.aoindustries.aoserv.client.CreditCard storedCard = conn.creditCards.get(ccPersistIdInt);
                    if(storedCard==null) throw new SQLException("Unable to find CreditCard: "+ccPersistIdInt);
                    creditCardCreatedBy = storedCard.getCreatedBy();
                    if(creditCardCreatedBy==null) {
                        // Might have been filtered - this is OK
                        creditCardCreatedBy = conn.getThisBusinessAdministrator();
                    }
                    ccBusiness = storedCard.getBusiness();
                }
            }
            int pkey = business.addCreditCardTransaction(
                processor,
                groupName,
                transactionRequest.getTestMode(),
                transactionRequest.getDuplicateWindow(),
                transactionRequest.getOrderNumber(),
                transactionRequest.getCurrencyCode().name(),
                transactionRequest.getAmount(),
                transactionRequest.getTaxAmount(),
                transactionRequest.getTaxExempt(),
                transactionRequest.getShippingAmount(),
                transactionRequest.getDutyAmount(),
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
                transactionRequest.getMerchantEmail(),
                transactionRequest.getInvoiceNumber(),
                transactionRequest.getPurchaseOrderNumber(),
                transactionRequest.getDescription(),
                creditCardCreatedBy,
                creditCard.getPrincipalName(),
                ccBusiness,
                creditCard.getGroupName(),
                creditCard.getProviderUniqueId(),
                creditCard.getMaskedCardNumber(),
                creditCard.getFirstName(),
                creditCard.getLastName(),
                creditCard.getCompanyName(),
                creditCard.getEmail(),
                creditCard.getPhone(),
                creditCard.getFax(),
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
        } catch(IOException err) {
            SQLException sqlErr = new SQLException();
            sqlErr.initCause(err);
            throw sqlErr;
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
     * The current status must be PROCESSING.
     */
    @Override
    public void saleCompleted(Principal principal, Transaction transaction, Locale userLocale) throws SQLException {
        try {
            long currentTime = System.currentTimeMillis();
            AOServConnector conn = getAOServConnector(principal);
            String providerId = transaction.getProviderId();
            CreditCardProcessor processor = conn.creditCardProcessors.get(providerId);
            if(processor==null) throw new SQLException("Unable to find CreditCardProcessor: "+providerId);
            // Get the stored creditCardTransaction
            int ccTransactionId = Integer.parseInt(transaction.getPersistenceUniqueId());
            CreditCardTransaction ccTransaction = conn.getCreditCardTransactions().get(ccTransactionId);
            if(ccTransaction==null) throw new SQLException("Unable to find CreditCardTransaction: "+ccTransactionId);
            if(!ccTransaction.getStatus().equals(Transaction.Status.PROCESSING.name())) throw new SQLException("CreditCardTransaction #"+ccTransactionId+" must have status "+Transaction.Status.PROCESSING.name()+", its current status is "+ccTransaction.getStatus());

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

            ccTransaction.saleCompleted(
                authorizationCommunicationResult==null ? null : authorizationCommunicationResult.name(),
                authorizationResult.getProviderErrorCode(),
                authorizationErrorCode==null ? null : authorizationErrorCode.name(),
                authorizationResult.getProviderErrorMessage(),
                authorizationResult.getProviderUniqueId(),
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
                transaction.getCaptureTime(),
                transaction.getCapturePrincipalName(),
                captureCommunicationResult==null ? null : captureCommunicationResult.name(),
                captureResult.getProviderErrorCode(),
                captureErrorCode==null ? null : captureErrorCode.name(),
                captureResult.getProviderErrorMessage(),
                captureResult.getProviderUniqueId(),
                transaction.getStatus().name()
            );
        } catch(IOException err) {
            SQLException sqlErr = new SQLException();
            sqlErr.initCause(err);
            throw sqlErr;
        }
    }

    @Override
    public void voidCompleted(Principal principal, Transaction transaction, Locale userLocale) throws SQLException {
        AOServConnector conn = getAOServConnector(principal);

        throw new RuntimeException("TODO: Implement method");
    }
}
