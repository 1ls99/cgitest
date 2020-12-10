package com.example.demo.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.example.demo.CSVRowSumApplication;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = CSVRowSumApplication.class)
@TestPropertySource(locations="classpath:application.properties")
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Sum up rows in csv files")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvRowSumRouteTest {

	private static final String LINE_SEPARATOR = "line.separator";
	private static final String DOT = ".";
	private static final String CAMEL_FOLDER = ".camel";
	private static final String EMPTY = "";
	private static String from;
	
	@Autowired
	private CamelContext camelContext;
	
    @Autowired
    private ProducerTemplate template;
    
	@Value("${from_path}")
	private String from_path;

	@Value("${to_path}")
	private String to_path;

	@Value("${to_file_extension}")
	private String toFileExt;
	
	@Autowired
	private CsvRowSumRoute route;
	
	@BeforeAll
	public void initFrom() {
		from = String.format("file:%s?recursive=false&include=.*csv$", from_path);
	}
	
	@Test
	@DisplayName("Empty config parameter throws RuntimeException")
	@Order(1)
	public void checkConfigParamNotEmpty_expectsRuntimeException() throws Exception {
		String from_path = EMPTY;
		String FROM_PATH_ERROR_MESSAGE = "Initialization error. Required 'from_path' configuration parameter is not set in application.properties";
		assertThrows(RuntimeException.class, () -> {route.checkConfigParamNotEmpty(from_path, FROM_PATH_ERROR_MESSAGE);});
	}
	
	@Test
	@DisplayName("Valid csv file content succeeds")
	@Order(2)
	public void validCsvFileContent_expectsFileWithRowSum() throws InterruptedException {
		template.sendBodyAndHeader(from,
				validFromBody(), Exchange.FILE_NAME, validFromFile());
		Thread.sleep(2000); // allow sufficient time for camel route operations
		
		File target = new File(validToFile());
	    assertTrue("To file found", target.exists());
	    String content = camelContext.getTypeConverter()
	                             .convertTo(String.class, target);
	    assertEquals(toBody(), content);
	    
	    cleanupValid();
	}
	
	@Test
	@DisplayName("Invalid csv file content fails")
	@Order(3)
	public void invalidCsvFileContent_expectsDoneFileMissing() throws InterruptedException {
		template.sendBodyAndHeader(from,
				invalidFromBody(), Exchange.FILE_NAME, invalidFromFile());
		Thread.sleep(2000); // allow sufficient time for camel route operations
		
		File target = new File(invalidToFile());
	    assertTrue("To file missing", !target.exists());
	    
	    cleanupInvalid();
	}
	
	private String validFromBody() {
		return "50, 1, 100" + System.getProperty(LINE_SEPARATOR) + 
			   "20, 5";
	}
	
	private String toBody() {
		return "151" + System.getProperty(LINE_SEPARATOR) + 
				"25" + System.getProperty(LINE_SEPARATOR);
	}
	
	private String invalidFromBody() {
		return "50, abc, 40";
	}
	
	private String invalidFromFile() {
		return "invalid" + DOT + "csv"; 
	}
	
	private String invalidToFile() {
		return to_path + File.separator + "invalid" + DOT + toFileExt;
	}
	
	private String validFromFile() {
		return "valid" + DOT + "csv"; 
	}
	
	private String validToFile() {
		return to_path + File.separator + "valid" + DOT + toFileExt;		
	}
	
	private void cleanupValid() {
	    cleanUp(from_path + File.separator + validFromFile(),
	    		from_path + File.separator + CAMEL_FOLDER + File.separator + validFromFile(),
	    		validToFile()
	    		);
	}
	
	private void cleanupInvalid() {
		 cleanUp(from_path + File.separator + invalidFromFile(),
				 from_path + File.separator + CAMEL_FOLDER + File.separator + invalidFromFile(),
				 invalidToFile()
		 );
	}

	private void cleanUp(String... fileNames) {
		for(String fileName: fileNames) {
			File file = new File(fileName);
			if (file.exists())
				file.deleteOnExit();	
		}
	}
}
