package org.nightlabs.jfire.trade.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

public class DeliveryNoteDAO extends BaseJDOObjectDAO<DeliveryNoteID, DeliveryNote> {
	
	private static DeliveryNoteDAO _sharedInstance;

	public static DeliveryNoteDAO sharedInstance() {
		if (_sharedInstance == null)
			_sharedInstance = new DeliveryNoteDAO();

		return _sharedInstance;
	}

	@Override
	@Implement
	protected Collection<DeliveryNote> retrieveJDOObjects(
			Set<DeliveryNoteID> deliveryNoteIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return sm.getDeliveryNotes(deliveryNoteIDs, fetchGroups, maxFetchDepth);
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
			AnchorID customerID, long rangeBeginIdx, long rangeEndIdx,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			List<DeliveryNoteID> deliveryNoteIDList = sm.getDeliveryNoteIDs(vendorID, customerID, rangeBeginIdx, rangeEndIdx);
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
			StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<DeliveryNoteID> deliveryNoteIDs = sm.getDeliveryNoteIDs(queries);

			return getJDOObjects(null, deliveryNoteIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(
					"Couldn't retrieve DeliveryNotes by Queries:", e); //$NON-NLS-1$
		}
	}
}
