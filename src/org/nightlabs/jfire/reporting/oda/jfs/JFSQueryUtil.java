/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.io.StringWriter;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFSQueryUtil {

	public static final String propertySetOpenTag = "<queryPropertySet>";
	public static final String propertySetCloseTag = "</queryPropertySet>";
	
	protected JFSQueryUtil() {}

	/**
	 * Creates the {@link JFSQueryPropertySet} from the query String of a
	 * JFS query.
	 * 
	 * @param query The query string.
	 * @return The {@link JFSQueryPropertySet} created from the given query String.
	 */
	public static JFSQueryPropertySet createPropertySetFromQueryString(String query) {
		if (query.startsWith(propertySetOpenTag)) {
			query = query.replaceAll(propertySetOpenTag, "");
			query = query.replaceAll(propertySetCloseTag, "");
			XStream xStream = new XStream(new XppDriver());
			Object obj = xStream.fromXML(query);
			if (!(obj instanceof JFSQueryPropertySet))
				throw new IllegalArgumentException("Query \""+query+"\" does not define a valid JFSQueryPropertySet.");
			return (JFSQueryPropertySet) obj;
		} else if (query.startsWith("jdo/")) {
			// try to create scriptID from query string -> backward compatibility			
			ObjectID idObject = ObjectIDUtil.createObjectID(query);
			if (!(idObject instanceof ScriptRegistryItemID))
				throw new IllegalArgumentException("The query string of this JFS DataSet does not define a valid JFSQueryPropertySet. It does refer to a JDO object but not to an ScriptRegistryItem. The query was "+query);
			ScriptRegistryItemID scriptID = (ScriptRegistryItemID)idObject;
			JFSQueryPropertySet propertySet = new JFSQueryPropertySet();
			propertySet.setScriptRegistryItemID(scriptID);
			return propertySet;
		} else
			return null;
	}

	/**
	 * Creates the query string of a JFS query with the given property set.
	 * @param queryPropertySet The property set that should be referenced in the query.
	 * @return The query string of a JFS query with the given property set.
	 */
	public static String createQueryStringFromPropertySet(JFSQueryPropertySet queryPropertySet) {
		XStream xStream = new XStream(new XppDriver());
		StringWriter writer = new StringWriter();
		writer.append(propertySetOpenTag);
		xStream.toXML(queryPropertySet, writer);
		writer.append(propertySetCloseTag);
		return writer.toString();
	}
}
