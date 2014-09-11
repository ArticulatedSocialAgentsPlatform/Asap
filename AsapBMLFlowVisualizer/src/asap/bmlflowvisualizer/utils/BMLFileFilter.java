package asap.bmlflowvisualizer.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class BMLFileFilter extends FileFilter {

	public static final String bmlFileFormat = "bmli";

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String extension = getExtension(f.getName());
		if (extension != null) {
			if (extension.equals(bmlFileFormat)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {

		return "BML Information Files (bmlI)";
	}

	/*
	 * Get the extension of a file.
	 */
	public static String getExtension(String s) {
		String ext = null;

		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}
