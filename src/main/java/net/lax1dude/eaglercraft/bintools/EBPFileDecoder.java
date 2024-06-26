package net.lax1dude.eaglercraft.bintools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import net.lax1dude.eaglercraft.bintools.utils.LabPBR2Eagler;

/**
 * Copyright (c) 2023-2024 lax1dude. All Rights Reserved.
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
public class EBPFileDecoder {

	private static int getFromBits(int idxx, int bits, byte[] bytes) {
		int startByte = idxx >> 3;
		int endByte = (idxx + bits - 1) >> 3;
		if(startByte == endByte) {
			return (((int)bytes[startByte] & 0xff) >> (8 - (idxx & 7) - bits)) & ((1 << bits) - 1);
		}else {
			return (((((int)bytes[startByte] & 0xff) << 8) | ((int)bytes[endByte] & 0xff)) >> (16 - (idxx & 7) - bits)) & ((1 << bits) - 1);
		}
	}

	public static void _main(String[] args) throws IOException {
		boolean labPBR = false;
		if(args.length > 1 && args[0].equalsIgnoreCase("--labPBR")) {
			labPBR = true;
			String[] e = new String[args.length - 1];
			System.arraycopy(args, 1, e, 0, e.length);
			args = e;
		}
		if(args.length > 1 && args.length < 4 && args[0].equalsIgnoreCase("-r")) {
			File input = new File(args[1]);
			if(!input.isDirectory()) {
				System.err.println("Error: Not a directory: " + input.getAbsolutePath());
				System.exit(-1);
				return;
			}
			convertDir(input, args.length == 3 ? new File(args[2]) : input, labPBR);
		}else if(args.length == 2) {
			System.out.println("Reading input file...");
			BufferedImage img;
			try(InputStream is = new FileInputStream(new File(args[0]))) {
				img = readEBP(is);
			}
			if(labPBR) {
				System.out.println("Converting from Eagler to LabPBR...");
				LabPBR2Eagler.convertEaglerToLabPBR(img, img);
			}
			File output = new File(args[1]);
			System.out.println("Writing PNG: " + output.getAbsolutePath());
			ImageIO.write(img, "PNG", output);
		}else {
			System.out.println("Usage: ebp-decode [--labPBR] <input file> <output file>");
			System.out.println("       ebp-decode [--labPBR] -r <directory> [output directory]");
		}
	}

	public static void convertDir(File inputDir, File outputDir) throws IOException {
		convertDir(inputDir, outputDir, false);
	}

	public static void convertDir(File inputDir, File outputDir, boolean labPBR) throws IOException {
		if(!outputDir.isDirectory() && !outputDir.mkdirs()) {
			throw new IOException("Could not create directory: " + outputDir.getAbsolutePath());
		}
		File[] f = inputDir.listFiles();
		for(int i = 0; i < f.length; ++i) {
			String name = f[i].getName();
			if(f[i].isDirectory()) {
				convertDir(f[i], new File(outputDir, name), labPBR);
				continue;
			}
			if(!name.toLowerCase().endsWith(".ebp")) {
				continue;
			}
			File ff = new File(outputDir, name.substring(0, name.length() - 3) + "png");
			System.out.println(f[i].getAbsolutePath());
			BufferedImage img;
			try(InputStream is = new FileInputStream(f[i])) {
				img = readEBP(is);
			}
			if(labPBR) {
				LabPBR2Eagler.convertEaglerToLabPBR(img, img);
			}
			ImageIO.write(img, "PNG", ff);
		}
	}

	public static BufferedImage readEBP(InputStream is) throws IOException {
		if(is.read() != '%' || is.read() != 'E' || is.read() != 'B' || is.read() != 'P') {
			throw new IOException("Not an EBP file!");
		}
		int v = is.read();
		if(v != 1) {
			throw new IOException("Unknown EBP version: " + v);
		}
		v = is.read();
		if(v != 3) {
			throw new IOException("Invalid component count: " + v);
		}
		int w = is.read() | (is.read() << 8);
		int h = is.read() | (is.read() << 8);
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		v = is.read();
		if(v == 0) {
			for(int i = 0, l = w * h; i < l; ++i) {
				img.setRGB(i % w, i / w, (is.read() << 16) | (is.read() << 8) | is.read() | 0xFF000000);
			}
		}else if(v == 1) {
			int paletteSize = is.read();
			int[] palette = new int[paletteSize + 1];
			palette[0] = 0xFF000000;
			for(int i = 0; i < paletteSize; ++i) {
				palette[i + 1] = (is.read() << 16) | (is.read() << 8) | is.read() | 0xFF000000;
			}
			int bpp = is.read();
			byte[] readSet = new byte[is.read() | (is.read() << 8) | (is.read() << 16)];
			is.read(readSet);
			for(int i = 0, l = w * h; i < l; ++i) {
				img.setRGB(i % w, i / w, palette[getFromBits(i * bpp, bpp, readSet)]);
			}
		}else {
			throw new IOException("Unknown EBP storage type: " + v);
		}
		if(is.read() != ':' || is.read() != '>') {
			throw new IOException("Invalid footer! (:>)");
		}
		return img;
	}
}
