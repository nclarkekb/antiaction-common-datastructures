package com.antiaction.common.datastructures.flatfilelookup;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.antiaction.common.datastructures.flatfilelookup.FlatfileLookupAbstract;
import com.antiaction.common.datastructures.flatfilelookup.FlatfileLookupCaching;

@RunWith(JUnit4.class)
public class TestFlatfileLookupCaching {

	@Test
	public void test_flatfilelookup() {
		byte[] ascii = "abcdefghijklmnopqrstuvwxyz".getBytes();
		byte[] line = new byte[ 5 ];
		line[ 4 ] = '\n';

		File testResources = TestHelpers.getTestResourceFile( "" );

		File indexFile = new File( testResources, "indexfile" );
		if ( indexFile.exists() ) {
			indexFile.delete();
		}

		RandomAccessFile raf;

		List<String> lines = new ArrayList<String>();

		List<String> subStrings = new ArrayList<String>();

		try {
			raf = new RandomAccessFile( indexFile, "rw" );
			int b1 = 0;
			int b2 = 0;
			int b3 = 0;
			int b4 = 0;
			while ( b1 < 26 ) {
				line[ 0 ] = ascii[ b1 ];
				line[ 1 ] = ascii[ b2 ];
				line[ 2 ] = ascii[ b3 ];
				line[ 3 ] = ascii[ b4 ];
				raf.write( line );
				lines.add( new String( line, 0, 4 ));
				if (b3 == 0 && b4 == 0) {
					subStrings.add(new String(line, 0, 2));
				}
				++b4;
				if ( b4 == 26 ) {
					b4 = 0;
					++b3;
					if ( b3 == 26 ) {
						b3 = 0;
						++b2;
						if ( b2 == 26 ) {
							b2 = 0;
							++b1;
						}
					}
				}
			}
			raf.close();
		}
		catch (IOException e) {
			Assert.fail( "Unexpected exception!" );
		}

		FlatfileLookupAbstract ffl = new FlatfileLookupCaching( indexFile, 4, 16 );
		boolean bOpened;
		long index;
		String tmpStr;

		try {
			ffl.lock();
			bOpened = ffl.open();
			Assert.assertEquals(true, bOpened);
			for ( int i=0; i<lines.size(); ++i ) {
				index = ffl.lookup( lines.get( i ) );
				Assert.assertEquals(i * 5, index);
				// debug
				//System.out.println( i + " - " + index );
				tmpStr = ffl.readLine();
				Assert.assertEquals(lines.get( i ), tmpStr);
			}
			ffl.close();
			ffl.unlock();
		}
		catch (IOException e) {
			Assert.fail( "Unexpected exception!" );
		}

		int matches;
		boolean bLoop;

		try {
			ffl.lock();
			bOpened = ffl.open();
			Assert.assertEquals(true, bOpened);
			for ( int i=0; i<subStrings.size(); ++i ) {
				index = ffl.lookup( subStrings.get( i ) );
				Assert.assertEquals(i * 5 * 26 * 26, index);
				// debug
				//System.out.println( i + " - " + subStrings.get( i ) + " - " + index );
				matches = 0;
				bLoop = true;
				while (bLoop) {
					tmpStr = ffl.readLine();
					if (tmpStr != null && tmpStr.startsWith( subStrings.get( i ) )) {
						++matches;
					}
					else {
						bLoop = false;
					}
				}
				Assert.assertEquals(26 * 26, matches);
			}
			ffl.close();
			ffl.unlock();
		}
		catch (IOException e) {
			Assert.fail( "Unexpected exception!" );
		}
	}

}
