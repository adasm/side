package com.softsquare.side;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Kernel {
	public static class FileSystem {
		
	}
	
	public static String[] tokens(String fileName) {
		FileHandle file = null;
		try { file = Gdx.files.getFileHandle(fileName, FileType.Local); }
		catch(GdxRuntimeException e) { return null; }
		if(!file.exists()) return null;
		return file.readString().split("[[:space:][:punct]=]+");
	}
}
