package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dunning.id.DunningLetterID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningLetterID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningLetter")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningLetter 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningLetterID;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningProcess dunningProcess;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Integer dunningLevel;
	
	@Join
	@Persistent(
		table="JFireDunning_DunningLetter_dunnedInvoices",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningLetterEntry> dunnedInvoices;
	
	@Join
	@Persistent(
		table="JFireDunning_DunningLetter_dunningFees",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningFee> dunningFees;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date finalizeDT;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date bookDT;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price priceExcludingInvoices;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price priceIncludingInvoices;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountPaidExcludingInvoices;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amountToPay;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean outstanding;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected DunningLetter() { }
	
	public DunningLetter(String organisationID, String dunningLetterID, DunningProcess dunningProcess) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningLetterID, "dunningLetterID"); //$NON-NLS-1$
		this.organisationID = organisationID;
		this.dunningLetterID = dunningLetterID;
		this.dunningProcess = dunningProcess;
		
	}
}
