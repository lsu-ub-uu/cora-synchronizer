/*
 * Copyright 2021 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.synchronizer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.synchronizer.initialize.LoggerFactorySpy;
import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;

public class CoraIndexerServletTest {

	private CoraIndexerServlet loginServlet;
	private CoraClientFactorySpy clientFactory;
	private HttpServletRequestSpy request;
	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private HttpServletResponseSpy response;
	private String testedClassName = "CoraIndexerServlet";
	private Map<String, String> initInfo = new HashMap<>();

	@BeforeMethod
	public void setup() {
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		setUpInitInfo();
		clientFactory = new CoraClientFactorySpy();
		setUpRequestAndResponse();
		SynchronizerInstanceProvider.setClientFactory(clientFactory);
		loginServlet = new CoraIndexerServlet();

	}

	private void setUpInitInfo() {
		initInfo.put("userId", "somePredefinedUserId");
		initInfo.put("appToken", "someKnownApptoken");
		SynchronizerInstanceProvider.setInitInfo(initInfo);
	}

	private void setUpRequestAndResponse() {
		request = new HttpServletRequestSpy();
		request.parametersToReturn.put("recordType", "someRecordType");
		request.parametersToReturn.put("recordId", "someRecordId");
		response = new HttpServletResponseSpy();
	}

	@Test
	public void testInit() {
		assertTrue(loginServlet instanceof HttpServlet);
	}

	@Test
	public void testClientFactory() throws ServletException, IOException {
		loginServlet.doGet(request, response);
		assertNotNull(clientFactory.returnedClient);
		assertEquals(clientFactory.userId, initInfo.get("userId"));
		assertEquals(clientFactory.appToken, initInfo.get("appToken"));
	}

	@Test
	public void testCoraClient() throws ServletException, IOException {

		loginServlet.doGet(request, response);
		CoraClientSpy coraClient = clientFactory.returnedClient;
		assertEquals(coraClient.recordTypes.get(0), request.parametersToReturn.get("recordType"));
		assertEquals(coraClient.recordIds.get(0), request.parametersToReturn.get("recordId"));

	}

	@Test
	public void testIndexData() throws ServletException, IOException {
		loginServlet.doGet(request, response);
		CoraClientSpy coraClient = clientFactory.returnedClient;
		assertEquals(coraClient.recordTypes.get(0), request.getParameter("recordType"));
		assertEquals(coraClient.recordIds.get(0), request.getParameter("recordId"));

		assertEquals(response.status, 200);

	}

	@Test
	public void testLogging() throws ServletException, IOException {
		loginServlet.doGet(request, response);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Indexing record. RecordType: someRecordType and recordId: someRecordId");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Indexing finished. RecordType: someRecordType and recordId: someRecordId");
	}

	@Test
	public void testIndexWithNoIndexlink() throws ServletException, IOException {
		clientFactory.throwErrorOnIndex = true;

		loginServlet.doGet(request, response);
		assertEquals(response.status, 401);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"CoraClient error when indexing record. RecordType: someRecordType and recordId: someRecordId. Some error from spy");

	}

	@Test
	public void testIndexWhenOtherErrorIsThrown() throws ServletException, IOException {
		clientFactory.throwErrorOnIndex = true;
		clientFactory.errorToThrow = "RuntimeException";

		loginServlet.doGet(request, response);
		assertEquals(response.status, 400);

		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when indexing record. RecordType: someRecordType and recordId: someRecordId. Some runtime error from spy");

	}
}
