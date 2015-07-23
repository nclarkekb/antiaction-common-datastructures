# antiaction-common-datastructures
Library of common data structures implemented in Java.

Temporary project until...
* I do a proper extraction of the flatfile code including history.
* I convert my other data structure code from SVN to Git.

## Overview

### Flatfile lookups
* FlatfileLookup.java
* FlatfileLookupAbstract.java
* FlatfileLookupCaching.java
* FlatfileLookupManager.java

Caching and non caching lookup implementations that extends the same base class.

Lookup manager for managing multiple lookup files. (Improvable)

### Optimized RandomAccessFile readLine implementation

* FlatfileReadLineByteBuffered.java

Uses a sliding ByteBuffer.

### Prefix string comparison

* PrefixStringComparator.java

Using string compare just seems wrong.
Optimized prefix char array comparator.
