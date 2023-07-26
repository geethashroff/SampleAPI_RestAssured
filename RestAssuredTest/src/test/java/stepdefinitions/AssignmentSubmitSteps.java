package stepdefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.http.HttpStatus;

import apiEngine.model.request.AddAssignmentRequest;
import apiEngine.model.request.AddBatchRequest;
import apiEngine.model.request.AddProgramRequest;
import apiEngine.model.request.AddSubmitRequest;
import apiEngine.model.request.AddUserRequest;
import apiEngine.model.response.Assignment;
import apiEngine.model.response.Batch;
import apiEngine.model.response.Program;
import apiEngine.model.response.Submission;
import apiEngine.model.response.User;
import dataProviders.ExcelReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import utilities.DynamicValues;
import utilities.LoggerLoad;

public class AssignmentSubmitSteps extends BaseStep 
{
	
	RequestSpecification request;
	Response response;
	AddSubmitRequest addSubmitRequest;
	
	Map<String, String> excelDataMap;

	Program program;
	Batch batch;
	User user;
	Assignment assignment;

	static int programId;
	static int batchId;
	static String userId;
	static int assignmentId;
	static int submissionId;
	static Submission submissionAdded;
	
	
	public void SetupPreRequisites() 
	{

		try 
		{
			excelDataMap = null;
			AddProgramRequest programReq = null;
			AddBatchRequest batchReq = null;
			AddUserRequest userReq = null;
			AddAssignmentRequest assignmentReq = null;

			// create program
			excelDataMap = ExcelReader.getData("Post_Program_Assignment", "program");
			if (null != excelDataMap && excelDataMap.size() > 0) 
			{
				programReq = new AddProgramRequest(excelDataMap.get("programName") + DynamicValues.SerialNumber(),
						excelDataMap.get("programStatus"), excelDataMap.get("programDescription"));
			}

			response = programEndpoints.CreateProgram(programReq);
			if(response.statusCode() == 201)
			{
				program = response.getBody().as(Program.class);
				programId = program.programId;
				LoggerLoad.logInfo("Program created with id- " + programId);
			}
			else
			{
				LoggerLoad.logInfo("Program not created for assignment submit module");
			}

			// create batch
			excelDataMap = ExcelReader.getData("Post_Batch_Assignment", "batch");
			if (null != excelDataMap && excelDataMap.size() > 0) 
			{
				batchReq = new AddBatchRequest(excelDataMap.get("BatchName") + DynamicValues.SerialNumber(), 
						excelDataMap.get("BatchStatus"), excelDataMap.get("BatchDescription"), 
						Integer.parseInt(excelDataMap.get("NoOfClasses")), programId);
			}

			response = batchEndpoints.CreateBatch(batchReq);
			if(response.statusCode() == 201)
			{
				batch = response.getBody().as(Batch.class);
				batchId = batch.batchId;
				LoggerLoad.logInfo("Program batch created with id- " + batchId);
			}
			else
			{
				LoggerLoad.logInfo("Program Batch not created for assignment submit module");
			}

			// create user
			excelDataMap = ExcelReader.getData("Post_User_Assignment", "user");
			if (null != excelDataMap && excelDataMap.size() > 0) 
			{
				userReq = new AddUserRequest(excelDataMap.get("userFirstName") + DynamicValues.SerialNumber(),
						excelDataMap.get("userLastName"), excelDataMap.get("userMiddleName"),
						excelDataMap.get("userComments"), excelDataMap.get("userEduPg"), excelDataMap.get("userEduUg"),
						excelDataMap.get("userLinkedinUrl"), excelDataMap.get("userLocation"),
						DynamicValues.PhoneNumber(), excelDataMap.get("roleId"), excelDataMap.get("userRoleStatus"),
						excelDataMap.get("userTimeZone"), excelDataMap.get("userVisaStatus"));
			}
			
			response = userEndpoints.CreateUser(userReq);
			if(response.statusCode() == 201)
			{
				user = response.getBody().as(User.class);
				userId = user.userId;
				LoggerLoad.logInfo("User created with id- " + userId);
			}
			else
			{
				LoggerLoad.logInfo("User not created for assignment submit module");
			}
			
			// create assignment
			excelDataMap = ExcelReader.getData("Post_Assignment_Submit", "assignment");
			if (null != excelDataMap && excelDataMap.size() > 0) 
			{
				assignmentReq = new AddAssignmentRequest(excelDataMap.get("assignmentName") + DynamicValues.SerialNumber(),
						excelDataMap.get("assignmentDescription"), batchId, excelDataMap.get("comments"), userId,
						excelDataMap.get("dueDate"), userId, excelDataMap.get("pathAttachment1"), excelDataMap.get("pathAttachment2"),
						excelDataMap.get("pathAttachment3"), excelDataMap.get("pathAttachment4"), excelDataMap.get("pathAttachment5"));
			}
			
			response = assignmentEndpoints.CreateAssignment(assignmentReq);
			if(response.statusCode() == 201)
			{
				assignment = response.getBody().as(Assignment.class);
				assignmentId = assignment.assignmentId;
				LoggerLoad.logInfo("Assignment created with id- " + assignmentId);
			}
			else
			{
				LoggerLoad.logInfo("Assignment not created for assignment submit module");
			}

		} 
		catch (Exception ex) 
		{
			LoggerLoad.logInfo(ex.getMessage());
			ex.printStackTrace();
		}

	}
	

