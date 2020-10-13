package com.antiaction.common.datastructures;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple class to quietly close implementors of the <code>AutoCloseable</code> or SQL Array interfaces.
 *
 * @author nicl
 */
public class CloseUtils {

	/** Logger used to report exception. */
	private static final Logger logger = LoggerFactory.getLogger(CloseUtils.class);

	/**
	 * Attempt to close an object implementing the <code>AutoCloseable</code> interface and log exceptions thrown in the process.
	 * @param autoCloseable object implementing the <code>AutoCloseable</code> interface
	 */
	public static void closeQuietly(AutoCloseable autoCloseable){
		if (autoCloseable != null) {
			try {
				autoCloseable.close();
			}
			catch (Exception e) {
				logger.warn("Caught Exception when closing {}", autoCloseable, e);
			}
		}
	}

	/**
	 * Attempt to free an object implementing the SQL Array interface and log exceptions thrown in the process.
	 * @param sqlArr object implementing the SQL Array interface
	 */
	public static void freeQuietly(java.sql.Array sqlArr) {
		if (sqlArr != null) {
			try {
				sqlArr.free();
			}
			catch (SQLException e) {
				logger.warn("Caught SQLException when freeing {}", sqlArr, e);
			}
		}
	}

}
