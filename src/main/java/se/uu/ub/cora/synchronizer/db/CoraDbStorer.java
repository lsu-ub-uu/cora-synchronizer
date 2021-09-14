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

import jakarta.servlet.http.HttpServletResponse;
import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.converter.DataToJsonConverter;
import se.uu.ub.cora.data.converter.DataToJsonConverterFactory;
import se.uu.ub.cora.data.converter.DataToJsonConverterProvider;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformation;

public class CoraDbStorer implements CoraStorer {

	private static Logger logger = LoggerProvider.getLoggerForClass(CoraDbStorer.class);

	private CoraTransformation transformation;
	private CoraClient coraClient;

	String typeAndIdLogPart;

	public CoraDbStorer(CoraTransformation transformation) {
		this.transformation = transformation;
		coraClient = factorCoraClient();
	}

	private CoraClient factorCoraClient() {
		String userId = SynchronizerInstanceProvider.getInitInfo().get("userId");
		String apptoken = SynchronizerInstanceProvider.getInitInfo().get("appToken");
		CoraClientFactory clientFactory = SynchronizerInstanceProvider.getCoraClientFactory();
		return clientFactory.factor(userId, apptoken);
	}

	@Override
	public int storeXML(String xml, String recordType, String recordId) {
		typeAndIdLogPart = " RecordType: " + recordType + " and recordId: " + recordId;
		String coraXml = transformation.transform(xml);

		String jsonCompactFormat = convertXmlToJson(coraXml);
		return tryToUpdate(recordType, recordId, jsonCompactFormat);
	}

	private String convertXmlToJson(String coraXml) {
		Converter xmlConverter = ConverterProvider.getConverter("xml");
		DataGroup dataGroup = (DataGroup) xmlConverter.convert(coraXml);

		DataToJsonConverter toJsonConverter = createJsonConverter(dataGroup);
		return toJsonConverter.toJsonCompactFormat();
	}

	private DataToJsonConverter createJsonConverter(DataGroup dataGroup) {
		DataToJsonConverterFactory dataToJsonConverterFactory = DataToJsonConverterProvider
				.createImplementingFactory();
		return dataToJsonConverterFactory.factorUsingConvertible(dataGroup);
	}

	private int tryToUpdate(String recordType, String recordId, String jsonCompactFormat) {
		try {
			return update(recordType, recordId, jsonCompactFormat);
		} catch (CoraClientException cce) {
			return handleCoraClientErrorWhenUpdate(cce);
		} catch (Exception e) {
			return handleUpdateError(e);
		}
	}

	private int update(String recordType, String recordId, String jsonCompactFormat) {
		logBeforeUpdate();
		coraClient.update(recordType, recordId, jsonCompactFormat);
		logAfterUpdate();

		return HttpServletResponse.SC_OK;
	}

	private void logBeforeUpdate() {
		logger.logInfoUsingMessage("Updating record." + typeAndIdLogPart);
	}

	private void logAfterUpdate() {
		logger.logInfoUsingMessage("Updating finished." + typeAndIdLogPart);
	}

	private int handleCoraClientErrorWhenUpdate(CoraClientException cce) {
		logger.logErrorUsingMessage(
				"Error when updating record." + typeAndIdLogPart + ". " + cce.getMessage());
		return HttpServletResponse.SC_UNAUTHORIZED;
	}

	private int handleUpdateError(Exception e) {
		logger.logErrorUsingMessage(
				"Error when updating record." + typeAndIdLogPart + ". " + e.getMessage());
		return HttpServletResponse.SC_BAD_REQUEST;
	}

	CoraClient getCoraClient() {
		return coraClient;
	}

	// alternativ 1
	// gör en konvertering motsvarande xmlToCoraData - xmlToClientData
	// Nytt projekt då?

	// alternativ 2
	// använd convertering till DataGroup för att sen använda konvertering till json
	// och skicka den till CoraClient

}
