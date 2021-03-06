package org.nightlabs.jfire.accounting.book;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransferFactory;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransferFactoryJDO;
import org.nightlabs.jfire.accounting.PayableObjectMoneyTransfer.BookType;
import org.nightlabs.jfire.accounting.pay.PayMoneyTransfer;
import org.nightlabs.jfire.accounting.pay.PayableObject;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Marius Heinzmann
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true"
)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class PartnerPayPayableObjectAccountantDelegate
	extends AccountantDelegate
{
	public static final String PAYABLE_OBJECT_FACTORY_SCOPE = "PartnerPayPayableObjectAccountantDelegate";

	private static final long serialVersionUID = 1L;
	private static final String ACCOUNTANT_DELEGATE_ID = "internal.partner.payPayableObject";

	private static final Logger logger = LoggerFactory.getLogger(PartnerPayPayableObjectAccountantDelegate.class);

	/**
	 * @deprecated Constructor only existing for JDO!
	 */
	@Deprecated
	protected PartnerPayPayableObjectAccountantDelegate() { }

	public PartnerPayPayableObjectAccountantDelegate(String organisationID)
	{
		super(organisationID, ACCOUNTANT_DELEGATE_ID);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.book.AccountantDelegate#bookTransfer(org.nightlabs.jfire.security.User, org.nightlabs.jfire.trade.LegalEntity, org.nightlabs.jfire.accounting.MoneyTransfer, java.util.Set)
	 */
	@Override
	public void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors)
	{
		if (! PayMoneyTransfer.class.isInstance(transfer))
			return;
		
		handlePayMoneyTransfer(user, mandator, (PayMoneyTransfer) transfer, involvedAnchors);
	}

	/**
	 * Handles payments by creating IntraLegalMoneyTransfer.<br/>
	 * Implements the following:
	 * <pre>
	 *   * sort invoices (organisationID / invoiceID)
	 *   * classify in invoicesPayMoney and invoicesRecieveMoney
	 *   * balance invoices; negative balance means, we have to pay
	 *   if (balance >= 0)
	 *     * clear all invoicesReceiveMoney
	 *     * clear all possible invoicesPayMoney following the order
	 *   else
	 *     if (have invoicesReceiveMoney)
	 *       * clear all invoicesPayMoney
	 *       * clear all possible invoicesRecieveMoney following the order
	 *     else
	 *       * not handled yet
	 * </pre>
	 * @param partner
	 * @param partnerAccount If you pay without related <tt>Invoice</tt>s, you must
	 *		define the Account of the partner to which the money shall be transferred.
	 *		This will be ignored if there are <tt>Invoice<tt>s. In this case, it might be
	 *		<tt>null</tt>.
	 * @param payMoneyTransfer
	 */
	protected void handlePayMoneyTransfer(
			User user, LegalEntity partner,
			PayMoneyTransfer payMoneyTransfer, Set<Anchor> involvedAnchors)
	{
		PersistenceManager pm = getPersistenceManager();
		Account partnerAccount = payMoneyTransfer.getPayment().getPartnerAccount();

		if (partnerAccount != null && partner != null && !partnerAccount.getOwner().getPrimaryKey().equals(partner.getPrimaryKey()))
			throw new IllegalArgumentException("PayMoneyTransfer \""+payMoneyTransfer.getPrimaryKey()+"\": partnerAccount \""+partnerAccount.getPrimaryKey()+"\" is not owned by partner \""+partner.getPrimaryKey()+"\"!");

		if (payMoneyTransfer.getPayment().getPayableObjects().isEmpty() && partnerAccount == null)
			throw new IllegalArgumentException("PayMoneyTransfer \""+payMoneyTransfer.getPrimaryKey()+"\" has no related invoices. Hence, partnerAccount must not be null! payMoneyTransfer.getPayment().getPartnerAccount() is null!!!");

		// Sort invoices (by finalizeDT, organisationID, invoiceIDPrefix, invoiceID).
		//
		// This way JFire guarantees that the booking is the same on vendor and customer side in a multi-organisation-scenario.
		// Without sorting, it might happen e.g. when paying too little money (underpaying), that the vendor keeps invoice 1001
		// open, while the customer pays invoice 1001 completely and instead keeps invoice 1002 open.
		Set<PayableObject> payableObjects = payMoneyTransfer.getPayment().getPayableObjects();
		List<PayableObject> sortedInvoices = new LinkedList<PayableObject>(payableObjects);
		Collections.sort(sortedInvoices, new PayableObjectComparator());

		// What direction is money flowing by the currently processed payment (payMoneyTransfer)?
		// From us (local organisation) to our partner or from our partner to us?
		long payMoneyTransferRealAmount;
		int partnerAnchorType = payMoneyTransfer.getAnchorType(partner);
		switch (partnerAnchorType) {
			case Transfer.ANCHORTYPE_FROM:
				// This means, the partner is RECEIVING money FROM the local organisation, because
				// the PayMoneyTransfer goes FROM the partner-LegalEntity to an outside-account.
				//
				// In other words, the local organisation is paying money to the partner.
				//
				// Transfers:
				//   inside-partner-accounts (customer/vendor/neutral)
				//      ||
				//      || localMoneyTransfers (one per invoice)
				//     \||/
				//      \/
				//   partner-legal-entity
				//      ||
				//      || *payMoneyTransfer* (determining ANCHORTYPE_FROM, i.e. from the legal-entity)
				//     \||/
				//      \/
				//   outside-account (depending on PaymentProcessor)

				payMoneyTransferRealAmount = -1L * payMoneyTransfer.getAmount();

				if (logger.isDebugEnabled())
					logger.debug("handlePayMoneyTransfer: localOrg pays money. payMoneyTransfer=" + payMoneyTransfer.getPrimaryKey());
				break;
			case Transfer.ANCHORTYPE_TO:
				// This means, the partner is PAYING money TO the local organisation, because
				// the PayMoneyTransfer goes TO the partner-LegalEntity from an outside-account.
				//
				// In other words, the local organisation is receiving money from the partner.
				//
				// Transfers:
				//   outside-account (depending on PaymentProcessor)
				//      ||
				//      || *payMoneyTransfer* (determining ANCHORTYPE_TO, i.e. to the legal-entity)
				//     \||/
				//      \/
				//   partner-legal-entity
				//      ||
				//      || localMoneyTransfers (one per invoice)
				//     \||/
				//      \/
				//   inside-partner-accounts (customer/vendor/neutral)

				payMoneyTransferRealAmount = payMoneyTransfer.getAmount();

				if (logger.isDebugEnabled())
					logger.debug("handlePayMoneyTransfer: localOrg receives money. payMoneyTransfer=" + payMoneyTransfer.getPrimaryKey());
				break;
			default:
				throw new UnsupportedOperationException("The partner seems not to be involved in the transfer or there is a new - not supported - anchor type!");
		}

		/* Now, payMoneyTransferRealAmount is positive, if the current PayMoneyTransfer
		 * causes the local organisation to gain money. And it is negative, if the local
		 * organisation looses money.
		 */

		List<TransferInvoiceEntry> invoicesPayMoney = new LinkedList<TransferInvoiceEntry>();
		List<TransferInvoiceEntry> invoicesReceiveMoney = new LinkedList<TransferInvoiceEntry>();

		/* allInvoicesBalance is the amount after summarizing all invoices.
		 * It is negative, if we (local organisation) should pay money to
		 * our partner; and positive, if we should receive money.
		 *
		 * NEGATIVE amount: When local organisation either creates an
		 * invoice with a negative price or partner created an invoice with
		 * a positive amount.
		 *
		 * POSITIVE amount: When local organisation either creates an
		 * invoice with a positive amount or partner created an invoice with
		 * a negative amount.
		 *
		 * Note, that the partner might be a vendor-legal-entity managed by the
		 * local organisation (i.e. without its own datastore).
		 */
		long allInvoicesBalance = 0;

		for (PayableObject payableObject : sortedInvoices) {
			boolean partnerInvoiceCustomer = payableObject.getCustomer().equals(partner);
			boolean partnerInvoiceVendor = !partnerInvoiceCustomer;

			long invoiceBalance = 0;
			if (partnerInvoiceVendor)
				invoiceBalance = - payableObject.getAmountToPay();
			else
				invoiceBalance = payableObject.getAmountToPay();

			allInvoicesBalance += invoiceBalance;

			if (invoiceBalance >= 0) {
				invoicesReceiveMoney.add(new TransferInvoiceEntry(payableObject, invoiceBalance));
			}
			else {
				invoicesPayMoney.add(new TransferInvoiceEntry(payableObject, invoiceBalance));
			}
		}
		/* If the allInvoicesBalance is positive, now, the local organisation is receiving
		 * money in total after all invoices have been summarized. If it's
		 * negative, here, the local organisation looses money by the given invoices.
		 */


		/* restAmount is negative, if after summarizing all invoices and the
		 * PayMoneyTransfer, still money has to be paid from the local organisation to
		 * the partner because of open invoices (or because the partner over-paid).
		 *
		 * restAmount is positive, if after summarizing and receiving by PayMoneyTransfer
		 * still a rest is to be expected from the partner.
		 */
		long restAmount = allInvoicesBalance - payMoneyTransferRealAmount;

		/* The capital is the money that the partner-LegalEntity currently (after the
		 * PayMoneyTransfer) has. It is negative, if the local org has to pay
		 * the partner and positive if the local org receives money from the partner.
		 * The LegalEntity "partner" represents a section within the local organisation,
		 * hence the directions (point of view) are the same as of the local organisation.
		 *
		 * in short: capital is negative when paying and postive when receiving
		 */
		long capital = payMoneyTransferRealAmount;

		if (restAmount >= 0) {
			// We can definitely clear all pay-invoices, because the partner
			// still owes us money. Hence, first all pay-invoices, then
			// as many receive-invoices as possible.

			// first all pay-invoices
			capital = processInvoicesPayMoney(
					user, partner, payMoneyTransfer, invoicesPayMoney, involvedAnchors,
					false, // ignore the value of capital and process really *all* invoices
					capital,
					invoicesReceiveMoney.isEmpty() // indicate that invoicesPayMoney contains the last invoice, if the receive-invoices are empty
			);

			// At least after all the pay-invoices (if not even before), capital must be positive here.
			if (capital < 0)
				throw new IllegalStateException("capital (" + capital + ") < 0 after processing pay-invoices!");

			// now as many receive-invoices as possible
			capital = processInvoicesReceiveMoney(
					user, partner, payMoneyTransfer, invoicesReceiveMoney, involvedAnchors,
					true, // stop when capital reaches 0
					capital,
					true // invoicesReceiveMoney always contains the last invoice, because this is the 2nd (and last) step
			);
		}
		else {
			// We can definitely clear all receive-invoices, because we still
			// owe money to the partner. Hence, first all receive-invoices, then
			// as many pay-invoices as possible.

			// first all receive-invoices
			capital = processInvoicesReceiveMoney(
					user, partner, payMoneyTransfer, invoicesReceiveMoney, involvedAnchors,
					false, // ignore the value of capital and process really *all* invoices
					capital,
					invoicesPayMoney.isEmpty() // indicate that invoicesReceiveMoney contains the last invoice, if the pay-invoices are empty
			);

			// At least after all the receive-invoices (if not even before), capital must be negative here.
			if (capital > 0)
				throw new IllegalStateException("capital (" + capital + ") > 0 after processing receive-invoices!");

			capital = processInvoicesPayMoney(
					user, partner, payMoneyTransfer, invoicesPayMoney, involvedAnchors,
					true, // stop when capital reaches 0
					capital,
					true // invoicesPayMoney always contains the last invoice, because this is the 2nd (and last) step
			);
		}

		// If there is any money left, we have to book it onto the NEUTRAL (neither vendor nor customer) account of the business partner.
		if (capital != 0) {
			Anchor from, to;
			long amountToTransfer;
			if (capital > 0) {
				amountToTransfer = capital;
				from = partner;
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_NEUTRAL);
				to = getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
			}
			else {
				amountToTransfer = -capital;
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_NEUTRAL);
				from = getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
				to = partner;
			}

			MoneyTransfer moneyTransfer = new MoneyTransfer(
					payMoneyTransfer,
					from,
					to,
					amountToTransfer);
			moneyTransfer = pm.makePersistent(moneyTransfer);
			moneyTransfer.bookTransfer(user, involvedAnchors);
		}
	}
	
	private static class TransferInvoiceEntry
	{
		private PayableObject invoice;
		private long invoiceBalance;
		
		public TransferInvoiceEntry(PayableObject payableObject, long invoiceBalance)
		{
			this.invoice = payableObject;
			this.invoiceBalance = invoiceBalance;
		}
		
		public PayableObject getPayableObject()
		{
			return invoice;
		}
		
		/**
		 * @return Returns the amount to pay. This is NEGATIVE if the local organisation
		 *		looses money by this payment and POSITIVE if the local organisation
		 *		receives money.
		 */
		public long getInvoiceBalance()
		{
			return invoiceBalance;
		}
	}

	private static class PayableObjectComparator implements Comparator<PayableObject>
	{
		public int compare(PayableObject inv0, PayableObject inv1) {
			long inv0Time = inv0.getFinalizeDT().getTime();
			long inv1Time = inv1.getFinalizeDT().getTime();

			int result = inv0Time > inv1Time ? 1 : (inv0Time < inv1Time ? -1 : 0);
			if (result != 0)
				return result;

			result = inv0.getOrganisationID().compareTo(inv1.getOrganisationID());
			if (result != 0)
				return result;

//			result = inv0.getInvoiceIDPrefix().compareTo(inv1.getInvoiceIDPrefix());
//			if (result != 0)
//				return result;

			String payableObjectID0 = inv0.getPayableObjectID();
			String payableObjectID1 = inv1.getPayableObjectID();
			return payableObjectID0.compareTo(payableObjectID1);
		}
	};

	/**
	 * @param user the current user.
	 * @param partner the business partner (customer or supplier [= vendor]). The role (customer/vendor) of this business partner
	 *		might be different for each invoice, but it must be one of them (i.e.
	 *		the invoice must not involve anyone else besides the local organisation and this partner).
	 * @param payMoneyTransfer the payment to be distributed (booked on one or more of the given invoices).
	 * @param invoicesPayMoney all invoices that require the local organisation to pay money to the <code>partner</code>. These
	 *		are (1) invoices with a positive amount and the partner being the vendor and (2) invoices with a negative amount
	 *		and the partner being the customer.
	 * @param involvedAnchors all {@link Anchor} instances (i.e. usually {@link Account} or {@link LegalEntity}) that take part in
	 *		the money transfers. This is used for sanity checks at the end of the transaction.
	 * @param stopWhenCapitalReachesZero whether to stop when the capital reaches 0 (<code>true</code>) or to ignore the
	 *		capital (<code>false</code>). If this is <code>false</code>, always <b>all</b> given invoices will be processed.
	 *		If it is <code>true</code>, only as many invoices are processed as it is possible with the given capital.
	 * @param capital the starting capital. This is the money the partner-LegalEntity has in its hands after booking the
	 *		<code>payMoneyTransfer</code> and before this method did its work. If <code>stopWhenCapitalReachesZero</code>
	 *		is <code>true</code>, this must be a negative value (or 0)!
	 * @param containsLastInvoice whether the given <code>invoicesPayMoney</code> contains the last invoice to be processed.
	 *		This flag is used to decide whether to book all left-over-money to the last invoice in the given <code>Collection</code>
	 *		(or whether another method call of either this method or
	 *		{@link #processInvoicesReceiveMoney(User, LegalEntity, PayMoneyTransfer, Collection, Set, boolean, long, boolean)}
	 *		with another invoices-collection is taking place after this method call).
	 * @return the new capital after processing.
	 */
	protected long processInvoicesPayMoney(
			User user,
			LegalEntity partner,
			PayMoneyTransfer payMoneyTransfer,
			Collection<TransferInvoiceEntry> invoicesPayMoney,
			Set<Anchor> involvedAnchors,
			boolean stopWhenCapitalReachesZero,
			long capital,
			boolean containsLastInvoice
	)
	{
		// Sanity check: If capital approaches 0 (and we need to check when it reaches exactly 0), it does so always from
		// the positive side (i.e. downwards).
		if (stopWhenCapitalReachesZero) {
			if (capital > 0)
				throw new IllegalStateException("capital (" + capital + ") > 0!");
		}

		for (Iterator<TransferInvoiceEntry> it = invoicesPayMoney.iterator(); it.hasNext(); ) {
			TransferInvoiceEntry entry = it.next();
			PayableObject payableObject = entry.getPayableObject();
			boolean isLastInvoice = containsLastInvoice && !it.hasNext();

			if (entry.getInvoiceBalance() > 0)
				throw new IllegalStateException("Invoice balance must always be negative in invoicesPayMoney!!");

			// Normally, we pay for each invoice exactly the outstanding money of this invoice.
			long amountToPay = -1 * entry.getInvoiceBalance();

			// But if this is the last invoice or if we have only limited capital, we might adjust the amount to be paid.
			if (isLastInvoice || stopWhenCapitalReachesZero) {
				long positiveCapital = -1 * capital;
				if (isLastInvoice || positiveCapital < amountToPay)
					amountToPay = positiveCapital;
			}
			// amountToPay must always be positive. The direction of the payment is detected
			// by handleSingleInvoicePayment(...).

			// handleSingleInvoicePayment(...) returns a negative value, if the money leaves the partner
			// (and goes to an inside-partner-account). It returns a positive value, if the money comes
			// from an inside-partner-account and goes to the partner-legal-entity.
			long val = handleSingleInvoicePayment(
					user, partner, payMoneyTransfer, payableObject,
					amountToPay, involvedAnchors
			);

			if (val < 0)
				throw new IllegalStateException("handleSingleInvoicePayment(...) returned a negative value for a pay-invoice!!!");

			// capital is negative - hence it will approach 0 from below (val is always >= 0)
			capital += val;

			if (stopWhenCapitalReachesZero) {
				if (capital == 0)
					break;

				if (capital > 0)
					throw new IllegalStateException("capital > 0!!! Why did it not reach exactly 0?");
			}
		} // iterate pay-invoices

		return capital;
	}

	/**
	 * @param user the current user.
	 * @param partner the business partner (customer or supplier [= vendor]). The role (customer/vendor) of this business partner
	 *		might be different for each invoice, but it must be one of them (i.e.
	 *		the invoice must not involve anyone else besides the local organisation and this partner).
	 * @param payMoneyTransfer the payment to be distributed (booked on one or more of the given invoices).
	 * @param invoicesReceiveMoney all invoices that benefit the local organisation, i.e. that require the <code>partner</code>
	 *		to pay money to the local org. These are (1) invoices with a negative amount and the partner being the vendor and (2)
	 *		invoices with a positive amount and the partner being the customer.
	 * @param involvedAnchors all {@link Anchor} instances (i.e. usually {@link Account} or {@link LegalEntity}) that take part in
	 *		the money transfers. This is used for sanity checks at the end of the transaction.
	 * @param stopWhenCapitalReachesZero whether to stop when the capital reaches 0 (<code>true</code>) or to ignore the
	 *		capital (<code>false</code>). If this is <code>false</code>, always <b>all</b> given invoices will be processed.
	 *		If it is <code>true</code>, only as many invoices are processed as it is possible with the given capital.
	 * @param capital the starting capital. This is the money the partner-LegalEntity has in its hands after booking the
	 *		<code>payMoneyTransfer</code> and before this method did its work. If <code>stopWhenCapitalReachesZero</code>
	 *		is <code>true</code>, this must be a positive value (or 0)!
	 * @param containsLastInvoice whether the given <code>invoicesReceiveMoney</code> contains the last invoice to be processed.
	 *		This flag is used to decide whether to book all left-over-money to the last invoice in the given <code>Collection</code>
	 *		(or whether another method call of either this method or
	 *		{@link #processInvoicesPayMoney(User, LegalEntity, PayMoneyTransfer, Collection, Set, boolean, long, boolean)}
	 *		with another invoices-collection is taking place after this method call).
	 * @return the new capital after processing.
	 */
	protected long processInvoicesReceiveMoney(
			User user,
			LegalEntity partner,
			PayMoneyTransfer payMoneyTransfer,
			Collection<TransferInvoiceEntry> invoicesReceiveMoney,
			Set<Anchor> involvedAnchors,
			boolean stopWhenCapitalReachesZero,
			long capital,
			boolean containsLastInvoice
	)
	{
		// Sanity check: If capital approaches 0 (and we need to check when it reaches exactly 0), it does so always from
		// the negative side (i.e. upwards).
		if (stopWhenCapitalReachesZero) {
			if (capital < 0)
				throw new IllegalStateException("capital (" + capital + ") < 0!");
		}

		for (Iterator<TransferInvoiceEntry> it = invoicesReceiveMoney.iterator(); it.hasNext(); ) {
			TransferInvoiceEntry entry = it.next();
			PayableObject payableObject = entry.getPayableObject();
			boolean isLastInvoice = containsLastInvoice && !it.hasNext();

			if (entry.getInvoiceBalance() < 0)
				throw new IllegalStateException("Invoice balance must always be positive in invoicesReceiveMoney!!");

			// Normally, we pay for each invoice exactly the outstanding money of this invoice.
			long amountToPay = entry.getInvoiceBalance();

			// But if this is the last invoice or if we have only limited capital, we might adjust the amount to be paid.
			if (isLastInvoice || stopWhenCapitalReachesZero) {
				if (isLastInvoice || capital < amountToPay)
					amountToPay = capital;
			}
			// amountToPay must always be positive. The direction of the payment is detected
			// by handleSingleInvoicePayment(...).

			// handleSingleInvoicePayment(...) returns a negative value, if the money leaves the partner
			// (and goes to an inside-partner-account). It returns a positive value, if the money comes
			// from an inside-partner-account and goes to the partner-legal-entity.
			long val = handleSingleInvoicePayment(
						user, partner, payMoneyTransfer, payableObject,
						amountToPay, involvedAnchors
			);

			if (val > 0)
				throw new IllegalStateException("handleSingleInvoicePayment(...) returned a positive value for a receive-invoice!!!");

			// capital is positive - hence it will approach 0 from above (val is always <= 0)
			capital += val;

			if (stopWhenCapitalReachesZero) {
				if (capital == 0)
					break;

				if (capital < 0)
					throw new IllegalStateException("capital < 0!!! Why did it not reach exactly 0?");
			}
		} // iterate receive-invoices

		return capital;
	}

	/**
	 * @param amountToPay I'm pretty sure, this is always the POSITIVE amount involved in this payment.
	 * @!param amountToPay Is seen from the partner LegalEntity. If it looses money
	 *		(arrow leaving) it is negative.
	 *
	 * @return the amount coming from or going to the partner-LegalEntity. This
	 *		is negative, if it leaves the partner (and goes to an inside-partner-account). It is positive
	 *		if it comes from an inside-partner-account and goes to the partner-legal-entity.
	 */
	private long handleSingleInvoicePayment(
			User user, LegalEntity partner, PayMoneyTransfer payMoneyTransfer,
			PayableObject invoice, long amountToPay, Set<Anchor> involvedAnchors)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("handleSingleInvoicePayment: amountToPay=" + amountToPay + " invoice=" + invoice.getPayableObjectID() + " payMoneyTransfer=" + payMoneyTransfer.getPrimaryKey());
			logger.debug("handleSingleInvoicePayment: payMoneyTransfer.amount=" + payMoneyTransfer.getAmount() + " payMoneyTransfer.currency=" + payMoneyTransfer.getCurrency().getCurrencyID());
			logger.debug("handleSingleInvoicePayment: payMoneyTransfer.from=" + payMoneyTransfer.getFrom());
			logger.debug("handleSingleInvoicePayment: payMoneyTransfer.to=" + payMoneyTransfer.getTo());
		}

		if (amountToPay < 0)
			throw new IllegalArgumentException("amountToPay=="+amountToPay+"! amountToPay must be positive or zero!");

		if (amountToPay == 0 && !invoice.isOutstanding())
			return 0;

		Anchor from = null;
		Anchor to = null;
		boolean partnerTransferFrom = payMoneyTransfer.getAnchorType(partner) == Transfer.ANCHORTYPE_FROM; // mandator.getPrimaryKey().equals(transfer.getFrom().getPrimaryKey());

		PersistenceManager pm = getPersistenceManager();

		if (partnerTransferFrom) {
			to = partner;
			// Local Accounting is paying
			if (partnerIsInvoiceVendor(partner, invoice)) {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR);
				from = getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
			}
			else {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
				from = getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
			}
		}
		else {
			from = partner;
			if (!partnerIsInvoiceVendor(partner, invoice)) {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
				to = getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
			}
			else {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR);
				to = getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
			}
		}

		// Because of invoice balancing, it might be (against the direction of the
		// PayMoneyTransfer) that this current invoice is causing a transfer in the
		// opposite direction.
