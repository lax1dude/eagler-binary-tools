package net.lax1dude.eaglercraft.bintools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
public class EBPFileEncoder {

	private static void setBit(int idx, boolean v, byte[] bytes) {
		int idx2 = idx >> 3;
		if(v) {
			bytes[idx2] |= (1 << (7 - (idx & 7)));
		}else {
			bytes[idx2] &= (-1 ^ (1 << (7 - (idx & 7))));
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
			BufferedImage img = ImageIO.read(new File(args[0]));
			File output = new File(args[1]);
			if(labPBR) {
				System.out.println("Converting from LabPBR to Eagler...");
				LabPBR2Eagler.convertLabPBRToEagler(img, img);
			}
			System.out.println("Encoding EBP: " + output.getAbsolutePath());
			try(OutputStream os = new FileOutputStream(output)) {
				writeEBP(img, os);
			}
		}else {
			System.out.println("Usage: ebp-encode [--labPBR] <input file> <output file>");
			System.out.println("       ebp-encode [--labPBR] -r <directory> [output directory]");
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
			if(!name.toLowerCase().endsWith(".png")) {
				continue;
			}
			File ff = new File(outputDir, name.substring(0, name.length() - 3) + "ebp");
			System.out.println(f[i].getAbsolutePath());
			BufferedImage img = ImageIO.read(f[i]);
			if(labPBR) {
				LabPBR2Eagler.convertLabPBRToEagler(img, img);
			}
			try(OutputStream os = new FileOutputStream(ff)) {
				writeEBP(img, os);
			}
		}
	}

	public static void writeEBP(BufferedImage img, OutputStream fos) throws IOException {
		fos.write(new byte[] { '%', 'E', 'B', 'P'});
		fos.write(1); // v1
		fos.write(3); // 3 component
		int w = img.getWidth();
		int h = img.getHeight();
		fos.write(w & 0xFF);
		fos.write((w >> 8) & 0xFF);
		fos.write(h & 0xFF);
		fos.write((h >> 8) & 0xFF);
		
		int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);
		
		Set<Integer> colorPalette = new HashSet();
		for(int i = 0; i < pixels.length; ++i) {
			if((pixels[i] & 0xFF000000) == 0) {
				pixels[i] = 0;
			}else {
				pixels[i] &= 0xFFFFFF;
			}
			if(pixels[i] == 0) {
				continue;
			}
			colorPalette.add(pixels[i]);
		}
		
		if(colorPalette.size() > 255) {
			fos.write(0); // type is no palette
			for(int i = 0; i < pixels.length; ++i) {
				fos.write((pixels[i] >> 16) & 0xFF);
				fos.write((pixels[i] >> 8) & 0xFF);
				fos.write(pixels[i] & 0xFF);
			}
		}else {
			Map<Integer,Integer> paletteHelper = new HashMap();
			fos.write(1); // type is palette
			fos.write(colorPalette.size()); // write palette size
			Iterator<Integer> paletteItr = colorPalette.iterator();
			int paletteIdx = 0;
			paletteHelper.put(0, 0);
			while(paletteItr.hasNext()) {
				int j = paletteItr.next().intValue();
				paletteHelper.put(j, ++paletteIdx);
				fos.write((j >> 16) & 0xFF);
				fos.write((j >> 8) & 0xFF);
				fos.write(j & 0xFF);
			}
			int bpp = 1;
			while((paletteIdx >> bpp) != 0) {
				++bpp;
			}
			fos.write(bpp); // write bpp
			int totalBits = pixels.length * bpp;
			byte[] completedBitSet = new byte[(totalBits & 7) == 0 ? (totalBits >> 3) : ((totalBits >> 3) + 1)];
			int bsi = 0;
			for(int i = 0; i < pixels.length; ++i) {
				int wr = paletteHelper.get(pixels[i]);
				for(int j = bpp - 1; j >= 0; --j) {
					setBit(bsi++, ((wr >> j) & 1) != 0, completedBitSet);
				}
			}
			fos.write(completedBitSet.length & 0xFF); // write expect length (as 24 bits not 32)
			fos.write((completedBitSet.length >> 8) & 0xFF);
			fos.write((completedBitSet.length >> 16) & 0xFF);
			fos.write(completedBitSet); // write the bits
		}
		fos.write(new byte[] { ':', '>' });
	}

}
