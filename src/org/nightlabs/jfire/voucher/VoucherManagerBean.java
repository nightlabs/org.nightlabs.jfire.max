package org.nightlabs.jfire.voucher;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

/**
 * @ejb.bean name="jfire/ejb/JFireVoucher/VoucherManager"	
 *					 jndi-name="jfire/ejb/JFireVoucher/VoucherManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class VoucherManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
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
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Implement
	public void ejbRemove() throws EJBException, RemoteException { }

}
