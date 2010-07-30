package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningProcessID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningProcess"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningProcess 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningProcess.class);
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningProcessID;
	
	@Persistent(
			loadFetchGroup="all",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;
	
	@Persistent(
			loadFetchGroup="all",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity customer;
	
	@Join
	@Persistent(table="JFireDunning_DunningProcess_invoices2DunningLevel")
	private Map<Invoice, Integer> invoices2DunningLevel;
	
	@Join
	@Persistent(
		table="JFireDunning_DunningProcess_dunningLetters",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningLetter> dunningLetters;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date paidDT;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date coolDownEnd;
	
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningProcess() { }
	
	/**
	 * Create an instance of <code>DunningProcess</code>.
	 *
	 */
	public DunningProcess(String organisationID, String dunningProcessID, DunningConfig dunningConfig) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(dunningProcessID, "dunningProcessID"); //$NON-NLS-1$
		this.organisationID = organisationID;
		this.dunningProcessID = dunningProcessID;
		this.dunningConfig = dunningConfig;
		
		this.invoices2DunningLevel = new HashMap<Invoice, Integer>();
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDunningProcessID() {
		return dunningProcessID;
	}
	
	public DunningConfig getDunningConfig() {
		return dunningConfig;
	}
	
	public LegalEntity getCustomer() {
		return customer;
	}
	
	public List<DunningLetter> getDunningLetters() {
		return dunningLetters;
	}
	
	public Date getCoolDownEnd() {
		return coolDownEnd;
	}
	
	public Date getPaidDT() {
		return paidDT;
	}
	
	public Map<Invoice, Integer> getInvoices2DunningLevel() {
		return invoices2DunningLevel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dunningProcessID == null) ? 0 : dunningProcessID.hashCode());
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DunningProcess other = (DunningProcess) obj;
		if (dunningProcessID == null) {
			if (other.dunningProcessID != null)
				return false;
		} else if (!dunningProcessID.equals(other.dunningProcessID))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DunningProcess [dunningProcessID=" + dunningProcessID
				+ ", organisationID=" + organisationID + "]";
	}
}