	public void Cleanup() 
	{
		// delete assignment
		response = assignmentEndpoints.DeleteAssignmentById(assignmentId);
		if(response.statusCode() == 200)
			LoggerLoad.logInfo("Assignment deleted with id- " + assignmentId);
		else
			LoggerLoad.logInfo("Assignment not deleted for assignment submit module");
		
		// delete user
		response = userEndpoints.DeleteUserById(userId);
		if(response.statusCode() == 200)
			LoggerLoad.logInfo("User deleted with id- " + userId);
		else
			LoggerLoad.logInfo("User not deleted for assignment submit module");

		// delete batch
		response = batchEndpoints.DeleteBatchById(batchId);
		if(response.statusCode() == 200)
			LoggerLoad.logInfo("Program batch deleted with id- " + batchId);
		else
			LoggerLoad.logInfo("Program batch not deleted for assignment submit module");
		
		// delete program
		response = programEndpoints.DeleteProgramById(programId);
		if(response.statusCode() == 200)
			LoggerLoad.logInfo("Program deleted with id- " + programId);
		else
			LoggerLoad.logInfo("Program not deleted for assignment submit module");
	}

	
	@Given("User creates POST Assignment Submit Request with fields from {string} with {string}")
	public void user_creates_post_assignment_submit_request_with_fields_from_with(String sheetName, String dataKey) 
	{
		try 
		{
			// create program, batch, user and assignment for creating new assignment submission
			if (dataKey.equals("Post_AssignmentSubmit_Valid")) 
			{
				SetupPreRequisites();
			}
			
			RestAssured.baseURI = baseUrl;
			RequestSpecification request = RestAssured.given();
			request.header("Content-Type", "application/json");

			Integer reqAssignmentId = null; 
			String reqUserId = null, subDesc = null, subComments = null, subPathAttach1 = null, subPathAttach2 = null, 
					subPathAttach3 = null, subPathAttach4 = null, subPathAttach5 = null, subDateTime = null;
			
			excelDataMap = ExcelReader.getData(dataKey, sheetName);

			if (excelDataMap != null && excelDataMap.size() > 0) 
			{
				if(!excelDataMap.get("subDesc").isBlank())
					subDesc = excelDataMap.get("subDesc");
				if(!excelDataMap.get("subDateTime").isBlank())
					subDateTime = excelDataMap.get("subDateTime");
				if(!dataKey.equals("Post_AssignmentSubmit_MissingAssignmentId"))
					reqAssignmentId = assignmentId;
				if(!dataKey.equals("Post_AssignmentSubmit_MissingUserId"))
					reqUserId = userId;
				
				subComments = excelDataMap.get("subComments");
				subPathAttach1 = excelDataMap.get("subPathAttach1");
				subPathAttach2 = excelDataMap.get("subPathAttach2");
				subPathAttach3 = excelDataMap.get("subPathAttach3");
				subPathAttach4 = excelDataMap.get("subPathAttach4");
				subPathAttach5 = excelDataMap.get("subPathAttach5");
			}
			
			addSubmitRequest = new AddSubmitRequest(reqAssignmentId, reqUserId,subDesc, subComments, subPathAttach1, 
					subPathAttach2, subPathAttach3, subPathAttach4, subPathAttach5, subDateTime);
			
			LoggerLoad.logInfo("Assignment Submit POST request created for- " + dataKey);
		} 
		catch (Exception ex) 
		{
			LoggerLoad.logInfo(ex.getMessage());
			ex.printStackTrace();
		}
	}

