package hu.ka.tagmapper.render;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.floreysoft.jmte.Engine;
import hu.ka.tagmapper.map.TagMapping;

public class TemplateTagMappingRenderer implements TagMappingRenderer {
	@Override
	public String render(TagMapping tagMapping) {
		Engine engine = new Engine();
		Map<String, Object> model = new HashMap<>();
		model.put("filesToTags", tagMapping.getFilesToTags());
		model.put("tagsToFiles", tagMapping.getTagsToFiles());
		String template = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("render.template"))).lines().collect(Collectors.joining("\n"));
		return engine.transform(template, model);
	}
}
