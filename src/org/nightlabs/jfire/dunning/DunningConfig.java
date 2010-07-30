package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningConfigID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningConfig"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningConfig
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningConfig.class);

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningConfigID;
	
	/**
	 */
	@Persistent(
			dependent="true",
			mappedBy="dunningConfig"
	)
	private DunningConfigName name;
	
	/**
	 */
	@Persistent(
			dependent="true",
			mappedBy="dunningConfig"
	)
	private DunningConfigDescription description;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long defaultTermOfPaymentMSec;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningAutoMode dunningAutoMode;
	
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDunning_invoiceDunningStepss",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private SortedSet<InvoiceDunningStep> invoiceDunningSteps;
	
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDunning_processDunningSteps",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private SortedSet<ProcessDunningStep> processDunningSteps;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningInterestCalculator dunningInterestCalculator;
	
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningConfig() { }
	
	/**
	 * Create an instance of <code>DunningConfig</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param dunningConfigID second part of the primary key. A local identifier within the namespace of the organisation.
	 * @see #DunningConfig(boolean)
	 */
	public DunningConfig(String organisationID, String dunningConfigID, DunningAutoMode dunningAutoMode) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningConfigID, "dunningConfigID"); //$NON-NLS-1$
		
		this.organisationID = organisationID;
		this.dunningConfigID = dunningConfigID;
		this.dunningAutoMode = dunningAutoMode;
		
		this.invoiceDunningSteps = new TreeSet<InvoiceDunningStep>();
		this.processDunningSteps = new TreeSet<ProcessDunningStep>();
		
		this.dunningInterestCalculator = new DunningInterestCalculatorCustomerFriendly();
	}
	
	public String getOrganisationID() {
		return organisationID;
	}

	public String getDunningConfigID() {
		return dunningConfigID;
	}

	public DunningConfigDescription getDescription() {
		return description;
	}
	
	public DunningAutoMode getDunningAutoMode() {
		return dunningAutoMode;
	}
	
	public long getDefaultTermOfPaymentMSec() {
		return defaultTermOfPaymentMSec;
	}
	
	public DunningInterestCalculator getDunningInterestCalculator() {
		return dunningInterestCalculator;
	}
	
	public SortedSet<InvoiceDunningStep> getInvoiceDunningSteps() {
		return invoiceDunningSteps;
	}
	
	public SortedSet<ProcessDunningStep> getProcessDunningSteps() {
		return processDunningSteps;
	}
	
	public DunningConfigName getName() {
		return name;
	}
}