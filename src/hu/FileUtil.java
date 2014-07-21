package hu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class FileUtil {

	public static String readFile(File file) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String l;
			while ((l = br.readLine()) != null) {
				sb.append(l).append("\n");
			}
		} catch (Exception e) {
			throw new RuntimeException("could not open file " + file, e);
		}
		return sb.toString();
	}
	
	public static void writeFile (File file, String text) {
		try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
			pw.println(text);
		} catch (Exception e) {
			throw new RuntimeException("could not open file " + file, e);
		}
	}
	
}
