package hu.ka.tagmapper.map;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import hu.ka.tagmapper.extract.TagExtractor;
import hu.ka.tagmapper.util.StacktracePrinter;

public class TagMapper {

	private static final int TIMEOUT_VALUE = 30;
	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
	private static Logger LOGGER = Logger.getLogger(TagMapper.class.getName());

	private static class ExecutionContext<V> {
		ExecutorCompletionService<V> executor = new ExecutorCompletionService<>(Executors.newWorkStealingPool());
		List<Future<V>> futures = new LinkedList<>();
		void submit(Callable<V> callable) {
			futures.add(executor.submit(callable));
		}
	}

	public TagMapping processPath(String path) {
		Map<String, Set<String>> filesToTags = new TreeMap<>();
		Map<String, Set<String>> tagsToFiles = new TreeMap<>();
		try {
			File fileOrDir = new File(path);
			if (fileOrDir.isFile()) {
				filesToTags.putAll(new TagExtractor(fileOrDir).call());
			} else {
				ExecutionContext executionContext = processDir(fileOrDir, new ExecutionContext<>());
				filesToTags.putAll(collectTags(executionContext));
			}
			LOGGER.info("Collected file->keyword mapping: " + filesToTags.toString());
			filesToTags.entrySet().forEach(e -> e.getValue().forEach(kw -> tagsToFiles.merge(kw, new TreeSet<>(Collections.singleton(e.getKey())), (a, b) ->
			{
				a.addAll(b);
				return a;
			})));
			LOGGER.info("Collected keyword->file mapping: " + tagsToFiles.toString());
		} catch (Exception e) {
			LOGGER.severe(new StacktracePrinter(e));
		}
		return new TagMapping(filesToTags, tagsToFiles);
	}

	private Map<String, Set<String>> collectTags(ExecutionContext<Map<String, Set<String>>> executionContext) {
		Map<String, Set<String>> result = new HashMap<>();
		try {
			for (int i = 0; i < executionContext.futures.size(); i++) {

				Future<Map<String, Set<String>>> future = executionContext.executor.poll(TIMEOUT_VALUE, TIMEOUT_UNIT);
				if (future != null) {
					if (future.isDone()) {
						try {
							result.putAll(future.get());
						} catch (ExecutionException e) {
							LOGGER.severe(new StacktracePrinter(e));
						}
					} else {
						LOGGER.warning("Computation was not completed successfully, status: " + (future.isCancelled() ? "cancelled" : "error"));
					}
				} else {
					LOGGER.severe("Processing timed out");
				}
			}
		} catch (InterruptedException e) {
			LOGGER.info(new StacktracePrinter(e));
		}
		return result;
	}

	private ExecutionContext processDir(File fileOrDir, ExecutionContext<Map<String, Set<String>>> executionContext) {
		DirectoryStream<Path> paths;
		try {
			paths = Files.newDirectoryStream(fileOrDir.toPath());
			paths.forEach(path -> {
				try {
					if (path.toFile().isFile()) {
						executionContext.submit(new TagExtractor(path.toFile()));
					} else {
						processDir(path.toFile(), executionContext);
					}
				} catch (Exception e) {
					LOGGER.severe(new StacktracePrinter(e));
				}
			});
		} catch (Exception e) {
			LOGGER.severe(new StacktracePrinter(e));
		}
		return executionContext;
	}

}
