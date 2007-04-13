package org.nightlabs.jfire.accounting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.id.TariffID;

public class TariffMapper
{
	private Collection<TariffMapping> tariffMappings;

//	private Map<String, TariffMapping> partnerTariffPK2tariffMapping = null;
//	private Map<String, Map<String, TariffMapping>> localTariffPK2partnerOrganisationID2tariffMapping = null;

	private Map<TariffID, TariffMapping> partnerTariffID2tariffMapping = null;
	private Map<TariffID, Map<String, TariffMapping>> localTariffID2partnerOrganisationID2tariffMapping = null;

	/**
	 * This is a convenience constructor calling {@link #TariffMapper(Collection)} with the result of
	 * {@link TariffMapping#getTariffMappings(PersistenceManager)}.
	 *
	 * @param pm This <code>PersistenceManager</code> is used only within the constructor in order to load the {@link TariffMapping}s. It is not kept within
	 *		the instance of <code>TariffMapper</code>.
	 */
	public TariffMapper(PersistenceManager pm)
	{
		this(TariffMapping.getTariffMappings(pm));
	}

	public TariffMapper(Collection<TariffMapping> tariffMappings)
	{
		this.tariffMappings = tariffMappings;
	}

//	protected Map<String, TariffMapping> getPartnerTariffPK2tariffMapping()
//	{
//		if (partnerTariffPK2tariffMapping == null) {
//			Map<String, TariffMapping> partnerTariffPK2tariffMapping = new HashMap<String, TariffMapping>();
//
//			for (TariffMapping tariffMapping : tariffMappings)
//				partnerTariffPK2tariffMapping.put(tariffMapping.getPartnerTariffPK(), tariffMapping);
//
//			this.partnerTariffPK2tariffMapping = partnerTariffPK2tariffMapping;
//		}
//		return partnerTariffPK2tariffMapping;
//	}
//
//	protected Map<String, Map<String, TariffMapping>> getLocalTariffPK2partnerOrganisationID2tariffMapping()
//	{
//		if (localTariffPK2partnerOrganisationID2tariffMapping == null) {
//			Map<String, Map<String, TariffMapping>> localTariffPK2partnerOrganisationID2tariffMapping = new HashMap<String, Map<String, TariffMapping>>();
//
//			for (TariffMapping tariffMapping : tariffMappings) {
//				Map<String, TariffMapping> partnerOrganisationID2tariffMapping = localTariffPK2partnerOrganisationID2tariffMapping.get(tariffMapping.getLocalTariffPK());
//				if (partnerOrganisationID2tariffMapping == null) {
//					partnerOrganisationID2tariffMapping = new HashMap<String, TariffMapping>();
//					localTariffPK2partnerOrganisationID2tariffMapping.put(tariffMapping.getLocalTariffPK(), partnerOrganisationID2tariffMapping);
//				}
//				partnerOrganisationID2tariffMapping.put(tariffMapping.getPartnerTariffOrganisationID(), tariffMapping);
//			}
//
//			this.localTariffPK2partnerOrganisationID2tariffMapping = localTariffPK2partnerOrganisationID2tariffMapping;
//		}
//
//		return localTariffPK2partnerOrganisationID2tariffMapping;
//	}

	protected Map<TariffID, TariffMapping> getPartnerTariffID2tariffMapping()
	{
		if (partnerTariffID2tariffMapping == null) {
			Map<TariffID, TariffMapping> partnerTariffID2tariffMapping = new HashMap<TariffID, TariffMapping>();

			for (TariffMapping tariffMapping : tariffMappings)
				partnerTariffID2tariffMapping.put(tariffMapping.getPartnerTariffID(), tariffMapping);

			this.partnerTariffID2tariffMapping = partnerTariffID2tariffMapping;
		}
		return partnerTariffID2tariffMapping;
	}

