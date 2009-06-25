package org.nightlabs.jfire.personrelation;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class PersonRelationManagerBean
extends BaseSessionBeanImpl
implements PersonRelationManagerRemote
{
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {




		} finally {
			pm.close();
		}
	}

}
