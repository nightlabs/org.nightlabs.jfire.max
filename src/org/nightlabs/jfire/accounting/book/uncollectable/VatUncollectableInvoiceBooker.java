package org.nightlabs.jfire.accounting.book.uncollectable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragment;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.book.Accountant;
import org.nightlabs.jfire.accounting.book.BookInvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.book.PartnerBookInvoiceAccountantDelegate;
import org.nightlabs.jfire.accounting.id.AccountTypeID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.id.AnchorID;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_VatUncollectableInvoiceBooker"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public class VatUncollectableInvoiceBooker
extends UncollectableInvoiceBooker
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VatUncollectableInvoiceBooker() { }

	public VatUncollectableInvoiceBooker(String organisationID,
			String uncollectableInvoiceBookerID) {
		super(organisationID, uncollectableInvoiceBookerID);
	}

	protected boolean isVatValuePriceFragment(PriceFragment priceFragment)
	{
		String priceFragmentTypeIDString = priceFragment.getPriceFragmentType().getPriceFragmentTypeID();
		if (!priceFragmentTypeIDString.startsWith("vat-"))
			return false;

		return priceFragmentTypeIDString.endsWith("-val");
	}

	protected Collection<PriceFragment> getVatValuePriceFragments(Price price)
	{
		Collection<PriceFragment> result = new LinkedList<PriceFragment>();
		for (PriceFragment priceFragment : price.getFragments()) {
			if (isVatValuePriceFragment(priceFragment))
				result.add(priceFragment);
		}
		return result;
	}

	protected Account createAccount(AnchorID accountID, Currency currency, PriceFragmentType vatValuePriceFragmentType)
	{
		PersistenceManager pm = getPersistenceManager();
		Account account;
		try {
			account = (Account) pm.getObjectById(accountID);
		} catch (JDOObjectNotFoundException x) {
			AccountType accountType = (AccountType) pm.getObjectById(AccountType.ACCOUNT_TYPE_ID_UNCOLLECTABLE);
			account = new Account(
					accountID.organisationID, accountID.anchorID,
					accountType,
					Accounting.getAccounting(pm).getMandator(),
					currency
			);

			if (vatValuePriceFragmentType == null) {
				account.getName().setText(Locale.ENGLISH, "Uncollectable (" + currency.getCurrencySymbol() + ")");
				account.getName().setText(Locale.GERMAN, "Uneinbringlich (" + currency.getCurrencySymbol() + ")");
			}
			else {
				account.getName().setText(Locale.ENGLISH, "Uncollectable " + vatValuePriceFragmentType.getName().getText(Locale.ENGLISH) + " (" + currency.getCurrencySymbol() + ")");
				account.getName().setText(Locale.GERMAN, "Uneinbringlich " + vatValuePriceFragmentType.getName().getText(Locale.GERMAN) + " (" + currency.getCurrencySymbol() + ")");
			}

			account = pm.makePersistent(account);
		}
		return account;
	}

	protected Account getUncollectableAccountNet(Currency currency)
	{
		AnchorID accountID = AnchorID.create(getOrganisationID(), Account.ANCHOR_TYPE_ID_ACCOUNT, "uncollectable#" + currency.getCurrencyID()+"#net");
		return createAccount(accountID, currency, null);
	}

	protected Account getUncollectableAccountVatValue(Currency currency, PriceFragmentType priceFragmentType)
	{
		AnchorID accountID = AnchorID.create(getOrganisationID(), Account.ANCHOR_TYPE_ID_ACCOUNT, "uncollectable#" + currency.getCurrencyID()+"#vat#" + priceFragmentType.getOrganisationID() + "#" + priceFragmentType.getPriceFragmentTypeID());
		return createAccount(accountID, currency, priceFragmentType);
	}

	/**
	 * Book an invoice as "uncollectable".
	 * <p>
	 * <b>Important:</b> This method must not be called directly! It is invoked by
	 * {@link Accounting#bookUncollectableInvoice(Invoice)}.
	 * </p>
	 * @param invoice the invoice that is uncollectable.
	 */
	@Override
	public void bookUncollectableInvoice(User user, Invoice invoice)
	{
		PersistenceManager pm = getPersistenceManager();
		Price invoicePrice = invoice.getPrice();
		Collection<PriceFragment> vatValuePriceFragments = getVatValuePriceFragments(invoicePrice);

		Map<PriceFragmentType, Long> vatPriceFragmentType2vatValueAmount = new HashMap<PriceFragmentType, Long>();
		long netAmount = invoicePrice.getAmount();
		for (PriceFragment priceFragment : vatValuePriceFragments) {
			PriceFragmentType pft = priceFragment.getPriceFragmentType();
			Long vatValueAmount = vatPriceFragmentType2vatValueAmount.get(pft);

			netAmount -= priceFragment.getAmount();
			vatPriceFragmentType2vatValueAmount.put(
					pft,
					(vatValueAmount == null ? 0 : vatValueAmount) + priceFragment.getAmount()
			);
		}

		// If the invoice is paid partially, we have to calculate the percentage
		// and book out only the percentage.
		if (invoice.getInvoiceLocal().getAmountPaid() != 0L) {
			long amountToPay = invoice.getInvoiceLocal().getAmountToPay();
			double uncollectablePercentage = (double)amountToPay / (double)invoicePrice.getAmount();

			if (uncollectablePercentage < 0)
				throw new IllegalStateException("uncollectablePercentage < 0");

			if (uncollectablePercentage > 100)
				throw new IllegalStateException("uncollectablePercentage > 100");

			netAmount = amountToPay;
			for (Map.Entry<PriceFragmentType, Long> me : vatPriceFragmentType2vatValueAmount.entrySet()) {
				long val = Math.round((double)me.getValue() * uncollectablePercentage);
				netAmount -= val;
				me.setValue(val);
			}
		}

		LegalEntity partner;
		AccountTypeID partnerAccountTypeID;
		Accounting accounting = Accounting.getAccounting(pm);
		if (accounting.getMandator().equals(invoice.getVendor())) {
			partner = invoice.getCustomer();
			partnerAccountTypeID = AccountType.ACCOUNT_TYPE_ID_PARTNER_CUSTOMER;
		}
		else {
			partner = invoice.getVendor();
			partnerAccountTypeID = AccountType.ACCOUNT_TYPE_ID_PARTNER_VENDOR;
		}

		Currency currency = invoice.getCurrency();
		AccountType partnerAccountType = (AccountType) pm.getObjectById(partnerAccountTypeID);

		Accountant partnerAccountant = partner.getAccountant();
		if (partnerAccountant == null)
			partnerAccountant = accounting.getPartnerAccountant();

		PartnerBookInvoiceAccountantDelegate partnerBookInvoiceAccountantDelegate =
			(PartnerBookInvoiceAccountantDelegate)partnerAccountant.getAccountantDelegate(BookInvoiceMoneyTransfer.class);
		Account partnerAccount = partnerBookInvoiceAccountantDelegate.getPartnerAccount(
				partnerAccountType,
				partner,
				currency
		);

		Set<Anchor> involvedAnchors = new HashSet<Anchor>();
		Set<MoneyTransfer> transfers = new HashSet<MoneyTransfer>();

		MoneyTransfer moneyTransfer = new MoneyTransfer(
				null, user,
				(netAmount < 0 ? partnerAccount : getUncollectableAccountNet(currency)),
				(netAmount < 0 ? getUncollectableAccountNet(currency) : partnerAccount),
				currency,
				Math.abs(netAmount)
		);
		transfers.add(moneyTransfer);
		moneyTransfer = pm.makePersistent(moneyTransfer);
		moneyTransfer.bookTransfer(user, involvedAnchors);

		for (Map.Entry<PriceFragmentType, Long> me : vatPriceFragmentType2vatValueAmount.entrySet()) {
			moneyTransfer = new MoneyTransfer(
					null, user,
					(me.getValue() < 0 ? partnerAccount : getUncollectableAccountVatValue(currency, me.getKey())),
					(me.getValue() < 0 ? getUncollectableAccountVatValue(currency, me.getKey()) : partnerAccount),
					currency,
					Math.abs(me.getValue())
			);
			transfers.add(moneyTransfer);
			moneyTransfer = pm.makePersistent(moneyTransfer);
			moneyTransfer.bookTransfer(user, involvedAnchors);
		}

		Anchor.checkIntegrity(transfers, involvedAnchors);
	}
}
