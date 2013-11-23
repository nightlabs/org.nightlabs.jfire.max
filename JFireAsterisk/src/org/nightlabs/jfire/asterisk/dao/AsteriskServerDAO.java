package org.nightlabs.jfire.asterisk.dao;

// There is a PhoneSystemDAO!!! AsteriskServer is a subclass!
///**
// * Data access object for {@link AsteriskServer}s.
// *
// * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
// */
//public class AsteriskServerDAO
//extends BaseJDOObjectDAO<PhoneSystemID, PhoneSystem>
//{
//	private AsteriskServerDAO() {}
//
//	private static AsteriskServerDAO sharedInstance = null;
//
//	public static AsteriskServerDAO sharedInstance()
//	{
//		if (sharedInstance == null) {
//			synchronized (AsteriskServerDAO.class) {
//				if (sharedInstance == null)
//					sharedInstance = new AsteriskServerDAO();
//			}
//		}
//		return sharedInstance;
//	}
//
//	@Override
//	protected Collection<AsteriskServer> retrieveJDOObjects(Set<PhoneSystemID> objectIDs,
//			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
//			throws Exception {
//		monitor.beginTask("Fetching "+objectIDs.size()+" asteriskServer information", 1);
//		List<PhoneSystem> asteriskServers = null;
//		try {
//			PhoneSystemManagerRemote phoneSystemManager = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
//			asteriskServers = phoneSystemManager.getPhoneSystems(objectIDs, fetchGroups, maxFetchDepth);
//			monitor.worked(1);
//		} catch (Exception e) {
//			monitor.done();
//			throw new RuntimeException("Failed downloading asteriskServers information!", e);
//		}
//
//		monitor.done();
//		return CollectionUtil.castCollection(asteriskServers);
//	}
//
//	public AsteriskServer storeAsteriskServer(AsteriskServer asteriskServer, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
//	{
//		if(asteriskServer == null)
//			throw new NullPointerException("AsteriskServer to save must not be null");
//		monitor.beginTask("Storing asteriskServer: "+ asteriskServer.getPhoneSystemID(), 3);
//		try {
//			AsteriskManagerRemote asteriskServerManager = JFireEjb3Factory.getRemoteBean(AsteriskManagerRemote.class, SecurityReflector.getInitialContextProperties());
//			monitor.worked(1);
//
//			AsteriskServer result = asteriskServerManager.storeAsteriskServer(asteriskServer, get, fetchGroups, maxFetchDepth);
//			if (result != null)
//				getCache().put(null, result, fetchGroups, maxFetchDepth);
//
//			monitor.worked(1);
//			monitor.done();
//			return result;
//		} catch (Exception e) {
//			monitor.done();
//			throw new RuntimeException("Error while storing AsteriskServer!\n" ,e);
//		}
//	}
//}
