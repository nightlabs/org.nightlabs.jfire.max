/*
 * Created on 20.10.2004
 */
package org.nightlabs.ipanema.transfer;


/**
 * This interface defines the registry which is used to manage Transfers of a certain type.
 * One registry might manage transfers of different types as well. It is responsible for
 * generating unique transferIDs within the context of the local organisation and dependent
 * of the transferTypeID.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface TransferRegistry
{
	/**
	 * This method should return the local organisationID. 
	 *
	 * @return Returns the organisationID of the organisation which owns the datastore.
	 */
	public String getOrganisationID();

	/**
	 * This method adds an instance of Transfer. In most of the cases, you don't need to call this
	 * method directly, because every Transfer does a self-registration, if it has been created
	 * by this organisation.
	 *
	 * @param transfer
	 */
	public void addTransfer(Transfer transfer);

	/**
	 * @param transferTypeID
	 * @return
	 */
	public long createTransferID(String transferTypeID);
}
