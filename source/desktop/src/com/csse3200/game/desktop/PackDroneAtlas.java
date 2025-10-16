package com.csse3200.game.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PackDroneAtlas {
	public static void main(String[] args) {
		Path root = Paths.get("").toAbsolutePath();

		String inputDir  = root.resolve("source/core/assets/commodore64/raw/boss").toString(); //newest png pack
		String outputDir = root.resolve("source/core/assets/images").toString();
		String packName  = "boss";

		TexturePacker.Settings s = new TexturePacker.Settings();
		// Automatically crop transparent edges
		s.stripWhitespaceX = true;
		s.stripWhitespaceY = true;

		// Other recommended settings (avoid bleeding, reasonable size)
		s.duplicatePadding = true; // Copy edge pixels for padding to reduce sampling bleeding
		s.edgePadding      = true;
		s.paddingX         = 2;
		s.paddingY         = 2;
		s.maxWidth         = 16384;
		s.maxHeight        = 16384;
		s.stripWhitespaceX = false;  // 不裁剪X方向空白
		s.stripWhitespaceY = false;  // 不裁剪Y方向空白
		s.flattenPaths     = true; // Ignore subdirectory names and write them all to atlas

		// repack
		// TexturePacker.processIfModified(s, inputDir, outputDir, packName);
		TexturePacker.process(s, inputDir, outputDir, packName);

		System.out.println("Packed -> " + outputDir + "/" + packName + ".atlas");
	}
}
