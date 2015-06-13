package hu.ka.documentprocessor;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
			extract(doc.getMainDocumentPart().getContents(), text);
			return text.toString();
		} catch (Docx4JException e) {
			LOGGER.error("Could not load file: " + filepath, e);
		} catch (Exception e) {
			LOGGER.error("Could not extract text", e);
		}
		return "";
	}

	private void extract(Object o, Writer w) throws JAXBException {
		Marshaller marshaller= Context.jc.createMarshaller();
		NamespacePrefixMapperUtils.setProperty(marshaller,
				NamespacePrefixMapperUtils.getPrefixMapper());
		marshaller.marshal(o, new TextExtractor(w, " "));
	}

	static class TextExtractor extends DefaultHandler {

		private Writer out;
		private String separator;

		public TextExtractor(Writer out, String separator) {
			this.out = out;
			this.separator = separator;
		}

		public void characters(char[] text, int start, int length)
				throws SAXException {

			try {
				out.write(text, start, length);
				out.write(separator);
			}
			catch (IOException e) {
				throw new SAXException(e);
			}

		}

	} // end TextExtractor
}
