package org.nightlabs.jfire.trade.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkTypeID;

@Stateless
public class ArticleContainerLinkManagerBean
extends BaseSessionBeanImpl
implements ArticleContainerLinkManagerRemote
{
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Offer.class); // to make sure, JDO knows at least one implementation of ArticleContainer since this interface is used as field in ArticleContainerLink
			pm.getExtent(ArticleContainerLinkType.class);
			pm.getExtent(ArticleContainerLink.class);

			ArticleContainerLinkType articleContainerLinkType;

			articleContainerLinkType = createOrUpdateArticleContainerLinkType(pm, ArticleContainerLinkType.ID_OFFER_REPLACED);
			articleContainerLinkType.setReverseArticleContainerLinkTypeID(ArticleContainerLinkType.ID_OFFER_REPLACING);

			if (!articleContainerLinkType.getFromClassNamesIncluded().contains(Offer.class.getName()))
				articleContainerLinkType.addFromClassIncluded(Offer.class);

			if (!articleContainerLinkType.getToClassNamesIncluded().contains(Offer.class.getName()))
				articleContainerLinkType.addToClassIncluded(Offer.class);


			articleContainerLinkType = createOrUpdateArticleContainerLinkType(pm, ArticleContainerLinkType.ID_OFFER_REPLACING);
			articleContainerLinkType.setReverseArticleContainerLinkTypeID(ArticleContainerLinkType.ID_OFFER_REPLACED);

			if (!articleContainerLinkType.getFromClassNamesIncluded().contains(Offer.class.getName()))
				articleContainerLinkType.addFromClassIncluded(Offer.class);

			if (!articleContainerLinkType.getToClassNamesIncluded().contains(Offer.class.getName()))
				articleContainerLinkType.addToClassIncluded(Offer.class);


			// TODO DEBUG START
			// Delete old "replacing" and "replaced" links (they only exist specifically for Offer now - not for all ArticleContainer implementations anymore).
			if (true) {
				ArticleContainerLinkTypeID[] articleContainerLinkTypeIDsToDelete = {
						ArticleContainerLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "replacing"),
						ArticleContainerLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "replaced"),
				};

				for (ArticleContainerLinkTypeID acltIDToDelete : articleContainerLinkTypeIDsToDelete) {
					ArticleContainerLinkType acltToDelete = null;
					try {
						acltToDelete = (ArticleContainerLinkType) pm.getObjectById(acltIDToDelete);
					} catch (JDOObjectNotFoundException x) { } // ignore
					if (acltToDelete != null) {
						Collection<? extends ArticleContainerLink> aclsToDelete = ArticleContainerLink.getArticleContainerLinks(pm, acltToDelete, null, null);

//						pm.deletePersistentAll(aclsToDelete);
// This is very slow - about 1 deletion (with the following warning) per second. Iterating an deleting them manually
// causes the same WARN message, but deletes about 50 to 100 per second.
//						17:23:14,585 WARN  [Persistence] Exception thrown by StateManager.isLoaded
//						No such database row
//						org.datanucleus.exceptions.NucleusObjectNotFoundException: No such database row
//						        at org.datanucleus.store.rdbms.request.FetchRequest2.execute(FetchRequest2.java:328)
//						        at org.datanucleus.store.rdbms.RDBMSPersistenceHandler.fetchObject(RDBMSPersistenceHandler.java:271)
//						        at org.datanucleus.state.JDOStateManagerImpl.loadSpecifiedFields(JDOStateManagerImpl.java:1519)
//						        at org.datanucleus.state.JDOStateManagerImpl.isLoaded(JDOStateManagerImpl.java:1897)
//						        at org.datanucleus.jdo.JDOAdapter.isLoaded(JDOAdapter.java:1044)
//						        at org.datanucleus.store.mapped.mapping.PersistenceCapableMapping.preDelete(PersistenceCapableMapping.java:1132)
//						        at org.datanucleus.store.rdbms.request.DeleteRequest.execute(DeleteRequest.java:175)
//						        at org.datanucleus.store.rdbms.RDBMSPersistenceHandler.deleteTable(RDBMSPersistenceHandler.java:447)
//						        at org.datanucleus.store.rdbms.RDBMSPersistenceHandler.deleteObject(RDBMSPersistenceHandler.java:420)
//						        at org.datanucleus.state.JDOStateManagerImpl.internalDeletePersistent(JDOStateManagerImpl.java:4110)
//						        at org.datanucleus.state.JDOStateManagerImpl.deletePersistent(JDOStateManagerImpl.java:4078)
//						        at org.datanucleus.ObjectManagerImpl.deleteObjectInternal(ObjectManagerImpl.java:1473)
//						        at org.datanucleus.ObjectManagerImpl.deleteObject(ObjectManagerImpl.java:1398)
//						        at org.datanucleus.jdo.JDOPersistenceManager.jdoDeletePersistent(JDOPersistenceManager.java:754)
//						        at org.datanucleus.jdo.JDOPersistenceManager.deletePersistentAll(JDOPersistenceManager.java:800)
//						        at org.datanucleus.jdo.connector.PersistenceManagerImpl.deletePersistentAll(PersistenceManagerImpl.java:735)

						for (ArticleContainerLink aclToDelete : aclsToDelete) {
							pm.deletePersistent(aclToDelete);
							pm.flush();
						}

						pm.flush();
						pm.deletePersistent(acltToDelete);
						pm.flush();
					}
				}
			}

			// Creating some test-links from *every* Offer to an arbitrary other offer.
			if (false) {
				@SuppressWarnings("unchecked")
				Collection<Offer> c = (Collection<Offer>) pm.newQuery(Offer.class).execute();
				ArrayList<Offer> offers = new ArrayList<Offer>(c);
				if (offers.size() > 2) {
					Random random = new Random();
					for (Offer offer1 : offers) {
						Offer offer2 = offer1;
						while (offer2 == offer1)
							offer2 = offers.get(random.nextInt(offers.size()));

						Collection<? extends ArticleContainerLink> articleContainerLinks = ArticleContainerLink.getArticleContainerLinks(pm, null, offer1, offer2);
						if (articleContainerLinks.isEmpty())
							pm.makePersistent(new ArticleContainerLink(articleContainerLinkType, offer1, offer2));
					}
				}
			}
			// TODO DEBUG STOP

		} finally {
			pm.close();
		}
	}

	private static ArticleContainerLinkType createOrUpdateArticleContainerLinkType(
			PersistenceManager pm,
			ArticleContainerLinkTypeID articleContainerLinkTypeID
	)
	{
		ArticleContainerLinkType articleContainerLinkType;
		try {
			articleContainerLinkType = (ArticleContainerLinkType) pm.getObjectById(articleContainerLinkTypeID);
		} catch (JDOObjectNotFoundException x) {
			articleContainerLinkType = pm.makePersistent(new ArticleContainerLinkType(articleContainerLinkTypeID));
		}

		// initialise name + description from resource bundle!
		String articleContainerLinkTypeMessagePrefix = ArticleContainerLinkType.class.getName() + '[' + articleContainerLinkType.getOrganisationID() + '/' + articleContainerLinkType.getArticleContainerLinkTypeID() + "]."; //$NON-NLS-1$
		ClassLoader loader = ArticleContainerLinkType.class.getClassLoader();
		articleContainerLinkType.getName().readFromProperties(
				org.nightlabs.jfire.trade.resource.Messages.BUNDLE_NAME,
				loader,
				articleContainerLinkTypeMessagePrefix + "name" //$NON-NLS-1$
		);
		articleContainerLinkType.getDescription().readFromProperties(
				org.nightlabs.jfire.trade.resource.Messages.BUNDLE_NAME,
				loader,
				articleContainerLinkTypeMessagePrefix + "description" //$NON-NLS-1$
		);

		return articleContainerLinkType;
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ArticleContainerLinkID> getArticleContainerLinkIDs(ArticleContainerLinkTypeID articleContainerLinkTypeID, ArticleContainerID fromID, ArticleContainerID toID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			ArticleContainerLinkType articleContainerLinkType = null;
			if (articleContainerLinkTypeID != null)
				articleContainerLinkType = (ArticleContainerLinkType) pm.getObjectById(articleContainerLinkTypeID);

			ArticleContainer from = null;
			if (fromID != null)
				from = (ArticleContainer) pm.getObjectById(fromID);

			ArticleContainer to = null;
			if (toID != null)
				to = (ArticleContainer) pm.getObjectById(toID);

			Collection<? extends ArticleContainerLink> articleContainerLinks = ArticleContainerLink.getArticleContainerLinks(pm, articleContainerLinkType, from, to);
			return NLJDOHelper.getObjectIDSet(articleContainerLinks);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ArticleContainerLink> getArticleContainerLinks(Collection<ArticleContainerLinkID> articleContainerLinkIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, articleContainerLinkIDs, ArticleContainerLink.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ArticleContainerLinkTypeID> getArticleContainerLinkTypeIDs(Class<? extends ArticleContainer> fromClass, Class<? extends ArticleContainer> toClass)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Collection<ArticleContainerLinkType> articleContainerLinkTypes = ArticleContainerLinkType.getArticleContainerLinkTypes(pm, fromClass, toClass);
			return NLJDOHelper.getObjectIDSet(articleContainerLinkTypes);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ArticleContainerLinkType> getArticleContainerLinkTypes(Collection<ArticleContainerLinkTypeID> articleContainerLinkTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, articleContainerLinkTypeIDs, ArticleContainerLinkType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
