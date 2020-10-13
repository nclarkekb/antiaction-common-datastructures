package com.antiaction.common.datastructures.flatfilelookup;

/**
 * Optimised binary char array prefix comparator.
 *
 * @author nicl
 */
public class PrefixStringComparator {

	/**
	 * Basic constructor.
	 */
	public PrefixStringComparator() {
	}

	/**
	 * 
	 * @param prefix
	 * @param str
	 * @return
	 */
	public int comparePrefix(char[] prefix, char[] str) {
		int res = 0;
		int limit;
		int c;
		if (prefix.length <= str.length) {
			limit = prefix.length;
		}
		else {
			limit = str.length;
		}
		boolean bLoop = true;
		int pos = 0;
		while (bLoop) {
			if (pos < limit) {
				c = prefix[pos] - str[pos];
				if (c == 0) {
					++pos;
				}
				else {
					res = Integer.signum(c);
					bLoop = false;
				}
			}
			else {
				if (limit < prefix.length) {
					res = 1;
				}
				bLoop = false;
			}
		}
		return res;
	}

}
