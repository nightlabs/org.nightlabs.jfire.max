/**
 * 
 */
package org.nightlabs.jfire.accounting;

import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jdo.moduleregistry.UpdateHistoryItem;
import org.nightlabs.jdo.moduleregistry.UpdateNeededHandle;
import org.nightlabs.jfire.accounting.book.Accountant;
import org.nightlabs.jfire.accounting.book.LocalBookInvoiceAccountantDelegate;
import org.nightlabs.jfire.accounting.book.PartnerBookInvoiceAccountantDelegate;
import org.nightlabs.jfire.trade.JFireTradeEAR;
import org.slf4j.LoggerFactory;

/**
 * @author abieber
 */
class UpdateTo12AccountantStructure {
	
	public static void update(PersistenceManager pm) {
		UpdateNeededHandle handle = UpdateHistoryItem.updateNeeded(pm, JFireTradeEAR.MODULE_NAME, Accountant.class.getName() + "#udpateTo12AccountantStrategy");
		if (handle != null) {
			
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireTradeEAR.MODULE_NAME);
			
			if (moduleMetaData != null) {
				// ModuleMetaData might be null for a fresh server, this however will be intialized by other code
				if (moduleMetaData.getSchemaVersionObj().equals(new org.nightlabs.version.Version(1, 2, 0, 0)) || 
						moduleMetaData.getSchemaVersionObj().equals(new org.nightlabs.version.Version(1, 2, 1, 0))) {
					
					Accounting accounting = Accounting.getAccounting(pm);
					if (accounting.getLocalAccountant().getAccountantDelegate(MoneyTransfer.class) == null) {
						accounting.getLocalAccountant().setAccountantDelegate(MoneyTransfer.class, new LocalBookInvoiceAccountantDelegate(accounting.getMandator()));
					}
					
					if (accounting.getPartnerAccountant().getAccountantDelegate(MoneyTransfer.class) == null) {
						accounting.getPartnerAccountant().setAccountantDelegate(MoneyTransfer.class, new PartnerBookInvoiceAccountantDelegate(accounting.getOrganisationID()));
					}
				} else {
					LoggerFactory.getLogger(UpdateTo12AccountantStructure.class).warn("UpdateTo12AccountantStructure was executed for a schemaVersion smaller than 1.2.0.0. How could this happen, as it was introduced in 1.2?", new Exception());
				}
			}
			
			UpdateHistoryItem.updateDone(handle);
		}
	}
}
