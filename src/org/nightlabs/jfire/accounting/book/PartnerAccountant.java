/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting.book;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.pay.PayMoneyTransfer;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

/**
 * One instance of PartnerAccountant exists per organisation.
 * It handles Transfers for trade partners that can be other
 * organisations or customers.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.book.Accountant"
 *		detachable="true"
 *		table="JFireTrade_PartnerAccountant"
 *
 * @jdo.inheritance strategy = "new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PartnerAccountant")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PartnerAccountant extends Accountant
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Constructor only existing for JDO!
	 */
	@Deprecated
	protected PartnerAccountant() {
	}

	public PartnerAccountant(String organisationID, String accountantID) {
		super(organisationID, accountantID);
	}

	@Override
	public void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors) {
		if (transfer instanceof BookMoneyTransfer)
			handleBookMoneyTransfer(mandator, (BookMoneyTransfer)transfer, user, involvedAnchors);
		else if (transfer instanceof PayMoneyTransfer)
			handlePayMoneyTransfer(user, mandator, (PayMoneyTransfer)transfer, involvedAnchors);
		// TODO: do something on simple MoneyTransfers ?? - I don't think so ... Marco ;-)
	}

	/**
	 * Handles MoneyTransfers between LegalEntities by creating one IntraLegalMoneyTransfer for the
	 * given BookMoneyTransfer.
	 *
	 * @param mandator
	 * @param transfer
	 */
	protected void handleBookMoneyTransfer(LegalEntity mandator, BookMoneyTransfer transfer, User user, Set<Anchor> involvedAnchors)
	{
		boolean mandatorIsVendor = transfer.getInvoice().getVendor().getPrimaryKey().equals(mandator.getPrimaryKey());
		boolean mandatorIsCustomer = !mandatorIsVendor;

		boolean mandatorIsTransferTo = transfer.getTo().getPrimaryKey().equals(mandator.getPrimaryKey());
		boolean mandatorIsTransferFrom = !mandatorIsTransferTo;

		Anchor createTransferFrom = null;
		Anchor createTransferTo = null;
		Accounting accounting = Accounting.getAccounting(getPersistenceManager());
		// create IntraLegalEntityMoneyTransfer from PartnerAccount(AsDebitor) to mandator (or <->)
		// determine the direction
		if (mandatorIsCustomer) {
			AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
			Account customerPartnerAccount = getPartnerAccount(accountType, mandator, transfer.getInvoice().getCurrency());
			if (mandatorIsTransferFrom) {
				createTransferFrom = customerPartnerAccount;
				createTransferTo = mandator;
			}
			else {
				createTransferFrom = mandator;
				createTransferTo = customerPartnerAccount;
			}
		}
		else {
			// if (mandatorIsVendor)
			AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR);
			Account vendorPartnerAccount = getPartnerAccount(accountType, mandator, transfer.getInvoice().getCurrency());
			if (mandatorIsTransferFrom) {
				createTransferFrom = vendorPartnerAccount;
				createTransferTo = mandator;
			}
			else {
				createTransferFrom = mandator;
				createTransferTo = vendorPartnerAccount;
			}
		}

		MoneyTransfer moneyTransfer = new MoneyTransfer(
			transfer,
			createTransferFrom,
			createTransferTo
		);
		moneyTransfer = getPersistenceManager().makePersistent(moneyTransfer);

