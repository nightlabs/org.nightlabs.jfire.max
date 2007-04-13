package org.nightlabs.jfire.accounting;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.id.TariffMappingID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.util.Utils;

/**
 * <p>
 * The <code>TariffMapping</code>s define how a foreign (partner) {@link Tariff} is mapped to a local one. This
 * is a bidirectionally unique mapping. Hence, for every local {@link Tariff} and every {@link #partnerTariffOrganisationID}
 * there is exactly one partner-{@link Tariff}. And for every partner-{@link Tariff}, there's exactly one local <code>Tariff</code>.
 * </p>
 * <p>
 * The <code>TariffMapping</code>s are used with the {@link GridPriceConfig}, because local {@link ProductType}s are always sold with local
 * <code>Tariff</code>s, but they can package imported partner-{@link ProductType}s which use the partner's <code>Tariff</code>s. 
 * </p>
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.TariffMappingID"
 *		detachable="true"
 *		table="JFireTrade_TariffMapping"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="partnerTariffOrganisationID, partnerTariffTariffID, localTariffOrganisationID, localTariffTariffID"
 *
 * @jdo.fetch-group name="TariffMapping.partnerTariff" fields="partnerTariff"
 * @jdo.fetch-group name="TariffMapping.localTariff" fields="localTariff"
 *
 * @jdo.query
 *		name="getTariffMappingForPartnerTariff"
 *		query="SELECT UNIQUE
 *				WHERE
 *					this.partnerTariffOrganisationID == :partnerTariffOrganisationID &&
 *					this.partnerTariffTariffID == :partnerTariffTariffID"
 *
 * @jdo.query
 *		name="getTariffMappingForLocalTariffAndPartner"
 *		query="SELECT UNIQUE
 *				WHERE
 *					this.partnerTariffOrganisationID == :partnerTariffOrganisationID &&
 *					this.localTariffOrganisationID == :localTariffOrganisationID &&
 *					this.localTariffTariffID == :localTariffTariffID"
 */
