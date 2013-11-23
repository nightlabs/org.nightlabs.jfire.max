package org.nightlabs.jfire.prop.file;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class PropFileManagerBean
extends BaseSessionBeanImpl
implements PropFileManagerRemote
{

	@RolesAllowed("_System_")
	@Override
	public void initialise()
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(FileStructField.class);
			pm.getExtent(FileDataField.class);

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFirePropFileEAR.MODULE_NAME);
			if (moduleMetaData == null) {
				moduleMetaData = ModuleMetaData.createModuleMetaDataFromManifest(JFirePropFileEAR.MODULE_NAME, JFirePropFileEAR.class);
				moduleMetaData = pm.makePersistent(moduleMetaData);
			}

		} finally {
			pm.close();
		}
	}

}
