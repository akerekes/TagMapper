package hu.ka.documentprocessor;

import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DocxParserTest {

	@Test
	public void testExtractText() throws Exception {
		URL resource = getClass().getClassLoader().getResource("docparser/docparsertest_basic.docx");
		assertNotNull(resource);
		Assertions.assertThat(new DocxParser(resource.getFile()).extractText()).isEqualTo("Test text.");
	}
}