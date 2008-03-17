package org.nightlabs.jfire.voucher.scripting;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.voucher.store.id.VoucherKeyID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptingInitialiser
{
	protected Logger LOGGER = Logger.getLogger(ScriptingInitialiser.class);

	private JFireServerManager jfsm;
	private String organisationID;
	private PersistenceManager pm;

	/**
	 * @param scriptSubDir This is the relative directory under the deploy base directory (e.g. "CrossTicketTrade.ear/script/Ticket")
	 * @param baseCategory All directories/files within the scriptSubDir will be created as sub-categories/scripts of this category.
	 * @param jfsm
	 * @param pm
	 * @param organisationID If you're writing a JFire Community Project, this is {@link Organisation#DEV_ORGANISATION_ID}.
	 */
	public ScriptingInitialiser(JFireServerManager jfsm, PersistenceManager pm, String organisationID)
	{
		this.jfsm = jfsm;
		this.organisationID = organisationID;
		this.pm = pm;
	}

	// init Default ParameterSets
	public void initialise() throws ScriptingIntialiserException
	{
//		Local variable never read
//		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
		ScriptCategory baseCategory;
		ScriptCategory rootCategory;
		ScriptParameterSet paramSet;

		// JFire Trade Root Category
		rootCategory = org.nightlabs.jfire.scripting.ScriptingInitialiser.createCategory(
				pm, null,
				organisationID,
				VoucherScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_TRADE_ROOT,
				VoucherScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "JFireTrade");
		rootCategory.getName().setText(Locale.GERMAN.getLanguage(), "JFireTrade");

		// Voucher category
		baseCategory = org.nightlabs.jfire.scripting.ScriptingInitialiser.createCategory(
				pm, rootCategory, organisationID,
				VoucherScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_TRADE_VOUCHER,
				VoucherScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_VOUCHER);
		baseCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Voucher");
		baseCategory.getName().setText(Locale.GERMAN.getLanguage(), "Gutschein");

		// Parameter Sets
		if (baseCategory.getParameterSet() == null) {
			paramSet = new ScriptParameterSet(organisationID, IDGenerator.nextID(ScriptParameterSet.class));
			paramSet.createParameter(VoucherScriptingConstants.PARAMETER_ID_PERSISTENCE_MANAGER).setScriptParameterClass(PersistenceManager.class);
//			paramSet.createParameter(VoucherScriptingConstants.PARAMETER_ID_VOUCHER_ID).setScriptParameterClass(ProductID.class);
			paramSet.createParameter(VoucherScriptingConstants.PARAMETER_ID_VOUCHER_KEY_ID).setScriptParameterClass(VoucherKeyID.class);
			baseCategory.setParameterSet(paramSet);
		}

		new org.nightlabs.jfire.scripting.ScriptingInitialiser(
				"JFireVoucher.ear/script/Voucher",
				baseCategory,
				VoucherScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_TRADE_VOUCHER,
				jfsm, pm, Organisation.DEV_ORGANISATION_ID).initialise(); // this is a throw-away-instance
	}
}
