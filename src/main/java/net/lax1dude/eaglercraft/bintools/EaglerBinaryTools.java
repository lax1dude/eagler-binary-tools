package net.lax1dude.eaglercraft.bintools;

/**
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
public class EaglerBinaryTools {

	public static void main(String[] args) throws Throwable {
		if(args.length < 1) {
			usage();
			return;
		}
		String[] argz = new String[args.length - 1];
		System.arraycopy(args, 1, argz, 0, argz.length);
		switch(args[0].toLowerCase()) {
		case "epkcompiler":
			EPKCompiler._main(argz);
			return;
		case "legacy-epkcompiler":
		case "legacyepkcompiler":
			EPKCompilerLegacy._main(argz);
			return;
		case "epkdecompiler":
			EPKDecompiler._main(argz);
			return;
		case "obj2mdl-1.5":
		case "obj2mdl1.5":
			return;
		case "obj2mdl-1.8":
		case "obj2mdl1.8":
			return;
		case "ebp-encode":
		case "ebpencode":
			return;
		case "ebp-decode":
		case "ebpdecode":
			return;
		case "skybox-gen":
		case "skyboxgen":
			return;
		case "eagler-moon-gen":
		case "eaglermoongen":
			return;
		case "lens-flare-gen":
		case "lensflaregen":
			return;
		default:
			usage();
			return;
		}
	}

	private static void usage() {
		System.out.println("Usage: java -jar EaglerBinaryTools.jar <epkcompiler|legacy-epkcompiler|epkdecompiler|obj2mdl-1.5|obj2mdl-1.8|ebp-encode|ebp-decode|skybox-gen|eagler-moon-gen|lens-flare-gen> [args...]");
		System.out.println(" - 'epkcompiler': Compile an EPK file from a folder");
		System.out.println(" - 'legacy-epkcompiler': Compile an EPK file in legacy format");
		System.out.println(" - 'epkdecompiler': Decompile an EPK file into a folder");
		System.out.println(" - 'obj2mdl-1.5': Compile FNAW skin MDL file for 1.5");
		System.out.println(" - 'obj2mdl-1.8': Compile FNAW skin MDL file for 1.8");
		System.out.println(" - 'ebp-encode': Encode EBP file from PNG");
		System.out.println(" - 'ebp-decode': Decode EBP file to PNG");
		System.out.println(" - 'skybox-gen': Generate skybox.dat from OBJ for shader packs");
		System.out.println(" - 'eagler-moon-gen': Generate eagler_moon.bmp from PNG for shader packs");
		System.out.println(" - 'lens-flare-gen': Generate lens_streaks.bmp, lens_ghosts.bmp from PNG for shader packs");
	}

}
