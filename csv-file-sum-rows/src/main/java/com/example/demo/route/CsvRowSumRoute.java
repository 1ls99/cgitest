package com.example.demo.route;

import java.io.File;
import java.lang.invoke.MethodHandles;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CsvRowSumRoute extends RouteBuilder {

	private Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String FROM_PATH_ERROR_MESSAGE = "Initialization error. Required 'from_path' configuration parameter is not set in application.properties";
	private static final String TO_PATH_ERROR_MESSAGE = "Initialization error. Required 'to_path' configuration parameter is not set in application.properties";
	private static final String TO_FILE_EXTENSION_ERROR_MESSAGE = "Initialization error. Required 'to_file_extension' configuration parameter is not set in application.properties";
	private static final String MISSING_FOLDER_ERROR_MESSAGE = "Initialization error. Folder is missing %s. Please crete this folder manually.";
	private static final String EMPTY = "";
	public static final String  CSV_ROW_SUM_ROUTE_ID = "CSV_ROW_SUM_ROUTE_ID";

	@Value("${from_path}")
	private String from_path;

	@Value("${to_path}")
	private String to_path;

	@Value("${to_file_extension}")
	private String toFileExt;

	@Override
	public void configure() throws Exception {
		checkConfiguration();
		
		String from = String.format("file:%s?recursive=false&include=.*csv$", from_path);
		String to = String.format("file:%s", to_path);

		logger.debug(String.format("from: %s to: %s", from, to));

		try {
			from(from)
				.routeId(CSV_ROW_SUM_ROUTE_ID)
				.unmarshal()
				.csv()
				.process(new CSVSumProcessor(toFileExt))
				.to(to)
				.end();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	private void checkConfiguration() {
		checkConfigParamNotEmpty(from_path, FROM_PATH_ERROR_MESSAGE);
		checkConfigParamNotEmpty(to_path, TO_PATH_ERROR_MESSAGE);
		checkConfigParamNotEmpty(toFileExt, TO_FILE_EXTENSION_ERROR_MESSAGE);		
		checkFolderExists(from_path);
		checkFolderExists(to_path);
	}

	//visibility changed for testing
	public void checkConfigParamNotEmpty(String configParam, String errorMessage) {
		if (configParam == null || EMPTY.equals(configParam)) {
			logger.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}
	
	public void checkFolderExists(String folder) {
		if (!new File(folder).exists()) {
			logger.error(String.format(MISSING_FOLDER_ERROR_MESSAGE, folder));
			throw new RuntimeException(String.format(MISSING_FOLDER_ERROR_MESSAGE, folder));
		}
	}
}
