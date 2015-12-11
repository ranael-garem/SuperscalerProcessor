package InputParse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BufferedReaderExample {
	public static ArrayList<String> returnContents() {
		BufferedReader br = null;
		ArrayList<String> content = new ArrayList<String>();
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("program3.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				content.add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return content;
	}

	public static void main(String[] args) {

		

	}
}
