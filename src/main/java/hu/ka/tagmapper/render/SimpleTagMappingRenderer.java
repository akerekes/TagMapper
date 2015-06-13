package hu.ka.tagmapper.render;

import hu.ka.tagmapper.map.TagMapping;

public class SimpleTagMappingRenderer implements TagMappingRenderer {
	@Override
	public String render(TagMapping tagMapping) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><head><title>Tags and files</title></head><body><h1>Tags</h1><p>");
		tagMapping.getTagsToFiles().forEach((tag, files) -> files.forEach((file -> builder.append("<b>").append(tag).append("</b> -> <a href='file://///").append(file).append("'>").append(file).append("</a><br>\n"))));
		builder.append("</p><h1>Files</h1><p>");
		tagMapping.getFilesToTags().forEach((file, tags) -> tags.forEach((tag -> builder.append("<b>").append(file).append("</b> -> <a href='#").append(tag).append("'>").append(tag).append("</a><br>\n"))));
		return builder.toString();
	}
}
