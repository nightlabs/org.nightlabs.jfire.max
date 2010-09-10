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
import javax.jdo.annotations.Queries;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * When Invoice.dueDateForPayment is exceeded (i.e. NOW is after this timestamp) 
 * and there is no DunningInvoiceProcess for the corresponding customer, 
 * a new DunningProcess is either manually (via UI) or automatically created 
 * (with the copy of its corresponding DunningConfig – a shallow copy is enough).<br> 
 * 
 * <br>Thus, there is at most one active DunningProcess for every customer in the database. 
 * If there is one already, the existing DunningProcess is used and the overdue invoice 
 * is added.<br>
 * 
 * <br>The DunningProcess governs the generation of DunningLetters which may be skipped depending on the coolDownEnd. 

 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningProcessID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningProcess"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Queries({
	@javax.jdo.annotations.Query(
			name="getDunningProcessIDsByCustomer",
			value="SELECT JDOHelper.getObjectId(this) " +
					"WHERE JDOHelper.getObjectId(customer) == :customerID"
	),
})
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
	
	/**
	 * A deep copy of the DunningConfig for the corresponding customer.
	 */
	@Persistent(
			loadFetchGroup="all",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;
	
	/**
	 * The customer for which this DunningProcess has been created.
	 */
	@Persistent(
			loadFetchGroup="all",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity customer;

	/**
	 * The currency used in all the invoices for that DunningProcess.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;
	
	/**
	 * The invoices for which this dunning process is happening and their corresponding dunning levels.
	 */
	@Join
	@Persistent(table="JFireDunning_DunningProcess_invoices2DunningLevel")
	private Map<Invoice, Integer> invoices2DunningLevel;
	
	/**
	 * All DunningLetters that have been created so far within the scope of this dunning process.
	 */
	@Join
	@Persistent(
		table="JFireDunning_DunningProcess_dunningLetters",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DunningLetter> dunningLetters;
	
	/**
	 * The date at which all invoices were paid. While this field is set to null, its DunningProcess is active!
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date paidDT;
	
	/**
	 * The point of time after which new DunningLetters should be created again.
	 */
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

	public void setCustomer(LegalEntity customer) {
		this.customer = customer;
	}
	
	public LegalEntity getCustomer() {
		return customer;
	}
	
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
	
	public Currency getCurrency() {
		return currency;
	}
	
	public List<DunningLetter> getDunningLetters() {
		return dunningLetters;
	}
	
	public void setCoolDownEnd(Date coolDownEnd) {
		this.coolDownEnd = coolDownEnd;
	}
	
	public Date getCoolDownEnd() {
		return coolDownEnd;
	}
	
	public void setPaidDT(Date paidDT) {
		this.paidDT = paidDT;
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