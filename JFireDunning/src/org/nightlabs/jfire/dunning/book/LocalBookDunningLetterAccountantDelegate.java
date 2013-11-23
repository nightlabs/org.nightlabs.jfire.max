package org.nightlabs.jfire.dunning.book;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.accounting.book.AccountantDelegate;
import org.nightlabs.jfire.dunning.DunningConfig;
import org.nightlabs.jfire.dunning.DunningFee;
import org.nightlabs.jfire.dunning.DunningFeeType;
import org.nightlabs.jfire.dunning.DunningLetter;
import org.nightlabs.jfire.dunning.DunningMoneyFlowConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.l10n.Currency;

/**
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 * @author Marius Heinzmann
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true"
//		table="JFireDunning_LocalBookDunningLetterAccountantDelegate"
)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class LocalBookDunningLetterAccountantDelegate
	extends AccountantDelegate
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	@Deprecated
	protected LocalBookDunningLetterAccountantDelegate() { }

	public LocalBookDunningLetterAccountantDelegate(OrganisationLegalEntity mandator, String accountantDelegateID)
	{
		super(mandator.getOrganisationID(), accountantDelegateID);
		this.mandator = mandator;
	}

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private OrganisationLegalEntity mandator;

	@Override
	public void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Set<Anchor> involvedAnchors)
	{
		// An Accountant gets all bookings and has to decide himself what to do.
		if (! BookDunningLetterMoneyTransfer.class.isInstance(transfer))
			return;
		
		BookDunningLetterMoneyTransfer dunningMoneyTransfer = (BookDunningLetterMoneyTransfer) transfer;
		DunningConfig dunningConfig = dunningMoneyTransfer.getPayableObject().getDunningRun().getDunningProcess().getDunningConfig();
		
		if (dunningConfig == null)
		{
			throw new IllegalStateException(
					"Trying to book a DunningLetter whose Process is already stopped and hence no DunningConfig is available!" +
					"\n\tDunningLetter = " + dunningMoneyTransfer.getPayableObject()
			);
		}

		DunningMoneyFlowConfig moneyFlowConfig = dunningConfig.getMoneyFlowConfig();
		if (moneyFlowConfig == null)
		{
			throw new IllegalStateException(
					"Trying to book a Dunningletter whose Process's config has no MoneyFlowConfig assigned!" +
					"\n\tDunningLetter = " + dunningMoneyTransfer.getPayableObject()
			);
		}
		
		DunningLetter letterToBook = dunningMoneyTransfer.getPayableObject();
		Currency currency = letterToBook.getCurrency();
		List<DunningFee> dunningFees = letterToBook.getDunningFees();
		if (dunningFees != null && !dunningFees.isEmpty())
		{
			Map<DunningFeeType, Set<DunningFee>> feetype2Fees = new HashMap<DunningFeeType, Set<DunningFee>>();
			Set<DunningFeeType> missingAccounts = new HashSet<DunningFeeType>();
			for (DunningFee fee : dunningFees)
			{
				Set<DunningFee> fees = feetype2Fees.get(fee.getDunningFeeType());
				if (fees == null)
				{
					fees = new HashSet<DunningFee>();
					feetype2Fees.put(fee.getDunningFeeType(), fees);
				}
				fees.add(fee);
				
				if (moneyFlowConfig.getAccount(fee, currency, false) == null)
				{
					missingAccounts.add(fee.getDunningFeeType());
				}
			}

			// check for consistency, i.e. all accounts that we need have to be set!
			boolean interestAccountMissing = moneyFlowConfig.getInterestAccount(currency, false) == null;
			if (!missingAccounts.isEmpty() || interestAccountMissing)
			{
				StringBuilder sb = new StringBuilder(
						"Moneyflow configuration incomplete! Don't know where to book at least one Dunningletter's DunningFee!");
				
				sb.append("\n Missing Mappings for:");
				if (interestAccountMissing)
					sb.append("\n\t The interests.");
				
				sb.append("\n\t The following feeTypes: ").append(missingAccounts);
				throw new IllegalStateException(
						sb.toString() +
						"\n\tDunningLetter = " + dunningMoneyTransfer.getPayableObject() +
						"\n\tMoneyFlowConfig = " + moneyFlowConfig
				);
			}
			
			List<MoneyTransfer> allTransfers = new LinkedList<MoneyTransfer>();
			
			Account interestAccount = moneyFlowConfig.getInterestAccount(currency, false);
			MoneyTransfer interestTransfer = new MoneyTransfer(
					transfer, user, mandator, interestAccount, 
					(org.nightlabs.jfire.accounting.Currency) currency, letterToBook.getTotalInterestAmountToPay()
			);
			
			allTransfers.add(interestTransfer);

			Map<DunningFeeType, Long> amountToPayPerDunningFeeType = letterToBook.getAmountToPayPerDunningFeeType();
			for (Map.Entry<DunningFeeType, Long> feeType2AmountToPay : amountToPayPerDunningFeeType.entrySet())
			{
				Account feeAccount = moneyFlowConfig.getAccount(feeType2AmountToPay.getKey(), currency, false);
				MoneyTransfer feeTransfer = new MoneyTransfer(
						transfer, user, mandator, feeAccount, 
						(org.nightlabs.jfire.accounting.Currency) currency, feeType2AmountToPay.getValue()
				);
				allTransfers.add(feeTransfer);
			}
			
			for (MoneyTransfer moneyTransfer : allTransfers)
			{
				moneyTransfer.bookTransfer(user, involvedAnchors);
			}
		}
	}

	protected OrganisationLegalEntity getMandator()
	{
		return mandator;
	}
}
