package net.mfjassociates.tools.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class SwitchLoggingLevel {
	
	public static boolean switchLevel(Level level, Logger logger) {
		boolean switched=false;
		if (logger instanceof org.apache.logging.log4j.Logger) {
			org.apache.logging.log4j.core.Logger logger2=(org.apache.logging.log4j.core.Logger)logger;
			logger2.setLevel(Level.valueOf(level.name()));
			switched=true;
		}
		return switched;
	}
}
