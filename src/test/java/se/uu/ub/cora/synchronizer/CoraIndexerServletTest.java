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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.synchronizer.initialize.LoggerFactorySpy;
import se.uu.ub.cora.synchronizer.initialize.LoggerSpy;
import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;

public class CoraIndexerServletTest {

	private CoraIndexerServlet loginServlet;
	// private Map<String, String> initInfo = new HashMap<>();
	private CoraClientFactorySpy clientFactory;
	private HttpServletRequestSpy request;
	private LoggerFactorySpy loggerFactorySpy;
	private HttpServletResponseSpy response;

	@BeforeMethod
	public void setup() {

		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		// initInfo.put("apptokenVerifierUrl", "http://localhost:8080/apptokenverifier/");
		// initInfo.put("tokenLogoutURL", "http://localhost:8080/apptokenverifier/rest/apptoken/");
		clientFactory = new CoraClientFactorySpy();
		setUpRequestAndResponse();
		SynchronizerInstanceProvider.setClientFactory(clientFactory);
		loginServlet = new CoraIndexerServlet();

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
		assertEquals(clientFactory.userId, "predefinedUserId");
		assertEquals(clientFactory.appToken, "someKnownApptoken");
	}

	@Test
	public void testCoraClient() throws ServletException, IOException {

		loginServlet.doGet(request, response);
		CoraClientSpy coraClient = (CoraClientSpy) clientFactory.returnedClient;
		assertTrue(coraClient.readAsDataRecordWasCalled);
		assertEquals(coraClient.recordTypes.get(0), request.parametersToReturn.get("recordType"));
		assertEquals(coraClient.recordIds.get(0), request.parametersToReturn.get("recordId"));

	}

	@Test
	public void testIndexData() throws ServletException, IOException {
		loginServlet.doGet(request, response);
		CoraClientSpy coraClient = (CoraClientSpy) clientFactory.returnedClient;
		ClientDataRecord dataRecord = coraClient.dataRecordToReturn;
		assertSame(dataRecord, coraClient.dataRecordSentToIndex);
		assertEquals(response.status, 200);

	}

	@Test
	public void testLogging() throws ServletException, IOException {
		loginServlet.doGet(request, response);

		LoggerSpy loggerSpy = loggerFactorySpy.createdLoggers.get("CoraIndexerServlet");

		assertEquals(loggerSpy.infoMessages.get(0),
				"Reading record with recordType: someRecordType and recordId: someRecordId for indexing");
		assertEquals(loggerSpy.infoMessages.get(1),
				"Indexing record with recordType: someRecordType and recordId: someRecordId");
		assertEquals(loggerSpy.infoMessages.get(2),
				"Indexing finished for record with recordType: someRecordType and recordId: someRecordId");
	}
}
