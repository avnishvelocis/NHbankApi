package com.nhbank.api.test;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class NHBank_InWard_Api {
	
	String token;
	HashMap<String, String> pendingTransaction;
	HashMap<String, String> failedTransaction;
	HashMap<String, String> senttogsbTransaction;
	HashMap<String, String> returnedTransaction;
	HashMap<String, String> creditticustomerTransaction;
	HashMap<String, String> settledTransaction;
	
	@BeforeMethod
	public void setup() {
		RestAssured.baseURI="http://192.168.50.52:9930";
		RestAssured.basePath="/api";
		
		if(Objects.isNull(token)) {
		token =  given().
         		body("{\"clientId\":\"56387388\",\"secretKey\":\"Test@1234\"}").
         		contentType(ContentType.JSON).
         		log().all().
         when().
         		post("/userauth/authenticate/createToken").
         then().
        		 log().all().
        		 assertThat().
        		 statusCode(200).extract().response().path("authToken");
		System.out.println(token);
		}
	}
	
	

		
	
	
	@DataProvider  //{"SETTLED"},{"FAILED"},{"RETURNED"},{"CRDTTOCUST"},{"PENDING"},
	public Object[][] data() {
	return new Object[][]{{"SENTTOGBS"}};
	}
	
	@Test(dataProvider = "data")
	public void getAllTypeTransaction(String type) {
		String requestPayload="{\"pageNo\":0,\"neftStartvalueDate\":\"2024-09-16\",\"neftEndvalueDate\":\"2024-09-16\",\"senderName\":\"\",\"beniName\":\"\",\"senderIfsc\":\"\",\"customerAccno\":\"\",\"amount\":\"0.00\",\"status\":\""+type+"\",\"msgType\":\"\",\"cbsRefNo\":\"\",\"utr\":\"\",\"senderAccno\":\"\",\"source\":\"\",\"totalRows\":10}";
		Response response =  given().
				header("Authorization",token).
         		body(requestPayload).
         		contentType(ContentType.JSON).
         		log().all().
         when().
         		post("/payment/neftin/all/getNeftInwardFilter").
         then().
        		 log().all().
        		 assertThat().
        		 statusCode(200).extract().response();
		
		if(type.equals("SETTLED")) {
			 settledTransaction = new HashMap<String, String>();
			int noOfTransaction = response.path("contentSize");
			if(noOfTransaction!=0) {
				for(int i =0;i<noOfTransaction;i++) {
					settledTransaction.put(response.path("data["+i+"].utrNumber"),response.path("data["+i+"].status"));}}
		}
		else if(type.equals("FAILED")) {
			
			 failedTransaction= new HashMap<String, String>();
			int noOfTransaction = response.path("contentSize");
			if(noOfTransaction!=0) {
				for(int i =0;i<noOfTransaction;i++) {
					failedTransaction.put(response.path("data["+i+"].utrNumber"),response.path("data["+i+"].status"));}}
		}
		else if(type.equals("RETURNED")) {
			 returnedTransaction= new HashMap<String, String>();
			int noOfTransaction = response.path("contentSize");
			if(noOfTransaction!=0) {
				for(int i =0;i<noOfTransaction;i++) {
					returnedTransaction.put(response.path("data["+i+"].utrNumber"),response.path("data["+i+"].status"));}}

		}
		else if(type.equals("SENTTOGBS")) {
			senttogsbTransaction= new HashMap<String, String>();
			int noOfTransaction = response.path("contentSize");
			if(noOfTransaction!=0) {
				for(int i =0;i<noOfTransaction;i++) {
					senttogsbTransaction.put(response.path("data["+i+"].utrNumber"),response.path("data["+i+"].status"));}}

		}
		else if(type.equals("PENDING")) {
			 pendingTransaction= new HashMap<String, String>();
			int noOfTransaction = response.path("contentSize");
			if(noOfTransaction!=0) {
				for(int i =0;i<noOfTransaction;i++) {
					pendingTransaction.put(response.path("data["+i+"].utrNumber"),response.path("data["+i+"].status"));
					}
				}
		}
		else if(type.equals("CRDTTOCUST")) {
			creditticustomerTransaction= new HashMap<String, String>();
			int noOfTransaction = response.path("contentSize");
			if(noOfTransaction!=0) {
				for(int i =0;i<noOfTransaction;i++) {
					creditticustomerTransaction.put(response.path("data["+i+"].utrNumber"),response.path("data["+i+"].status"));}}

		}
	
		
		
		  System.out.println(failedTransaction);
		  System.out.println(senttogsbTransaction);
		  System.out.println(settledTransaction);
		  System.out.println(pendingTransaction);
		  System.out.println(creditticustomerTransaction);
		  System.out.println(returnedTransaction);
		 
		
	}
	
	
	@Test(dependsOnMethods = "getAllTypeTransaction")
	public void validatePending() {
		String utrNum = null ;
		Response response;
			for(int i =0;i<pendingTransaction.size();i++) {
				for(String utr : pendingTransaction.keySet()) {
					utrNum=utr;
					
					 response =  given().
								header("Authorization",token).
				         		contentType(ContentType.JSON).
				         		log().all().
				         when().
			         		post("/payment/neftin/all/getNeftInMsgLog?utrNumber="+utrNum+"").
				         then().
				        		 log().all().
				        		 assertThat().
				        		 statusCode(200).extract().response();
					 
					 
						assertThat(response.path("data[0].msgType"),equals("Pacs.008.001.09"));
						assertThat(response.path("data[0].msgDesc"),equals("NEFT Outward Transaction Message."));
						assertThat(response.path("data[1].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[1].msgDesc"),equals("Acknowledgement Message(F27)."));
						assertThat(response.path("data[2].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[2].msgDesc"),equals("Acknowledgement Message(F20)."));
						assertThat(response.path("data[3].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[3].msgDesc"),equals("Acknowledgement Message(F23)."));
						assertThat(response.path("data[4].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[4].msgDesc"),equals("Acknowledgement Message(F29)."));
					 					 
				}				
			}			
		}
	
	@Test(dependsOnMethods = "getAllTypeTransaction")
	public void validateCreditToCustomer() {
		String utrNum = null ;
		Response response;
			for(int i =0;i<creditticustomerTransaction.size();i++) {
				for(String utr : creditticustomerTransaction.keySet()) {
					utrNum=utr;
					
					 response =  given().
								header("Authorization",token).
				         		contentType(ContentType.JSON).
				         		log().all().
				         when().
			         		post("/payment/neftin/all/getNeftInMsgLog?utrNumber="+utrNum+"").
				         then().
				        		 log().all().
				        		 assertThat().
				        		 statusCode(200).extract().response();
					 
					 
						assertThat(response.path("data[0].msgType"),equals("Pacs.008.001.09"));
						assertThat(response.path("data[0].msgDesc"),equals("NEFT Outward Transaction Message."));
						assertThat(response.path("data[1].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[1].msgDesc"),equals("Acknowledgement Message(F27)."));
						assertThat(response.path("data[2].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[2].msgDesc"),equals("Acknowledgement Message(F20)."));
						assertThat(response.path("data[3].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[3].msgDesc"),equals("Acknowledgement Message(F23)."));
						assertThat(response.path("data[4].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[4].msgDesc"),equals("Acknowledgement Message(F29)."));
						assertThat(response.path("data[5].msgType"),equals("Camt.059.001.06"));
						assertThat(response.path("data[5].msgDesc"),equals("NEFT Outward Settlement Message."));
					 					 
				}				
			}			
		}
	
	@Test(dependsOnMethods = "getAllTypeTransaction")
	public void validateSenttogbs() {
		String utrNum = null ;
		Response response;
			for(int i =0;i<senttogsbTransaction.size();i++) {
				for(String utr : senttogsbTransaction.keySet()) {
					utrNum=utr;
					
					 response =  given().
								header("Authorization",token).
				         		contentType(ContentType.JSON).
				         		log().all().
				         when().
			         		post("/payment/neftin/all/getNeftInMsgLog?utrNumber="+utrNum+"").
				         then().
				        		 log().all().
				        		 assertThat().
				        		 statusCode(200).extract().response();
					 
					 
						assertThat(response.path("data[4].msgDesc"),equals("Acknowledgement Message(F29)."));			 					 
						assertThat(response.path("data[0].msgType"),equals("Pacs.008.001.09"));
						assertThat(response.path("data[0].msgDesc"),equals("NEFT Outward Transaction Message."));
						assertThat(response.path("data[1].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[1].msgDesc"),equals("Acknowledgement Message(F27)."));
						assertThat(response.path("data[2].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[2].msgDesc"),equals("Acknowledgement Message(F20)."));
						assertThat(response.path("data[3].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[3].msgDesc"),equals("Acknowledgement Message(F23)."));
						assertThat(response.path("data[4].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[4].msgDesc"),equals("Acknowledgement Message(F29)."));			 					 
				}				
			}			
		}
	
	@Test(dependsOnMethods = "getAllTypeTransaction")
	public void validateReturned() {
		String utrNum = null ;
		Response response;
			for(int i =0;i<returnedTransaction.size();i++) {
				for(String utr : returnedTransaction.keySet()) {
					utrNum=utr;
					
					 response =  given().
								header("Authorization",token).
				         		contentType(ContentType.JSON).
				         		log().all().
				         when().
				         		post("/payment/neftin/all/getNeftInMsgLog?utrNumber="+utrNum+"").
				         then().
				        		 log().all().
				        		 assertThat().
				        		 statusCode(200).extract().response();
					 
					 
						assertThat(response.path("data[0].msgType"),equals("Pacs.008.001.09"));
						assertThat(response.path("data[0].msgDesc"),equals("NEFT Outward Transaction Message."));
						assertThat(response.path("data[1].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[1].msgDesc"),equals("Acknowledgement Message(F27)."));
						assertThat(response.path("data[2].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[2].msgDesc"),equals("Acknowledgement Message(F20)."));
						assertThat(response.path("data[3].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[3].msgDesc"),equals("Acknowledgement Message(F23)."));
						assertThat(response.path("data[4].msgType"),equals("Admi.004.001.02"));
						assertThat(response.path("data[4].msgDesc"),equals("Acknowledgement Message(F29)."));
						assertThat(response.path("data[5].msgType"),equals("Pacs.004.001.10"));
						assertThat(response.path("data[5].msgDesc"),equals("NEFT Outward Rejection Message."));
				}				
			}			
		}
	
	

		
}
		
	
	
	
	


