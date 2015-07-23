package com.antiaction.common.datastructures.flatfilelookup;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Read lines from a <code>RandomAccessFile</code> using a <code>ByteBuffer</code> to improve speed.
 * Also keeps track of last read line which can be made visible by calling the reset method.
 * Includes logic to seek around the file.
 *
 * @author nicl
 */
public class FlatfileReadLineByteBuffered {

	/** Default read line buffer size. */
	public static final int DEFAULT_READLINE_BYTEBUFFER_SIZE = 8192;

	/** <code>RandomAccessFile</code> to read from. */
	protected RandomAccessFile raf;

	/** File pointer of last read line. */
	protected long fileMark;

	/** Current file pointer to read next line from. */
	protected long filePointer;

	/** Byte array. */
	protected byte[] buffer;

	/** Byte array <code>ByteBuffer</code> wrapper. */
	protected ByteBuffer byteBuffer;

	/** Position of last read line in <code>ByteBuffer</code> byte array. */
	protected int oldPos;

	/** Current position in <code>ByteBuffer</code> byte array. */
	protected int pos;

	/**
	 * Initialise reader with default buffer size value.
	 */
	public FlatfileReadLineByteBuffered() {
		this(DEFAULT_READLINE_BYTEBUFFER_SIZE);
	}

	/**
	 * Initialise reader with specific buffer size value.
	 * @param bufsize buffer size
	 */
	public FlatfileReadLineByteBuffered(int bufsize) {
		this.buffer = new byte[DEFAULT_READLINE_BYTEBUFFER_SIZE];
		this.byteBuffer = ByteBuffer.wrap(buffer);
		this.byteBuffer.limit(0);
	}

	/**
	 * Set the <code>RandomAccessFile</code> to read from.
	 * @param raf <code>RandomAccessFile</code> to read from
	 */
	public void setRaf(RandomAccessFile raf) {
		this.raf = raf;
	}

	/**
	 * Move reader to a new position in the file.
	 * @param pos new position
	 * @throws IOException if an I/O exception occurs while seeking
	 */
	public void seek(long pos) throws IOException {
		raf.seek(pos);
		fileMark = pos;
		filePointer = pos;
		this.pos = 0;
		oldPos = 0;
		byteBuffer.limit(0);
	}

	/**
	 * Reset reader to re-read last read line.
	 */
	public void reset() {
		filePointer = fileMark;
		pos = oldPos;
	}

	/**
	 * Read next line, keeping info of previous line.
	 * @return next read line
	 * @throws IOException if an I/O exception occurs while reading line
	 */
	public String readLine() throws IOException {
		String line = null;
		fileMark = filePointer;
		oldPos = pos;
		byteBuffer.position(pos);
		int limit = byteBuffer.limit();
		int dtPos;
		int read;
		boolean bLoop = true;
		while (bLoop) {
			if (pos < limit) {
				if (buffer[pos++] == '\n') {
					if (pos > 0 && buffer[pos - 1] == '\r') {
						line = new String(buffer, oldPos, pos - oldPos - 2);
					}
					else {
						line = new String(buffer, oldPos, pos - oldPos - 1);
					}
					bLoop = false;
				}
			}
			else {
				dtPos = pos - oldPos;
				byteBuffer.compact();
				pos = byteBuffer.position();
				read = raf.read(buffer, pos, byteBuffer.remaining());
				if (read != -1) {
					byteBuffer.position(pos + read);
				}
				else {
					bLoop = false;
				}
				byteBuffer.flip();
				oldPos =  byteBuffer.position();
				pos = oldPos + dtPos;
				limit = byteBuffer.limit();
			}
		}
		filePointer += pos - oldPos;
		return line;
	}

}
