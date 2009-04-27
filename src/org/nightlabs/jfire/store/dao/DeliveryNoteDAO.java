package org.nightlabs.jfire.store.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

public class DeliveryNoteDAO extends BaseJDOObjectDAO<DeliveryNoteID, DeliveryNote> {

	private static DeliveryNoteDAO _sharedInstance;

	public static DeliveryNoteDAO sharedInstance() {
		if (_sharedInstance == null)
			_sharedInstance = new DeliveryNoteDAO();

		return _sharedInstance;
	}

	@Override
	protected Collection<DeliveryNote> retrieveJDOObjects(
			Set<DeliveryNoteID> deliveryNoteIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return CollectionUtil.castCollection(sm.getDeliveryNotes(deliveryNoteIDs, fetchGroups, maxFetchDepth));
	}

	public DeliveryNote getDeliveryNote(DeliveryNoteID deliveryNoteID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, deliveryNoteID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<DeliveryNote> getDeliveryNotes(
			Set<DeliveryNoteID> deliveryNoteIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, deliveryNoteIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<DeliveryNote> getDeliveryNotes(AnchorID vendorID,
			AnchorID customerID, AnchorID endCustomerID, long rangeBeginIdx, long rangeEndIdx,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			List<DeliveryNoteID> deliveryNoteIDList = CollectionUtil.castList(
					sm.getDeliveryNoteIDs(vendorID, customerID, endCustomerID, rangeBeginIdx, rangeEndIdx)
			);
			Set<DeliveryNoteID> deliveryNoteIDs = new HashSet<DeliveryNoteID>(deliveryNoteIDList);

			Map<DeliveryNoteID, DeliveryNote> deliveryNoteMap = new HashMap<DeliveryNoteID, DeliveryNote>(
					deliveryNoteIDs.size());
			for (DeliveryNote deliveryNote : getJDOObjects(null, deliveryNoteIDs, fetchGroups, maxFetchDepth, monitor)) {
				deliveryNoteMap.put((DeliveryNoteID) JDOHelper.getObjectId(deliveryNote), deliveryNote);
			}

			List<DeliveryNote> res = new ArrayList<DeliveryNote>(deliveryNoteIDList.size());
			for (DeliveryNoteID deliveryNoteID : deliveryNoteIDList) {
				DeliveryNote deliveryNote = deliveryNoteMap.get(deliveryNoteID);
				if (deliveryNote != null) {
					res.add(deliveryNote);
				}
			}

			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<DeliveryNote> getDeliveryNotesByQueries(
			QueryCollection<? extends AbstractJDOQuery> queries,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<DeliveryNoteID> deliveryNoteIDs = CollectionUtil.castSet(sm.getDeliveryNoteIDs(queries));

			return getJDOObjects(null, deliveryNoteIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(
					"Couldn't retrieve DeliveryNotes by Queries:", e); //$NON-NLS-1$
		}
	}
}
