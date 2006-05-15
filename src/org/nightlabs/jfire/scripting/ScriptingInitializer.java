/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.scripting;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xerces.util.DOMUtil;
import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.Utils;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ScriptingInitializer 
{
	protected Logger LOGGER = Logger.getLogger(ScriptingInitializer.class);

	private String scriptSubDir;
	private ScriptCategory baseCategory;
	private JFireServerManager jfsm;
	private PersistenceManager pm;
	private String organisationID;
	private String scriptRegistryItemType;
	
	private SAXException parseException;
	private Map<File, Document> categoryDescriptors = new HashMap<File, Document>();

	/**
	 * 
	 * @param parent optional may be null
	 * @param organisationID the organisation ID
	 * @param scriptRegistryItemType 
	 * @param scriptRegistryItemID
	 */
	public static final ScriptCategory createCategory(PersistenceManager pm, ScriptCategory parent, String organisationID, 
			String scriptRegistryItemType, String scriptRegistryItemID)  
	{
		ScriptCategory category;
		try {
			category = (ScriptCategory) pm.getObjectById(ScriptRegistryItemID.create(organisationID, scriptRegistryItemType, scriptRegistryItemID));			
		} catch (JDOObjectNotFoundException e) {
			category = new ScriptCategory(parent, organisationID, scriptRegistryItemType, scriptRegistryItemID);
			if (parent == null)
				pm.makePersistent(category);
			else
				parent.addChild(category);
		}
		return category;
	}
	
	/**
	 * @param scriptSubDir This is the relative directory under the deploy base directory (e.g. "IpanemaTicketing.ear/script/Ticket")
	 * @param baseCategory All directories/files within the scriptSubDir will be created as sub-categories/scripts of this category.
	 * @param jfsm
	 * @param pm
	 * @param scriptRegistryItemType is the type (identifier) for the scripts in categories, categories get the scriptRegistryItemType from their parent 
	 * @param organisationID If you're writing a JFire Community Project, this is {@link Organisation#DEVIL_ORGANISATION_ID}.
	 */
	public ScriptingInitializer(
			String scriptSubDir, ScriptCategory baseCategory, String scriptRegistryItemType,
			JFireServerManager jfsm, PersistenceManager pm, String organisationID)
	{
		this.scriptSubDir = scriptSubDir;
		this.baseCategory = baseCategory;
		this.jfsm = jfsm;
		this.pm = pm;
		this.organisationID = organisationID;
		this.scriptRegistryItemType = scriptRegistryItemType;		
	}

	private ScriptRegistry scriptRegistry = null;
	protected ScriptRegistry getScriptRegistry()
	{
		if (scriptRegistry == null)
			scriptRegistry = ScriptRegistry.getScriptRegistry(pm);

		return scriptRegistry;
	}

	protected Collection<String> getFileExtensions()  
	{
		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
		return scriptRegistry.getRegisteredFileExtensions();
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public void initialize() 
	throws ModuleException 
	{
		String j2eeBaseDir = jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File scriptDir = new File(j2eeBaseDir, scriptSubDir);

		if (!scriptDir.exists())
			throw new IllegalStateException("Script directory does not exist: " + scriptDir.getAbsolutePath());

		LOGGER.info("BEGIN initialization of Scripts");	
//		initDefaultParameterSets();
		createScriptCategories(scriptDir, baseCategory);
	}

	private Document getCategoryDescriptor(File categoryDir) 
	throws SAXException, IOException 
	{
		Document doc = categoryDescriptors.get(categoryDir);
		if (true)
			return null;
		// TODO: Remove this to enable content.xml parsing
		if (doc == null) {
			final File contentFile = new File(categoryDir, "content.xml");
			if (contentFile.exists()) { 
				DOMParser parser = new DOMParser();
				parser.setErrorHandler(new ErrorHandler(){
					public void error(SAXParseException exception) throws SAXException {
						LOGGER.error("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void fatalError(SAXParseException exception) throws SAXException {
						LOGGER.fatal("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void warning(SAXParseException exception) throws SAXException {
						LOGGER.warn("Parse ("+contentFile+"): ", exception);
					}
				});
				parseException = null;
				InputSource inputSource;
				try {
					inputSource = new InputSource(new FileInputStream(contentFile));
				} catch (FileNotFoundException e) {
					throw new IllegalStateException("Although checked with .exists() file "+contentFile+" does not seem to exist. ", e);
				}
				parser.parse(inputSource);
				if (parseException != null)
					throw parseException;
//				parser.getDocument().get
				categoryDescriptors.put(categoryDir, parser.getDocument());
			}
		}
		return doc;
	}
	
	private Node getScriptDescriptor(File scriptFile, Document categoryDocument) 
	throws TransformerException 
	{
		Collection<Node> nodes = NLDOMUtil.findNodeList(categoryDocument, "script-category/script");
		for (Node scriptNode : nodes) {
			Node fNode = scriptNode.getAttributes().getNamedItem("file");
			if (fNode != null && scriptFile.getName().equals(fNode.getTextContent()))
				return scriptNode;
		}
		return null;
	}
	
	private ScriptParameterSet createParameterSet(
			PersistenceManager pm,
			String organisationID,
			ScriptParameterSet parameterSet, 
			Node parentNode
		) 
	throws TransformerException 
	{
		if (parameterSet == null) {
			ScriptRegistry registry = ScriptRegistry.getScriptRegistry(pm);
			parameterSet = new ScriptParameterSet(organisationID, registry.createScriptParameterSetID());
		}
		parameterSet.removeAllParameters();
		Collection<Node> nodes = NLDOMUtil.findNodeList(parentNode, "parameter-set/parameter");
		for (Node node : nodes) {
			Node pIDNode = node.getAttributes().getNamedItem("name");
			if (pIDNode != null && !"".equals(pIDNode.getTextContent())) {
				Node pTypeNode = node.getAttributes().getNamedItem("type");
				if (pTypeNode != null && !"".equals(pTypeNode.getTextContent())) {
					parameterSet.createParameter(pIDNode.getTextContent()).setScriptParameterClassName(pTypeNode.getTextContent());
				}
				else
					LOGGER.warn("parameter element of parameter-set has an invalid/missing type attribute");
			} else
				LOGGER.warn("parameter element of parameter-set has an invalid/missing name attribute");
		}
		return parameterSet;
	}
	
	private void createElementName(Node elementNode, I18nText name, String def) 
	{
		// script name
		boolean nameSet = false;
		if (elementNode != null) {
			Collection<Node> nodes = NLDOMUtil.findNodeList(elementNode, "name");
			for (Node node : nodes) {
				Node lIDNode = node.getAttributes().getNamedItem("language");
				if (lIDNode != null && !"".equals(lIDNode.getTextContent())) {
					name.setText(lIDNode.getTextContent(), node.getTextContent());
					nameSet = true;
				}
				else
					LOGGER.warn("name element of node "+elementNode.getNodeName()+" has an invalid/missing language attribute");
			}
		}
		if (!nameSet)
			name.setText(Locale.ENGLISH.getLanguage(), def);
	}
	
	private void createScriptCategories(File dir, ScriptCategory parent) 
	throws ModuleException
	{
		try {
			String categoryID = dir.getName();
			String itemType = parent.getScriptRegistryItemType();
			Document catDocument = getCategoryDescriptor(dir);
			if (catDocument != null) {
				Node catNode = NLDOMUtil.findSingleNode(catDocument, "script-category");
				if (catNode != null) {
					Node idAttr = catNode.getAttributes().getNamedItem("id");
					if (idAttr != null && !"".equals(idAttr.getTextContent()))
						categoryID = idAttr.getTextContent();

					Node typeAttr = catNode.getAttributes().getNamedItem("type");
					if (typeAttr != null && !"".equals(typeAttr.getTextContent()))
						itemType = typeAttr.getTextContent();
				}
			}

			// Create the category
			ScriptCategory category;
			try {
				pm.getExtent(ScriptCategory.class);
				category = (ScriptCategory) pm.getObjectById(
						ScriptRegistryItemID.create(
								organisationID,
								itemType,
								categoryID));			
			} catch(JDOObjectNotFoundException e) {
				category = new ScriptCategory(parent, organisationID, itemType, categoryID);
				parent.addChild(category);
			}

			// category name and parameters
			if (catDocument != null) {
				Node catNode = NLDOMUtil.findSingleNode(catDocument, "script-category");
				if (catNode != null) {
					createElementName(catNode, category.getName(), categoryID);
					createParameterSet(pm, organisationID, category.getParameterSet(), catNode);
				}
			}
			LOGGER.info("create Script Category = "+category.getName());
			

			// Create scripts
			File[] scripts = dir.listFiles(scriptFileNameFilter);
			for (int j=0; j<scripts.length; j++) {
				File scriptFile = scripts[j];

				Node scriptNode = getScriptDescriptor(scriptFile, catDocument);				
				
				String scriptID = Utils.getFileNameWithoutExtension(scriptFile.getName());
				String scriptItemType = scriptRegistryItemType;
				
				if (scriptNode != null) {
					Node idNode = scriptNode.getAttributes().getNamedItem("id");
					if (idNode != null && !"".equals(idNode.getTextContent()))
						scriptID = idNode.getTextContent();
					Node typeNode = scriptNode.getAttributes().getNamedItem("type");
					if (typeNode != null && !"".equals(typeNode.getTextContent()))
						scriptItemType = typeNode.getTextContent();
				}
				
				try {			
					LOGGER.info("create Script = " + scriptID);				
					String scriptContent = Utils.readTextFile(scriptFile);
					LOGGER.debug("scriptContent = " + scriptContent);
					Script script;
					try {
						pm.getExtent(Script.class);
						script = (Script) pm.getObjectById(ScriptRegistryItemID.create(
								organisationID,
								scriptItemType,
								scriptID)
						);
					} catch (JDOObjectNotFoundException e) {
						script = new Script(category, organisationID, scriptRegistryItemType, scriptID);
						category.addChild(script);
					}
					script.setText(scriptContent);
					script.setLanguage(getScriptRegistry().getLanguageByFileName(scriptFile.getName(), true));
					
					// script name and parameters
					if (scriptNode != null) {
						createElementName(scriptNode, script.getName(), scriptID);
						createParameterSet(pm, organisationID, script.getParameterSet(), scriptNode);
					}
					
				} catch (Exception e) {
					LOGGER.warn("could NOT create script "+scriptID+"!", e);
				}
			}

			
			// recurse
			File[] subDirs = dir.listFiles(dirFileFilter);		
			for (int i=0; i<subDirs.length; i++) {
				createScriptCategories(subDirs[i], category);
			}
		} catch (IOException e) {
			throw new ModuleException(e);
		} catch (TransformerException e) {
			throw new ModuleException(e);
		} catch (SAXException e) {
			throw new ModuleException(e);
		}
	}

//	protected String getScriptContent(File f) 
//	throws FileNotFoundException, IOException 
//	{
//		FileInputStream fin = new FileInputStream(f);
////		InputStreamReader reader = new InputStreamReader(fin, "utf-8");
//		DataInputStream din = new DataInputStream(fin);
//		StringBuffer sb = new StringBuffer();
//		while(din.available() != 0) {
//			sb.append(din.readUTF());
//		}
//		din.close();
//		fin.close();
//		return null;
//	}

	protected FileFilter dirFileFilter = new FileFilter() {	
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}	
	};

	private FilenameFilter scriptFileNameFilter = new FilenameFilter()
	{	
		public boolean accept(File dir, String name) 
		{				
			String fileExtension = Utils.getFileExtension(name);
			for (Iterator<String> it = getFileExtensions().iterator(); it.hasNext(); ) {
				String registeredFileExt = it.next();
				if (registeredFileExt.equals(fileExtension))
					return true;
			}
			return false;
		}	
	};
	
//	// init Default ParameterSets
//	private void initDefaultParameterSets() 
//	{
//		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
//		ScriptParameterSet paramSet = new ScriptParameterSet(organisationID, scriptRegistry.createScriptParameterSetID());
//		paramSet.createParameter(ScriptingConstants.PARAMETER_ID_PERSISTENCE_MANAGER).setScriptParameterClass(PersistenceManager.class);
//		paramSet.createParameter(ScriptingConstants.PARAMETER_ID_TICKET_ID).setScriptParameterClass(ProductID.class);
//	}
}