	protected Map<TariffID, Map<String, TariffMapping>> getLocalTariffID2partnerOrganisationID2tariffMapping()
	{
		if (localTariffID2partnerOrganisationID2tariffMapping == null) {
			Map<TariffID, Map<String, TariffMapping>> localTariffID2partnerOrganisationID2tariffMapping = new HashMap<TariffID, Map<String,TariffMapping>>();
			
			for (TariffMapping tariffMapping : tariffMappings) {
				Map<String, TariffMapping> partnerOrganisationID2tariffMapping = localTariffID2partnerOrganisationID2tariffMapping.get(tariffMapping.getLocalTariffID());
				if (partnerOrganisationID2tariffMapping == null) {
					partnerOrganisationID2tariffMapping = new HashMap<String, TariffMapping>();
					localTariffID2partnerOrganisationID2tariffMapping.put(tariffMapping.getLocalTariffID(), partnerOrganisationID2tariffMapping);
				}
				partnerOrganisationID2tariffMapping.put(tariffMapping.getPartnerTariffOrganisationID(), tariffMapping);
			}

			this.localTariffID2partnerOrganisationID2tariffMapping = localTariffID2partnerOrganisationID2tariffMapping;
		}

		return localTariffID2partnerOrganisationID2tariffMapping;
	}

	public TariffID getTariffIDForProductType(TariffID sourceTariffID, String productTypeOrganisationID, boolean throwExceptionIfNotFound)
	{
		if (productTypeOrganisationID.equals(sourceTariffID.organisationID))
			return sourceTariffID;

		TariffID res = null;

		Map<String, TariffMapping> partnerOrganisationID2tariffMapping = getLocalTariffID2partnerOrganisationID2tariffMapping().get(sourceTariffID);
		if (partnerOrganisationID2tariffMapping != null) {
			TariffMapping tm = partnerOrganisationID2tariffMapping.get(productTypeOrganisationID);
			if (tm != null)
				res = tm.getPartnerTariffID();
		}

		if (res == null) {
			TariffMapping tm = getPartnerTariffID2tariffMapping().get(sourceTariffID);
			if (tm != null) {
				res = tm.getLocalTariffID();

				if (!productTypeOrganisationID.equals(res.organisationID)) { // should never happen that we map from one partner-org to another partner-org
					return getTariffIDForProductType(res, productTypeOrganisationID, throwExceptionIfNotFound);
//					throw new IllegalArgumentException("None of these organisations seem to be my local one! Cannot map from sourceTariffID.organisationID=\"" + sourceTariffID.organisationID + "\" to productTypeOrganisationID=\"" + productTypeOrganisationID + "\"!");
				}
			}
		}

		if (throwExceptionIfNotFound && res == null)
			throw new IllegalArgumentException("No mapping found to map from sourceTariffID.organisationID=\"" + sourceTariffID.organisationID + "\" to productTypeOrganisationID=\"" + productTypeOrganisationID + "\"!");

		return res;
	}

//	public String getTariffPKForProductType(String sourceTariffPK, String productTypeOrganisationID, boolean throwExceptionIfNotFound)
//	{
//		if (productTypeOrganisationID.equals(sourceTariffID.organisationID))
//			return sourceTariffID;
//
//		TariffID res = null;
//
//		Map<String, TariffMapping> partnerOrganisationID2tariffMapping = getLocalTariffID2partnerOrganisationID2tariffMapping().get(sourceTariffID);
//		if (partnerOrganisationID2tariffMapping != null) {
//			TariffMapping tm = partnerOrganisationID2tariffMapping.get(productTypeOrganisationID);
//			if (tm != null)
//				res = tm.getPartnerTariffID();
//		}
//
//		if (res == null) {
//			TariffMapping tm = getPartnerTariffID2tariffMapping().get(sourceTariffID);
//			if (tm != null) {
//				res = tm.getLocalTariffID();
//
//				if (!productTypeOrganisationID.equals(res.organisationID)) { // should never happen that we map from one partner-org to another partner-org
//					throw new IllegalArgumentException("None of these organisations seem to be my local one! Cannot map from sourceTariffID.organisationID=\"" + sourceTariffID.organisationID + "\" to productTypeOrganisationID=\"" + productTypeOrganisationID + "\"!");
//				}
//			}
//		}
//
//		if (throwExceptionIfNotFound && res == null)
//			throw new IllegalArgumentException("No mapping found to map from sourceTariffID.organisationID=\"" + sourceTariffID.organisationID + "\" to productTypeOrganisationID=\"" + productTypeOrganisationID + "\"!");
//
//		return res;
//	}
}