//		createTransferFrom.bookTransfer(user, moneyTransfer, involvedAnchors);
//		createTransferTo.bookTransfer(user, moneyTransfer, involvedAnchors);
		moneyTransfer.bookTransfer(user, involvedAnchors);
	}



	private static class TransferInvoiceEntry {
		private Invoice invoice;
		private long invoiceBalance;
		public TransferInvoiceEntry(Invoice invoice, long invoiceBalance) {
			this.invoice = invoice;
			this.invoiceBalance = invoiceBalance;
		}
		public Invoice getInvoice() {
			return invoice;
		}
		/**
		 * @return Returns the amount to pay. This is NEGATIVE if the local organisation
		 *		looses money by this payment and POSITIVE if the local organisation
		 *		receives money.
		 */
		public long getInvoiceBalance() {
			return invoiceBalance;
		}
	}

	private static boolean partnerIsInvoiceVendor(LegalEntity partner, Invoice invoice) {
		return invoice.getVendor().getPrimaryKey().equals(partner.getPrimaryKey());
	}

	/**
	 * @param amountToPay I'm pretty sure, this is always the POSITIVE amount involved in this payment.
	 * @!param amountToPay Is seen from the partner LegalEntity. If it looses money
	 *		(arrow leaving) it is negative.
	 *
	 * @return The amount coming from or going to the partner LegalEntity. This
	 *		is negative if it leaves the partner (and goes to an account) and positive
	 *		if it comes from an account and goes to the partner legal entity.
	 */
	private long handleSingleInvoicePayment(
			User user, LegalEntity partner, PayMoneyTransfer transfer,
			Invoice invoice, long amountToPay, Set<Anchor> involvedAnchors)
	{
		if (amountToPay < 0)
			throw new IllegalArgumentException("amountToPay=="+amountToPay+"! amountToPay must be positive or zero!");

		if (amountToPay == 0 && !invoice.getInvoiceLocal().isOutstanding())
			return 0;

		Anchor from = null;
		Anchor to = null;
		boolean partnerTransferFrom = transfer.getAnchorType(partner) == Transfer.ANCHORTYPE_FROM; // mandator.getPrimaryKey().equals(transfer.getFrom().getPrimaryKey());

		PersistenceManager pm = getPersistenceManager();
//		Accounting accounting = Accounting.getAccounting(pm);

		if (partnerTransferFrom) {
			to = partner;
			// Local Accounting is paying
			if (partnerIsInvoiceVendor(partner, invoice)) {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR);
				from = getPartnerAccount(accountType, partner, transfer.getCurrency());
			}
			else {
//				amountToPay *= -1; // TODO korrekt?
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
				from = getPartnerAccount(accountType, partner, transfer.getCurrency());
			}
		}
		else {
			from = partner;
			if (!partnerIsInvoiceVendor(partner, invoice)) {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
				to = getPartnerAccount(accountType, partner, transfer.getCurrency());
			}
			else {
//				amountToPay *= -1; // TODO korrekt?
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR);
				to = getPartnerAccount(accountType, partner, transfer.getCurrency());
			}
		}

		// Because of invoice balancing, it might be (against the direction of the
		// PayMoneyTransfer) that this current invoice is causing a transfer in the
		// opposite direction.
		if (amountToPay < 0) {
			Anchor tmpAnchor = from;
			from = to;
			to = tmpAnchor;
			amountToPay *= -1;
		}
		InvoiceMoneyTransfer moneyTransfer = new InvoiceMoneyTransfer(
				InvoiceMoneyTransfer.BOOK_TYPE_PAY,
				transfer,
				from,
				to,
				invoice,
				amountToPay);
		moneyTransfer = pm.makePersistent(moneyTransfer);

