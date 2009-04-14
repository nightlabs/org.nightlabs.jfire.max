/**
 *
 */
package org.nightlabs.jfire.issuetimetracking;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.department.prop.DepartmentStructField;
import org.nightlabs.jfire.department.prop.PropHelper;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * This {@link IssueTimeTrackingStruct} class provides methods for creating {@link Struct}.
 *
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssueTimeTrackingStruct 
{
	private static final Logger logger = Logger.getLogger(IssueTimeTrackingStruct.class);
	public static IStruct getIssueTimeTrackingStruct(PersistenceManager pm) {
		String devOrganisationID = Organisation.DEV_ORGANISATION_ID;
		Struct issueStruct = null;
		
		issueStruct = Struct.getStruct(devOrganisationID, Issue.class, Struct.DEFAULT_SCOPE, pm);
		try {
			logger.debug("Found Struct............");
			StructBlock departmentStructBlock = issueStruct.getStructBlock(DEPARTMENT_BLOCK);
			try {
				departmentStructBlock.getStructField(DEPARTMENT_FIELD);
			} catch (StructFieldNotFoundException e) {
				createDepartmentStructField(departmentStructBlock);
			}
		} catch (Exception e) {
			createDepartmentStructBlock(issueStruct);
		}
					
		pm.makePersistent(issueStruct);
		
		return issueStruct;
	}

	private static void createDepartmentStructBlock(IStruct issueStruct) {
		logger.debug("Creating Struct Block.....................");
	
		Map<String, String> blockNameMap = new HashMap<String, String>();
		blockNameMap.put(Locale.GERMAN.getLanguage(), "Abteilungen");
		blockNameMap.put(Locale.ENGLISH.getLanguage(), "Department");
		
		StructBlock structBlock = PropHelper.createStructBlock(issueStruct, DEPARTMENT_BLOCK, blockNameMap);
		structBlock.setUnique(true);		
		try {
			issueStruct.addStructBlock(structBlock);
		} catch (DuplicateKeyException e) {
			throw new RuntimeException(e);
		}
		
		createDepartmentStructField(structBlock);
	}
	
	private static void createDepartmentStructField(StructBlock issueStructBlock) {
		logger.debug("Creating Struct Field.................");
		
		Map<String, String> fieldNameMap = new HashMap<String, String>();
		fieldNameMap.put(Locale.GERMAN.getLanguage(), "Abteilungen");
		fieldNameMap.put(Locale.ENGLISH.getLanguage(), "Department");
		
		DepartmentStructField departmentStructField = PropHelper.createDepartmentField(issueStructBlock, DEPARTMENT_FIELD, fieldNameMap);
		try {
			issueStructBlock.addStructField(departmentStructField);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}

	// *************** DEFAULT StructBlocks StructField IDs ***************************

	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;

	public static final StructBlockID DEPARTMENT_BLOCK = StructBlockID.create(DEV_ORGANISATION_ID,"Department");
	public static final StructFieldID DEPARTMENT_FIELD = StructFieldID.create(DEPARTMENT_BLOCK,"Department");
}
