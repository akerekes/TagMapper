package hu.ka.tagmapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class Main {

	private static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
		File fileOrDir = new File(args[0]);
		Map<String, Set<String>> filesToKws;
		if (fileOrDir.isFile()) {
			filesToKws = processFile(fileOrDir);
		} else {
			filesToKws = processDir(fileOrDir);
		}
		LOGGER.info("Collected file->keyword mapping: " + filesToKws.toString());
		Map<String, Set<String>> kwToFiles = new HashMap<>();
		filesToKws.entrySet().forEach(e -> e.getValue().forEach(kw -> kwToFiles.merge(kw, new HashSet<>(Collections.singleton(e.getKey())), (a,b) ->
		{a.addAll(b);
			return a;})));
		LOGGER.info("Collected keyword->file mapping: " + kwToFiles.toString());
	}

	private static Map<String, Set<String>> processDir(File fileOrDir) {
		DirectoryStream<Path> paths;
		final Map<String, Set<String>> fileToKw = new HashMap<>();
		try {
			paths = Files.newDirectoryStream(fileOrDir.toPath());
			paths.forEach(path -> {
				Map<String, Set<String>> kws;
				if (path.toFile().isFile()) {
					kws = processFile(path.toFile());
				} else {
					kws = processDir(path.toFile());
				}
				kws.forEach((s, strings) ->
						fileToKw.merge(s, strings, (a,b) ->
						{a.addAll(b);
							return a;}));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileToKw;
	}

	private static Map<String, Set<String>> processFile(File filename) {
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

	private static Set<String> parseKeywords(String keywords) {
		LOGGER.info(keywords);
		StringTokenizer scanner = new StringTokenizer(keywords.substring("Keywords:".length()), ";");
		Set<String> result = new HashSet<>();
		while (scanner.hasMoreTokens()) {
			String kw = scanner.nextToken().trim();
			LOGGER.info(kw);
			result.add(kw);
		}
		return result;
	}

	private static String getCommand(File filename) {
		return String.format("powershell \"%s\" -file \"%s\"" , getScriptPath(), filename.getAbsolutePath());
	}

	private static String getScriptPath() {
		return Paths.get("").toAbsolutePath().toString() + "\\get-props.ps1";
	}
}
