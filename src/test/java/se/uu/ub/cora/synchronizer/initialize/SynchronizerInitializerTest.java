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

package se.uu.ub.cora.synchronizer.initialize;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.ServletContextEvent;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;

public class SynchronizerInitializerTest {
	private SynchronizerInitializer synchronizerInitializer;
	private ServletContextSpy source;
	private ServletContextEvent context;
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "SynchronizerInitializer";

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		source = new ServletContextSpy();
		source.setInitParameter("apptokenVerifierURL", "http://localhost:8080/apptokenverifier/");
		source.setInitParameter("baseURL", "http://localhost:8080/baseSystem/");
		source.setInitParameter("userId", "someUserId");
		source.setInitParameter("appToken", "someApptoken");
		context = new ServletContextEvent(source);

		synchronizerInitializer = new SynchronizerInitializer();
	}

	@Test
	public void testCoraClientFactoryIsSet() {
		synchronizerInitializer.contextInitialized(context);
		CoraClientFactoryImp clientFactory = (CoraClientFactoryImp) SynchronizerInstanceProvider
				.getCoraClientFactory();
		assertEquals(clientFactory.getAppTokenVerifierUrl(),
				"http://localhost:8080/apptokenverifier/");
		assertEquals(clientFactory.getBaseUrl(), "http://localhost:8080/baseSystem/");

	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting Synchronizer: InitInfo must contain apptokenVerifierURL")
	public void testInitializeSystemWithoutApptokenVerifierURL() {
		source.removeInitParameter("apptokenVerifierURL");

		synchronizerInitializer.contextInitialized(context);
	}

	@Test
	public void testErrorIsLoggedIfMissingApptokenVerifierURL() throws Exception {
		source.removeInitParameter("apptokenVerifierURL");
		try {
			synchronizerInitializer.contextInitialized(context);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain apptokenVerifierURL");
	}

	@Test
	public void testInitInfoSetInSynchronizerInstanceProvider() throws Exception {
		synchronizerInitializer.contextInitialized(context);
		assertEquals(SynchronizerInstanceProvider.getInitInfo().get("userId"), "someUserId");
		assertEquals(SynchronizerInstanceProvider.getInitInfo().get("appToken"), "someApptoken");
		assertEquals(SynchronizerInstanceProvider.getInitInfo().get("apptokenVerifierURL"),
				"http://localhost:8080/apptokenverifier/");
		assertEquals(SynchronizerInstanceProvider.getInitInfo().get("baseURL"),
				"http://localhost:8080/baseSystem/");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting Synchronizer: InitInfo must contain baseURL")
	public void testInitializeSystemWithoutBaseURL() {
		source.removeInitParameter("baseURL");
		synchronizerInitializer.contextInitialized(context);
	}

	@Test
	public void testInitializeSystemWithoutBaseURLSendsAlongInitalException() {
		source.removeInitParameter("baseURL");
		try {
			synchronizerInitializer.contextInitialized(context);
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof RuntimeException);
		}
	}

	@Test
	public void testErrorIsLoggedIfMissingBaseURL() throws Exception {
		source.removeInitParameter("baseURL");
		try {
			synchronizerInitializer.contextInitialized(context);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain baseURL");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting Synchronizer: InitInfo must contain userId")
	public void testInitializeSystemWithoutUserId() {
		source.removeInitParameter("userId");
		synchronizerInitializer.contextInitialized(context);
	}

	@Test
	public void testErrorIsLoggedIfMissingUserId() throws Exception {
		source.removeInitParameter("userId");
		try {
			synchronizerInitializer.contextInitialized(context);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain userId");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error "
			+ "starting Synchronizer: InitInfo must contain appToken")
	public void testInitializeSystemWithoutAppToken() {
		source.removeInitParameter("appToken");
		synchronizerInitializer.contextInitialized(context);
	}

	@Test
	public void testErrorIsLoggedIfMissingAppToken() throws Exception {
		source.removeInitParameter("appToken");
		try {
			synchronizerInitializer.contextInitialized(context);
		} catch (Exception e) {

		}
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"InitInfo must contain appToken");
	}

	@Test
	public void testDestroySystem() {
		SynchronizerInitializer synchronizerInitializer = new SynchronizerInitializer();
		synchronizerInitializer.contextDestroyed(null);
		// TODO: should we do something on destroy?
	}

	@Test
	public void testLogMessagesOnStartUp() throws Exception {
		synchronizerInitializer.contextInitialized(context);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"SynchronizerInitializer starting...");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Found http://localhost:8080/apptokenverifier/ as apptokenVerifierURL");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 2),
				"Found http://localhost:8080/baseSystem/ as baseURL");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 3),
				"Found userId");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 4),
				"Found appToken");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 5),
				"SynchronizerInitializer started");
	}
}
