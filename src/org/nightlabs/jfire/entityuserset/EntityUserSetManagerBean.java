package org.nightlabs.jfire.entityuserset;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;

/**
 * @ejb.bean name="jfire/ejb/JFireEntityUserSet/EntityUserSetManager"
 *					 jndi-name="jfire/ejb/JFireEntityUserSet/EntityUserSetManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class EntityUserSetManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException { }

	/**
	 * {@inheritDoc}
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Override
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * This method is called by the organisation initialisation mechanism.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise() throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {

		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@Override
	public String ping(String message) {
		return super.ping(message);
	}
}
