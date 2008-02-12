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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.pay.PayMoneyTransfer;
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
public class PartnerAccountant extends Accountant
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Constructor only existing for JDO!
	 */
	@Deprecated
	protected PartnerAccountant() {
	}

	/**
	 * @param organisationID
	 * @param accountantID
	 */
	public PartnerAccountant(String organisationID, String accountantID) {
		super(organisationID, accountantID);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.book.Accountant#bookTransfer(User, LegalEntity, MoneyTransfer, Map)
	 */
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
			Account customerPartnerAccount = accounting.getPartnerAccount(accountType, mandator, transfer.getInvoice().getCurrency()); 
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
			Account vendorPartnerAccount = accounting.getPartnerAccount(accountType, mandator, transfer.getInvoice().getCurrency());
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
		Accounting accounting = Accounting.getAccounting(pm);

		if (partnerTransferFrom) {
			to = partner;
			// Local Accounting is paying
			if (partnerIsInvoiceVendor(partner, invoice)) {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR);
				from = accounting.getPartnerAccount(accountType, partner, transfer.getCurrency());
			}
			else {
//				amountToPay *= -1; // TODO korrekt?
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
				from = accounting.getPartnerAccount(accountType, partner, transfer.getCurrency());
			}
		}
		else {
			from = partner;
			if (!partnerIsInvoiceVendor(partner, invoice)) {
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
				to = accounting.getPartnerAccount(accountType, partner, transfer.getCurrency());
			}
			else {
//				amountToPay *= -1; // TODO korrekt?
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR);
				to = accounting.getPartnerAccount(accountType, partner, transfer.getCurrency());
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
		// sort order is finalizeDT, organisationID, invoiceID
		List sortedInvoices = new LinkedList(payMoneyTransfer.getPayment().getInvoices());
		Comparator comparator = new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if ( (arg0 instanceof Invoice) && (arg1 instanceof Invoice) ) {
					Invoice inv0 = (Invoice)arg0;
					Invoice inv1 = (Invoice)arg1;
					long inv0Time = inv0.getFinalizeDT().getTime();
					long inv1Time = inv1.getFinalizeDT().getTime();

					if (inv0Time == inv1Time) {
						if (inv0.getOrganisationID().equals(inv1.getOrganisationID())) {
							if (inv0.getInvoiceID() < inv1.getInvoiceID())
								return -1;
							else if (inv0.getInvoiceID() > inv1.getInvoiceID())
								return 1;
							else
								return 0;
						}
						else
							return inv0.getOrganisationID().compareTo(inv1.getOrganisationID());
					}
					else {
						if (inv0Time > inv1Time)
							return 1;
						else
							return -1;
					}
				}
				else
					return -1;
			}
		};		
		Collections.sort(sortedInvoices,comparator);
		
		

		// classify Invoices and compute overallBalance
		boolean partnerTransferTo = payMoneyTransfer.getAnchorType(partner) == Transfer.ANCHORTYPE_TO; // (payMoneyTransfer.getTo() != null) && (payMoneyTransfer.getTo().getPrimaryKey().equals(partner.getPrimaryKey()));
		boolean partnerTransferFrom = !partnerTransferTo;

		List invoicesPayMoney = new LinkedList();
		List invoicesReceiveMoney = new LinkedList();

		Accounting accounting = Accounting.getAccounting(pm);

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

		for (Iterator iter = sortedInvoices.iterator(); iter.hasNext();) {
			Invoice invoice = (Invoice) iter.next();			
			boolean partnerInvoiceCustomer = invoice.getCustomer().getPrimaryKey().equals(partner.getPrimaryKey());
			boolean partnerInvoiceVendor = !partnerInvoiceCustomer;

			long invoiceBalance = 0;
			if (partnerTransferFrom && partnerInvoiceVendor)
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
		 * the partner because of open invoices.
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
				for (Iterator iter = invoicesReceiveMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				for (Iterator iter = invoicesPayMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				for (Iterator iter = invoicesReceiveMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				for (Iterator iter = invoicesPayMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				for (Iterator iter = invoicesPayMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				for (Iterator iter = invoicesReceiveMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				for (Iterator iter = invoicesPayMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				for (Iterator iter = invoicesReceiveMoney.iterator(); iter.hasNext();) {
					TransferInvoiceEntry entry = (TransferInvoiceEntry) iter.next();
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
				to = accounting.getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
			}
			else {
				amountToTransfer = -capital;
				AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_NEUTRAL);
				from = accounting.getPartnerAccount(accountType, partner, payMoneyTransfer.getCurrency());
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

}