public class TariffMapping
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PARTNER_TARIFF = "TariffMapping.partnerTariff";
	public static final String FETCH_GROUP_LOCAL_TARIFF = "TariffMapping.localTariff";

	@SuppressWarnings("unchecked")
	public static Collection<TariffMapping> getTariffMappings(PersistenceManager pm)
	{
		Query q = pm.newQuery(TariffMapping.class);
		return (Collection<TariffMapping>) q.execute();
	}

	/**
	 * If the mapping for the given <code>partnerTariffID</code> and <code>localTariffID</code> already exists, this method returns it without
	 * any action. Otherwise it will be created, if it would not infringe on the rule of bidirectional uniqueness.
	 *
	 * @param pm The door to the datastore.
	 * @param partnerTariffID Reference to the partner's {@link Tariff}.
	 * @param localTariffID Reference to the local {@link Tariff}.
	 * @return The {@link TariffMapping} for the given tariffs.
	 */
	public static TariffMapping createTariffMapping(PersistenceManager pm, TariffID partnerTariffID, TariffID localTariffID)
	{
		pm.getExtent(TariffMapping.class);
		TariffMappingID tariffMappingID = TariffMappingID.create(
				partnerTariffID.organisationID, partnerTariffID.tariffID,
				localTariffID.organisationID, localTariffID.tariffID);
		TariffMapping tariffMapping;
		try {
			tariffMapping = (TariffMapping) pm.getObjectById(tariffMappingID);
			tariffMapping.getLocalTariff(); // ensure JPOX bug doesn't affect us
			// it exists => return it
			return tariffMapping;
		} catch (JDOObjectNotFoundException x) {
			// not yet existing => we'll create it
		}

		// ensure that the partner-Tariff is not yet mapped
		TariffMapping tm = getTariffMappingForPartnerTariff(pm, partnerTariffID);
		if (tm != null)
			throw new IllegalStateException("The partner-Tariff is already mapped to another local Tariff! " + JDOHelper.getObjectId(tm));

		// ensure that the local Tariff is not yet mapped for this partner-organisation
		tm = getTariffMappingForLocalTariffAndPartner(pm, localTariffID, partnerTariffID.organisationID);
		if (tm != null)
			throw new IllegalStateException("For the partner-organisation " + partnerTariffID.organisationID + " the local Tariff is already mapped to another partner-Tariff! " + JDOHelper.getObjectId(tm));

		// if we come here, there are no collisions => create the new TariffMapping
		pm.getExtent(Tariff.class);
		Tariff partnerTariff = (Tariff) pm.getObjectById(partnerTariffID);
		Tariff localTariff = (Tariff) pm.getObjectById(localTariffID);

		tariffMapping = new TariffMapping(partnerTariff, localTariff);
		return (TariffMapping) pm.makePersistent(tariffMapping);
	}

	/**
	 * @param pm Accessor to the datastore.
	 * @param partnerTariffID The ID of the partner-{@link Tariff} for which to search a {@link TariffMapping}.
	 * @return <code>null</code>, if there is no {@link TariffMapping} for the given <code>partnerTariffID</code> or the appropriate instance.
	 */
	public static TariffMapping getTariffMappingForPartnerTariff(PersistenceManager pm, TariffID partnerTariffID)
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("partnerTariffOrganisationID", partnerTariffID.organisationID);
		params.put("partnerTariffTariffID", partnerTariffID.tariffID);
		Query q = pm.newNamedQuery(TariffMapping.class, "getTariffMappingForPartnerTariff");
		return (TariffMapping) q.executeWithMap(params);
	}

	public static TariffMapping getTariffMappingForLocalTariffAndPartner(PersistenceManager pm, TariffID localTariffID, String partnerTariffOrganisationID)
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("partnerTariffOrganisationID", partnerTariffOrganisationID);
		params.put("localTariffOrganisationID", localTariffID.organisationID);
		params.put("localTariffTariffID", localTariffID.tariffID);
		Query q = pm.newNamedQuery(TariffMapping.class, "getTariffMappingForLocalTariffAndPartner");
		return (TariffMapping) q.executeWithMap(params);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String partnerTariffOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long partnerTariffTariffID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String localTariffOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long localTariffTariffID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Tariff partnerTariff;
	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Tariff localTariff;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient TariffID partnerTariffID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient TariffID localTariffID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient String partnerTariffPK = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient String localTariffPK = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected TariffMapping() { }

	public TariffMapping(Tariff partnerTariff, Tariff localTariff)
	{
		this.partnerTariff = partnerTariff;
		this.localTariff = localTariff;

		this.partnerTariffOrganisationID = partnerTariff.getOrganisationID();
		this.partnerTariffTariffID = partnerTariff.getTariffID();

		this.localTariffOrganisationID = localTariff.getOrganisationID();
		this.localTariffTariffID = localTariff.getTariffID();
	}

	public String getPartnerTariffOrganisationID()
	{
		return partnerTariffOrganisationID;
	}
	public long getPartnerTariffTariffID()
	{
		return partnerTariffTariffID;
	}
	public Tariff getPartnerTariff()
	{
		return partnerTariff;
	}

	public String getLocalTariffOrganisationID()
	{
		return localTariffOrganisationID;
	}
	public long getLocalTariffTariffID()
	{
		return localTariffTariffID;
	}
	public Tariff getLocalTariff()
	{
		return localTariff;
	}

	public TariffID getPartnerTariffID()
	{
		if (partnerTariffID == null)
			partnerTariffID = TariffID.create(partnerTariffOrganisationID, partnerTariffTariffID);

		return partnerTariffID;
	}

	public TariffID getLocalTariffID()
	{
		if (localTariffID == null)
			localTariffID = TariffID.create(localTariffOrganisationID, localTariffTariffID);

		return localTariffID;
	}

	public String getPartnerTariffPK()
	{
		if (partnerTariffPK == null)
			partnerTariffPK = Tariff.getPrimaryKey(partnerTariffOrganisationID, partnerTariffTariffID);

		return partnerTariffPK;
	}

	public String getLocalTariffPK()
	{
		if (localTariffPK == null)
			localTariffPK = Tariff.getPrimaryKey(localTariffOrganisationID, localTariffTariffID);

		return localTariffPK;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof TariffMapping)) return false;
		TariffMapping o = (TariffMapping) obj;
		return
				Utils.equals(o.partnerTariffOrganisationID, this.partnerTariffOrganisationID) &&
				Utils.equals(o.partnerTariffTariffID, this.partnerTariffTariffID) &&
				Utils.equals(o.localTariffOrganisationID, this.localTariffOrganisationID) &&
				Utils.equals(o.localTariffTariffID, this.localTariffTariffID);
	}
	@Override
	public int hashCode()
	{
		return
				Utils.hashCode(partnerTariffOrganisationID) +
				Utils.hashCode(partnerTariffTariffID) +
				Utils.hashCode(localTariffOrganisationID) +
				Utils.hashCode(localTariffTariffID);
	}
}
