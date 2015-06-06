package hu.ka.tagmapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import hu.ka.tagmapper.util.StacktracePrinter;

public class TagMapper {

	private static Logger LOGGER = Logger.getLogger(TagMapper.class.getName());

	public void processPath(String path) {
		try {
			File fileOrDir = new File(path);
			Map<String, Set<String>> filesToKws;
			if (fileOrDir.isFile()) {
				filesToKws = new TagExtractor(fileOrDir).call();
			} else {
				filesToKws = processDir(fileOrDir);
			}
			LOGGER.info("Collected file->keyword mapping: " + filesToKws.toString());
			Map<String, Set<String>> kwToFiles = new HashMap<>();
			filesToKws.entrySet().forEach(e -> e.getValue().forEach(kw -> kwToFiles.merge(kw, new HashSet<>(Collections.singleton(e.getKey())), (a, b) ->
			{
				a.addAll(b);
				return a;
			})));
			LOGGER.info("Collected keyword->file mapping: " + kwToFiles.toString());
		} catch (Exception e) {
			LOGGER.severe(new StacktracePrinter(e));
		}
	}

	private Map<String, Set<String>> processDir(File fileOrDir) {
		DirectoryStream<Path> paths;
		final Map<String, Set<String>> fileToKw = new HashMap<>();
		try {
			paths = Files.newDirectoryStream(fileOrDir.toPath());
			paths.forEach(path -> {
				try {
					Map<String, Set<String>> kws;
					if (path.toFile().isFile()) {
						kws = new TagExtractor(path.toFile()).call();
					} else {
						kws = processDir(path.toFile());
					}
					kws.forEach((s, strings) ->
							fileToKw.merge(s, strings, (a, b) ->
							{
								a.addAll(b);
								return a;
							}));
				} catch (Exception e) {
					LOGGER.severe(new StacktracePrinter(e));
				}
			});
		} catch (Exception e) {
			LOGGER.severe(new StacktracePrinter(e));
		}
		return fileToKw;
	}

}
