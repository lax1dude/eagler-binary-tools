# Eagler Binary Tools

### Open source tools for working with the various binary file formats invented for Eaglercraft (EPK, EBP, MDL, etc.)

### Previously unreleased source files for generating some official assets are included in this repository

**(Including the original FNAW skins in OBJ format)**

## How to Use

This is meant to be used from the command line. The minimum required Java version is 8 and it is recommended to add it to your PATH before continuing. Functions in this JAR file can also easily be called from other Java programs in order to program automation more easily.

Download [EaglerBinaryTools.jar](https://github.com/lax1dude/eagler-binary-tools/raw/main/EaglerBinaryTools.jar) into a folder and run it by typing `java -jar EaglerBinaryTools.jar` into command prompt in that folder. Here is the default help message:

```
Usage: java -jar EaglerBinaryTools.jar <epkcompiler|legacy-epkcompiler|
        epkdecompiler|obj2mdl-1.5|obj2mdl-1.8|ebp-encode|ebp-decode|
        skybox-gen|light-mesh-gen|eagler-bmp-gen> [args...]
 - 'epkcompiler': Compile an EPK file from a folder
 - 'legacy-epkcompiler': Compile an EPK file in legacy format
 - 'epkdecompiler': Decompile an EPK file into a folder
 - 'obj2mdl-1.5': Compile FNAW skin MDL file for 1.5
 - 'obj2mdl-1.8': Compile FNAW skin MDL file for 1.8
 - 'ebp-encode': Encode EBP file from PNG
 - 'ebp-decode': Decode EBP file to PNG
 - 'skybox-gen': Generate skybox.dat from OBJ for shader packs
 - 'light-mesh-gen': Generate light_point.dat from OBJ for shader packs
 - 'eagler-bmp-gen': Generate moon and lens flare BMP textures from PNG
    for shader packs
```

## 'epkcompiler': Compile an EPK file from a folder

This is the same EPKCompiler included in Eaglercraft 1.5 and EaglercraftX 1.8 except modified to be more verbose and not require JZLib to work.

The default compression type is `gzip` and the default file-type is `epk/resources`, eagler 1.5 worlds use compression `none` file-type `epk/world152` and eagler 1.8 worlds use compression `none` file-type `epk/world188`. This 'epkcompiler' prompt does not support Eaglercraft Beta 1.3, use 'legacy-epkcompiler' instead for that version.

```
Usage: epkcompiler <input directory> <output file> [gzip|zlib|none] [file-type]
```

## 'legacy-epkcompiler': Compile an EPK file in legacy format

This is the EPKCompiler used in Eaglercraft Beta 1.3 and older versions of Eaglercraft 1.5

Setting the compression type and headers is not part of the legacy format, the Eaglercraft Beta 1.3 client will load any EPK generated via this command regardless of its contents.

```
Usage: legacy-epkcompiler <input directory> <output file>
```

## 'epkdecompiler': Decompile an EPK file into a folder

Up to this point you've probably been using ayunWebEPK whenever you need to decompile an EPK file, Eagler Binary Tools now includes the EPK reader class from the Eaglercraft 1.5 client and allows you to decompile an EPK file in your command prompt. Both modern and legacy EPK files are supported, the file type is detected automatically.

```
Usage: epkdecompiler <input epk> <output folder>
```

## 'obj2mdl-1.5', 'obj2mdl-1.8': Compile FNAW skin MDL file

If you would like to make resource packs to replace the FNAW skins with your own 3D models you can use the 'obj2mdl' prompt to generate Eaglercraft MDL files from Wavefront OBJ files. Most of the models are broken up into multiple parts in order to make simple animations easier to program.

**The original FNAW skins are located in the "samples/obj2mdl-fnaw" folder on OBJ format**

```
Usage: obj2mdl-1.8 <input file> <output file> <texture mode>
Input file format is Wavefront OBJ file exported from your 3D modeling program
Texture mode can be 'true' or 'false' to enable/disable exporting texture UV coordinates
```

Due to some limitations, there are two different versions of this format for Eaglercraft 1.5 and EaglercraftX 1.8

If "texture mode" is `true`, the vertex position, normals, and UV coordinates will be exported with the mesh, use this for the body and arm parts of each model.

If "texture mode" is `false`, only the vertex position and normals will be exported, use this for the eye parts of each model.

The input mesh can only contain triangles, if you use Blender you can apply the "Triangulate" modifier to your mesh to tessellate all polygons with more than 3 sides into triangles. I believe there is also a checkbox in modern versions of Blender where you can do this automatically whenever you export the OBJ file.

There are limits on the number of unique vertices a model can have, and large models can take up lots of VRAM, so make sure you reduce the poly count of your models as much as possible first. Blender has a "Decimate" modifier for reducing poly count automatically by merging a certain percentage of the neighboring faces together.

Yes, you can use the Eaglercraft FNAW models to in your fan game. Please give credit to lax1dude though if possible.

## 'ebp-encode': Encode EBP file from PNG

Eagler Bitwise Packed is a lossless indexed raster image format designed for compressing textures with a limited number of colors, the format allows for less than 8 bits per pixel to be used to store textures which leads to massive savings when the texture is using indexed color and has only has a few unique colors. The encoded files are then compressed a second time by distributing them inside a compressed EPK file or a ZIP file. The format is meant to be used in EaglercraftX 1.8 for storing the default PBR material resource pack, so there is no alpha channel, just RGB.

**The default PBR material textures are located in the "samples/ebp-encode" folder in PNG format**

```
Usage: ebp-encode [--labPBR] <input file> <output file>");
       ebp-encode [--labPBR] -r <directory> [output directory]");
```

In the EaglercraftX 1.8 client, the EBP files for the default PBR resource pack are located in the "assets/eagler/glsl/deferred/assets_pbr" folder in the EPK.

**EBP compression will often be ineffective unless you manually use image editing software to posterize the texture to between 2 and 7 colors. The fewer colors the better.**

Do not make resource packs to replace these files because they will just be ignored, if you would like to make a PBR material resource pack for Eaglercraft you must use PNG files in the labPBR format and place them in "assets/minecraft/textures" instead. THE INCLUDED PNG FILES IN THIS REPOSITORY ARE NOT IN LabPBR FORMAT AND WILL NOT WORK FOR A RESOURCE PACK!

If you are trying to compile PNG material texture files in labPBR format to EBP you can pass the `--labPBR` flag as the first argument to convert from labPBR to Eagler's internal format automatically. **Use this if you're compiling a vanilla labPBR resource pack to EBP**

To obtain the default material texture pack in labPBR format, use `ebp-decode` on a copy of EaglercraftX 1.8's assets_pbr folder with the `--labPBR` option. This will allow you to correctly make a resource pack with the default PBR resource pack textures.

## 'ebp-decode': Decode EBP file to PNG

This works the same as 'ebp-encode' except it can be used to convert EBP files back to PNG files for debug/exploration.

```
Usage: ebp-decode [--labPBR] <input file> <output file>
       ebp-decode [--labPBR] -r <directory> [output directory]
```

If you use the `--labPBR` option, the EBP files will automatically be converted to PNG files in labPBR format that can be used to make a resource pack for EaglercraftX 1.8 or the vanilla Optifine.

## 'skybox-gen': Generate skybox.dat from OBJ for shader packs

EaglercraftX 1.8 clients include a file called "skybox.dat" in the deferred shaders folder. This file contains the special mesh used for the skybox used whenever shaders are enabled, along with a special lookup texture that is used when caching atmospheric scattering data. It's not as much of a "skybox" as it is a "skydome" in order to use per-vertex shading to interpolate between cached atmospheric scatteing values.

```
Usage: skybox-gen <top OBJ> <bottom OBJ> <output file>
Input file format is Wavefront OBJ file exported from your 3D modeling program
The top and bottom shapes should combine to form one seamless dome/sphere when
both are rendered in the same position. Make sure to flip the normals!
```

**The default skybox top and bottom are located in "samples/skybox-gen" in OBJ format**

To generate your own skybox, you must break the mesh into a top and bottom half, and flip the normals so they are facing inward instead of outward. The mesh can only contain triangles, same deal as the FNAW models.

Make sure the origin point of the mesh (0, 0, 0) is in the center where the player is meant to view the inside of the skybox from.

## 'light-mesh-gen': Generate light_point_mesh.dat from OBJ for shader packs

One of the tricks EaglercraftX 1.8 uses to speed up dynamic lighting in shaders is to draw a spherical mesh on the screen using a matrix that rescales it to only cover the pixels affected by the current dynamic light being rendered, and then only calculate lighting on those pixels. The mesh used for this trick is stored in a file called "light_point_mesh.dat".

```
Usage: light-mesh-gen <obj file> <output file> [mesh name]
Input file format is Wavefront OBJ file exported from your 3D modeling program
The only mesh name currently used by eagler is "light_point_mesh"
```

**The file used to generate "light_point_mesh.dat" is located in "samples/light-mesh-gen" in OBJ format**

The mesh can only contain triangles and the maximum number of unique vertices is limited.

## 'eagler-bmp-gen': Generate moon and lens flare BMP textures from PNG for shader packs

The files generated by this prompt are usually much larger than the input PNG file and are meant to be distributed inside a compressed EPK file or ZIP file. EPK, PNG, and ZIP files are all based on the same compression so it makes a lot of sense to not redundantly compress this file's contents multiple times if its gonna be distributed in a compressed archive anyway.

```
Usage: eagler-bmp-gen <--red|--rgba> <input file> <output file>
The texture's width and height must be a power of 2!
```

**The files required to generate lens_streaks.bmp, lens_ghosts.bmp, and eagler_moon.bmp are located in "samples/eagler-bmp-gen" in PNG format**

For lens_streaks.bmp and lens_ghosts.bmp, run the command with `--red` to generate grayscale Eagler BMP textures from the red channel of the PNG files.

For eagler_moon.bmp, run the command with `--rgba` to generate RGBA Eagler BMP files from the PNG files.

Mipmap levels will be generated automatically and embedded in the Eagler BMP file.

## Compiling and Contributing

The code in the `src/main/java` folder is written in plain Java. There are no dependencies besides what is already included by default in the JRE. Minimum required Java version is 8, just compile to JAR and you're ready to go.

**For a PR:** Tabs, not spaces, and format the code like the Eclipse auto format tool on factory settings.