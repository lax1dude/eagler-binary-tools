/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
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

package net.lax1dude.eaglercraft.bintools.utils;

import java.awt.image.BufferedImage;

public class LabPBR2Eagler {

	/**
	 * See
	 * net.lax1dude.eaglercraft.v1_8.opengl.ext.deferred.texture.PBRTextureMapUtils
	 */
	public static void convertLabPBRToEaglerLegacy(BufferedImage input, BufferedImage output) {
		for (int w = input.getWidth(), x = 0; x < w; ++x) {
			for (int h = input.getHeight(), y = 0; y < h; ++y) {
				int pixel = input.getRGB(x, y);
				int a = (pixel >>> 24) & 0xFF;
				if (a == 0xFF)
					a = 0;
				output.setRGB(x, y, (pixel & 0x00FFFF00) | Math.min(a << 2, 0xFF) | 0xFF000000);
			}
		}
	}

	public static void convertLabPBRToEagler(BufferedImage input, BufferedImage output) {
		for (int w = input.getWidth(), x = 0; x < w; ++x) {
			for (int h = input.getHeight(), y = 0; y < h; ++y) {
				int pixel = input.getRGB(x, y);
				int a = (pixel >>> 24) & 0xFF;
				if (a == 0xFF)
					a = 0;
				int b = ((pixel & 0xFF) - 65) * 255 / 190;
				if (b < 0)
					b = 0;
				output.setRGB(x, y, (pixel & 0x00FFFF00) | Math.min(a << 2, 0xFF) | ((255 - b) << 24));
			}
		}
	}

	public static void convertEaglerToLabPBR(BufferedImage input, BufferedImage output) {
		for (int w = input.getWidth(), x = 0; x < w; ++x) {
			for (int h = input.getHeight(), y = 0; y < h; ++y) {
				int pixel = input.getRGB(x, y);
				int a = (pixel >>> 2) & 0x3F;
				if (a == 0)
					a = 0xFF;
				int b = 255 - (pixel >>> 24);
				if (b > 0)
					b = (b * 190 / 255) + 65;
				output.setRGB(x, y, (pixel & 0x00FFFF00) | (a << 24) | b);
			}
		}
	}

}
