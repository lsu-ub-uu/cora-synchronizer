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

import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;

public class CoraIndexerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerProvider.getLoggerForClass(CoraIndexerServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String recordType = request.getParameter("recordType");
		String recordId = request.getParameter("recordId");
		String workOrderType = request.getParameter("workOrderType");

		if ("removeFromIndex".equals(workOrderType)) {
			int status = tryToRemoveFromIndex(response, recordType, recordId);
			response.setStatus(status);
		} else {
			int status = tryToIndexRecord(recordType, recordId);
			response.setStatus(status);
		}
	}

	private int tryToRemoveFromIndex(HttpServletResponse response, String recordType,
			String recordId) {
		try {
			removeFromIndex(recordType, recordId);
		} catch (Exception e) {
			log.logErrorUsingMessage("Error when removing from index."
					+ getCommonLogMessagePart(recordType, recordId) + ". " + e.getMessage());
			return HttpServletResponse.SC_BAD_REQUEST;
		}
		return HttpServletResponse.SC_OK;
	}

	private void removeFromIndex(String recordType, String recordId) {
		CoraClient coraClient = factorCoraClient();
		logBeforeRemovingFromIndex(recordType, recordId);
		coraClient.removeFromIndex(recordType, recordId);
		logAfterRemovingFromIndex(recordType, recordId);
	}

	private CoraClient factorCoraClient() {
		String userId = SynchronizerInstanceProvider.getInitInfo().get("userId");
		String apptoken = SynchronizerInstanceProvider.getInitInfo().get("appToken");

		CoraClientFactory clientFactory = SynchronizerInstanceProvider.getCoraClientFactory();
		return clientFactory.factor(userId, apptoken);
	}

	private void logBeforeRemovingFromIndex(String recordType, String recordId) {
		log.logInfoUsingMessage(
				"Removing from index." + getCommonLogMessagePart(recordType, recordId));
	}

	private int tryToIndexRecord(String recordType, String recordId) {
		try {
			indexRecord(recordType, recordId);
		} catch (CoraClientException cce) {
			log.logErrorUsingMessage("CoraClient error when indexing record."
					+ getCommonLogMessagePart(recordType, recordId) + ". " + cce.getMessage());
			return HttpServletResponse.SC_UNAUTHORIZED;
		} catch (Exception e) {
			log.logErrorUsingMessage("Error when indexing record."
					+ getCommonLogMessagePart(recordType, recordId) + ". " + e.getMessage());
			return HttpServletResponse.SC_BAD_REQUEST;

		}
		return HttpServletResponse.SC_OK;
	}

	private void logAfterRemovingFromIndex(String recordType, String recordId) {
		log.logInfoUsingMessage(
				"Finished removing." + getCommonLogMessagePart(recordType, recordId));
	}

	private void indexRecord(String recordType, String recordId) {
		CoraClient coraClient = factorCoraClient();

		logBeforeIndexing(recordType, recordId);
		coraClient.indexData(recordType, recordId);
		logAfterIndexing(recordType, recordId);
	}

	private void logAfterIndexing(String recordType, String recordId) {
		log.logInfoUsingMessage(
				"Indexing finished." + getCommonLogMessagePart(recordType, recordId));
	}

	private void logBeforeIndexing(String recordType, String recordId) {
		log.logInfoUsingMessage("Indexing record." + getCommonLogMessagePart(recordType, recordId));
	}

	private String getCommonLogMessagePart(String recordType, String recordId) {
		return " RecordType: " + recordType + " and recordId: " + recordId;
	}

}
