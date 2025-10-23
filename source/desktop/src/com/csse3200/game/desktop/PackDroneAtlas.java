package com.csse3200.game.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PackDroneAtlas {
	static Logger logger = LoggerFactory.getLogger(PackDroneAtlas.class);
	public static void main(String[] args) {
		Path root = Paths.get("").toAbsolutePath();
        logger.info("Working dir = {}", root);


		String inputDir  = root.resolve("core/assets/commodore64/raw/drone_chaser").toString(); //newest png pack
		String outputDir = root.resolve("core/assets/images").toString();
		String packName  = "drone_scout";

		TexturePacker.Settings s = new TexturePacker.Settings();
		// Automatically crop transparent edges
		s.stripWhitespaceX = true;
		s.stripWhitespaceY = true;

		// Other recommended settings (avoid bleeding, reasonable size)
		s.duplicatePadding = true; // Copy edge pixels for padding to reduce sampling bleeding
		s.edgePadding      = true;
		s.paddingX         = 2;
		s.paddingY         = 2;
		s.maxWidth         = 2048;
		s.maxHeight        = 2048;
		s.stripWhitespaceX = false;
		s.stripWhitespaceY = false;
		s.flattenPaths     = true; // Ignore subdirectory names and write them all to atlas
		s.scale = new float[]{0.5f}; // scale to 1/2

		// repack
		TexturePacker.process(s, inputDir, outputDir, packName);

        logger.info("Packed -> {}/{}.atlas", outputDir, packName);
	}
}
