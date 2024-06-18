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

import net.lax1dude.eaglercraft.bintools.utils.IEEE754;

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
public class LightMeshGen {

	public static void _main(String[] args) throws IOException {
		if(args.length != 2 && args.length != 3) {
			System.out.println("Usage: light-mesh-gen <obj file> <output file> [mesh name]");
			System.out.println("Input file format is Wavefront OBJ file exported from your 3D modeling program");
			System.out.println("The only mesh name currently used by eagler is \"light_point_mesh\"");
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
		System.out.println("Exporting light mesh: " + output.getAbsolutePath());
		try(FileOutputStream fs = new FileOutputStream(output)) {
			convertModel(lns, args.length == 3 ? args[2] : "light_point_mesh", fs);
		}
		System.out.println("Light mesh export complete!");
	}

	public static void convertModel(Collection<String> lines, String name, OutputStream out) throws IOException {
		
		List<float[]> vertexes = new ArrayList<float[]>();
		List<int[][]> faces = new ArrayList<int[][]>();
		List<byte[]> vboentries = new ArrayList<byte[]>();
		List<float[]> vboentriesF = new ArrayList<float[]>();
		List<byte[]> indexablevboentries = new ArrayList<byte[]>();
		List<Integer> indexbuffer = new ArrayList<Integer>();
		for(String ul : lines) {
			String[] l = ul.split(" ");
			if(l[0].equals("v")) {
				vertexes.add(new float[] {Float.parseFloat(l[1]), Float.parseFloat(l[2]), Float.parseFloat(l[3])});
			}
			if(l[0].equals("f")) {
				if(l.length != 4) {
					OBJConverter.printTriangulationMessage();
					throw new IOException("Incompatible model! (This can be fixed)");
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
		
		int w = 32;
		int h = 16;
		int[] normalsLookupTexture = new int[w * h];
		int normalsId = 0;
		
		
		for(int[][] f : faces) {
			
			for(int i = 0; i < 3; i++) {
				byte[] b = new byte[6];
				
				float[] v = vertexes.get(f[i][0]-1);
				
				int ix = IEEE754.encodeHalfFloat(v[0]);
				int iy = IEEE754.encodeHalfFloat(v[1]);
				int iz = IEEE754.encodeHalfFloat(v[2]);
				
				int idx = 0;
				
				b[idx++] = (byte)(ix); b[idx++] = (byte)(ix >> 8);
				b[idx++] = (byte)(iy); b[idx++] = (byte)(iy >> 8);
				b[idx++] = (byte)(iz); b[idx++] = (byte)(iz >> 8);

				vboentries.add(b);
				vboentriesF.add(v);
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
				if(l > 255) {
					throw new UnsupportedOperationException("Too many vertices!");
				}
				indexbuffer.add(l);
				indexablevboentries.add(v);
			}
		}
		
		DataOutputStream o = new DataOutputStream(out);
		o.write(0xEE);
		o.write(0xAA);
		o.write(0x66);
		o.write('%');
		o.write(name.length());
		o.write(name.getBytes(StandardCharsets.US_ASCII));
		o.writeInt(indexablevboentries.size());
		for(int i = 0; i < indexablevboentries.size(); ++i) {
			byte[] b = indexablevboentries.get(i);
			o.write(b, 0, b.length);
		}

		o.writeInt(indexbuffer.size());
		o.write(1); // 1 = byte, 2 = short, 4 = int
		
		for(int i : indexbuffer) {
			o.write(i & 0xFF);
		}
		
		o.close();
	}

}
