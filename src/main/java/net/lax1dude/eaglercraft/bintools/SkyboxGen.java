package net.lax1dude.eaglercraft.bintools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class SkyboxGen {

	public static void _main(String[] args) throws IOException {
		if(args.length != 3) {
			System.out.println("Usage: skybox-gen <top OBJ> <bottom OBJ> <output file>");
			System.out.println("Input file format is Wavefront OBJ file exported from your 3D modeling program");
			System.out.println("The top and bottom shapes should combine to form one seamless dome/sphere when");
			System.out.println("both are rendered in the same position. Make sure to flip the normals!");
			return;
		}
		System.out.println("Reading top half...");
		List<String> lnsTop = new ArrayList();
		try(BufferedReader bu = new BufferedReader(new FileReader(new File(args[0])))) {
			String s;
			while((s = bu.readLine()) != null) {
				lnsTop.add(s);
			}
		}
		System.out.println("Reading bottom half...");
		List<String> lnsBottom = new ArrayList();
		try(BufferedReader bu = new BufferedReader(new FileReader(new File(args[1])))) {
			String s;
			while((s = bu.readLine()) != null) {
				lnsBottom.add(s);
			}
		}
		File output = new File(args[2]);
		System.out.println("Exporting skybox.dat: " + output.getAbsolutePath());
		try(FileOutputStream fs = new FileOutputStream(output)) {
			convertModel(lnsTop, lnsBottom, fs);
		}
		System.out.println("Skybox export complete!");
	}

	public static void convertModel(Collection<String> linesTop, Collection<String> linesBottom, OutputStream out) throws IOException {
		
		List<float[]> vertexes = new ArrayList<float[]>();
		List<int[][]> faces = new ArrayList<int[][]>();
		List<byte[]> vboentriesTop = new ArrayList<byte[]>();
		List<float[]> vboentriesFTop = new ArrayList<float[]>();
		List<byte[]> indexablevboentries = new ArrayList<byte[]>();
		List<Integer> indexbuffer = new ArrayList<Integer>();
		for(String ul : linesTop) {
			String[] l = ul.split(" ");
			if(l[0].equals("v")) {
				vertexes.add(new float[] {Float.parseFloat(l[1]), Float.parseFloat(l[2]), Float.parseFloat(l[3])});
			}
			if(l[0].equals("f")) {
				if(l.length != 4) {
					OBJConverter.printTriangulationMessage();
					throw new IOException("Incompatible top model! (This can be fixed)");
				}
				String[] v1 = l[1].split("/");
				String[] v2 = l[2].split("/");
				String[] v3 = l[3].split("/");
				faces.add(new int[][] {
					{Integer.parseInt(v1[0]), Integer.parseInt(v1[1]), Integer.parseInt(v1[2])},
					{Integer.parseInt(v2[0]), Integer.parseInt(v2[1]), Integer.parseInt(v2[2])},
					{Integer.parseInt(v3[0]), Integer.parseInt(v3[1]), Integer.parseInt(v3[2])}
				});
			}
		}
		
		
		for(int[][] f : faces) {
			
			for(int i = 0; i < 3; i++) {
				byte[] b = new byte[6];
				
				float[] v = vertexes.get(f[i][0]-1);
				
				int ix = encodeHalfFloat(v[0]);
				int iy = encodeHalfFloat(v[1]);
				int iz = encodeHalfFloat(v[2]);
				
				int idx = 0;
				
				b[idx++] = (byte)(ix); b[idx++] = (byte)(ix >> 8);
				b[idx++] = (byte)(iy); b[idx++] = (byte)(iy >> 8);
				b[idx++] = (byte)(iz); b[idx++] = (byte)(iz >> 8);

				vboentriesTop.add(b);
				vboentriesFTop.add(v);
			}
		}
		
		vertexes.clear();
		faces.clear();
		List<byte[]> vboentriesBottom = new ArrayList<byte[]>();
		List<float[]> vboentriesFBottom = new ArrayList<float[]>();

		for(String ul : linesBottom) {
			String[] l = ul.split(" ");
			if(l[0].equals("v")) {
				vertexes.add(new float[] {Float.parseFloat(l[1]), Float.parseFloat(l[2]), Float.parseFloat(l[3])});
			}
			if(l[0].equals("f")) {
				if(l.length != 4) {
					OBJConverter.printTriangulationMessage();
					throw new IOException("Incompatible bottom model! (This can be fixed)");
				}
				String[] v1 = l[1].split("/");
				String[] v2 = l[2].split("/");
				String[] v3 = l[3].split("/");
				faces.add(new int[][] {
					{Integer.parseInt(v1[0]), Integer.parseInt(v1[1]), Integer.parseInt(v1[2])},
					{Integer.parseInt(v2[0]), Integer.parseInt(v2[1]), Integer.parseInt(v2[2])},
					{Integer.parseInt(v3[0]), Integer.parseInt(v3[1]), Integer.parseInt(v3[2])}
				});
			}
		}
		
		
		for(int[][] f : faces) {
			
			for(int i = 0; i < 3; i++) {
				byte[] b = new byte[6];
				
				float[] v = vertexes.get(f[i][0]-1);
				
				int ix = encodeHalfFloat(v[0]);
				int iy = encodeHalfFloat(v[1]);
				int iz = encodeHalfFloat(v[2]);
				
				int idx = 0;
				
				b[idx++] = (byte)(ix); b[idx++] = (byte)(ix >> 8);
				b[idx++] = (byte)(iy); b[idx++] = (byte)(iy >> 8);
				b[idx++] = (byte)(iz); b[idx++] = (byte)(iz >> 8);

				vboentriesBottom.add(b);
				vboentriesFBottom.add(v);
			}
		}
		
		int w = 32;
		int h = 16;
		int[] normalsLookupTexture = new int[w * h];
		int normalsId = 0;
		
		for(int j = 0; j < vboentriesTop.size(); ++j) {
			byte v[] = vboentriesTop.get(j);
			int l = indexablevboentries.size();
			boolean flag = true;
			e: for(int i = 0; i < l; i++) {
				byte[] bb = indexablevboentries.get(i);
				for(int k = 0; k < v.length; ++k) {
					if(bb[k] != v[k]) {
						continue e;
					}
				}
				indexbuffer.add(i);
				flag = false;
				break;
			}
			if(flag) {
				if(l > 65535) {
					throw new IOException("Too many vertices!");
				}
				indexbuffer.add(l);
				byte[] vboWithLUT = new byte[v.length + 2];
				vboWithLUT[v.length + 0] = (byte)(int)(((normalsId % w) + 0.5f) / (float)w * 255.0f);
				vboWithLUT[v.length + 1] = (byte)(int)(((normalsId / w) + 0.5f) / (float)h * 255.0f);
				System.arraycopy(v, 0, vboWithLUT, 0, v.length);
				
				float[] vv = vboentriesFTop.get(j);
				float xx = vv[0];
				float yy = vv[1];
				float zz = vv[2];
				float len = (float) Math.sqrt(xx * xx + yy * yy + zz * zz);
				xx /= len;
				yy /= len;
				zz /= len;
				
				normalsLookupTexture[normalsId] = 0xFF000000 | ((int)((xx + 1.0f) * 0.5f * 255.0f) << 16) | ((int)((yy + 1.0f) * 0.5f * 255.0f) << 8) | (int)((zz + 1.0f) * 0.5f * 255.0f);
				
				indexablevboentries.add(vboWithLUT);
				++normalsId;
			}
		}
		
		for(int j = 0; j < vboentriesBottom.size(); ++j) {
			byte v[] = vboentriesBottom.get(j);
			int l = indexablevboentries.size();
			boolean flag = true;
			e: for(int i = 0; i < l; i++) {
				byte[] bb = indexablevboentries.get(i);
				for(int k = 0; k < v.length; ++k) {
					if(bb[k] != v[k]) {
						continue e;
					}
				}
				indexbuffer.add(i);
				flag = false;
				break;
			}
			if(flag) {
				if(l > 65535) {
					throw new IOException("Too many vertices!");
				}
				indexbuffer.add(l);
				byte[] vboWithLUT = new byte[v.length + 2];
				vboWithLUT[v.length + 0] = (byte)(int)(((normalsId % w) + 0.5f) / (float)w * 255.0f);
				vboWithLUT[v.length + 1] = (byte)(int)(((normalsId / w) + 0.5f) / (float)h * 255.0f);
				System.arraycopy(v, 0, vboWithLUT, 0, v.length);
				
				float[] vv = vboentriesFBottom.get(j);
				float xx = vv[0];
				float yy = vv[1];
				float zz = vv[2];
				float len = (float) Math.sqrt(xx * xx + yy * yy + zz * zz);
				xx /= len;
				yy /= len;
				zz /= len;
				
				normalsLookupTexture[normalsId] = 0xFF000000 | ((int)((xx + 1.0f) * 0.5f * 255.0f) << 16) | ((int)((yy + 1.0f) * 0.5f * 255.0f) << 8) | (int)((zz + 1.0f) * 0.5f * 255.0f);
				
				indexablevboentries.add(vboWithLUT);
				++normalsId;
			}
		}
		

		DataOutputStream o = new DataOutputStream(out);
		o.write(0xEE);
		o.write(0xAA);
		o.write(0x66);
		o.write('%');
		o.write(6);
		o.write("skybox".getBytes(StandardCharsets.US_ASCII));
		o.writeShort(w);
		o.writeShort(h);
		for(int i = 0; i < normalsLookupTexture.length; ++i) {
			o.write((normalsLookupTexture[i] >> 16) & 0xFF);
			o.write((normalsLookupTexture[i] >> 8) & 0xFF);
			o.write(normalsLookupTexture[i] & 0xFF);
			o.write((normalsLookupTexture[i] >> 24) & 0xFF);
		}

		o.writeInt(0);
		o.writeInt(vboentriesTop.size());
		o.writeInt(vboentriesTop.size());
		o.writeInt(vboentriesBottom.size());
		
		o.writeInt(indexablevboentries.size());
		
		for(int i = 0; i < indexablevboentries.size(); ++i) {
			byte[] b = indexablevboentries.get(i);
			o.write(b, 0, b.length);
		}

		o.writeInt(indexbuffer.size());
		o.write(2); // 1 = byte, 2 = short, 4 = int
		
		for(int i : indexbuffer) {
			o.write(i & 0xFF);
			o.write((i >> 8) & 0xFF);
		}
		
		o.close();
	}


	//source: https://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java

	public static int encodeHalfFloat(float fval) {
		int fbits = Float.floatToIntBits(fval);
		int sign = fbits >>> 16 & 0x8000; // sign only
		int val = (fbits & 0x7fffffff) + 0x1000; // rounded value

		if (val >= 0x47800000) // might be or become NaN/Inf
		{ // avoid Inf due to rounding
			if ((fbits & 0x7fffffff) >= 0x47800000) { // is or must become NaN/Inf
				if (val < 0x7f800000) // was value but too large
					return sign | 0x7c00; // make it +/-Inf
				return sign | 0x7c00 | // remains +/-Inf or NaN
						(fbits & 0x007fffff) >>> 13; // keep NaN (and Inf) bits
			}
			return sign | 0x7bff; // unrounded not quite Inf
		}
		if (val >= 0x38800000) // remains normalized value
			return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
		if (val < 0x33000000) // too small for subnormal
			return sign; // becomes +/-0
		val = (fbits & 0x7fffffff) >>> 23; // tmp exp for subnormal calc
		return sign | ((fbits & 0x7fffff | 0x800000) // add subnormal bit
				+ (0x800000 >>> val - 102) // round depending on cut off
				>>> 126 - val); // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
	}
}
