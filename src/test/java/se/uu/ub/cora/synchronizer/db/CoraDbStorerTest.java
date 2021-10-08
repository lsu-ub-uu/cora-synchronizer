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
package se.uu.ub.cora.synchronizer.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletResponse;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.converter.DataToJsonConverterProvider;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.synchronizer.CoraClientFactorySpy;
import se.uu.ub.cora.synchronizer.CoraClientSpy;
import se.uu.ub.cora.synchronizer.initialize.LoggerFactorySpy;
import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;

public class CoraDbStorerTest {

	private LoggerFactorySpy loggerFactorySpy = new LoggerFactorySpy();
	private String testedClassName = "CoraDbStorer";

	private CoraTransformationSpy transformation;
	private ConverterFactorySpy xmlConverterFactory;
	private DataToJsonConverterFactoryCreatorSpy dataToJsonFactoryCreator;
	private CoraClientFactorySpy clientFactory;

	private Map<String, String> initInfo = new HashMap<>();
	private String recordId = "someRecordId";
	private String recordType = "someRecordType";
	private String xml = "<place id=\"1\">\n" + "	<pid>alvin-place:679</pid>\n"
			+ "	<dsId>METADATA</dsId>\n" + "</place>";
	private CoraStorerImp dbStorer;

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		xmlConverterFactory = new ConverterFactorySpy();
		ConverterProvider.setConverterFactory("xml", xmlConverterFactory);

		dataToJsonFactoryCreator = new DataToJsonConverterFactoryCreatorSpy();
		DataToJsonConverterProvider.setDataToJsonConverterFactoryCreator(dataToJsonFactoryCreator);
		clientFactory = new CoraClientFactorySpy();
		SynchronizerInstanceProvider.setClientFactory(clientFactory);

		transformation = new CoraTransformationSpy();
		setUpInitInfo();
		dbStorer = new CoraStorerImp(transformation);
	}

	private void setUpInitInfo() {
		initInfo.put("userId", "somePredefinedUserId");
		initInfo.put("appToken", "someKnownApptoken");
		SynchronizerInstanceProvider.setInitInfo(initInfo);
	}

	@Test
	public void testInit() {
		assertSame(dbStorer.getCoraClient(), clientFactory.returnedClient);
		assertEquals(clientFactory.appToken, initInfo.get("appToken"));
		assertEquals(clientFactory.userId, initInfo.get("userId"));
	}

	@Test
	public void testStoreXml() {

		int result = dbStorer.storeXML(xml, recordType, recordId);
		assertEquals(result, 200);

		ConverterSpy factoredXmlConverter = xmlConverterFactory.factoredConverter;
		DataToJsonConverterFactorySpy dataToJsonConverterFactorySpy = dataToJsonFactoryCreator.dataToJsonConverterFactorySpy;

		assertXmlIsConvertedToJson(factoredXmlConverter, dataToJsonConverterFactorySpy);

		DataToJsonConverterSpy toJsonConverter = dataToJsonConverterFactorySpy.dataToJsonSpy;
		CoraClientSpy returnedCoraClient = clientFactory.returnedClient;

		assertCorrectParametersAreSentToCoraClient(toJsonConverter, returnedCoraClient);

	}

	private void assertXmlIsConvertedToJson(ConverterSpy factoredXmlConverter,
			DataToJsonConverterFactorySpy dataToJsonConverterFactorySpy) {
		assertEquals(transformation.inputXml, xml);
		assertEquals(factoredXmlConverter.dataString, transformation.returnedCoraXml);

		assertSame(dataToJsonConverterFactorySpy.convertible,
				factoredXmlConverter.returnedDataElement);
	}

	private void assertCorrectParametersAreSentToCoraClient(DataToJsonConverterSpy toJsonConverter,
			CoraClientSpy returnedCoraClient) {
		assertEquals(returnedCoraClient.json, toJsonConverter.returnedCompactFormat);
		assertEquals(returnedCoraClient.recordTypes.get(0), recordType);
		assertEquals(returnedCoraClient.recordIds.get(0), recordId);
	}

	@Test
	public void testLoggingWhenStoringXml() {
		dbStorer.storeXML(xml, recordType, recordId);
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Updating record. RecordType: someRecordType and recordId: someRecordId");
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 1),
				"Updating finished. RecordType: someRecordType and recordId: someRecordId");

	}

	@Test
	public void testCoraClientErrorOnUpdate() {
		clientFactory.throwErrorOnUpdate = true;
		dbStorer = new CoraStorerImp(transformation);
		int result = dbStorer.storeXML(xml, recordType, recordId);
		assertEquals(result, HttpServletResponse.SC_UNAUTHORIZED);

		assertCorrectLoggingWhenClientError();
	}

	private void assertCorrectLoggingWhenClientError() {
		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Updating record. RecordType: someRecordType and recordId: someRecordId");
		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when updating record. RecordType: someRecordType and recordId: someRecordId. Error from spy when updating");
	}

	@Test
	public void testErrorOnUpdate() {
		clientFactory.throwErrorOnUpdate = true;
		clientFactory.errorToThrow = "RuntimeException";
		dbStorer = new CoraStorerImp(transformation);
		int result = dbStorer.storeXML(xml, recordType, recordId);
		assertEquals(result, HttpServletResponse.SC_BAD_REQUEST);

		assertEquals(loggerFactorySpy.getInfoLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Updating record. RecordType: someRecordType and recordId: someRecordId");
		assertEquals(loggerFactorySpy.getErrorLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Error when updating record. RecordType: someRecordType and recordId: someRecordId. Some runtime error from spy");

	}

}
