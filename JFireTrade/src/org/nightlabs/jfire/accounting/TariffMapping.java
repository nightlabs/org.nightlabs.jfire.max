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
import org.nightlabs.util.Util;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * <p>
 * The <code>TariffMapping</code>s define how a local {@link Tariff} is mapped to a foreign (partner) one. This
 * is a unidirectionally unique mapping from local to partner for any given partner-organisationID.
 * Hence, for every local {@link Tariff} and every {@link #partnerTariffOrganisationID}
 * there is exactly one partner-{@link Tariff}. But for every partner-{@link Tariff}, there can be multiple local <code>Tariff</code>s.
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
 * @jdo.create-objectid-class field-order="localTariffOrganisationID, localTariffTariffID, partnerTariffOrganisationID, partnerTariffTariffID"
 *
 * @jdo.fetch-group name="TariffMapping.localTariff" fields="localTariff"
 * @jdo.fetch-group name="TariffMapping.partnerTariff" fields="partnerTariff"
 *
 * @jdo.query
 *		name="getTariffMappingForLocalTariffAndPartner"
 *		query="SELECT UNIQUE
 *				WHERE
 *					this.partnerTariffOrganisationID == :partnerTariffOrganisationID &&
 *					this.localTariffOrganisationID == :localTariffOrganisationID &&
 *					this.localTariffTariffID == :localTariffTariffID"
 */
@PersistenceCapable(
	objectIdClass=TariffMappingID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_TariffMapping")
@FetchGroups({
	@FetchGroup(
		name=TariffMapping.FETCH_GROUP_LOCAL_TARIFF,
		members=@Persistent(name="localTariff")),
	@FetchGroup(
		name=TariffMapping.FETCH_GROUP_PARTNER_TARIFF,
		members=@Persistent(name="partnerTariff"))
})
@Queries(
	@javax.jdo.annotations.Query(
		name="getTariffMappingForLocalTariffAndPartner",
		value="SELECT UNIQUE WHERE this.partnerTariffOrganisationID == :partnerTariffOrganisationID && this.localTariffOrganisationID == :localTariffOrganisationID && this.localTariffTariffID == :localTariffTariffID")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TariffMapping
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PARTNER_TARIFF = "TariffMapping.partnerTariff";
	public static final String FETCH_GROUP_LOCAL_TARIFF = "TariffMapping.localTariff";

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
	public static TariffMapping create(PersistenceManager pm, TariffID localTariffID, TariffID partnerTariffID)
	{
		pm.getExtent(TariffMapping.class);
		TariffMappingID tariffMappingID = TariffMappingID.create(
				localTariffID.organisationID, localTariffID.tariffID,
				partnerTariffID.organisationID, partnerTariffID.tariffID
		);
		TariffMapping tariffMapping;
		try {
			tariffMapping = (TariffMapping) pm.getObjectById(tariffMappingID);
			tariffMapping.getLocalTariff(); // ensure JPOX bug doesn't affect us
			// it exists => return it
			return tariffMapping;
		} catch (JDOObjectNotFoundException x) {
			// not yet existing => we'll create it
		}

		// ensure that the local Tariff is not yet mapped for this partner-organisation
		TariffMapping tm = getTariffMappingForLocalTariffAndPartner(pm, localTariffID, partnerTariffID.organisationID);
		if (tm != null)
			throw new IllegalStateException("For the partner-organisation " + partnerTariffID.organisationID + " the local Tariff is already mapped to another partner-Tariff! " + JDOHelper.getObjectId(tm));

		// if we come here, there are no collisions => create the new TariffMapping
		pm.getExtent(Tariff.class);
		Tariff localTariff = (Tariff) pm.getObjectById(localTariffID);
		Tariff partnerTariff = (Tariff) pm.getObjectById(partnerTariffID);

		tariffMapping = new TariffMapping(localTariff, partnerTariff);
		return pm.makePersistent(tariffMapping);
	}

	public static TariffMapping getTariffMappingForLocalTariffAndPartner(PersistenceManager pm, TariffID localTariffID, String partnerTariffOrganisationID)
	{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("localTariffOrganisationID", localTariffID.organisationID);
		params.put("localTariffTariffID", localTariffID.tariffID);
		params.put("partnerTariffOrganisationID", partnerTariffOrganisationID);
		Query q = pm.newNamedQuery(TariffMapping.class, "getTariffMappingForLocalTariffAndPartner");
		return (TariffMapping) q.executeWithMap(params);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String localTariffOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String localTariffTariffID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String partnerTariffOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String partnerTariffTariffID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Tariff localTariff;
	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Tariff partnerTariff;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient TariffID localTariffID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient TariffID partnerTariffID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient String localTariffPK = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient String partnerTariffPK = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TariffMapping() { }

	public TariffMapping(Tariff localTariff, Tariff partnerTariff)
	{
		this.localTariff = localTariff;
		this.partnerTariff = partnerTariff;
		
		this.localTariffOrganisationID = localTariff.getOrganisationID();
		this.localTariffTariffID = localTariff.getTariffID();

		this.partnerTariffOrganisationID = partnerTariff.getOrganisationID();
		this.partnerTariffTariffID = partnerTariff.getTariffID();
	}

	public String getLocalTariffOrganisationID()
	{
		return localTariffOrganisationID;
	}
	public String getLocalTariffTariffID()
	{
		return localTariffTariffID;
	}
	public Tariff getLocalTariff()
	{
		return localTariff;
	}

	public String getPartnerTariffOrganisationID()
	{
		return partnerTariffOrganisationID;
	}
	public String getPartnerTariffTariffID()
	{
		return partnerTariffTariffID;
	}
	public Tariff getPartnerTariff()
	{
		return partnerTariff;
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
				Util.equals(o.localTariffOrganisationID, this.localTariffOrganisationID) &&
				Util.equals(o.localTariffTariffID, this.localTariffTariffID) &&
				Util.equals(o.partnerTariffOrganisationID, this.partnerTariffOrganisationID) &&
				Util.equals(o.partnerTariffTariffID, this.partnerTariffTariffID);
	}
	@Override
	public int hashCode()
	{
		return
				Util.hashCode(localTariffOrganisationID) +
				Util.hashCode(localTariffTariffID) +
				Util.hashCode(partnerTariffOrganisationID) +
				Util.hashCode(partnerTariffTariffID);
	}
}
