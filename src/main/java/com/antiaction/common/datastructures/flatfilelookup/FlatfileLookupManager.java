package com.antiaction.common.datastructures.flatfilelookup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for looking up URL-prefixes in multiple CDX-files.
 */
public class FlatfileLookupManager {

	private static Logger logger = Logger.getLogger(FlatfileLookupManager.class.getName());

	/** The singleton handling the CDXlookup. */
	private static FlatfileLookupManager cdxIndexManager;

	private static final FileComparator fileComparator = new FileComparator();

	private List<FlatfileLookupAbstract> flatfileLookupList = new LinkedList<FlatfileLookupAbstract>();

	private Map<String, FlatfileLookupAbstract> flatfileLookupMap = new HashMap<String, FlatfileLookupAbstract>();

	private FlatfileLookupAbstract[] lookup_arr;

	/**
	 * Constructor of the FlatfileLookupManager.
	 */
	private FlatfileLookupManager(String[] roots) {
		List<File> cdxes = findLookupFiles(roots);
		File file;
		FlatfileLookupAbstract flatfileLookup;
		for (int i = 0; i < cdxes.size(); ++i) {
			file = cdxes.get(i);
			flatfileLookup = new FlatfileLookupCaching(file);
			flatfileLookupList.add(flatfileLookup);
			flatfileLookupMap.put(file.getPath(), flatfileLookup);
			// debug
			System.out.println(cdxes.get(i).lastModified() + " - " + cdxes.get(i).getPath());
		}
		lookup_arr = flatfileLookupList.toArray(new FlatfileLookupAbstract[0]);
	}

	public static synchronized FlatfileLookupManager getInstance(String[] roots) {
		if (cdxIndexManager == null) {
			cdxIndexManager = new FlatfileLookupManager(roots);
		}
		return cdxIndexManager;
	}

	public List<String> lookup(String prefix) {
		FlatfileLookupAbstract lookupFile;
		String tmpStr;
		List<String> results = new LinkedList<String>();
		long millis;
		for (int i = 0; i < lookup_arr.length; ++i) {
			lookupFile = lookup_arr[i];
			//logger.log(Level.INFO, "Looking in " + lookupFile.flatFile.getName());
			millis = System.currentTimeMillis();
			if (lookupFile.lock()) {
				try {
					if (lookupFile.open()) {
						try {
							lookupFile.lookup(prefix);
							while ((tmpStr = lookupFile.readLine()) != null && tmpStr.startsWith(prefix)) {
								results.add(tmpStr);
							}
						}
						catch (IOException e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
						lookupFile.close();
					}
				}
				catch (FileNotFoundException e) {
				}
				lookupFile.unlock();
			}
			millis = System.currentTimeMillis() - millis;
			//logger.log(Level.INFO, "Lookup in " + lookupFile.flatFile.getPath() + " took " + millis + " ms.");
		}
		return results;
	}

	public static List<File> findLookupFiles(String[] roots) {
		List<File> lookupFilesList = new LinkedList<File>();
		Stack<File> stack = new Stack<File>();
		for (int i = 0; i < roots.length; ++i) {
			stack.add(new File(roots[i]));
		}
		long ctm = System.currentTimeMillis() - (60 * 60 * 1000);
		File file;
		File[] files;
		while (!stack.isEmpty()) {
			file = stack.pop();
			if (file.exists()) {
				if (file.isDirectory()) {
					files = file.listFiles();
					if (files != null) {
						for (int i = 0; i < files.length; ++i) {
							file = files[i];
							if (file.isDirectory()) {
								if (!file.getName().equalsIgnoreCase(".snapshot")) {
									stack.push(file);
								}
							} else {
								if (file.getName().endsWith(".cdx") || file.getName().startsWith("wayback.index")) {
									// Screw the aggregator.
									/*
									if (file.lastModified() < ctm) {
									}
									*/
									lookupFilesList.add(file);
								}
							}
						}
					}
				} else {
					if (file.getName().endsWith(".cdx") || file.getName().startsWith("wayback.index")) {
						// Screw the aggregator.
						/*
						if (file.lastModified() < ctm) {
						}
						*/
						lookupFilesList.add(file);
					}
				}
			}
		}
		Collections.sort(lookupFilesList, fileComparator);
		return lookupFilesList;
	}

	public static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File o1, File o2) {
			return Long.signum(o1.lastModified() - o2.lastModified());
		}
	}

}
