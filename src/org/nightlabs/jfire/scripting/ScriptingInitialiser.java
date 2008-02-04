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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.Util;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Helper class to initialize default scripts. The ScriptInitializer will
 * recusursively scan a given directory and create a tree of {@link ScriptCategory}s
 * and {@link Script}s according to the directory structure it finds.<br/>
 * 
 * For each folder the initializer finds a category will be created as child of
 * the upper directory's one. A descriptor file 'content.xml' can be placed in
 * the directory to define in detail what id and names and parameter-sets
 * the created categories and scripts should have.
 * 
 * Each file found will cause a {@link Script} to be created. Depending on the 
 * file extension some assumptions will be made:
 * <ul>
 *   <li><b>.js</b>: The script is assumed to be a JavaScript and the file-contents to state the script text.</li>
 *   <li><b>.javaclass</b>: The script is assumed to be a JavaClass and the file-contents to reference the
 *   class by its fully qualified class-name</li>
 * </ul>
 * For more detailed information consult the the dtd of the scriping initializer content.xml at
 * <a href="http://www.nightlabs.de/dtd/scripting-initializer-content_1_0.dtd">http://www.nightlabs.de/dtd/scripting-initializer-content_1_0.dtd</a>
 * 
 * The recommended usage is 
 * <ul>
 * <li><b>Create the initializer</b>: Use {@link #ScriptingInitializer(String, ScriptCategory, String, JFireServerManager, PersistenceManager, String)} 
 * to create the initializer and set the base category, root directory and fallback values for ids</li>
 * <li><b>Initialize from (sub)directories</b>: Use {@link #initialise()} to start the initialization</li>
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptingInitialiser 
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ScriptingInitialiser.class);

	private String scriptSubDir;
	private ScriptCategory baseCategory;
	private JFireServerManager jfsm;
	private PersistenceManager pm;
	private String organisationID;
	private String scriptRegistryItemType;
	
	private SAXException parseException;
	private Map<File, Document> categoryDescriptors = new HashMap<File, Document>();

	/**
	 * Returns the ScriptCategory for the given primary key either out of the datastore
	 * or after creating it.
	 * 
	 * @param parent optional may be null
	 * @param organisationID the organisation ID
	 * @param scriptRegistryItemType The category's item type.
	 * @param scriptRegistryItemID The categroy's ID.
	 */
	public static final ScriptCategory createCategory(PersistenceManager pm, ScriptCategory parent, String organisationID, 
			String scriptRegistryItemType, String scriptRegistryItemID)  
	{
		// initialise meta-data
		pm.getExtent(ScriptCategory.class);

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
	 * @param scriptSubDir This is the relative directory under the deploy base directory (e.g. "CrossTicketTrade.ear/script/Ticket")
	 * @param baseCategory All directories/files within the scriptSubDir will be created as sub-categories/scripts of this category.
	 * @param jfsm
	 * @param pm
	 * @param scriptRegistryItemType is the type (identifier) for the scripts in categories, categories get the scriptRegistryItemType from their parent 
	 * @param organisationID If you're writing a JFire Community Project, this is {@link Organisation#DEV_ORGANISATION_ID}.
	 */
	public ScriptingInitialiser(
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public void initialise() 
	throws ScriptingIntialiserException 
	{
		String j2eeBaseDir = jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File scriptDir = new File(j2eeBaseDir, scriptSubDir);

		if (!scriptDir.exists())
			throw new IllegalStateException("Script directory does not exist: " + scriptDir.getAbsolutePath());

		logger.debug("BEGIN initialization of Scripts");	

		// initialise meta-data
		pm.getExtent(ScriptParameter.class);
		pm.getExtent(ScriptParameterSet.class);
		pm.getExtent(ScriptRegistry.class);
		pm.getExtent(Script.class);
		pm.getExtent(ScriptCategory.class);

		createScriptCategories(scriptDir, baseCategory);
	}

	private Document getCategoryDescriptor(File categoryDir) 
	throws SAXException, IOException 
	{
		Document doc = categoryDescriptors.get(categoryDir);
		if (doc == null) {
			final File contentFile = new File(categoryDir, "content.xml");
			if (contentFile.exists()) { 
				DOMParser parser = new DOMParser();
				parser.setErrorHandler(new ErrorHandler(){
					public void error(SAXParseException exception) throws SAXException {
						logger.error("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void fatalError(SAXParseException exception) throws SAXException {
						logger.fatal("Parse ("+contentFile+"): ", exception);
						parseException = exception;
					}

					public void warning(SAXParseException exception) throws SAXException {
						logger.warn("Parse ("+contentFile+"): ", exception);
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
				doc = parser.getDocument();
				categoryDescriptors.put(categoryDir, doc);
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
			ScriptRegistryItem registryItem,
			Node parentNode
		) 
	throws TransformerException 
	{
		Node setNode = NLDOMUtil.findSingleNode(parentNode, "parameter-set");
		ScriptParameterSet parameterSet = registryItem.getParameterSet();
		if (setNode == null)
			return parameterSet;
		ScriptCategory parent = registryItem.getParent();
		if (parent != null && parent.getParameterSet() != null && parent.getParameterSet().equals(parameterSet))
			parameterSet = null;
		
		if (parameterSet == null) {
			parameterSet = new ScriptParameterSet(organisationID, IDGenerator.nextID(ScriptParameterSet.class));
			pm.makePersistent(parameterSet);
		}
		createElementName(setNode, parameterSet.getName(), "ParameterSet"+parameterSet.getScriptParameterSetID(), "name");
		Collection<Node> nodes = NLDOMUtil.findNodeList(parentNode, "parameter-set/parameter");
		parameterSet.removeAllParameters();
		for (Node node : nodes) {
			Node pIDNode = node.getAttributes().getNamedItem("name");
			if (pIDNode != null && !"".equals(pIDNode.getTextContent())) {
				Node pTypeNode = node.getAttributes().getNamedItem("type");
				if (pTypeNode != null && !"".equals(pTypeNode.getTextContent())) {
					parameterSet.createParameter(pIDNode.getTextContent()).setScriptParameterClassName(pTypeNode.getTextContent());
				}
				else
					logger.warn("parameter element of parameter-set has an invalid/missing type attribute");
			} else
				logger.warn("parameter element of parameter-set has an invalid/missing name attribute");
		}
		return parameterSet;
	}
	
	private void createElementName(Node elementNode, I18nText name, String def, String elementName) 
	{
		if (name == null) {
			logger.warn("createElementName called with null element!", new NullPointerException("name"));
			return;
		}		
		boolean nameSet = false;
		if (elementNode != null) {
			Collection<Node> nodes = NLDOMUtil.findNodeList(elementNode, elementName);
			for (Node node : nodes) {
				Node lIDNode = node.getAttributes().getNamedItem("language");
				if (lIDNode != null && !"".equals(lIDNode.getTextContent())) {
					name.setText(lIDNode.getTextContent(), node.getTextContent());
					nameSet = true;
				}
				else
					logger.warn("\" " + elementName + "\" element of node "+elementNode.getNodeName()+" has an invalid/missing language attribute");
			}
		}
		if (!nameSet && def != null)
			name.setText(Locale.ENGLISH.getLanguage(), def);
	}
	
	private void createScriptCategories(File dir, ScriptCategory parent) 
	throws ScriptingIntialiserException
	{
		try {
			String categoryID = dir.getName();
			String itemType = parent.getScriptRegistryItemType();
			Document catDocument = getCategoryDescriptor(dir);
			if (catDocument != null) {
				logger.debug("Have category-descriptor");
				Node catNode = NLDOMUtil.findSingleNode(catDocument, "script-category");
				if (catNode != null) {
					logger.debug("Have script-category element: "+catNode.getLocalName());
					NamedNodeMap attributes = catNode.getAttributes();
					if (attributes != null) {
						Node typeAttr = attributes.getNamedItem("type");
						if (typeAttr != null && !"".equals(typeAttr.getTextContent())) {
							logger.debug("Have type-attribute in script-category element: "+typeAttr.getTextContent());
							itemType = typeAttr.getTextContent();
						}

						Node idAttr = attributes.getNamedItem("id");
						if (idAttr != null && !"".equals(idAttr.getTextContent())) {
							logger.debug("Have id-attribute in script-category element: "+idAttr.getTextContent());
							categoryID = idAttr.getTextContent();
						}
					}
					else {
						logger.warn("Attributes NamedNodeMap of script-category element is null!!!");
					}
				}
			}

			// Create the category
			ScriptCategory category;
			try {
				pm.getExtent(ScriptCategory.class);
				ScriptRegistryItemID registryItemID = ScriptRegistryItemID.create(organisationID, itemType, categoryID); 
				Object cat = pm.getObjectById(registryItemID);
				if (!(cat instanceof ScriptCategory))
					throw new IllegalStateException("Found ScriptRegistryItem for id "+registryItemID+" but it is not an instance of ScriptCategory, it is "+cat.getClass().getName());
				category = (ScriptCategory)cat;
			} catch(JDOObjectNotFoundException e) {
				category = new ScriptCategory(parent, organisationID, itemType, categoryID);
				parent.addChild(category);
			}

			// category name and parameters
			Node catNode = null;
			if (catDocument != null) {
				catNode = NLDOMUtil.findSingleNode(catDocument, "script-category");
				if (catNode != null) {
					ScriptParameterSet parameterSet = createParameterSet(pm, organisationID, category, catNode);
					if (parameterSet != null)
						category.setParameterSet(parameterSet);
				}
			}
			createElementName(catNode, category.getName(), categoryID, "name");
			createElementName(catNode, category.getDescription(), null, "description");
			logger.debug("create Script Category = "+itemType + "/" + categoryID);
			

			// Create scripts
			File[] scripts = dir.listFiles(scriptFileNameFilter);
			for (int j=0; j<scripts.length; j++) {
				File scriptFile = scripts[j];

				Node scriptNode = getScriptDescriptor(scriptFile, catDocument);				
				
				String scriptID = Util.getFileNameWithoutExtension(scriptFile.getName());				
				String scriptItemType = scriptRegistryItemType;
				String scriptResultClass = "java.lang.Object";
//				String[] fetchGroups = new String[] {FetchPlan.DEFAULT};
				String[] fetchGroups = null;
				int maxFetchDepth = 1;
				boolean needsDetach = false;
				
				if (category != null) {
					if (category.getScriptRegistryItemType() != null)
						scriptItemType = category.getScriptRegistryItemType();
				}
				
				if (scriptNode != null) {
					logger.debug("Have script element");
					Node idNode = scriptNode.getAttributes().getNamedItem("id");
					if (idNode != null && !"".equals(idNode.getTextContent())) {
						logger.debug("Have id-attribute in script element: "+idNode.getTextContent());
						scriptID = idNode.getTextContent();
					}
					Node typeNode = scriptNode.getAttributes().getNamedItem("type");
					if (typeNode != null && !"".equals(typeNode.getTextContent())) {
						logger.debug("Have type-attribute in script element: "+idNode.getTextContent());
						scriptItemType = typeNode.getTextContent();
					}
					Node resultClassNode = scriptNode.getAttributes().getNamedItem("resultClass");
					if (resultClassNode != null && !"".equals(resultClassNode.getTextContent())) {
						logger.debug("Have resultClass-attribute in script element: "+idNode.getTextContent());
						scriptResultClass = resultClassNode.getTextContent();
					}
					Node maxFetchDepthNode = scriptNode.getAttributes().getNamedItem("maxFetchDepth");
					if (maxFetchDepthNode != null && !"".equals(maxFetchDepthNode.getTextContent())) {
						logger.debug("Have maxFetchDepthNode-attribute in script element: "+idNode.getTextContent());
						try {
							maxFetchDepth = Integer.parseInt(maxFetchDepthNode.getTextContent());
						} catch (NumberFormatException e) {
							maxFetchDepth = 1;
						}
					}
					Node fetchGroupsNode = scriptNode.getAttributes().getNamedItem("fetchGroups");
					if (fetchGroupsNode != null && !"".equals(fetchGroupsNode.getTextContent())) {
						logger.debug("Have fetchGroupsNode-attribute in script element: "+idNode.getTextContent());
						String fetchGroupsContent = fetchGroupsNode.getTextContent();
						fetchGroups = parseFetchGroups(fetchGroupsContent);
					}
					Node needsDetachNode = scriptNode.getAttributes().getNamedItem("needsDetach");
					if (needsDetachNode != null && !"".equals(needsDetachNode.getTextContent())) {
						logger.debug("Have needsDetachNode-attribute in script element: "+idNode.getTextContent());
						needsDetach = Boolean.parseBoolean(needsDetachNode.getTextContent());
					}					
				}
				
				try {			
					logger.debug("create Script = "+scriptRegistryItemType + "/" + scriptID);				
					String scriptContent = Util.readTextFile(scriptFile);
					logger.debug("scriptContent = " + scriptContent);
					Script script;
					try {
						pm.getExtent(Script.class);
						script = (Script) pm.getObjectById(ScriptRegistryItemID.create(
								organisationID,
								scriptItemType,
								scriptID)
						);
					} catch (JDOObjectNotFoundException e) {
						script = new Script(category, organisationID, scriptItemType, scriptID);
						category.addChild(script);
					}
					script.setText(scriptContent);
					script.setLanguage(getScriptRegistry().getLanguageByFileName(scriptFile.getName(), true));
					script.setResultClassName(scriptResultClass);
					script.setMaxFetchDepth(maxFetchDepth);
					script.setFetchGroups(fetchGroups);
					script.setNeedsDetach(needsDetach);
					
					// script name and parameters
					if (scriptNode != null) {
						ScriptParameterSet parameterSet = createParameterSet(pm, organisationID, script, scriptNode);						
						if (parameterSet != null)
							script.setParameterSet(parameterSet);
					}
					createElementName(scriptNode, script.getName(), scriptID, "name");
					createElementName(scriptNode, script.getDescription(), null, "description");
					
				} catch (Exception e) {
					logger.warn("could NOT create script "+scriptID+"!", e);
				}
			}

			
			// recurse
			File[] subDirs = dir.listFiles(dirFileFilter);		
			for (int i=0; i<subDirs.length; i++) {
				createScriptCategories(subDirs[i], category);
			}
		} catch (IOException e) {
			throw new ScriptingIntialiserException(e);
		} catch (TransformerException e) {
			throw new ScriptingIntialiserException(e);
		} catch (SAXException e) {
			throw new ScriptingIntialiserException(e);
		}
	}

	private String[] parseFetchGroups(String s) 
	{
		StringBuffer sb = new StringBuffer(s);
		List<String> fetchGroups = new LinkedList<String>();
		int firstIndex = 0;
		int index = sb.indexOf(",");		
		while(index != -1) {			
			String fetchGroup = sb.substring(firstIndex, index);
			fetchGroups.add(fetchGroup);
			firstIndex = index + 1;
			index = sb.indexOf(",");
		}
		if (logger.isDebugEnabled()) {
			for (String fetchGroup : fetchGroups) {
				logger.debug("fetchGroup = "+fetchGroup);
			}
		}			
		return fetchGroups.toArray(new String[fetchGroups.size()]);
	}
	
	protected FileFilter dirFileFilter = new FileFilter() {	
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}	
	};

	private FilenameFilter scriptFileNameFilter = new FilenameFilter()
	{	
		public boolean accept(File dir, String name) 
		{				
			String fileExtension = Util.getFileExtension(name);
			for (Iterator<String> it = getFileExtensions().iterator(); it.hasNext(); ) {
				String registeredFileExt = it.next();
				if (registeredFileExt.equals(fileExtension))
					return true;
			}
			return false;
		}	
	};
	
}

