package com.csse3200.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.tools.texturepacker.TextureUnpacker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UnpackDroneAtlas {
	public static void main(String[] args) throws Exception {

		Path root = Paths.get("").toAbsolutePath();

		String atlasRel     = "core/assets/images/drone.atlas";
		String imagesRel    = "core/assets/images";
		String outputRel    = "core/assets/commodore64/raw/drone_unpacked";

		File atlasFile  = root.resolve(atlasRel).toFile();
		File imagesDir  = root.resolve(imagesRel).toFile();
		File outputDir  = root.resolve(outputRel).toFile();
		outputDir.mkdirs();

		// check
		System.out.println("Working dir = " + root);
		System.out.println("atlas exists? " + atlasFile.exists() + " -> " + atlasFile);
		System.out.println("images dir exists? " + imagesDir.exists() + " -> " + imagesDir);

		// 构造 TextureAtlasData 并拆包
		Lwjgl3Files files = new Lwjgl3Files();
		FileHandle pack    = files.absolute(atlasFile.getAbsolutePath());
		FileHandle imgDir  = files.absolute(imagesDir.getAbsolutePath());
		TextureAtlasData data = new TextureAtlasData(pack, imgDir, false);

		new TextureUnpacker().splitAtlas(data, outputDir.getAbsolutePath());

		System.out.println("✅ Unpacked to: " + outputDir.getAbsolutePath());
	}
}