	@When("User sends HTTP POST Assignment Submit Request for {string}")
	public void user_sends_http_post_assignment_submit_request_for(String dataKey) 
	{
		try 
		{
			response = submitEndpoints.CreateAssignmentSubmission(addSubmitRequest);
			LoggerLoad.logInfo("Assignment Submit POST request sent for- " + dataKey);
		} 
		catch (Exception ex) 
		{
			LoggerLoad.logInfo(ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Then("User receives response for POST Assignment Submit {string} with {string}")
	public void user_receives_response_for_post_assignment_submit_with(String sheetName, String dataKey) 
	{
		try 
		{
			response.then().log().all().extract().response();
			
			switch(dataKey)
			{
				case "Post_AssignmentSubmit_Valid" : 
					response.then().assertThat()
						// Validate response status
						.statusCode(HttpStatus.SC_CREATED)
						// Validate content type
						.contentType(ContentType.JSON)
						// Validate json schema
						.body(JsonSchemaValidator.matchesJsonSchema(
							getClass().getClassLoader().getResourceAsStream("getsubmissionjsonschema.json")));
					
					// Validate values in response
					Submission submissionResponse = response.getBody().as(Submission.class);
					
					assertTrue(submissionResponse.submissionId != null && submissionResponse.submissionId != 0);
					
					assertEquals(addSubmitRequest.assignmentId, submissionResponse.assignmentId);
					assertEquals(addSubmitRequest.subComments, submissionResponse.subComments);
					assertEquals(addSubmitRequest.userId, submissionResponse.userId);
					assertEquals(addSubmitRequest.subDesc, submissionResponse.subDesc);
					assertEquals(addSubmitRequest.subPathAttach1, submissionResponse.subPathAttach1);
					assertEquals(addSubmitRequest.subPathAttach2, submissionResponse.subPathAttach2);
					assertEquals(addSubmitRequest.subPathAttach3, submissionResponse.subPathAttach3);
					assertEquals(addSubmitRequest.subPathAttach4, submissionResponse.subPathAttach4);
					assertEquals(addSubmitRequest.subPathAttach5, submissionResponse.subPathAttach5);
					//assertEquals(addSubmitRequest.subDateTime, submissionResponse.subDateTime);
					assertTrue(submissionResponse.gradedBy == null);
					assertTrue(submissionResponse.gradedDateTime == null);
					assertTrue(submissionResponse.grade == -1);
					
					submissionId = submissionResponse.submissionId;
					submissionAdded = submissionResponse;
					
					break;
					
				default : 
					response.then().assertThat()
						// Validate response status
						.statusCode(HttpStatus.SC_BAD_REQUEST)
						// Validate json schema
						.body(JsonSchemaValidator.matchesJsonSchema(
							getClass().getClassLoader().getResourceAsStream("400statuscodejsonschema.json")));
					
					// Validate error json
					JsonPath jsonPathEvaluator = response.jsonPath();
					assertEquals(excelDataMap.get("message"), jsonPathEvaluator.get("message"));
					assertEquals(excelDataMap.get("success"), Boolean.toString(jsonPathEvaluator.get("success")));
					
					break;
					
			}
			
			LoggerLoad.logInfo("Assignment POST response validated for- " + dataKey);
		} 
		catch (Exception ex) 
		{
			LoggerLoad.logInfo(ex.getMessage());
			ex.printStackTrace();
		}

	}

	@Given("User creates DELETE Request with {string} scenario")
	public void user_creates_delete_request_with_scenario(String string) 
	{
		try
		{
			RestAssured.baseURI = baseUrl;
			request = RestAssured.given();
			
			LoggerLoad.logInfo("DELETE assignment submit by id request for scenario " + string + " created");
		} 
		catch (Exception ex) 
		{
			LoggerLoad.logInfo(ex.getMessage());
			ex.printStackTrace();
		}
	}

	@When("User sends the HTTP Delete Assignment Submit Request {string}")
	public void user_sends_the_http_delete_assignment_submit_request(String string) 
	{
		try
		{
			response = submitEndpoints.DeleteSubmissionById(submissionId);
			
			LoggerLoad.logInfo("DELETE assignment submit by id request sent for - " + string);
		} 
		catch (Exception ex) 
		{
			LoggerLoad.logInfo(ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Then("User receives response for DELETE Assignment Submit {string} with {string}")
	public void user_receives_response_for_delete_assignment_submit_with(String sheetName, String dataKey) 
	{
		try
		{
			JsonPath jsonPathEvaluator = null;
			excelDataMap = ExcelReader.getData(dataKey, sheetName);
			
			switch(dataKey)
			{
				case "Delete_AssignmentSubmit_ValidId" :
					response.then().assertThat()
						// Validate response status
						.statusCode(HttpStatus.SC_OK)
						// Validate content type
						.contentType(ContentType.JSON)
						// Validate json schema
						.body(JsonSchemaValidator.matchesJsonSchema(
						getClass().getClassLoader().getResourceAsStream("200statuscodejsonschema.json")));
					
					// Validate response json
					jsonPathEvaluator = response.jsonPath();
					assertEquals(excelDataMap.get("message"), jsonPathEvaluator.get("message"));
					assertEquals(excelDataMap.get("success"), Boolean.toString(jsonPathEvaluator.get("success")));
					
					break;
				
				case "Delete_AssignmentSubmit_DeletedId" :
					response.then().assertThat()
						// Validate response status
						.statusCode(HttpStatus.SC_NOT_FOUND)
						// Validate content type
						.contentType(ContentType.JSON)
						// Validate json schema
						.body(JsonSchemaValidator.matchesJsonSchema(
						getClass().getClassLoader().getResourceAsStream("404statuscodejsonschema.json")));
					
					// Validate response json
					jsonPathEvaluator = response.jsonPath();
					assertEquals(excelDataMap.get("message") + submissionAdded.submissionId + " ", jsonPathEvaluator.get("message"));
					assertEquals(excelDataMap.get("success"), Boolean.toString(jsonPathEvaluator.get("success")));
					
					break;
			}
		
			LoggerLoad.logInfo("DELETE assignment submission by id response validated for- " + dataKey);
			
			if(dataKey.equals("Delete_AssignmentSubmit_ValidId"))
				Cleanup();	
		} 
		catch (Exception ex) 
		{
			LoggerLoad.logInfo(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
