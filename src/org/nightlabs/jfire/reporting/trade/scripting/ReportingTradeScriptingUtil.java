/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragment;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.ResultSetMetaData;
import org.nightlabs.jfire.trade.Article;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportingTradeScriptingUtil {
	
	
	/**
	 * Returns all {@link PriceFragmentType}s (also those of other organisations)
	 * in a {@link List} sorted by their primary key.
	 *  
	 * @param pm The {@link PersistenceManager} used to resolve the {@link PriceFragmentType}s.
	 * @return All {@link PriceFragmentType}s in a sorted list.
	 */
	@SuppressWarnings("unchecked")
	public static List<PriceFragmentType> getSortedPriceFragmentTypes(PersistenceManager pm) {
		Query q = pm.newQuery(PriceFragmentType.class);
		List<PriceFragmentType> pfTypes = new ArrayList<PriceFragmentType>((List<PriceFragmentType>) q.execute());
			Collections.sort(pfTypes, new Comparator<PriceFragmentType>() {
			public int compare(PriceFragmentType o1, PriceFragmentType o2) {
				return o1.getPrimaryKey().compareTo(o2.getPrimaryKey());
			}
		});
		return pfTypes;
	}
	
	/**
	 * Adds all {@link PriceFragmentType}s as columns to the given {@link ResultSetMetaData}.
	 * 
	 * @param pm The {@link PersistenceManager} used to resolve the {@link PriceFragmentType}s.
	 * @param metaData The {@link ResultSetMetaData} that will be populated with new columns for the {@link PriceFragmentType}s.
	 */
	public static void addPriceFragmentListToMetaData(PersistenceManager pm, ResultSetMetaData metaData) {
		List<PriceFragmentType> pfTypes = getSortedPriceFragmentTypes(pm);
		for (PriceFragmentType type : pfTypes) {
			metaData.addColumn(fakeResultColumnName(type.getPrimaryKey()), DataType.DOUBLE);
		}
	}

	/**
	 * Adds the values of all {@link PriceFragmentType}s of the given {@link Price} as {@link Double} value to the given list.
	 * The types will be sorted according to {@link #getSortedPriceFragmentTypes(PersistenceManager)}.
	 * If there is no {@link PriceFragment} defined for a type in the price the value for that type will be <code>0</code>. 
	 * The values(price-amounts) are converted to {@link Double}s by {@link Currency#toDouble(long)}.
	 * 
	 * @param pm The {@link PersistenceManager} used to resolve the {@link PriceFragmentType}s.
	 * @param price The {@link Price} whose values will be added.
	 * @param cols The List where the values are stored.
	 */
	public static void addPriceFragmentsToResultSet(PersistenceManager pm, Price price, List<Object> cols) {
		List<PriceFragmentType> pfTypes = getSortedPriceFragmentTypes(pm);
		for (PriceFragmentType type : pfTypes) {
			PriceFragment fragment = price.getPriceFragment(type.getPrimaryKey(), false);
			if (fragment == null)
				cols.add(new Double(0));
			else
				cols.add(new Double(price.getCurrency().toDouble(fragment.getAmount())));
		}
	}
	
	/**
	 * Adds the default columns for an {@link Article} row to the given metaData.
	 * 
	 * @param metaData The {@link ResultSetMetaData} to add the columns to.
	 */
	public static void addDefaultArticleFieldsToMetaData(ResultSetMetaData metaData) {
		metaData.addColumn("articleJDOID", DataType.STRING);
		metaData.addColumn("articleID", DataType.BIGDECIMAL);
		metaData.addColumn("allocated", DataType.BOOLEAN);
		metaData.addColumn("allocationPending", DataType.BOOLEAN);
		metaData.addColumn("delivered", DataType.BOOLEAN);
		metaData.addColumn("deliveryJDOID", DataType.STRING);
		metaData.addColumn("deliveryNoteJDOID", DataType.STRING);
		metaData.addColumn("createDT", DataType.DATE);
		metaData.addColumn("createUserID", DataType.STRING);
		metaData.addColumn("createUserJDOID", DataType.STRING);
		metaData.addColumn("currencyID", DataType.STRING);
		metaData.addColumn("currencyJDOID", DataType.STRING);
		metaData.addColumn("invoiceJDOID", DataType.STRING);
		metaData.addColumn("offerJDOID", DataType.STRING);
		metaData.addColumn("orderJDOID", DataType.STRING);
		metaData.addColumn("productJDOID", DataType.STRING);
		metaData.addColumn("productTypeJDOID", DataType.STRING);
		metaData.addColumn("productTypeName", DataType.STRING);
		metaData.addColumn("receptionNoteJDOID", DataType.STRING);
		metaData.addColumn("reversed", DataType.BOOLEAN);
		metaData.addColumn("reversingArticleJDOID", DataType.STRING);
		metaData.addColumn("reversing", DataType.BOOLEAN);
		metaData.addColumn("reversedArticleJDOID", DataType.STRING);
		metaData.addColumn("tariffJDOID", DataType.STRING);
		metaData.addColumn("tariffName", DataType.STRING);
	}
	
	public static void addDefaultArticleFieldsToResultSet(Article article, List<Object> row) {
		row.add(JDOHelper.getObjectId(article).toString());
		row.add(new Long(article.getArticleID()));
		row.add(article.isAllocated());
		row.add(article.isAllocationPending());
		row.add(article.getArticleLocal().isDelivered());
		row.add(article.getArticleLocal().getDelivery() == null ? null : JDOHelper.getObjectId(article.getArticleLocal().getDelivery()).toString());
		row.add(article.getDeliveryNote() == null ? null : JDOHelper.getObjectId(article.getDeliveryNote()).toString());
		row.add(article.getCreateDT());
		row.add(article.getCreateUser().getUserID());
		row.add(JDOHelper.getObjectId(article.getCreateUser()).toString());		
		row.add(article.getCurrency().getCurrencyID());		
		row.add(JDOHelper.getObjectId(article.getCurrency()).toString());		
		row.add(article.getInvoice() == null ? null : JDOHelper.getObjectId(article.getInvoice()).toString());
		row.add(article.getOffer() == null ? null : JDOHelper.getObjectId(article.getOffer()).toString());
		row.add(article.getOrder() == null ? null : JDOHelper.getObjectId(article.getOrder()).toString());
		row.add(article.getProduct() == null ? null : JDOHelper.getObjectId(article.getProduct()).toString());
		row.add(JDOHelper.getObjectId(article.getProductType()).toString());
		row.add(article.getProductType().getName().getText());
		row.add(article.getReceptionNote() == null ? null : JDOHelper.getObjectId(article.getReceptionNote()).toString());
		row.add(article.isReversed());
		row.add(article.getReversingArticle() == null ? null : JDOHelper.getObjectId(article.getReversingArticle()).toString());
		row.add(article.isReversing());
		row.add(article.getReversedArticle() == null ? null : JDOHelper.getObjectId(article.getReversedArticle()).toString());
		row.add(article.getTariff() == null ? null : JDOHelper.getObjectId(article.getTariff()).toString());
		row.add(article.getTariff() == null ? null : article.getTariff().getName().getText());
	}
	
	/**
	 * Returns an instance of {@link Properties} with the content of the given
	 * Map where in the keys all "." are replaced with "_" and "_" with "__" 
	 * 
	 * @param mapProps The map to escape.
	 */
	private static String fakeResultColumnName(String columnName) {
		String key = columnName.replaceAll("_", "__");
		key = key.replaceAll("\\.", "_");
		return key;
	}
}
