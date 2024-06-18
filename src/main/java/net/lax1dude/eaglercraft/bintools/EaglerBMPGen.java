package net.lax1dude.eaglercraft.bintools;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

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
public class EaglerBMPGen {
	
	public static void _main(String[] args) throws IOException {
		if(args.length != 3 || (!args[0].equalsIgnoreCase("--red") && !args[0].equalsIgnoreCase("--rgba"))) {
			System.out.println("Usage: eagler-bmp-gen <--red|--rgba> <input file> <output file>");
			System.out.println("The texture's width and height must be a power of 2!");
			return;
		}
		System.out.println("Reading input file...");
		BufferedImage img = ImageIO.read(new File(args[1]));
		File output = new File(args[2]);
		System.out.println("Encoding Eagler BMP: " + output.getAbsolutePath());
		int lvls;
		try(FileOutputStream fs = new FileOutputStream(output)) {
			ByteArrayOutputStream bao = new ByteArrayOutputStream(0x7FFF);
			lvls = encode(img, args[0].equalsIgnoreCase("--rgba"), bao);
			fs.write(bao.toByteArray());
		}
		System.out.println("Wrote " + lvls + " mipmap levels");
	}

	public static int encode(BufferedImage input, boolean rgba, OutputStream os) throws IOException {
		boolean invalid = false;
		if(!isPowOf2(input.getWidth())) {
			System.err.println("The image's width is not a power of 2!");
			invalid = true;
		}
		if(!isPowOf2(input.getHeight())) {
			System.err.println("The image's height is not a power of 2!");
			invalid = true;
		}
		if(invalid) {
			throw new IOException("The image has invalid dimensions!");
		}
		DataOutputStream dos = new DataOutputStream(os);
		int lvls = 0;
		while(true) {
			++lvls;
			dos.write('E');
			dos.writeShort(input.getWidth());
			dos.writeShort(input.getHeight());
			for(int y = 0; y < input.getHeight(); ++y) {
				for(int x = 0; x < input.getWidth(); ++x) {
					int c = input.getRGB(x, input.getHeight() - y - 1);
					dos.write((c >>> 16) & 0xFF);
					if(rgba) {
						dos.write((c >>> 8) & 0xFF);
						dos.write((c >>> 0) & 0xFF);
						dos.write((c >>> 24) & 0xFF);
					}
				}
			}
			if(input.getWidth() > 1 && input.getHeight() > 1) {
				BufferedImage img2 = new BufferedImage(input.getWidth() >> 1, input.getHeight() >> 1, BufferedImage.TYPE_INT_ARGB);
				Graphics g = img2.getGraphics();
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(input, 0, 0, img2.getWidth(), img2.getHeight(), 0, 0, input.getWidth(), input.getHeight(), null);
				g.dispose();
				input = img2;
			}else {
				dos.write(0);
				break;
			}
		}
		return lvls;
	}

	private static boolean isPowOf2(int x) {
		return (x & (x - 1)) == 0;
	}
}
