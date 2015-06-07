package hu.ka.tagmapper;

import java.util.logging.Logger;

public class Main {

	private static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
		if (args.length != 1) {
			LOGGER.severe("Single command line argument is path to a file or directory to search for tags.");
		} else {
			TagMapping tagMapping = new TagMapper().processPath(args[0]);
			String renderedOutput = new TagMappingRenderer().render(tagMapping);
			System.out.println(renderedOutput);
		}
	}

}
