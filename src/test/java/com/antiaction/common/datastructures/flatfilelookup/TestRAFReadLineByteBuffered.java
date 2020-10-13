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

import com.antiaction.common.datastructures.flatfilelookup.RAFReadLineByteBuffered;

@RunWith(JUnit4.class)
public class TestRAFReadLineByteBuffered {

	@Test
	public void test_flatfilereadline() {
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

		String tmpStr;

		try {
			raf = new RandomAccessFile( indexFile, "r" );
			RAFReadLineByteBuffered ffReadLine = new RAFReadLineByteBuffered();
			ffReadLine.setRaf(raf);

			for ( int i=0; i<lines.size(); ++i ) {
				tmpStr = ffReadLine.readLine();
				// debug
				//System.out.println(tmpStr);
				Assert.assertEquals(lines.get(i), tmpStr);
				ffReadLine.reset();
				tmpStr = ffReadLine.readLine();
				// debug
				//System.out.println(tmpStr);
				Assert.assertEquals(lines.get(i), tmpStr);
			}

			tmpStr = ffReadLine.readLine();
			Assert.assertEquals(null, tmpStr);
			ffReadLine.reset();
			tmpStr = ffReadLine.readLine();
			Assert.assertEquals(null, tmpStr);

			raf.close();
		} catch (IOException e) {
			Assert.fail( "Unexpected exception!" );
		}

		try {
			raf = new RandomAccessFile( indexFile, "r" );
			RAFReadLineByteBuffered ffReadLine = new RAFReadLineByteBuffered();
			ffReadLine.setRaf(raf);

			for ( int i=0; i<lines.size(); ++i ) {
				ffReadLine.seek(i * 5);
				tmpStr = ffReadLine.readLine();
				// debug
				//System.out.println(tmpStr);
				Assert.assertEquals(lines.get(i), tmpStr);
				ffReadLine.reset();
				tmpStr = ffReadLine.readLine();
				// debug
				//System.out.println(tmpStr);
				Assert.assertEquals(lines.get(i), tmpStr);
			}

			tmpStr = ffReadLine.readLine();
			Assert.assertEquals(null, tmpStr);
			ffReadLine.reset();
			tmpStr = ffReadLine.readLine();
			Assert.assertEquals(null, tmpStr);

			raf.close();
		} catch (IOException e) {
			Assert.fail( "Unexpected exception!" );
		}
	}

}
