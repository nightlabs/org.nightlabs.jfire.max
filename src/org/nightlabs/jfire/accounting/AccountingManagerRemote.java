package org.nightlabs.jfire.accounting;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.id.LocalAccountantDelegateID;
import org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowMapping;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate.ResolvedMapEntry;
import org.nightlabs.jfire.accounting.book.mappingbased.MappingBasedAccountantDelegate.ResolvedMapKey;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.id.TariffMappingID;
import org.nightlabs.jfire.accounting.pay.CheckRequirementsEnvironment;
import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.accounting.pay.config.ModeOfPaymentConfigModule;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.accounting.priceconfig.AffectedProductType;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.accounting.query.MoneyTransferIDQuery;
import org.nightlabs.jfire.accounting.query.MoneyTransferQuery;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.query.InvoiceQuery;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.jfire.transfer.id.TransferID;

@Remote
public interface AccountingManagerRemote {

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 */
	void initialise() throws IOException;

	/**
	 * Get the object-ids of all existing {@link TariffMapping}s.
	 * <p>
	 * This method can be called by everyone, because it does not reveal critical data.
	 * </p>
	 */
	Set<TariffMappingID> getTariffMappingIDs();

	/**
	 * Get the object-ids of all existing {@link TariffMapping}s.
	 * <p>
	 * This method can be called by everyone, because it does not reveal critical data.
	 * </p>
	 */
	Collection<TariffMapping> getTariffMappings(
			Collection<TariffMappingID> tariffMappingIDs, String[] fetchGroups,
			int maxFetchDepth);

