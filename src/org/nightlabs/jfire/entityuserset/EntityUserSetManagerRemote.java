package org.nightlabs.jfire.entityuserset;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.entityuserset.id.EntityUserSetID;

public interface EntityUserSetManagerRemote {

	void initialise() throws Exception;

	void importEntityUserSetsOnCrossOrganisationRegistration(Context context) throws Exception;

	Set<EntityUserSetID> getEntityUserSetIDs(String organisationID, Class<?> entityClass);

	Collection<? extends EntityUserSet<?>> getEntityUserSetsForReseller(Collection<EntityUserSetID> entityUserSetIDs);

	String ping(String message);

	List<EntityUserSet<?>> getEntityUserSets(Set<EntityUserSetID> entityUserSetsIDs, String[] fetchGroups, int maxFetchDepth);

	EntityUserSet<?> storeEntityUserSet(EntityUserSet<?> entityUserSet, boolean get, String[] fetchGroups, int maxFetchDepth);

}