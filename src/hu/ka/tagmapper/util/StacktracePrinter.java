package hu.ka.tagmapper.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

public class StacktracePrinter implements Supplier<String> {

	private Throwable t;

	public StacktracePrinter(Throwable t) {
		this.t = t;
	}

	public static String print(Throwable t) {
		StringWriter msg = new StringWriter();
		PrintWriter stringWriter = new PrintWriter(msg);
		t.printStackTrace(stringWriter);
		return msg.toString();
	}

	@Override
	public String get() {
		return print(t);
	}
}