	TariffMapping createTariffMapping(TariffID localTariffID,
			TariffID partnerTariffID, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get the object-ids of all {@link Tariff}s or the ones matching (or not matching, if <code>inverse == true</code>)
	 * the given <code>organisationID</code>.
	 * <p>
	 * This method can be called by everyone, because it does not reveal any sensitive information.
	 * </p>
	 *
	 */
	Set<TariffID> getTariffIDs(String organisationID, boolean inverse);

	/**
	 * Get the {@link Tariff}s for the specified object-ids.
	 * <p>
	 * Everyone can call this method. At the moment, all requested {@link Tariff}s will be returned. Later on, this
	 * method will filter the result and only give the user those <code>Tariff</code>s he is allowed to see.
	 * </p>
	  */
	Collection<Tariff> getTariffs(Collection<TariffID> tariffIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Save a new or modified <code>Tariff</code> to the server.
	 *
	 */
	Tariff storeTariff(Tariff tariff, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get all {@link Currency} instances.
	 * <p>
	 * This method can be called by everyone, because currencies are not confidential.
	 * </p>
	 */
	Collection<Currency> getCurrencies(String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get all {@link Currency} instances by Collection of {@link CurrencyID}
	 * <p>
	 * This method can be called by everyone, because currencies are not confidential.
	 * </p>
	 */
	Collection<Currency> getCurrencies(Collection<CurrencyID> currencyIDs,
			String[] fetchGroups, int maxFetchDepth);

	Collection<CurrencyID> getCurrencyIDs();

	Set<AnchorID> getAccountIDs(AccountSearchFilter searchFilter);


	List<Account> getAccounts(Collection<AnchorID> accountIDs,
			String[] fetchGroups, int maxFetchDepth);

	Account storeAccount(Account account, boolean get, String[] fetchGroups,
			int maxFetchDepth);



	Currency storeCurrency(Currency currency, boolean get, String[] fetchGroups,
			int maxFetchDepth);





	/**
	 * @param anchorID The anchorID of the Account wich SummaryAccounts are to be set
	 * @param SummaryAccounts A Collection of the AnchorIDs of the SummaryAccounts to be set to the given Account.
	*/
	void setAccountSummaryAccounts(AnchorID anchorID,
			Collection<AnchorID> _summaryAccountIDs);

	/**
	 * @param summaryAccountID The anchorID of the SummaryAccount wich summedAccounts are to be set
	 * @param summedAccountIDs A Collection of the AnchorIDs of the Accounts to be summed up by the given SummaryAccount.
	 */
	void setSummaryAccountSummedAccounts(AnchorID summaryAccountID,
			Collection<AnchorID> _summedAccountIDs);

	/**
	 * Returns a Collection of {@link LocalAccountantDelegateID} not detached
	 * delegates.
	 *
	 * @param delegateClass The class/type of delegates that should be returned.
	  */
	Collection<LocalAccountantDelegateID> getTopLevelAccountantDelegates(
			Class<? extends LocalAccountantDelegate> delegateClass);

	/**
	 * Returns a Clloection of {@link LocalAccountantDelegateID} not detached
	 * delegates which have the given delegate as extendedLocalAccountantDelegate
	 *
	 * @param delegateID The LocalAccountantDelegateID children should be searched for.
	 */
	Collection<LocalAccountantDelegateID> getChildAccountantDelegates(
			LocalAccountantDelegateID delegateID);

	/**
	 * Returns detached instances of the LocalAccountantDelegates referenced by
	 * the given LocalAccountantDelegateIDs in the delegateIDs parameter.
	 *
	 * @param delegateIDs The LocalAccountantDelegateID of the delegates to return.
	 * @param fetchGroups The fetchGroups to detach the delegates with.
	*/
	Collection<LocalAccountantDelegate> getLocalAccountantDelegates(
			Collection<LocalAccountantDelegateID> delegateIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the detached LocalAccountantDelegates referenced by
	 * the given LocalAccountantDelegateID.
	 *
	 * @param delegateID The LocalAccountantDelegateID of the delegate to return.
	 * @param fetchGroups The fetchGroups to detach the delegates with.
	*/
	LocalAccountantDelegate getLocalAccountantDelegate(
			LocalAccountantDelegateID delegateID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Stores the given LocalAccountantDelegate and returns a newly detached
	 * version of it if desired.
	 *
	 * @param delegate The LocalAccountantDelegate to store.
	 * @param get Whether or not to return the a newly detached version of the stored delegate.
	 * @param fetchGroups The fetchGroups to detach the stored delegate with.
	*/
	LocalAccountantDelegate storeLocalAccountantDelegate(
			LocalAccountantDelegate delegate, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	MoneyFlowMapping storeMoneyFlowMapping(MoneyFlowMapping mapping,
			boolean get, String[] fetchGroups, int maxFetchDepth);

	Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(
			ProductTypeID productTypeID, String[] mappingFetchGroups,
			int maxFetchDepth);

	Map<ResolvedMapKey, ResolvedMapEntry> getResolvedMoneyFlowMappings(
			ProductTypeID productTypeID, LocalAccountantDelegateID delegateID,
			String[] mappingFetchGroups, int maxFetchDepth);

	/**
	 * Get all {@link PriceFragmentType}s or those specified by <code>priceFragmentTypeIDs</code>.
	 * <p>
	 * This method can be called by everyone, since price fragment types don't reveal any information
	 * except their name. This is - at least for now - not considered confidential.
	 * </p>
	 *
	 * @param priceFragmentTypeIDs Can be <code>null</code> in order to return ALL {@link PriceFragmentType}s or a collection of {@link PriceFragmentTypeID}s to return only a subset.
	 */
	Collection<PriceFragmentType> getPriceFragmentTypes(
			Collection<PriceFragmentTypeID> priceFragmentTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get a <code>Collection</code> of <code>PriceFragmentTypeID</code> of all known <code>PriceFragementType</code>s.
	 * <p>
	 * This method can be called by everyone, because the object-ids don't reveal any confidential information.
	 * </p>
	 */
	Collection<PriceFragmentTypeID> getPriceFragmentTypeIDs();

	/**
	 * Creates an Invoice for all specified <code>Article</code>s. If
	 * get is true, a detached version of the new Invoice will be returned.
	 *
	 * @param articleIDs The {@link ArticleID}s of those {@link Article}s that shall be added to the new <code>Invoice</code>.
	 * @param get Whether a detached version of the created Invoice should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the invoice should be detached with.
	 * @return Detached Invoice or null.
	 *
	 * @throws InvoiceEditException if the invoice cannot be created with the given parameters.
	 */
	Invoice createInvoice(Collection<ArticleID> articleIDs,
			String invoiceIDPrefix, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws InvoiceEditException;

	/**
	 * Creates an Invoice for all Articles of the Offer identified by
	 * the given offerID. If get is true, a detached version of the
	 * new Invoice will be returned.
	 *
	 * @param offerID OfferID of the offer to be billed.
	 * @param get Whether a detached version of the created Invoice should be returned, otherwise null will be returned.
	 * @param fetchGroups Array ouf fetch-groups the invoice should be detached with.
	 * @return Detached Invoice or null.
	 *
	 * @throws InvoiceEditException if the invoice cannot be created with the given parameters.
	  */
	Invoice createInvoice(ArticleContainerID articleContainerID,
			String invoiceIDPrefix, boolean get, String[] fetchGroups,
			int maxFetchDepth) throws InvoiceEditException;


	Invoice addArticlesToInvoice(InvoiceID invoiceID,
			Collection<ArticleID> articleIDs, boolean validate, boolean get,
			String[] fetchGroups, int maxFetchDepth)
			throws InvoiceEditException;

	Invoice removeArticlesFromInvoice(InvoiceID invoiceID,
			Collection<ArticleID> articleIDs, boolean validate, boolean get,
			String[] fetchGroups, int maxFetchDepth)
			throws InvoiceEditException;

	/**
	 * @param paymentDataList A <tt>List</tt> of {@link PaymentData}.
	 * @return A <tt>List</tt> with instances of {@link PaymentResult} in the same
	 *		order as and corresponding to the {@link PaymentData} objects passed in
	 *		<tt>paymentDataList</tt>.
	 */
	List<PaymentResult> payBegin(List<PaymentData> paymentDataList);

	/**
	 * @param serverPaymentProcessorID Might be <tt>null</tt>.
	 * @param paymentDirection Either
	 *		{@link ServerPaymentProcessor#PAYMENT_DIRECTION_INCOMING}
	 *		or {@link ServerPaymentProcessor#PAYMENT_DIRECTION_OUTGOING}.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#payBegin(User, PaymentData)
	 */
	PaymentResult payBegin(PaymentData paymentData);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @param paymentIDs Instances of {@link PaymentID}
	 * @param payDoWorkClientResults Instances of {@link PaymentResult} corresponding
	 *		to the <tt>paymentIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all payies will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link PaymentResult} in the same
	 *		order as and corresponding to the {@link PaymentID} objects passed in
	 *		<tt>paymentIDs</tt>.
	 */
	List<PaymentResult> payDoWork(List<PaymentID> paymentIDs,
			List<PaymentResult> payDoWorkClientResults, boolean forceRollback);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#payEnd(User, PaymentData)
	 */
	PaymentResult payDoWork(PaymentID paymentID,
			PaymentResult payEndClientResult, boolean forceRollback);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @param paymentIDs Instances of {@link PaymentID}
	 * @param payEndClientResults Instances of {@link PaymentResult} corresponding
	 *		to the <tt>paymentIDs</tt>. Hence, both lists must have the same number of items.
	 * @param forceRollback If <tt>true</tt> all payments will be rolled back, even if they
	 *		have been successful so far.
	 * @return A <tt>List</tt> with instances of {@link PaymentResult} in the same
	 *		order as and corresponding to the {@link PaymentID} objects passed in
	 *		<tt>paymentIDs</tt>.
	 *
	 */
	List<PaymentResult> payEnd(List<PaymentID> paymentIDs,
			List<PaymentResult> payEndClientResults, boolean forceRollback);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>payBegin</code> was
	 * called before, which is restricted.
	 *
	 * @throws ModuleException
	 *
	 * @see Accounting#payEnd(User, PaymentData)
	 */
	PaymentResult payEnd(PaymentID paymentID, PaymentResult payEndClientResult,
			boolean forceRollback);

	/**
	 * @param invoiceQueries Instances of {@link InvoiceQuery} that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link AbstractJDOQuery#setCandidates(Collection)}.
	 */
	Set<InvoiceID> getInvoiceIDs(
			QueryCollection<? extends AbstractJDOQuery> invoiceQueries);

	/**
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	List<Invoice> getInvoices(Set<InvoiceID> invoiceIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer.
	 * They are ordered by invoiceID descending (means newest first).
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link Invoice}.
	 */
	List<InvoiceID> getInvoiceIDs(AnchorID vendorID, AnchorID customerID,
			AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx);

	/**
	 * This method queries all <code>Invoice</code>s which exist between the given vendor and customer and
	 * are not yet finalized. They are ordered by invoiceID descending (means newest first).
	 *
	 */
	List<Invoice> getNonFinalizedInvoices(AnchorID vendorID,
			AnchorID customerID, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the mode of payment flavours that are available to the specified customer groups.
	 * <p>
	 * This method can be called by everyone, because mode of payment flavours are not considered confidential.
	 * </p>
	 *
	 * @param customerGroupIDs A <tt>Collection</tt> of {@link CustomerGroupID}. If <tt>null</tt>, all {@link ModeOfPaymentFlavour}s will be returned.
	 * @param mergeMode one of {@link ModeOfPaymentFlavour#MERGE_MODE_INTERSECTION} or {@link ModeOfPaymentFlavour#MERGE_MODE_UNION}
	 * @param filterByConfig
	 * 		If this is <code>true</code> the flavours available found for the given customer-groups will also be filtered by the
	 * 		intersection of the entries configured in the {@link ModeOfPaymentConfigModule} for the current user and the
	 * 		workstation he is currently loggen on.
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 */
	Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForAllCustomerGroups(
			Collection<CustomerGroupID> customerGroupIDs, byte mergeMode,
			boolean filterByConfig, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the object-ids of all modes of payment.
	 * <p>
	 * This method can be called by everyone, because the returned object-ids are not confidential.
	 * </p>
	 */
	Set<ModeOfPaymentID> getAllModeOfPaymentIDs();

	/**
	 * Get the modes of payment that are specified by the given object-ids.
	 * <p>
	 * This method can be called by everyone, because modes of payment are not considered confidential.
	 * </p>
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 *
	 */
	Collection<ModeOfPayment> getModeOfPayments(
			Set<ModeOfPaymentID> modeOfPaymentIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get the object-ids of all mode of payment flavours.
	 * <p>
	 * This method can be called by everyone, because the returned object-ids are not confidential.
	 * </p>
	 */
     Set<ModeOfPaymentFlavourID> getAllModeOfPaymentFlavourIDs();

	/**
	 * Get the mode of payment flavours that are specified by the given object-ids.
	 * <p>
	 * This method can be called by everyone, because mode of payment flavours are not considered confidential.
	 * </p>
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 */
	Collection<ModeOfPaymentFlavour> getModeOfPaymentFlavours(
			Set<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the server payment processors available for the specified mode of payment flavour.
	 * <p>
	 * This method can be called by everyone, because the returned server payment processors do not reveal confidential data.
	 * </p>
	 *
	 * @param fetchGroups Either <tt>null</tt> or all desired fetch groups.
	 */
	Collection<ServerPaymentProcessor> getServerPaymentProcessorsForOneModeOfPaymentFlavour(
			ModeOfPaymentFlavourID modeOfPaymentFlavourID,
			CheckRequirementsEnvironment checkRequirementsEnvironment,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Get the mode of payment flavours that are available to the specified customer group.
	 * <p>
	 * This method can be called by everyone, because the mode of payment flavours are not confidential.
	 * </p>
	 */
	Collection<ModeOfPaymentFlavour> getAvailableModeOfPaymentFlavoursForOneCustomerGroup(
			CustomerGroupID customerGroupID, boolean filterByConfig,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @param productTypeID The object ID of the desired ProductType.
	 *
	 */
	ProductType getProductTypeForPriceConfigEditing(ProductTypeID productTypeID);


	void signalInvoice(InvoiceID invoiceID, String jbpmTransitionName);

	/**
	 * @return The returned Map&lt;PriceConfigID, List&lt;AffectedProductType&gt;&gt; indicates which modified
	 *		price config would result in which products to have their prices recalculated.
	 */
	Map<PriceConfigID, List<AffectedProductType>> getAffectedProductTypes(
			Set<PriceConfigID> priceConfigIDs, ProductTypeID productTypeID,
			PriceConfigID innerPriceConfigID);


	Set<AnchorID> getAccountIDs(
			QueryCollection<? extends AbstractJDOQuery> queries);


	ManualMoneyTransfer createManualMoneyTransfer(AnchorID fromID,
			AnchorID toID, CurrencyID currencyID, long amount, I18nText reason,
			boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * This method is faster than {@link #getMoneyTransferIDs(Collection)}, because
	 * it directly queries object-ids.
	 *
	 * @param productTransferIDQuery The query to execute.
	 */
	List<TransferID> getMoneyTransferIDs(
			MoneyTransferIDQuery productTransferIDQuery);

	/**
	 * Unlike {@link #getMoneyTransferIDs(MoneyTransferIDQuery)}, this method allows
	 * for cascading multiple queries. It is slower than {@link #getMoneyTransferIDs(MoneyTransferIDQuery)}
	 * and should therefore only be used, if it's essentially necessary.
	 *
	 * @param moneyTransferQueries A <code>Collection</code> of {@link MoneyTransferQuery}. They will be executed
	 *		in the given order (if it's a <code>List</code>) and the result of the previous query will be passed as candidates
	 *		to the next query.

	 */
	List<TransferID> getMoneyTransferIDs(
			Collection<MoneyTransferQuery> moneyTransferQueries);


	List<MoneyTransfer> getMoneyTransfers(
			Collection<TransferID> moneyTransferIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Get the object-ids of all {@link AccountType}s.
	 * <p>
	 * This method can be called by everyone, because the object-ids are not confidential.
	 * </p>

	 */
	Set<AccountTypeID> getAccountTypeIDs();

	/**
	 * Get the <code>AccountType</code>s for the specified object-ids.
	 * <p>
	 * This method can be called by everyone, because the <code>AccountType</code>s are not confidential.
	 */
	List<AccountType> getAccountTypes(Collection<AccountTypeID> accountTypeIDs,
			String[] fetchGroups, int maxFetchDepth);


	PriceFragmentType storePriceFragmentType(
			PriceFragmentType priceFragmentType, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	Set<PaymentID> getPaymentIDsForPayableObjectID(ObjectID payableObject);
	
	List<Payment> getPayments(final Collection<PaymentID> paymentIDs, final String[] fetchGroups, final int maxFetchDepth);
	
	String ping(String message);
}