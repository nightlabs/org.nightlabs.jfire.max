/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.sql.SQLException;

import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.nightlabs.db.Column;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jfire.reporting.oda.DataType;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFSResultUtil {

	public static TableBuffer createTableBuffer(JFSResultSetMetaData metaData)
	throws JFireOdaException, SQLException
	{
		TableBuffer tableBuffer = new TableBuffer();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			Column col = new Column(
					metaData.getColumnName(i),
					DataType.dataTypeToSQLType(metaData.getColumnType(i)),
					metaData.getColumnDisplayLength(i),
					metaData.getScale(i),
					metaData.isNullable(i)
				);
			tableBuffer.addColumn(col, null);
		}
		return tableBuffer;
	}
}
