package com.antiaction.common.datastructures.flatfilelookup;

import java.io.File;
import java.io.IOException;

/**
 * Caching flat file binary search lookup. Caches n levels of the binary search lookup.
 *
 * @author nicl
 */
public class FlatfileLookupCaching extends FlatfileLookupAbstract {

	/** Cached root index node. */
	protected CacheNode root = new CacheNode(1);

	/**
	 * Initialise lookup class with default buffer size value.
	 * @param flatFile flat file to read from
	 */
	public FlatfileLookupCaching(File flatFile) {
		this(flatFile, DEFAULT_SQRN_BUFSIZE, FlatfileReadLineByteBuffered.DEFAULT_READLINE_BYTEBUFFER_SIZE);
	}

	/**
	 * Initialise lookup class with specific buffer size value.
	 * @param flatFile flat file to read from
	 * @param sqrNBufSize sqr(n) buffer size used to divide file into blocks
	 * @param readLineBufSize buffer size used to read lines
	 */
	public FlatfileLookupCaching(File flatFile, int sqrNBufSize, int readLineBufSize) {
		this.flatFile = flatFile;
		this.length = flatFile.length();
		this.lastModified = flatFile.lastModified();
		this.sqrNBufSize = sqrNBufSize;
		this.ffReadLine = new FlatfileReadLineByteBuffered(readLineBufSize);
		this.psComparator = new PrefixStringComparator();
	}

	/**
	 * An index node for caching lookups. The root node is always the block in the middle of the file.
	 * Level represents the depth of the caching tree.
	 */
	protected static class CacheNode {

		/** Cached index node of the block in the middle of the parents min and mid blocks. */
		protected CacheNode lt;

		/** Cached index node of the block in the middle of the parents mid and max blocks. */
		protected CacheNode gt;

		/** Depth level in the caching tree. */
		protected int level;

		/** First complete line read in the block. */
		protected String line;

		/*
		 * Create a new node with the specified level.
		 */
		public CacheNode(int level) {
			this.level = level;
		}

	}

	@Override
	public synchronized long lookup(String prefix) throws IOException {
		char[] prefixArr = prefix.toCharArray();
		long minBlk = 0;
		long maxBlk = length >> sqrNBufSize;
		long midBlk;
		String tmpStr;
		midBlk = (maxBlk - minBlk) >> 1;
		CacheNode node = root;
		while (maxBlk - minBlk > 1) {
			// debug
			//System.out.println(minBlk + " " + midBlk + " " + maxBlk);
			if (node != null) {
				if (node.line == null) {
					ffReadLine.seek(midBlk << sqrNBufSize);
					if (midBlk > 0) {
						ffReadLine.readLine();
					}
					tmpStr = ffReadLine.readLine();
					node.line = tmpStr;
				} else {
					tmpStr = node.line;
				}
			} else {
				ffReadLine.seek(midBlk << sqrNBufSize);
				if (midBlk > 0) {
					ffReadLine.readLine();
				}
				tmpStr = ffReadLine.readLine();
			}
			if (tmpStr != null) {
				switch (psComparator.comparePrefix(prefixArr, tmpStr.toCharArray())) {
				case 1:
					minBlk = midBlk;
					midBlk = minBlk + ((maxBlk - minBlk) >> 1);
					if (node != null) {
						if (node.gt == null && node.level < 16) {
							node.gt = new CacheNode(node.level + 1);
						}
						node = node.gt;
					}
					break;
				case 0:
					// debug
					//System.out.println("Semi bingo!");
					maxBlk = midBlk;
					midBlk = maxBlk - intSqrt(maxBlk - minBlk);
					node = null;
					break;
				case -1:
					maxBlk = midBlk;
					midBlk = minBlk + ((maxBlk - minBlk) >> 1);
					if (node != null) {
						if (node.lt == null && node.level < 16) {
							node.lt = new CacheNode(node.level + 1);
						}
						node = node.lt;
					}
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
