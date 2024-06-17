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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
public class OBJConverter {

	public static void _main(String[] args, boolean v1_8) throws IOException {
		if(args.length != 3) {
			System.out.println("Usage: obj2mdl-" + (v1_8 ? "1.8" : "1.5") + " <input file> <output file> <texture mode>");
			System.out.println("Input file format is Wavefront OBJ file exported from your 3D modeling program");
			System.out.println("Texture mode can be 'true' or 'false' to enable/disable exporting texture UV coordinates");
			return;
		}
		System.out.println("Reading input file...");
		List<String> lns = new ArrayList();
		try(BufferedReader bu = new BufferedReader(new FileReader(new File(args[0])))) {
			String s;
			while((s = bu.readLine()) != null) {
				lns.add(s);
			}
		}
		File output = new File(args[1]);
		System.out.println("Exporting MDL: " + output.getAbsolutePath());
		boolean tex = args[2].equalsIgnoreCase("true") || args[2].equals("1");
		try(FileOutputStream fs = new FileOutputStream(output)) {
			convertModel(lns, fs, tex, v1_8);
		}
		if(tex) {
			System.out.println("Export with UVs completed!");
		}else {
			System.out.println("Export without UVs competed!");
		}
	}
	
	public static void convertModel(Collection<String> lines, OutputStream out, boolean textureMode, boolean v1_8) throws IOException {
		List<float[]> vertexes = new ArrayList<float[]>();
		List<byte[]> normals = new ArrayList<byte[]>();
		List<float[]> texcoords = new ArrayList<float[]>();
		List<int[][]> faces = new ArrayList<int[][]>();
		List<byte[]> vboentries = new ArrayList<byte[]>();
		List<byte[]> indexablevboentries = new ArrayList<byte[]>();
		List<Integer> indexbuffer = new ArrayList<Integer>();
		for(String ul : lines) {
			String[] l = ul.split(" ");
			if(l[0].equals("v")) {
				vertexes.add(new float[] {Float.parseFloat(l[1]), Float.parseFloat(l[2]), Float.parseFloat(l[3])});
			}
			if(l[0].equals("vn")) {
				int dumb = v1_8 ? 0 : 127;
				normals.add(new byte[] {(byte)((int)(Float.parseFloat(l[1]) * 127.0F) + dumb), (byte)((int)(Float.parseFloat(l[2]) * 127.0F) + dumb), (byte)((int)(Float.parseFloat(l[3]) * 127.0F) + dumb), (byte)0});
			}
			if(textureMode) {
				if(l[0].equals("vt")) {
					texcoords.add(new float[] {Float.parseFloat(l[1]), 1.0f - Float.parseFloat(l[2])});
				}
			}
			if(l[0].equals("f")) {
				if(l.length != 4) {
					printTriangulationMessage();
					System.exit(-1);
					return;
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
				byte[] b = new byte[textureMode ? 24 : 16];
				
				float[] v = vertexes.get(f[i][0]-1);
				
				int ix = Float.floatToRawIntBits(v[0]);
				int iy = Float.floatToRawIntBits(v[1]);
				int iz = Float.floatToRawIntBits(v[2]);
				
				int idx = 0;
				
				b[idx++] = (byte)(ix); b[idx++] = (byte)(ix >> 8); b[idx++] = (byte)(ix >> 16); b[idx++] = (byte)(ix >> 24);
				b[idx++] = (byte)(iy); b[idx++] = (byte)(iy >> 8); b[idx++] = (byte)(iy >> 16); b[idx++] = (byte)(iy >> 24);
				b[idx++] = (byte)(iz); b[idx++] = (byte)(iz >> 8); b[idx++] = (byte)(iz >> 16); b[idx++] = (byte)(iz >> 24);
				
				byte[] n = normals.get(f[i][2]-1);
				
				b[idx++] = n[0];
				b[idx++] = n[1];
				b[idx++] = n[2];
				b[idx++] = n[3];
				
				if(textureMode) {
					float[] t = texcoords.get(f[i][1]-1);
					int ix3 = Float.floatToRawIntBits(t[0]);
					int iy3 = Float.floatToRawIntBits(t[1]);
					
					b[idx++] = (byte)(ix3); b[idx++] = (byte)(ix3 >> 8); b[idx++] = (byte)(ix3 >> 16); b[idx++] = (byte)(ix3 >> 24);
					b[idx++] = (byte)(iy3); b[idx++] = (byte)(iy3 >> 8); b[idx++] = (byte)(iy3 >> 16); b[idx++] = (byte)(iy3 >> 24);
				}
				
				vboentries.add(b);
			}
		}
		
		for(int j = 0; j < vboentries.size(); ++j) {
			byte v[] = vboentries.get(j);
			int l = indexablevboentries.size();
			boolean flag = true;
			for(int i = 0; i < l; i++) {
				if(Arrays.equals(v, indexablevboentries.get(i))) {
					indexbuffer.add(i);
					flag = false;
					break;
				}
			}
			if(flag) {
				indexbuffer.add(l);
				indexablevboentries.add(v);
			}
		}
		

		DataOutputStream o = new DataOutputStream(out);
		o.write((v1_8 ? "!EAG$mdl" : "!EAG%mdl").getBytes(StandardCharsets.US_ASCII));
		o.write(textureMode ? (byte)'T' : (byte)'C');
		
		o.writeUTF("\n\nthis file was generated with EaglerBinaryTools\n\n");
		
		o.writeInt(indexablevboentries.size());
		o.writeInt(indexbuffer.size());
		for(int i = 0; i < indexablevboentries.size(); ++i) {
			byte[] b = indexablevboentries.get(i);
			o.write(b, 0, b.length);
		}
		for(int i : indexbuffer) {
			o.write((byte)i);
			o.write((byte)(i >> 8));
		}
		o.write("_:>+".getBytes(StandardCharsets.US_ASCII));
		
		o.close();
	}

	private static void printTriangulationMessage() {
		System.err.println("=====================================");
		System.err.println("THIS OBJ FILE IS NOT COMPATIBLE WITH EAGLERCRAFT!");
		System.err.println();
		System.err.println("Eaglercraft meshes can only have triangles in them.");
		System.err.println();
		System.err.println("If you're using Blender, add the \"triangulate\"");
		System.err.println("modifier to the mesh before exporting to fix");
		System.err.println("=====================================");
	}
}
