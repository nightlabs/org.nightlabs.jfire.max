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
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.l10n.Currency;

/**
 * One instance of PartnerBookInvoiceAccountantDelegate exists per organisation.
 * It handles Transfers for trade partners that can be other
 * organisations or customers.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(detachable="true")
public class PartnerBookPayableObjectAccountantDelegate
	extends AccountantDelegate
{
	private static final long serialVersionUID = 1L;
	
	private static final String ACCOUNTANT_DELEGATE_ID = "internal.partner.bookPayableObject";

	/**
	 * @deprecated Constructor only existing for JDO!
	 */
	@Deprecated
	protected PartnerBookPayableObjectAccountantDelegate() {
	}

	public PartnerBookPayableObjectAccountantDelegate(String organisationID) {
		super(organisationID, ACCOUNTANT_DELEGATE_ID);
	}

	@Override
	public void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors)
	{
		handleBookMoneyTransfer(mandator, (BookInvoiceMoneyTransfer)transfer, user, involvedAnchors);
	}

	/**
	 * Handles MoneyTransfers between LegalEntities by creating one IntraLegalMoneyTransfer for the
	 * given BookInvoiceMoneyTransfer.
	 */
	protected void handleBookMoneyTransfer(LegalEntity mandator, BookInvoiceMoneyTransfer transfer, User user, Set<Anchor> involvedAnchors)
	{
		boolean mandatorIsVendor = transfer.getPayableObject().getVendor().getPrimaryKey().equals(mandator.getPrimaryKey());
		boolean mandatorIsCustomer = !mandatorIsVendor;

		boolean mandatorIsTransferTo = transfer.getTo().getPrimaryKey().equals(mandator.getPrimaryKey());
		boolean mandatorIsTransferFrom = !mandatorIsTransferTo;

		Anchor createTransferFrom = null;
		Anchor createTransferTo = null;
		// create IntraLegalEntityMoneyTransfer from PartnerAccount(AsDebitor) to mandator (or <->)
		// determine the direction
		if (mandatorIsCustomer) {
			AccountType accountType = (AccountType) getPersistenceManager().getObjectById(AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER);
			Account customerPartnerAccount = getPartnerAccount(accountType, mandator, transfer.getPayableObject().getCurrency());
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
			Account vendorPartnerAccount = getPartnerAccount(accountType, mandator, transfer.getPayableObject().getCurrency());
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
		moneyTransfer.bookTransfer(user, involvedAnchors);
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

		// TODO: remove this cast once the Entities store a reference to the interface instead of the implementation.
		org.nightlabs.jfire.accounting.Currency currencyImpl = (org.nightlabs.jfire.accounting.Currency) currency;
		Collection<? extends Account> accounts = Account.getAccounts(getPersistenceManager(), accountType, partner, currencyImpl);
		// there should be only one account, but in case a user later adds one, we don't throw an exception
		Account account = accounts.isEmpty() ? null : accounts.iterator().next();
		if (account == null) {
			// TODO how to generate the IDs here? Give the user the possibility to define rules (e.g. number ranges)
			account = new Account(
					this.getOrganisationID(),
					"partner." + ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Anchor.class, Account.ANCHOR_TYPE_ID_ACCOUNT + ".partner")),
					accountType, partner, currencyImpl);
			account = getPersistenceManager().makePersistent(account);
			account.setOwner(partner);
		}
		return account;
	}
}
