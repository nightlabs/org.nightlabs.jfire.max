/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting.javaclass.articlecontainer;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.db.Record;
import org.nightlabs.db.TableBuffer;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.reporting.oda.SQLResultSet;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSScriptExecutorDelegate;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSResultUtil;
import org.nightlabs.jfire.reporting.trade.scripting.ReportingTradeScriptingUtil;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;

/**
 * Script that returns all {@link Article}s for a given {@link ArticleContainer}.
 * It accepts one parameter:
 * <ul>
 * <li>
 * 	<code>articleContainerID</code>: Defines the id of the specific {@link ArticleContainer} that should be loaded.
 * </li>
 * </ul>
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ArticleList extends AbstractJFSScriptExecutorDelegate {

	/**
	 * 
	 */
	public ArticleList() {
		super();
	}

	private JFSResultSetMetaData metaData;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()
	 */
	public IResultSetMetaData getResultSetMetaData() {
		if (metaData == null) {
			metaData = new JFSResultSetMetaData();
			ReportingTradeScriptingUtil.addDefaultArticleFieldsToMetaData(metaData);
			ReportingTradeScriptingUtil.addPriceFragmentListToMetaData(getPersistenceManager(), metaData);
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doExecute()
	 */
	public Object doExecute() throws ScriptException {
		ObjectID articleContainerID = (ObjectID) getParameterValue("articleContainerID");
		if (articleContainerID == null)
			throw new IllegalArgumentException("The parameter articleContainerID was not set.");
		
		PersistenceManager pm = getScriptExecutorJavaClass().getPersistenceManager();
		ArticleContainer articleContainer = (ArticleContainer) pm.getObjectById(articleContainerID);

		getResultSetMetaData();
		TableBuffer buffer = null;
		try {
			buffer = JFSResultUtil.createTableBuffer(metaData);
		} catch (Exception e) {
			throw new ScriptException(e);
		}
		SortedMap<Long, Article> sortedArticles = new TreeMap<Long, Article>();
		for (Article article : articleContainer.getArticles()) {
			sortedArticles.put(article.getArticleID(), article);
		}
		List<Object> row = new ArrayList<Object>();
		for (Article article : sortedArticles.values()) {
			row.clear();
			ReportingTradeScriptingUtil.addDefaultArticleFieldsToResultSet(article, row);
			ReportingTradeScriptingUtil.addPriceFragmentsToResultSet(getPersistenceManager(), article.getPrice(), row);
			try {
				buffer.addRecord(new Record(new ArrayList<Object>(row)));
			} catch (Exception e) {
				throw new ScriptException(e);
			}
		}
		SQLResultSet resultSet = new SQLResultSet(buffer);
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	public void doPrepare() throws ScriptException {
	}

}
