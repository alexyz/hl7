package hu;

import java.io.*;

public class FileUtil {
	
	/** read file with lines separated by LF */
	public static String readFile (File file) throws Exception {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String l;
			while ((l = br.readLine()) != null) {
				sb.append(l).append("\n");
			}
		}
		return sb.toString();
	}
	
	/** write file with no interpretation of line terminator */
	public static void writeFile (File file, String text) throws Exception {
		try (FileWriter fw = new FileWriter(file)) {
			fw.write(text.toCharArray());
		}
	}
	
}
