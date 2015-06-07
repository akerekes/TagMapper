package hu.ka.tagmapper;

import java.util.Map;
import java.util.Set;

public class TagMapping {
	private Map<String, Set<String>> filesToTags;
	private Map<String, Set<String>> tagsToFiles;

	public TagMapping(Map<String, Set<String>> filesToTags, Map<String, Set<String>> tagsToFiles) {
		this.filesToTags = filesToTags;
		this.tagsToFiles = tagsToFiles;
	}

	public Map<String, Set<String>> getTagsToFiles() {
		return tagsToFiles;
	}

	public Map<String, Set<String>> getFilesToTags() {
		return filesToTags;
	}
}
