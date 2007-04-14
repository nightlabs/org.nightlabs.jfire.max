package org.nightlabs.jfire.voucher.store.deliver;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorManual;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * This implementation of {@link ServerDeliveryProcessor} is used for client-sided voucher print.
 * The client-sided delivery processor does all the real work - this means this class does
 * (nearly) nothing.
 *
 * @author Daniel Mazurek - daniel at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class ServerDeliveryProcessorClientSideVoucherPrint 
extends ServerDeliveryProcessor 
{
	public static ServerDeliveryProcessorClientSideVoucherPrint getServerDeliveryProcessorClientSideVoucherPrint(PersistenceManager pm)
	{
		ServerDeliveryProcessorClientSideVoucherPrint serverDeliveryProcessor;
		try {
			pm.getExtent(ServerDeliveryProcessorClientSideVoucherPrint.class);
			serverDeliveryProcessor = (ServerDeliveryProcessorClientSideVoucherPrint) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, 
							ServerDeliveryProcessorClientSideVoucherPrint.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessor = new ServerDeliveryProcessorClientSideVoucherPrint(
					Organisation.DEVIL_ORGANISATION_ID, 
					ServerDeliveryProcessorClientSideVoucherPrint.class.getName());
			serverDeliveryProcessor.getName().setText(
					Locale.ENGLISH.getLanguage(), 
					"Server Delivery Processor for delivering tickets to print on the client");			
			serverDeliveryProcessor = (ServerDeliveryProcessorClientSideVoucherPrint) pm.makePersistent(serverDeliveryProcessor);
		}

		return serverDeliveryProcessor;
	}
	
	public ServerDeliveryProcessorClientSideVoucherPrint(String organisationID, String serverDeliveryProcessorID)
	{
		super(organisationID, serverDeliveryProcessorID);		
	}
	
	@Override
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams)
			throws DeliveryException {
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams)
			throws DeliveryException {
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException {
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams)
			throws DeliveryException {
		return null;
	}

	@Override
	public Anchor getAnchorOutside(DeliverParams deliverParams) {
		return getRepositoryOutside(deliverParams, "anchorOutside.voucherPrint");
	}

}
