package hu.ka.tagmapper;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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

	public void processPath(String path) {
		try {
			File fileOrDir = new File(path);
			Map<String, Set<String>> filesToKws;
			if (fileOrDir.isFile()) {
				filesToKws = new TagExtractor(fileOrDir).call();
			} else {
				ExecutionContext executionContext = processDir(fileOrDir, new ExecutionContext<>());
				filesToKws = collectTags(executionContext);
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
