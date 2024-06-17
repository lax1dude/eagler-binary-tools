package net.lax1dude.eaglercraft.bintools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

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
public class EPKCompilerLegacy {

	public static void _main(String[] args) throws IOException, NoSuchAlgorithmException {
		if (args.length != 2) {
			System.out.print("Usage: legacy-epkcompiler <input directory> <output file>");
			return;
		}
		File root = new File(args[0]);
		ArrayList<File> files = new ArrayList();
		System.out.println("Scanning input directory...");
		listDirectory(root, files);
		ByteArrayOutputStream osb = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(osb);
		String start = root.getAbsolutePath();
		File output = new File(args[1]);
		System.out.println("Compiling: " + output.getAbsolutePath());
		os.write("EAGPKG!!".getBytes(StandardCharsets.UTF_8));
		os.writeUTF(
				"\n\n #  eaglercraft package file - assets copyright mojang ab\n #  eagler eagler eagler eagler eagler eagler eagler\n\n");
		Deflater d = new Deflater(9);
		os = new DataOutputStream(new DeflaterOutputStream(osb, d));
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		for (File f : files) {
			os.writeUTF("<file>");
			String p = f.getAbsolutePath().replace(start, "").replace('\\', '/');
			if (p.startsWith("/"))
				p = p.substring(1);
			os.writeUTF(p);

			InputStream stream = new FileInputStream(f);
			byte[] targetArray = new byte[stream.available()];
			stream.read(targetArray);
			stream.close();

			os.write(md.digest(targetArray));
			os.writeInt(targetArray.length);
			os.write(targetArray);
			os.writeUTF("</file>");
		}
		os.writeUTF(" end");
		os.flush();
		os.close();

		System.out.println("Compiled " + files.size() + " files into the EPK");
		System.out.println("Writing to disk...");

		FileOutputStream out = new FileOutputStream(output);
		out.write(osb.toByteArray());
		out.close();
	}

	public static void listDirectory(File dir, ArrayList<File> files) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				listDirectory(f, files);
			} else {
				files.add(f);
			}
		}
	}

}
