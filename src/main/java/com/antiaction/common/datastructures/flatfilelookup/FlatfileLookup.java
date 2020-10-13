package com.antiaction.common.datastructures.flatfilelookup;

import java.io.File;
import java.io.IOException;

/**
 * Simple flat file binary search lookup without any type of caching.
 *
 * @author nicl
 */
public class FlatfileLookup extends FlatfileLookupAbstract {

	/**
	 * Initialise lookup class with default buffer size value.
	 * @param flatFile flat file to read from
	 */
	public FlatfileLookup(File flatFile) {
		this(flatFile, DEFAULT_SQRN_BUFSIZE, RAFReadLineByteBuffered.DEFAULT_READLINE_BYTEBUFFER_SIZE);
	}

	/**
	 * Initialise lookup class with specific buffer size value.
	 * @param flatFile flat file to read from
	 * @param sqrNBufSize sqr(n) buffer size used to divide file into blocks
	 * @param readLineBufSize buffer size used to read lines
	 */
	public FlatfileLookup(File flatFile, int sqrNBufSize, int readLineBufSize) {
		this.flatFile = flatFile;
		this.length = flatFile.length();
		this.lastModified = flatFile.lastModified();
		this.sqrNBufSize = sqrNBufSize;
		this.ffReadLine = new RAFReadLineByteBuffered(readLineBufSize);
		this.psComparator = new PrefixStringComparator();
	}

	@Override
	public synchronized long lookup(String prefix) throws IOException {
		return lookup(prefix, ffReadLine);
	}

	@Override
	public long lookup(String prefix, RAFReadLineByteBuffered ffReadLine) throws IOException {
		char[] prefixArr = prefix.toCharArray();
		long minBlk = 0;
		long maxBlk = length >> sqrNBufSize;
		long midBlk;
		String tmpStr;
		midBlk = (maxBlk - minBlk) >> 1;
		while (maxBlk - minBlk > 1) {
			// debug
			//System.out.println(minBlk + " " + midBlk + " " + maxBlk);
			ffReadLine.seek(midBlk << sqrNBufSize);
			if (midBlk > 0) {
				ffReadLine.readLine();
			}
			tmpStr = ffReadLine.readLine();
			if (tmpStr != null) {
				switch (psComparator.comparePrefix(prefixArr, tmpStr.toCharArray())) {
				case 1:
					minBlk = midBlk;
					midBlk = minBlk + ((maxBlk - minBlk) >> 1);
					break;
				case 0:
					maxBlk = midBlk;
					midBlk = maxBlk - intSqrt(maxBlk - minBlk);
					break;
				case -1:
					maxBlk = midBlk;
					midBlk = minBlk + ((maxBlk - minBlk) >> 1);
					break;
				}
			}
			else {
				minBlk = midBlk;
				break;
			}
		}
		return search(minBlk << sqrNBufSize, prefixArr);
	}

}
