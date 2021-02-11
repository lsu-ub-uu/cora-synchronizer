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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;

public class CoraIndexerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Logger log = LoggerProvider.getLoggerForClass(CoraIndexerServlet.class);

		String recordType = request.getParameter("recordType");
		String recordId = request.getParameter("recordId");

		int status = tryToIndexRecord(log, recordType, recordId);
		response.setStatus(status);

	}

	private int tryToIndexRecord(Logger log, String recordType, String recordId) {
		int status = HttpServletResponse.SC_OK;
		String commonLogMessage = " RecordType: " + recordType + " and recordId: " + recordId;
		try {
			indexRecord(recordType, recordId, commonLogMessage, log);
		} catch (CoraClientException cce) {
			status = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
			log.logErrorUsingMessage(
					"Error when indexing record." + commonLogMessage + ". " + cce.getMessage());
		} catch (Exception e) {
			status = HttpServletResponse.SC_BAD_REQUEST;
			log.logErrorUsingMessage(
					"Error when indexing record." + commonLogMessage + ". " + e.getMessage());

		}
		return status;
	}

	private void indexRecord(String recordType, String recordId, String commonLogMessage,
			Logger log) {
		CoraClient coraClient = factorCoraClient();
		ClientDataRecord readRecord = readRecord(coraClient, recordType, recordId, log);

		log.logInfoUsingMessage("Indexing record." + commonLogMessage);

		coraClient.indexData(readRecord);
		log.logInfoUsingMessage("Indexing finished." + commonLogMessage);
	}

	private CoraClient factorCoraClient() {
		// don't know what this should be yet
		String predefinedUserId = "predefinedUserId";
		String predefinedApptoken = "someKnownApptoken";

		CoraClientFactory clientFactory = SynchronizerInstanceProvider.getClientFactory();
		return clientFactory.factor(predefinedUserId, predefinedApptoken);
	}

	private ClientDataRecord readRecord(CoraClient coraClient, String recordType, String recordId,
			Logger log) {
		log.logInfoUsingMessage("Reading for indexing record. RecordType: " + recordType
				+ " and recordId: " + recordId);
		return coraClient.readAsDataRecord(recordType, recordId);
	}

}
