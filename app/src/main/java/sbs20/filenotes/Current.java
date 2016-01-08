package sbs20.filenotes;

import java.io.File;

public class Current {
	private static File file;
	
	public static void setFile(File file) {
		Current.file = file;
	}
	
	public static File getFile() {
		return Current.file;
	}
}
