package org.nightlabs.jfire.issuetimetracking;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.department.prop.DepartmentStructField;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * This {@link IssueTimeTrackingStruct} class provides methods for creating {@link Struct}.
 *
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 * @author marco schulze - marco at nightlabs dot de
 */
public class IssueTimeTrackingStruct
{
	private static final Logger logger = Logger.getLogger(IssueTimeTrackingStruct.class);

	public static IStruct getIssueTimeTrackingStruct(PersistenceManager pm) {
		pm.getExtent(DepartmentStructField.class);

		Struct issueStruct = null;
		issueStruct = Struct.getStruct(DEV_ORGANISATION_ID, Issue.class, Struct.DEFAULT_SCOPE, pm);
		try {
			issueStruct.getStructBlock(DEPARTMENT_BLOCK);
		} catch (StructBlockNotFoundException x) {
			createDepartmentStructBlock(issueStruct);
		}

		return issueStruct;
	}

	private static StructBlock createDepartmentStructBlock(IStruct issueStruct) {
		if (logger.isDebugEnabled())
			logger.debug("Creating Struct Block.....................");

		StructBlock structBlock = new StructBlock(issueStruct, DEPARTMENT_BLOCK);
		structBlock.setUnique(true);
		structBlock.getName().setText(Locale.GERMAN, "Abteilung");
		structBlock.getName().setText(Locale.ENGLISH, "Department");

		createDepartmentStructField(structBlock);
		try {
			issueStruct.addStructBlock(structBlock);
		} catch (DuplicateKeyException e) {
			throw new RuntimeException(e);
		}
		return structBlock;
	}

	private static void createDepartmentStructField(StructBlock issueStructBlock) {
		if (logger.isDebugEnabled())
			logger.debug("Creating Struct Field.................");

		DepartmentStructField departmentStructField = new DepartmentStructField(issueStructBlock, DEPARTMENT_FIELD);
		departmentStructField.getName().setText(Locale.GERMAN, "Abteilung");
		departmentStructField.getName().setText(Locale.ENGLISH, "Department");
		try {
			issueStructBlock.addStructField(departmentStructField);
		} catch (DuplicateKeyException e) {
			throw new RuntimeException(e);
		}
	}

	// *************** DEFAULT StructBlocks StructField IDs ***************************

	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;

	public static final StructBlockID DEPARTMENT_BLOCK = StructBlockID.create(DEV_ORGANISATION_ID,"Department");
	public static final StructFieldID DEPARTMENT_FIELD = StructFieldID.create(DEPARTMENT_BLOCK,"Department");
}