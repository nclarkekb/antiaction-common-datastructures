package com.antiaction.common.datastructures.flatfilelookup;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.antiaction.common.datastructures.flatfilelookup.PrefixStringComparator;

@RunWith(JUnit4.class)
public class TestPrefixStringComparator {

	@Test
	public void test_substringcomparator() {
		//System.out.println("aa".compareTo("aaaaa"));
		//System.out.println("aaaaa".compareTo("aa"));

		PrefixStringComparator psComparator = new PrefixStringComparator();

		//System.out.println(psComparator.compareSubStringTo("aa".toCharArray(), "aa".toCharArray()));
		//System.out.println(psComparator.compareSubStringTo("aa".toCharArray(), "aaaa".toCharArray()));
		//System.out.println(psComparator.compareSubStringTo("aaaa".toCharArray(), "aa".toCharArray()));
		//System.out.println(psComparator.compareSubStringTo("aa".toCharArray(), "bb".toCharArray()));
		//System.out.println(psComparator.compareSubStringTo("bb".toCharArray(), "aa".toCharArray()));
		//System.out.println(psComparator.compareSubStringTo("aaaa".toCharArray(), "bb".toCharArray()));
		//System.out.println(psComparator.compareSubStringTo("bbbb".toCharArray(), "aa".toCharArray()));

		Assert.assertEquals(0, psComparator.comparePrefix("aa".toCharArray(), "aa".toCharArray()));
		Assert.assertEquals(0, psComparator.comparePrefix("aa".toCharArray(), "aaaa".toCharArray()));
		Assert.assertEquals(1, psComparator.comparePrefix("aaaa".toCharArray(), "aa".toCharArray()));
		Assert.assertEquals(-1, psComparator.comparePrefix("aa".toCharArray(), "bb".toCharArray()));
		Assert.assertEquals(1, psComparator.comparePrefix("bb".toCharArray(), "aa".toCharArray()));
		Assert.assertEquals(-1, psComparator.comparePrefix("aaaa".toCharArray(), "bb".toCharArray()));
		Assert.assertEquals(1, psComparator.comparePrefix("bbbb".toCharArray(), "aa".toCharArray()));
	}

}
