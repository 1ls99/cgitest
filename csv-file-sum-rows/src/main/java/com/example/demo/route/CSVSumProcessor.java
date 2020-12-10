package com.example.demo.route;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVSumProcessor implements Processor {
	private static final String UNABLE_TO_PROCESS_FILE = "Unable to process file %s. Reason: %s";
	private static final String DOT = ".";
	private static final String LINE_SEPARATOR = "line.separator";

	private Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private String toFileExt;

	public CSVSumProcessor(String toFileExt) {
		this.toFileExt = toFileExt;
	}

	/**
	 * Current implementation stops CsvRowSumRoute when encounters an invalid csv file (ex. with string content)
	 * 
	 * TODO: 
	 * - Change current behavior to skip invalid csv files (allowing consumable csv files to be processed).
	 * 
	 * @param th
	 * @param exchange
	 */
    
	@Override
	public void process(Exchange exch) throws Exception {
		try {
			String toBody = computeRowSums(fromBody(exch));
			applyToFileBody(exch.getMessage(), toBody);
			changeToFileExt(exch.getMessage(), toFileExt());
		} catch (Throwable th) {
			exceptionHandler(th, exch);
		}
	}

	private String computeRowSums(List<List<String>> rows) {
		StringBuilder output = new StringBuilder();
		rows.stream().forEach(row -> {
			int rowSum = row.stream()
					.mapToInt(CSVCell::asInteger)
					.reduce(0, Integer::sum);
			output.append(rowSum).append(System.getProperty(LINE_SEPARATOR));
		});
		return output.toString();
	}

	@SuppressWarnings("unchecked")
	private List<List<String>> fromBody(Exchange exchange) {
		return (List<List<String>>) exchange.getIn().getBody();
	}

	private String toFileExt() {
		return DOT + toFileExt;
	}

	private void exceptionHandler(Throwable th, Exchange exchange) {
		String inFileName = exchange.getMessage().getHeader(Exchange.FILE_NAME_ONLY, String.class);
		logger.error(String.format(UNABLE_TO_PROCESS_FILE, inFileName, th.getMessage()));

		exchange.getContext().getShutdownStrategy().setTimeout(1); // 1 second
		exchange.getContext().stop(); // Stop route on error
	}

	private void changeToFileExt(Message msg, String toFileExt) {
		String inFileName = msg.getHeader(Exchange.FILE_NAME_ONLY, String.class);
		msg.setHeader(Exchange.FILE_NAME, FilenameUtils.removeExtension(inFileName) + toFileExt);
	}

	private void applyToFileBody(Message msg, String sum) {
		msg.setBody(sum);
	}
}