//		if (amountToPay < 0) {
//			Anchor tmpAnchor = from;
//			from = to;
//			to = tmpAnchor;
//			amountToPay *= -1;
//		}
		PayableObjectMoneyTransferFactory moneyTransferFactory = PayableObjectMoneyTransferFactoryJDO.getFactory(
				pm, getOrganisationID(), PAYABLE_OBJECT_FACTORY_SCOPE, invoice.getClass()
				);
		PayableObjectMoneyTransfer<?> moneyTransfer = moneyTransferFactory.createMoneyTransfer(
				payMoneyTransfer, BookType.pay, invoice, from, to, amountToPay
				); 

		moneyTransfer = pm.makePersistent(moneyTransfer);

		if (logger.isDebugEnabled()) {
			logger.debug("handleSingleInvoicePayment: created new InvoiceMoneyTransfer: " + moneyTransfer.getPrimaryKey());
			logger.debug("handleSingleInvoicePayment: from: " + from.getPrimaryKey());
			logger.debug("handleSingleInvoicePayment: to: " + to.getPrimaryKey());
			logger.debug("handleSingleInvoicePayment: amountToPay: " + amountToPay);
		}

		moneyTransfer.bookTransfer(user, involvedAnchors);

		if (moneyTransfer.getAnchorType(partner) == Transfer.ANCHORTYPE_FROM)
			return -amountToPay;
		else
			return amountToPay;
	}

	private static boolean partnerIsInvoiceVendor(LegalEntity partner, PayableObject payableObject) {
		return payableObject.getVendor().getPrimaryKey().equals(partner.getPrimaryKey());
	}

	/**
	 * Returns the PersitenceManager of this PartnerBookInvoiceAccountantDelegate. This
	 * is not cached (which is not necessary, anyway, because it's a fast operation via {@link JDOHelper#getObjectId(Object)}).
	 */
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager accountantPM = JDOHelper.getPersistenceManager(this);
		if (accountantPM == null)
			throw new IllegalStateException("This instance of PartnerBookInvoiceAccountantDelegate is currently not persistent, can not get a PesistenceManager!!");

		return accountantPM;
	}

	/**
	 * Finds (and creates if neccessary) the right Account for the given LegalEntity and Currency.
	 *
	 * @param accountType See {@link Account} for static anchorTypeID definitions
	 * @param partner The legal entity the account should be searched for.
	 * @param currency The currency the account should record.
	 * @return The found or created acccount. Never null.
	 */
	public Account getPartnerAccount(AccountType accountType, LegalEntity partner, Currency currency) {
		if (partner == null)
			throw new IllegalArgumentException("Parameter partner must not be null!");
		if (currency == null)
			throw new IllegalArgumentException("Parameter currency must not be null!");

		Collection<? extends Account> accounts = Account.getAccounts(getPersistenceManager(), accountType, partner, currency);
		// there should be only one account, but in case a user later adds one, we don't throw an exception
		Account account = accounts.isEmpty() ? null : accounts.iterator().next();
		if (account == null) {
			// TODO how to generate the IDs here? Give the user the possibility to define rules (e.g. number ranges)
			account = new Account(
					this.getOrganisationID(),
					"partner." + ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Anchor.class, Account.ANCHOR_TYPE_ID_ACCOUNT + ".partner")),
					accountType, partner, currency);
			account = getPersistenceManager().makePersistent(account);
			account.setOwner(partner);
		}
		return account;
	}

}
