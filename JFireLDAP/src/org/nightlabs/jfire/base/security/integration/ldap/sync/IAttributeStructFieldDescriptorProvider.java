package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.util.Collection;

import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.AttributeStructFieldDescriptor;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.LDAPAttributeSyncPolicy;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * Implementations of this interface are intended to hold {@link LDAPServer}s schema specific {@link AttributeStructFieldDescriptor}s. 
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public interface IAttributeStructFieldDescriptorProvider {
	
	/**
	 * Get ID of {@link StructBlock} which holds {@link StructField}s used in mapping between attributes and {@link Person} datafields. 
	 * 
	 * @return {@link StructBlockID}
	 */
	StructBlockID getAttributeStructBlockID();

	/**
	 * Get a {@link Collection} of {@link AttributeStructFieldDescriptor}s by given {@link LDAPAttributeSyncPolicy}.
	 * 
	 * @param attributeSyncPolicy
	 * @return
	 */
	Collection<AttributeStructFieldDescriptor> getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy attributeSyncPolicy);

}
