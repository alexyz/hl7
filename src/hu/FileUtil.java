package hu;

import java.io.*;

public class FileUtil {
	
	/** read file with lines separated by LF */
	public static String readFile (File file) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String l;
			while ((l = br.readLine()) != null) {
				System.out.println("l=" + l);
				sb.append(l).append("\n");
			}
		} catch (Exception e) {
			throw new RuntimeException("could not open file " + file, e);
		}
		return sb.toString();
	}
	
	/** write file with no interpretation of line terminator */
	public static void writeFile (File file, String text) {
		try (FileWriter fw = new FileWriter(file)) {
			fw.write(text.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException("could not open file " + file, e);
		}
	}
	
}
