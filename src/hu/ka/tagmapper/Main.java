package hu.ka.tagmapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class Main {

	private static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
		new TagMapper().processPath(args[0]);
	}

}
