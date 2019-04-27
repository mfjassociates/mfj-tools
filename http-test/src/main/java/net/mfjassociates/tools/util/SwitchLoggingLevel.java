package net.mfjassociates.tools.util;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public class SwitchLoggingLevel {
	
	public static boolean switchLevel(Level level, Logger logger) {
		boolean switched=false;
		if (logger instanceof ch.qos.logback.classic.Logger) {
			ch.qos.logback.classic.Logger logger2=(ch.qos.logback.classic.Logger)logger;
			logger2.setLevel(ch.qos.logback.classic.Level.valueOf(level.name()));
			switched=true;
		}
		return switched;
	}
}
