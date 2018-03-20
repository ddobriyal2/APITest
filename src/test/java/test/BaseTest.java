package test;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import data.DataProviderClass;
import helper.ReadPropertyFile;
import helper.RestMe;
//import io.restassured.RestAssured;
import io.restassured.response.Response;
import junit.framework.Assert;
//import static io.restassured.RestAssured.*;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Severity;
import ru.yandex.qatools.allure.annotations.Stories;
import ru.yandex.qatools.allure.annotations.Title;
import ru.yandex.qatools.allure.model.SeverityLevel;

/**
 *	assumptions:
 *	headers will remain fixed for all calls hence reading from config.properties file
 *
 *	@author devesh
 *	@date Feb 2017
 */
@Title("Base Test Class")
@Description("Description: Common class for all API tests")
public class BaseTest {
	
	
private static final Logger logger = Logger.getLogger(BaseTest.class.getName());
	
	
	@BeforeClass
		public void loadlog4J(){
			String log4jConfPath = System.getProperty("user.dir")+"/log4j.properties";
			PropertyConfigurator.configure(log4jConfPath);
		}
	
	public static ExtentReports extent;
	public static ExtentTest test;
	
	static {
	Calendar calender = Calendar.getInstance();
	SimpleDateFormat formater = new SimpleDateFormat("dd_mm_yyyy_hh_mm_ss");
	
	extent = new ExtentReports("/home/ddobriyal/git/PriveRestAssured/src/test/java/Report/test" +formater.format(calender.getTime())+ ".html",false);
	
	}
	
	public void getresult(ITestResult result)
	{
		if (result.getStatus()==ITestResult.SUCCESS) {
		test.log(LogStatus.PASS, result.getName()+"Test Is pass");
	}
		else if (result.getStatus()==ITestResult.FAILURE) {
			test.log(LogStatus.FAIL, result.getName()+"Test has failed");
		}
		
		
	}
	
	RestMe restme;
	Response response;
	String jsonString;
	ReadPropertyFile readPropFile;
	Map<String, String> propertyMap;
	String headerkey1,headervalue1,headerkey2,headervalue2; 
	String whatToRun;
	
	@BeforeSuite
	public void setup(){
		readPropFile = new ReadPropertyFile();
		propertyMap = readPropFile.getMap();
		restme =  new RestMe(propertyMap);
		whatToRun = propertyMap.get("whattorun");	
	}
	
	
	@AfterSuite
	public void tearDown(){
		
	}
	
	@BeforeMethod()
	public void beforeMethod(Method result){
		test= extent.startTest(result.getName());
		test.log(LogStatus.INFO,result.getName()+ "Test started");
	}
	@AfterMethod()
	public void afterMethod(ITestResult result){
	getresult(result);}
	
	@AfterClass(alwaysRun=true)
	public void endTest(){
		extent.endTest(test);
		extent.flush();
	}
	
	
	
	/**
	 * csv format: test_id,test_step,suite,action,data(url-path#content-type#status-code#list_of_parameters_pipe_separated)
	 * 
	 * 
	 * This method will get data from Data Provider for all the cases present in csv file
	 * Based on 'whattorun' value in config.properties, performAction() method will be called
	 */
	@Features("Test Method")
	@Stories("Data provider Consumer")
	@Severity(SeverityLevel.CRITICAL)
	@Title("API Calls")
	@Description("Description: All types of calls")
	
	@Test(dataProvider = "csvdataprovider",dataProviderClass=DataProviderClass.class)
	public void testRequest(String test_id, String test_step, String suite, String action, String data){
		
		if(whatToRun.equals("suite")){
			String [] testsuitestorun = propertyMap.get("testsuitestorun").split(",");
			boolean isRunSuitePresent = Arrays.asList(testsuitestorun).contains(suite);
			if(isRunSuitePresent){
				logger.info("Test case for suites Started");
				performAction(action, data);
			}else{
				logger.info("No Tests to move forward");
				System.out.println("########### Dont report this @test");
				throw new SkipException("SUITE NOT MATCHING.....");
			}
			
		}else if (whatToRun.equals("testids")) {
			String [] testidstorun = propertyMap.get("testidstorun").split(",");
			boolean isRunTestIdPresent = Arrays.asList(testidstorun).contains(test_id);
			if(isRunTestIdPresent){
				logger.info("Test case for test id Started");
				performAction(action, data);
			}else{
				logger.info("No Tests to move forward");
				System.out.println("########### Dont report this @test");
				throw new SkipException("SUITE NOT MATCHING.....");
			}
			
		}else{
			performAction(action, data);
		}
	}
	
	
	@Test
	public void  testbogas(){
		System.out.println("test");
		Assert.assertTrue("Passed", true);
	}
	
	
	/**
	 * This method get action,data values from @Test testRequest method 
	 * and perform actual call to endpoints
	 * 
	 * @param action : GET, POST etc
	 * @param data: url, status code, parameters etc
	 */
	public void performAction(String action, String data){
		
		switch (action.toUpperCase()) {
		case "GET": restme.getMe(data,"token");break;
		case "POST": restme.postMe(data);break;
		case "DELETE":restme.deleteMe(data,"token"); break;
		default: throw new IllegalStateException("Given action name(http methods) in csv are not matching with any of the existing action name");
		}
	
	}
	
		
}


