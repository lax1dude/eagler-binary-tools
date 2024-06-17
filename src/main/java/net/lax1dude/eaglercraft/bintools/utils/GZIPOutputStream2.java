/*
 * Copyright (c) 1996, 2022, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package net.lax1dude.eaglercraft.bintools.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * GZIP output stream class that allows you to set the compression level
 */
public class GZIPOutputStream2 extends DeflaterOutputStream {

	protected CRC32 crc = new CRC32();

	private final static int GZIP_MAGIC = 0x8b1f;

	private final static int TRAILER_SIZE = 8;

	public GZIPOutputStream2(OutputStream out, int compressionLevel, int size, boolean syncFlush) throws IOException {
		super(out, new Deflater(compressionLevel, true), size, syncFlush);
		writeHeader();
		crc.reset();
	}

	public synchronized void write(byte[] buf, int off, int len) throws IOException {
		super.write(buf, off, len);
		crc.update(buf, off, len);
	}

	public void finish() throws IOException {
		if (!def.finished()) {
			try {
				def.finish();
				while (!def.finished()) {
					int len = def.deflate(buf, 0, buf.length);
					if (def.finished() && len <= buf.length - TRAILER_SIZE) {
						// last deflater buffer. Fit trailer at the end
						writeTrailer(buf, len);
						len = len + TRAILER_SIZE;
						out.write(buf, 0, len);
						return;
					}
					if (len > 0)
						out.write(buf, 0, len);
				}
				// if we can't fit the trailer at the end of the last
				// deflater buffer, we write it separately
				byte[] trailer = new byte[TRAILER_SIZE];
				writeTrailer(trailer, 0);
				out.write(trailer);
			} catch (IOException e) {
				def.end();
				throw e;
			}
		}
	}

	private void writeHeader() throws IOException {
		out.write(new byte[] { (byte) GZIP_MAGIC, // Magic number (short)
				(byte) (GZIP_MAGIC >> 8), // Magic number (short)
				Deflater.DEFLATED, // Compression method (CM)
				0, // Flags (FLG)
				0, // Modification time MTIME (int)
				0, // Modification time MTIME (int)
				0, // Modification time MTIME (int)
				0, // Modification time MTIME (int)
				0, // Extra flags (XFLG)
				0 // Operating system (OS)
		});
	}

	private void writeTrailer(byte[] buf, int offset) throws IOException {
		writeInt((int) crc.getValue(), buf, offset); // CRC-32 of uncompr. data
		writeInt(def.getTotalIn(), buf, offset + 4); // Number of uncompr. bytes
	}

	private void writeInt(int i, byte[] buf, int offset) throws IOException {
		writeShort(i & 0xffff, buf, offset);
		writeShort((i >> 16) & 0xffff, buf, offset + 2);
	}

	private void writeShort(int s, byte[] buf, int offset) throws IOException {
		buf[offset] = (byte) (s & 0xff);
		buf[offset + 1] = (byte) ((s >> 8) & 0xff);
	}
}
