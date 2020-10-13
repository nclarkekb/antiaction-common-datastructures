package com.antiaction.common.datastructures.indexedtextfile;

import java.io.IOException;

public interface Pageable {

	public long getTextFilesize();

	public long getIndexFilesize();

	public long getLastIndexedTextPosition();

	public long getIndexedTextLines();

	public byte[] readPage(long page, long itemsPerPage, boolean descending) throws IOException;

}
