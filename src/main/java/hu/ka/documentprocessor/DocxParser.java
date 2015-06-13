package hu.ka.documentprocessor;

import java.io.File;
import java.io.StringWriter;

import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocxParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocxParser.class);
	private final String filepath;

	public DocxParser(String filepath) {
		this.filepath = filepath;
	}

	public String extractText() {
		File docxFile = new File(filepath);
		try {
			WordprocessingMLPackage doc = WordprocessingMLPackage.load(docxFile);
			StringWriter text = new StringWriter();
			TextUtils.extractText(doc.getMainDocumentPart().getContents(), text);
			return text.toString();
		} catch (Docx4JException e) {
			LOGGER.error("Could not load file: " + filepath, e);
		} catch (Exception e) {
			LOGGER.error("Could not extract text", e);
		}
		return "";
	}
}
