package com.antiaction.common.datastructures.flatfilelookup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Semaphore;

/**
 * TODO javadoc and last modified.
 *
 * @author nicl
 */
public abstract class FlatfileLookupAbstract {

	/** Default 2^n binary tree block size. */
	protected static final int DEFAULT_SQRN_BUFSIZE = 13;

	/** Used 2^n binary tree block size. */
	protected int sqrNBufSize;

	/** Read line implementation used. */
	protected FlatfileReadLineByteBuffered ffReadLine;

	protected PrefixStringComparator psComparator;

	/** Flat file <code>File</code> object. */
	protected File flatFile;

	/** Flat file length. */
	protected long length;

	/** Flat file <code>RandomAccessFile</code> object. */
	protected RandomAccessFile raf;

	/** Last modified date of flat file. */
	protected long lastModified;

	/** Object instance lock. */
	protected Semaphore lock = new Semaphore(1);

	/**
	 * Lock this instance, so others can not use it.
	 * @return true, if the locking was successful
	 */
	public boolean lock() {
		boolean bAcquired = false;
		while (!bAcquired) {
			try {
				lock.acquire();
				bAcquired = true;
			}
			catch (InterruptedException e) {
			}
		}
		return bAcquired;
	}

	/**
	 * Try to open the flat file, suppress file not found exception for open lock problems but throw it if the file does not exist.
	 * @return true, if the opening was successful
	 * @throws FileNotFoundException if the flat file does not exist
	 */
	public boolean open() throws FileNotFoundException {
		if (raf == null) {
			if (flatFile.exists() && flatFile.isFile()) {
				try {
					raf = new RandomAccessFile(flatFile, "r");
					ffReadLine.setRaf(raf);
				}
				catch (FileNotFoundException e) {
				}
			}
			else {
				throw new FileNotFoundException();
			}
		}
		return raf != null;
	}

	/**
	 * Close <code>RandomAccessFile</code>.
	 */
	public void close() {
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
			}
			raf = null;
			ffReadLine.setRaf(raf);
		}
	}

	/**
	 * Unlock the flat file for use by another RandomAccessFile.
	 */
	public void unlock() {
		close();
		lock.release();
	}

	/**
	 * Calculates the approximate numeric square root of a long.
	 * @param value long value to calculate square root of
	 * @return the numeric square root value
	 */
	public static long intSqrt(long value) {
		long q = 1;
		while (q <= value) {
			q <<= 2;
		}
		q >>= 2;
		long d = q;
		long remainder = value - q;
		long root = 1;
		while (q > 1) {
			root <<= 1;
			q >>= 2;
			long s = q + d;
			d >>= 1;
			if (s <= remainder) {
				remainder -= s;
				root++;
				d += q;
			}
		}
		return root;
	}

	/**
	 * Binary search flat file for prefix.
	 * @param prefix prefix to search for
	 * @return file pointer where the lookup last searched
	 * @throws IOException if an I/O exception occurs while looking up
	 */
	public abstract long lookup(String prefix) throws IOException;

	/**
	 * Read the next line at the current file pointer.
	 * @return next line at the current file pointer
	 * @throws IOException if an I/O exception occurs while reading tne next line
	 */
	public String readLine() throws IOException {
		return ffReadLine.readLine();
	}

	/**
	 * 
	 * @param offset file pointer to start reading lines from
	 * @param prefixArr prefix to search forward for
	 * @return offset where the search found the first match or EOF
	 * @throws IOException if an I/O exception occurs while searching
	 */
	public long search(long offset, char[] prefixArr) throws IOException {
		// debug
		//System.out.println(offset + " - " + prefix);
		String tmpStr;
		ffReadLine.seek(offset);
		if (offset > 0) {
			ffReadLine.readLine();
		}
		while (true) {
			offset = ffReadLine.filePointer;
			tmpStr = ffReadLine.readLine();
			if (tmpStr == null) {
				break;
			}
			if (psComparator.comparePrefix(prefixArr, tmpStr.toCharArray()) <= 0) {
				break;
			}
		}
		ffReadLine.reset();
		return offset;
	}

}