//		invoice.bookPayInvoiceMoneyTransfer(moneyTransfer, false);
//		from.bookTransfer(user, moneyTransfer, involvedAnchors);
//		to.bookTransfer(user, moneyTransfer, involvedAnchors);
		moneyTransfer.bookTransfer(user, involvedAnchors);

		if (moneyTransfer.getAnchorType(partner) == Transfer.ANCHORTYPE_FROM)
			return -amountToPay;
		else
			return amountToPay;
	}

	/**
	 * Handles payments by creating IntraLegalMoneyTransfer.<br/>
	 * Implements the following:
	 * <pre>
	 *   * sort invoices (organisationID / invoiceID)
	 *   * classify in invoicesPayMoney and invoicesRecieveMoney
	 *   * balance invoices - negative balance means, we have to pay
	 *   if (balance >= 0)
	 *     * clear all invoicesRecieveMoney
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

		if (payMoneyTransfer.getPayment().getInvoices().isEmpty() && partnerAccount == null)
			throw new IllegalArgumentException("PayMoneyTransfer \""+payMoneyTransfer.getPrimaryKey()+"\" has no related invoices. Hence, partnerAccount must not be null! payMoneyTransfer.getPayment().getPartnerAccount() is null!!!");

		// Sort Invoices
		// sort order is finalizeDT, organisationID, invoiceIDPrefix, invoiceID
		List<Invoice> sortedInvoices = new LinkedList<Invoice>(payMoneyTransfer.getPayment().getInvoices());
		Comparator<Invoice> comparator = new Comparator<Invoice>() {
			public int compare(Invoice inv0, Invoice inv1) {
				long inv0Time = inv0.getFinalizeDT().getTime();
				long inv1Time = inv1.getFinalizeDT().getTime();

				int result = inv0Time > inv1Time ? 1 : (inv0Time < inv1Time ? -1 : 0);
				if (result != 0)
					return result;

				result = inv0.getOrganisationID().compareTo(inv1.getOrganisationID());
				if (result != 0)
					return result;

				result = inv0.getInvoiceIDPrefix().compareTo(inv1.getInvoiceIDPrefix());
				if (result != 0)
					return result;

				result = inv0.getInvoiceID() > inv1.getInvoiceID() ? 1 : (inv0.getInvoiceID() < inv1.getInvoiceID() ? -1 : 0);
				return result;
			}
		};
		Collections.sort(sortedInvoices,comparator);



		// classify Invoices and compute overallBalance
		boolean partnerTransferTo = payMoneyTransfer.getAnchorType(partner) == Transfer.ANCHORTYPE_TO; // (payMoneyTransfer.getTo() != null) && (payMoneyTransfer.getTo().getPrimaryKey().equals(partner.getPrimaryKey()));
		boolean partnerTransferFrom = !partnerTransferTo;

		List<TransferInvoiceEntry> invoicesPayMoney = new LinkedList<TransferInvoiceEntry>();
		List<TransferInvoiceEntry> invoicesReceiveMoney = new LinkedList<TransferInvoiceEntry>();

//		Accounting accounting = Accounting.getAccounting(pm);

		/* allInvoicesBalance is the amount after summarizing all invoices
		 * It is negative if the local organisation looses money to
		 * the outside partner and positive if the local organisation receives money.
		 *
		 * NEGATIVE amount: When local organisation either creates an
		 * invoice with a negative price or partner created an invoice with
		 * a positive amount.
		 *
		 * POSITIVE amount: When local organisation either creates an
		 * invoice with a positive amount or partner created an invoice with
		 * a negative amount.
		 */
		long allInvoicesBalance = 0;

		for (Invoice invoice : sortedInvoices) {
			boolean partnerInvoiceCustomer = invoice.getCustomer().equals(partner);
			boolean partnerInvoiceVendor = !partnerInvoiceCustomer;

			long invoiceBalance = 0;
//			if (partnerTransferFrom && partnerInvoiceVendor)
			if (partnerInvoiceVendor)
				invoiceBalance = - invoice.getInvoiceLocal().getAmountToPay();
			else
				invoiceBalance = invoice.getInvoiceLocal().getAmountToPay();

			allInvoicesBalance += invoiceBalance;


			if (invoiceBalance >= 0) {
				invoicesReceiveMoney.add(new TransferInvoiceEntry(invoice, invoiceBalance));
			}
			else {
				invoicesPayMoney.add(new TransferInvoiceEntry(invoice, invoiceBalance));
			}
		}

//		for (Iterator iter = sortedInvoices.iterator(); iter.hasNext();) {
//			Invoice invoice = (Invoice) iter.next();
//			long invoiceBalance = 0;
//			boolean partnerInvoiceCustomer = invoice.getCustomer().getPrimaryKey().equals(partner.getPrimaryKey());
//			boolean partnerInvoiceVendor = !partnerInvoiceCustomer;
//			long factor = 1;
////			if ( (partnerTransferTo && partnerInvoiceVendor) || (partnerTransferFrom && partnerInvoiceCustomer) )
//			if ((partnerTransferFrom && partnerInvoiceVendor) ||
//					(partnerTransferTo && partnerInvoiceCustomer))
//				factor = -1;
//			invoiceBalance = invoice.getAmountToPay() * factor;
//			allInvoicesBalance += invoiceBalance;
//
////			if (partnerInvoiceVendor)
////				amountToGetAsVendor += invoice.getAmountToPay();
////			else
////				amountToPayAsCustomer += invoice.getAmountToPay();
//
//			if (invoiceBalance >= 0) {
//				invoicesReceiveMoney.add(new TransferInvoiceEntry(invoice, invoiceBalance));
//			}
//			else {
//				invoicesPayMoney.add(new TransferInvoiceEntry(invoice, invoiceBalance));
//			}
//		}

