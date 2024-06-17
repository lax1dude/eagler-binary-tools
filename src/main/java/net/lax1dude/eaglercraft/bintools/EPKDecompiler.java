package net.lax1dude.eaglercraft.bintools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.lax1dude.eaglercraft.bintools.utils.EPKDecompilerSP;

/**
 * Copyright (c) 2022-2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
public class EPKDecompiler {

	public static void _main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.print("Usage: epkdecompiler <input epk> <output folder>");
			return;
		}
		File input = new File(args[0]);
		if(!input.isFile()) {
			System.err.println("Input file does not exist!");
			return;
		}
		System.out.println("Decompiling: " + input.getAbsolutePath());
		File output = new File(args[1]);
		byte[] inputBytes = new byte[(int)input.length()];
		try(FileInputStream fis = new FileInputStream(input)) {
			fis.read(inputBytes);
		}
		EPKDecompilerSP epkDecompiler = new EPKDecompilerSP(inputBytes);
		if(epkDecompiler.isOld()) {
			System.out.println("Detected legacy EPK format!");
		}
		int filesWritten = 0;
		try {
			EPKDecompilerSP.FileEntry f = null;
			while((f = epkDecompiler.readFile()) != null) {
				if(f.type.equals("HEAD")) {
					System.out.println("Skipping HEAD: \"" + f.name + "\": \"" + (new String(f.data, StandardCharsets.US_ASCII)) + "\"");
				}else if(f.type.equals("FILE")) {
					String safeName = f.name.replace('\\', '/');
					if(safeName.startsWith("../") || safeName.contains("/../") || safeName.endsWith("/..") || safeName.equals("..")) {
						System.out.println("Skipping unsafe relative path: \"" + f.name + "\"");
					}else {
						File destFile = new File(output, safeName);
						File parent = destFile.getParentFile();
						if(!parent.isDirectory()) {
							if(!parent.mkdirs()) {
								throw new IOException("Could not create directory: " + parent.getAbsolutePath());
							}
						}
						try(FileOutputStream fos = new FileOutputStream(destFile)) {
							fos.write(f.data);
							++filesWritten;
						}
					}
				}else {
					System.err.println("Skipping unknown entry type \"" + f.type + "\" name \"" + f.name + "\", data is " + f.data.length + " bytes");
				}
			}
		}finally {
			epkDecompiler.close();
		}
		System.out.println("Extracted " + filesWritten + " from the EPK");
	}

}
