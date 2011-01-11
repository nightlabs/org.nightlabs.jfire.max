package org.nightlabs.jfire.dunning.accounting;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.book.Accountant;
import org.nightlabs.jfire.accounting.book.AccountantDelegate;
import org.nightlabs.jfire.dunning.book.BookDunningLetterMoneyTransfer;
import org.nightlabs.jfire.dunning.book.LocalBookDunningLetterAccountantDelegate;
import org.nightlabs.jfire.dunning.book.PartnerBookDunningLetterAccountantDelegate;
import org.nightlabs.jfire.dunning.id.DunningAccountingID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;

/**
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
	objectIdClass=DunningAccountingID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDunning_DunningAccounting")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningAccounting
implements StoreCallback
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DunningAccounting.class);

	/**
	 * This method returns the singleton instance of DunningAccounting. If there is
	 * no instance of DunningAccounting in the datastore, yet, it will be created.
	 *
	 * @param pm
	 * @return
	 */
	public static DunningAccounting getDunningAccounting(PersistenceManager pm)
	{
		Query q = pm.newNamedQuery(DunningAccounting.class, "getDunningAccounting");
		DunningAccounting dunningAccounting = (DunningAccounting) q.execute();
		q.closeAll();
		if (dunningAccounting != null)
			return dunningAccounting;

		dunningAccounting = new DunningAccounting();

		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		dunningAccounting.organisationID = organisationID;
		dunningAccounting.mandator = OrganisationLegalEntity.getOrganisationLegalEntity(pm, organisationID); 
		
		dunningAccounting.localAccountant = new Accountant(IDGenerator.getOrganisationID(), Accountant.LOCAL_ACCOUNTANT_ID);
		dunningAccounting.mandator.setAccountant(dunningAccounting.localAccountant);
		
		dunningAccounting.partnerAccountant = new Accountant(IDGenerator.getOrganisationID(), Accountant.PARTNER_ACCOUTANT_ID);
		
		AccountantDelegate localBookDunningLetterAccountantDelegate = new LocalBookDunningLetterAccountantDelegate(dunningAccounting.mandator, IDGenerator.nextIDString(AccountantDelegate.class)); 
		dunningAccounting.localAccountant.setAccountantDelegate(BookDunningLetterMoneyTransfer.class, localBookDunningLetterAccountantDelegate);
		
		AccountantDelegate partnerBookDunningLetterAccountantDelegate = new PartnerBookDunningLetterAccountantDelegate(organisationID, IDGenerator.nextIDString(AccountantDelegate.class));
		dunningAccounting.partnerAccountant.setAccountantDelegate(BookDunningLetterMoneyTransfer.class, partnerBookDunningLetterAccountantDelegate);
		
		dunningAccounting = pm.makePersistent(dunningAccounting);
		return dunningAccounting;
	}

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private OrganisationLegalEntity mandator;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Accountant localAccountant;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Accountant partnerAccountant;

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager accountingPM = JDOHelper.getPersistenceManager(this);
		if (accountingPM == null)
			throw new IllegalStateException("This instance of DunningAccounting is not persistent, can not get a PersistenceManager!");

		return accountingPM;
	}

	public OrganisationLegalEntity getMandator() {
		return mandator;
	}


	/**
	 * @return Returns the localAccountant.
	 */
	public Accountant getLocalAccountant()
	{
		return localAccountant;
	}
	/**
	 * @return Returns the partnerAccountant.
	 */
	public Accountant getPartnerAccountant()
	{
		return partnerAccountant;
	}

	@Override
	public void jdoPreStore() {
		// TODO Auto-generated method stub
		
	}
}
