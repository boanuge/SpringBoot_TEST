package com.example.servicebroker.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonServiceLog {

	public static final Logger logger = LoggerFactory.getLogger(CommonServiceLog.class);

	public static void logInfo(String message) {
		if (message == null)
			return;

		logger.info("[{}][{}][{}] {}",
				new Throwable().getStackTrace()[1].getFileName(),
				new Throwable().getStackTrace()[1].getMethodName(),
				new Throwable().getStackTrace()[1].getLineNumber(),
				message);
	}

	public static void logError(String message) {
		if (message == null)
			return;

		logger.error("[{}][{}][{}] {}",
				new Throwable().getStackTrace()[1].getFileName(),
				new Throwable().getStackTrace()[1].getMethodName(),
				new Throwable().getStackTrace()[1].getLineNumber(),
				message);
	}
}
