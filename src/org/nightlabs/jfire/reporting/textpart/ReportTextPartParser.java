/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.regex.Matcher;

import org.nightlabs.util.IOUtil;


/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ReportTextPartParser {

	private static String printWriter = "context.out";
	
	private static final char lt = '<';
	private static final char gt ='>';
	private static final char eq =  '=';
	private static final char qm = '?';
	private static final char qt = '"';
	private static final char lb = '\n';
	
	/** Greater then <  */
	private static final int EXPECT_GT = -2;
	/** Question mark ?  */
	private static final int EXPECT_QM = -3;
	/** Equals =  */
	private static final int EXPECT_EQ = -4;
	/** Normal  */
	private static final int NORMAL = 0;
	
	private String textPartContent;
	private StringBuffer buffer;
	private StringBuffer nextJS;
	private StringBuffer nextTxt;

	private boolean nextJSSingleReturn;
	
	/**
	 * 
	 */
	public ReportTextPartParser(String textPartContent) {
		this.textPartContent = textPartContent;
	}
	
	public synchronized void parse() throws IOException {
		StringReader reader = new StringReader(textPartContent);
		StreamTokenizer stk = new StreamTokenizer(reader);
		try {
			stk.resetSyntax();
			stk.wordChars(0, Integer.MAX_VALUE);
			stk.ordinaryChar(lt);
			stk.ordinaryChar(gt);
			stk.ordinaryChar(eq);
			stk.ordinaryChar(qm);
			stk.ordinaryChar(qt);
			stk.ordinaryChar(lb);
			boolean inString = false;
			boolean inJS = false;
			buffer = new StringBuffer();
			nextJS = new StringBuffer();
			nextTxt = new StringBuffer();
			
			int expectedToken = NORMAL;
			char previousOrdinary = 0;
			
			while (stk.nextToken() != StreamTokenizer.TT_EOF) {
				if (stk.ttype == StreamTokenizer.TT_EOL) {
					if (inJS) {						
						writeJS();
					}
					else {
						writeTxt();
					}					
				} else if (stk.ttype == StreamTokenizer.TT_WORD) {
					if (expectedToken < 0 && expectedToken != EXPECT_EQ) {
						// if we expect something but get a word
						// we add the last ordinary char
						// eq is excluded here as because that's only expected
						// when js-intializatinon finished, therefore if it does 
						// not follow right away we don't want to append the initialization
						if (inJS) {
							nextJS.append(previousOrdinary);
							nextJS.append(stk.sval);
						}
						else {
							nextTxt.append(previousOrdinary);
							nextTxt.append(stk.sval);
						}
						
					} else {
						// For a word where we expect normal we add
						// the token to the current type and reset
						// the buffers.
						if (inJS) {
							nextJS.append(stk.sval);
						} else {
							nextTxt.append(stk.sval);
						}
					}
					expectedToken = NORMAL;
				} else {
					// ordinary chars, meaning the separators for us <?>="
					if (inJS) {
						if (stk.ttype == qm && !inString) {
							expectedToken = EXPECT_GT;							
						} else if (stk.ttype == gt && expectedToken == EXPECT_GT) {
							writeJS();
							inJS = false;
							expectedToken = NORMAL;
						} else if (stk.ttype == qt) {
							nextJS.append(qt);
							inString = !inString;
						} else if (stk.ttype == eq && expectedToken == EXPECT_EQ) {
							expectedToken = NORMAL;
							nextJSSingleReturn = true; 
						} else {
							nextJS.append((char)stk.ttype);
							expectedToken = NORMAL;
						}
					}
					else {
						if (expectedToken == NORMAL && stk.ttype == lt) {
							expectedToken = EXPECT_QM;
						} else if (stk.ttype == EXPECT_QM && inJS) {
							expectedToken = EXPECT_GT;
						} else if (expectedToken == EXPECT_QM) {
							if (stk.ttype == qm) {
								inJS = true;
								writeTxt();
								expectedToken = EXPECT_EQ;
							} else {
								nextTxt.append(lt);
								nextTxt.append(stk.ttype);
								expectedToken = NORMAL;
							}
						} else {
							nextTxt.append((char)stk.ttype);
							expectedToken = NORMAL;
						}
					}
					previousOrdinary = (char) stk.ttype; 
				}
			}
			System.out.println(escapeEvalString(buffer.toString(), 1, true));
		} finally {
			reader.close();
		}
	}

	private void writeJS() {
		if (nextJSSingleReturn) {
			buffer.append(printWriter + ".print(" + nextJS.toString() + ");\n");
			nextJSSingleReturn = false;
		} else {
			buffer.append(nextJS.toString() + "\n");
		}
		nextJS.setLength(0);
	}
	
	private void writeTxt() {
		buffer.append(printWriter + ".print(\"" + nextTxt.toString() + "\");\n");
		nextTxt.setLength(0);
	}
	
	private static String escapeEvalString(String strToEval, int level, boolean convertStringLineBreaks) {
		String escaped = strToEval;
		for (int i = 0; i < level; i++) {
			escaped = escaped.replaceAll("\\\\", Matcher.quoteReplacement("\\\\"));
			escaped = escaped.replaceAll("\"", Matcher.quoteReplacement("\\\""));
		}
		if (convertStringLineBreaks) {
			escaped = escaped.replaceAll("\\n", Matcher.quoteReplacement("\" + \n\""));
		}
		return escaped;
	}

	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String js = IOUtil.readTextFile(new File("/home/alex/test.js"));
		ReportTextPartParser parser = new ReportTextPartParser(js);
		parser.parse();
	}
}
