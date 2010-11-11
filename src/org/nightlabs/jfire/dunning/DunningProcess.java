package org.nightlabs.jfire.dunning;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
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
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.dunning.id.DunningConfigID;
import org.nightlabs.jfire.dunning.id.DunningProcessID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.CollectionUtil;

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
	@javax.jdo.annotations.Query(
			name="getDunningProcessByCustomerAndCurrency",
			value="SELECT this " +
					"WHERE JDOHelper.getObjectId(customer) == :customerID &&" +
					"JDOHelper.getObjectId(currency) == :currencyID"
	),
	@javax.jdo.annotations.Query(
			name="getActiveDunningProcessesByDunningConfig",
			value="SELECT this " +
					"WHERE paidDT == null && JDOHelper.getObjectId(dunningConfig) == :dunningConfigID"
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
	@Persistent(table="JFireDunning_DunningProcess_dunnedInvoices2DunningLevel")
	private Map<Invoice, Integer> dunnedInvoices2DunningLevel;
	
	/**
	 * All DunningLetters that have been created so far within the scope of this dunning process.
	 * There's only one active DunningLetter and when a new DunningLetter is finalized & booked, the 
	 * previous DunningLetter needs to be booked out.
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
		
		this.dunnedInvoices2DunningLevel = new HashMap<Invoice, Integer>();
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
		return Collections.unmodifiableList(dunningLetters);
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
	
	public boolean isActive() {
		return paidDT == null;
	}
	
	public void addOverdueInvoice(Invoice invoice, int dunningLevel) {
		dunnedInvoices2DunningLevel.put(invoice, dunningLevel);
	}
	
	public Map<Invoice, Integer> getInvoices2DunningLevel() {
		return dunnedInvoices2DunningLevel;
	}
	
	public DunningLetter getLastDunningLetter() {
		return dunningLetters.size() == 0 ? null:dunningLetters.get(dunningLetters.size() - 1);
	}
	
	public boolean isDunnedInvoice(Invoice invoice) {
		return dunnedInvoices2DunningLevel.containsKey(invoice);
	}
	
	public void createDunningLetter(boolean isFinalized) {
		DunningLetter prevDunningLetter = getLastDunningLetter();
		if (isFinalized && prevDunningLetter != null) {
			prevDunningLetter.setBookDT(new Date());
			prevDunningLetter.setFinalized();
			//TODO 6.3.6 books out the prev letter
		}
		
		DunningLetter newDunningLetter = new DunningLetter(this);
		
		//Create entries in the new letter
		for (Invoice dunnedInv : dunnedInvoices2DunningLevel.keySet()) {
			int dunningLevel = dunnedInvoices2DunningLevel.get(dunnedInv);
			newDunningLetter.addDunnedInvoice(dunningConfig, prevDunningLetter, dunningLevel, dunnedInv);
		}
		
		//Calculate fees for the new letter
		DunningFeeAdder feeAdder = dunningConfig.getDunningFeeAdder();
		feeAdder.addDunningFee(prevDunningLetter, newDunningLetter);
		
		//Add the new letter to the list
		dunningLetters.add(newDunningLetter);
		
		DunningLetterNotifier letterNotifier = dunningConfig.getLevel2DunningLetterNotifiers().get(newDunningLetter.getDunningLevel());
		letterNotifier.triggerNotifier();
	}
	
	public static Collection<DunningProcessID> getDunningProcessesByCustomer(PersistenceManager pm, AnchorID customerID) {
		Query query = pm.newNamedQuery(DunningProcess.class, "getDunningProcessIDsByCustomer");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customerID", customerID);
		return CollectionUtil.castList((List<?>) query.executeWithMap(params));
	}
	
	public static DunningProcess getDunningProcessByCustomerAndCurrency(PersistenceManager pm, AnchorID customerID, CurrencyID currencyID) {
		Query query = pm.newNamedQuery(DunningProcess.class, "getDunningProcessByCustomerAndCurrency");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customerID", customerID);
		params.put("currencyID", currencyID);
		return (DunningProcess)query.executeWithMap(params);
	}
	
	public static Collection<DunningProcess> getActiveDunningProcessesByDunningConfig(PersistenceManager pm, DunningConfigID dunningConfigID) {
		Query query = pm.newNamedQuery(DunningProcess.class, "getActiveDunningProcessesByDunningConfig");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dunningConfigID", dunningConfigID);
		return CollectionUtil.castList((List<?>)query.executeWithMap(params));
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