//		Account mandatorVendorAccount = accounting.getPartnerAccount(Account.ANCHOR_TYPE_ID_PARTNER_VENDOR,partner,payMoneyTransfer.getCurrency());
//		Account mandatorCustomerAccount = accounting.getPartnerAccount(Account.ANCHOR_TYPE_ID_PARTNER_CUSTOMER,partner,payMoneyTransfer.getCurrency());

// we create one InvoiceMoneyTransfer for each Invoice and don't need a general
// moneytransfer
//		// create a IntraLegalEntityMoneyTransfer to transfer the money from LE to Account or vice versa
//		if (partnerTransferFrom) {
//			//               PayMoneyTransfer
//			// Mandator   --------------------->  NULL
//			Account fromAccount = accounting.getPartnerAccount(Account.ANCHOR_TYPE_ID_PARTNER_VENDOR,partner,payMoneyTransfer.getCurrency());
//			MoneyTransfer moneyTransfer = new MoneyTransfer(
//				accounting,
//				payMoneyTransfer,
//				fromAccount,
//				partner,
////				payMoneyTransfer,
//				payMoneyTransfer.getCurrency(),
//				payMoneyTransfer.getAmount()
//			);
//
//			fromAccount.bookTransfer(user, moneyTransfer, involvedAnchors);
//			partner.bookTransfer(user, moneyTransfer, involvedAnchors);
//		}
//		else {
//			//            PayMoneyTransfer
//			// NULL   --------------------->  Mandator
//			Account toAccount = accounting.getPartnerAccount(Account.ANCHOR_TYPE_ID_PARTNER_CUSTOMER, partner, payMoneyTransfer.getCurrency());
//			MoneyTransfer moneyTransfer = new MoneyTransfer(
//				accounting,
//				payMoneyTransfer,
//				partner,
//				toAccount,
////				payMoneyTransfer,
//				payMoneyTransfer.getCurrency(),
//				payMoneyTransfer.getAmount()
//			);
//
//			partner.bookTransfer(user, moneyTransfer, involvedAnchors);
//			toAccount.bookTransfer(user, moneyTransfer, involvedAnchors);
//		}


		/* If the overallBalance is positive, the local organisation is receiving
		 * money in total after all invoices have been summarized. If it's
		 * negative, the local organisation looses money by the given invoices.
		 *
		 * Hence, the PayMoneyTransfer has partnerTransferFrom, if
		 * allInvoicesBalance is negative.
		 */
		long payMoneyTransferRealAmount;
		if (partnerTransferFrom)
			payMoneyTransferRealAmount = - payMoneyTransfer.getAmount();
		else
			payMoneyTransferRealAmount = payMoneyTransfer.getAmount();

		/* Now, payMoneyTransferRealAmount is positive if the current PayMoneyTransfer
		 * causes the local organisation to gain money and negative, if the local
		 * organisation looses money.
		 */

		/* restAmount is negative, if after summarizing all invoices and the
		 * PayMoneyTransfer, still money has to be paid from the local organisation to
		 * the partner because of open invoices (or because the partner over-paid).
		 *
		 * restAmount is positive, if after summarizing and receiving by PayMoneyTransfer
		 * still a rest is to be expected from the partner.
		 */
		long restAmount = allInvoicesBalance - payMoneyTransferRealAmount;

		/* The capital is the money that the LegalEntity currently (after the
		 * PayMoneyTransfer) has. It is negative if the LegalEntity has to pay
		 * the partner and positive if the LegalEntity receives money. The LegalEntity
		 * "partner" represents a section within the local organisation, hence the
		 * directions (point of view) are the same as of the local organisation.
		 *
		 * in short: capital is negative when paying and postive when receiving
		 */
		long capital = payMoneyTransferRealAmount;

		if (restAmount <= 0) {
			if (partnerTransferFrom) { // capital starts NEGATIVE and stops at 0
				// underpayment on customer side

				// clear all receive-invoices first
				// and pay until money's gone

				// first all receive invoices
				for (TransferInvoiceEntry entry : invoicesReceiveMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() < 0)
						throw new IllegalStateException("Invoice balance must always be positive in invoicesReceiveMoney!!");

					if (capital > 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be negative here!");

					// capital is negative - hence --=+ (let the capital grow = away from 0)
					capital -= handleSingleInvoicePayment(
							user, partner, payMoneyTransfer, invoice,
							entry.getInvoiceBalance(), involvedAnchors);
				} // iterate receive-invoices

				// now as many pay-invoices as possible
				for (TransferInvoiceEntry entry : invoicesPayMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() > 0)
						throw new IllegalStateException("Invoice balance must always be negative in invoicesPayMoney!!");

					if (capital > 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be negative here!");

					// let the capital shrink (get closer to 0)
					if (capital <= entry.getInvoiceBalance()) {
						capital += handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								-1 * entry.getInvoiceBalance(), involvedAnchors);
					}
					else {
						capital += handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								-1 * capital, involvedAnchors);
						break;
					}
				} // iterate pay-invoices

			} // if (partnerTransferFrom) { // capital negative
			else {
				// capital starts POSITIVE and stops at 0 (or never reaches 0)
				// overpayment on vendor side

				// clear all receive-invoices first
				// then as many pay invoices as possible

				// first ALL receive-invoices
				for (TransferInvoiceEntry entry : invoicesReceiveMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() < 0)
						throw new IllegalStateException("Invoice balance must always be positive in invoicesReceiveMoney!!");

					if (capital < 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be positive here!");

					// let the capital grow (get away from 0)
					capital += handleSingleInvoicePayment(
							user, partner, payMoneyTransfer, invoice,
							entry.getInvoiceBalance(), involvedAnchors);
				} // iterate receive-invoices

				// now as many pay-invoices as possible
				for (TransferInvoiceEntry entry : invoicesPayMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() > 0)
						throw new IllegalStateException("Invoice balance must always be negative in invoicesPayMoney!!");

					if (capital < 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be positive here!");

					// let the capital shrink (get closer to 0)
					if (capital >= -1 * entry.getInvoiceBalance()) {
						capital -= handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								-1 * entry.getInvoiceBalance(), involvedAnchors);
					}
					else {
						capital -= handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								capital, involvedAnchors);
						break;
					}
				} // iterate pay-invoices

			} // if (partnerTransferTo) {

		} // if (restAmount <= 0) {
		else {
			// if (restAmount > 0) {

			if (partnerTransferFrom) { // capital starts NEGATIVE and stops at 0
				// overpayment on customer side

				// clear all pay-invoices first
				// and receive as many receive-invoices as possible

				// first all pay-invoices
				for (TransferInvoiceEntry entry : invoicesPayMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() > 0)
						throw new IllegalStateException("Invoice balance must always be negative in invoicesPayMoney!!");

					if (capital > 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be negative here!");

					// capital is negative - hence --=+ (let the capital grow)
					capital += handleSingleInvoicePayment(
							user, partner, payMoneyTransfer, invoice,
							-1 * entry.getInvoiceBalance(), involvedAnchors);
				} // iterate pay-invoices

				// now as many receive-invoices as possible
				for (TransferInvoiceEntry entry : invoicesReceiveMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() < 0)
						throw new IllegalStateException("Invoice balance must always be positive in invoicesReceiveMoney!!");

					if (capital > 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be negative here!");

					// capital is negative - hence --=+ (let the capital grow)
					if (capital <= entry.getInvoiceBalance()) {
						capital -= handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								entry.getInvoiceBalance(), involvedAnchors);
					}
					else {
						capital -= handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								-1 * capital, involvedAnchors);
						break;
					}
				} // iterate receive-invoices

			} // if (partnerTransferFrom) { // capital starts negative and stops at 0
			else {
				// capital starts POSITIVE and stops at 0
				// underpayment on vendor side

				// first all pay-invoices (vendor side!)
				// then as many receive-invoices as possible

				// first all pay-invoices
				for (TransferInvoiceEntry entry : invoicesPayMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() > 0)
						throw new IllegalStateException("Invoice balance must always be negative in invoicesPayMoney!!");

					if (capital < 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be positive here!");

					capital -= handleSingleInvoicePayment(
							user, partner, payMoneyTransfer, invoice,
							-1 * entry.getInvoiceBalance(), involvedAnchors);
				} // iterate pay-invoices

				// now as many receive-invoices as possible
				for (TransferInvoiceEntry entry : invoicesReceiveMoney) {
					Invoice invoice = entry.getInvoice();

					if (entry.getInvoiceBalance() < 0)
						throw new IllegalStateException("Invoice balance must always be positive in invoicesReceiveMoney!!");

					if (capital < 0)
						throw new IllegalStateException("capital=="+capital+"! capital must be positive here!");

					if (capital >= entry.getInvoiceBalance()) {
						capital += handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								entry.getInvoiceBalance(), involvedAnchors);
					}
					else {
						capital += handleSingleInvoicePayment(
								user, partner, payMoneyTransfer, invoice,
								capital, involvedAnchors);
						break;
					}
				} // iterate receive-invoices

			} // if (partnerTransferTo) {

		} // if (restAmount > 0) {


		if (capital != 0) {
//			throw new UnsupportedOperationException("capital=="+capital+"! The case capital != 0 after payment is not yet supported!");
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

//	private PersistenceManager accountantPM = null;

	/**
	 * Returns the PersitenceManager of this PartnerAccountant. This
	 * is not cached.
	 */
	protected PersistenceManager getPersistenceManager() {
//		if (accountantPM == null) {
		PersistenceManager accountantPM = JDOHelper.getPersistenceManager(this);
		if (accountantPM == null)
			throw new IllegalStateException("This instance of PartnerAccountant is currently not persistent, can not get a PesistenceManager!!");
//		}
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

		Collection<Account> accounts = (Collection<Account>) Account.getAccounts(getPersistenceManager(), accountType, partner, currency);
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

//		String searchAccountID = accountType.getOrganisationID() + ':' + accountType.getAccountTypeID() + ':' + partner.getOrganisationID() + ':' + partner.getAnchorID() + ':' + currency.getCurrencyID();
//		AnchorID anchorID = AnchorID.create(this.getOrganisationID(), Account.ANCHOR_TYPE_ID_ACCOUNT, searchAccountID);
//
//		Account account = null;
//		Object o = null;
//		try {
//			o = getPersistenceManager().getObjectById(anchorID);
//			account = (Account)o;
//		}
//		catch (ClassCastException ce)  {
//			IllegalStateException ill = new IllegalStateException("Found persistent object with oid "+anchorID+" but is not of type Account but "+o.getClass().getName());
//			ill.initCause(ce);
//			throw ill;
//		}
//		catch (JDOObjectNotFoundException je) {
//			// account not existing, create it
//			account = new Account(this.getOrganisationID(), searchAccountID, partner, currency, accountType);
//			account = getPersistenceManager().makePersistent(account);
//			account.setOwner(partner);
//		}
//
//		if (account == null)
//			throw new IllegalStateException("Account with oid "+anchorID+" could neither be found nor created!");
//
//		if (!account.getOwner().equals(partner))
//			throw new IllegalStateException("An account for oid "+anchorID+" could be found, but its owner is not the partner the search was performed for. Owner: "+account.getOwner().getPrimaryKey()+", Partner: "+partner.getPrimaryKey());
//
//		if (!account.getAccountType().equals(accountType))
//			throw new IllegalStateException("An account for oid "+anchorID+" could be found, but its accountType is not the accountType the search was performed for. assignedAccountType: "+JDOHelper.getObjectId(account.getAccountType())+", expectedAccountType: "+JDOHelper.getObjectId(accountType));

		return account;
	}
}
