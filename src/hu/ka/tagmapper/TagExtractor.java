package hu.ka.tagmapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class TagExtractor implements Callable<Map<String, Set<String>>> {

	private static Logger LOGGER = Logger.getLogger(TagExtractor.class.getName());
	private File filename;

	public TagExtractor(File filename) {
		this.filename = filename;
	}

	private Map<String, Set<String>> processFile() {
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec(getCommand(filename));
			proc.getOutputStream().close();
			InputStream is = proc.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}
			Set<String> keywords = parseKeywords(output.toString());
			reader.close();
			return Collections.singletonMap(filename.getAbsolutePath(), keywords);
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

	private Set<String> parseKeywords(String keywords) {
		LOGGER.fine(keywords);
		StringTokenizer scanner = new StringTokenizer(keywords.substring("Keywords:".length()), ";");
		Set<String> result = new HashSet<>();
		while (scanner.hasMoreTokens()) {
			String kw = scanner.nextToken().trim();
			LOGGER.fine(kw);
			result.add(kw);
		}
		return result;
	}

	private String getCommand(File filename) {
		return String.format("powershell \"%s\" -file \"%s\"" , getScriptPath(), filename.getAbsolutePath());
	}

	private String getScriptPath() {
		return Paths.get("").toAbsolutePath().toString() + "\\get-props.ps1";
	}

	@Override
	public Map<String, Set<String>> call() throws Exception {
		return processFile();
	}
}
