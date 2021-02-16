package se.uu.ub.cora.synchronizer;

import javax.servlet.http.HttpServletResponse;

import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.synchronizer.initialize.SynchronizerInstanceProvider;

public class CoraIndexer {
	private Logger log;
	private String recordType;
	private String recordId;
	private String typeAndIdLogPart;
	private CoraClient coraClient;

	public CoraIndexer(Logger log, String recordType, String recordId) {
		this.log = log;
		this.recordType = recordType;
		this.recordId = recordId;
		typeAndIdLogPart = " RecordType: " + recordType + " and recordId: " + recordId;
		coraClient = factorCoraClient();
	}

	private CoraClient factorCoraClient() {
		String userId = SynchronizerInstanceProvider.getInitInfo().get("userId");
		String apptoken = SynchronizerInstanceProvider.getInitInfo().get("appToken");

		CoraClientFactory clientFactory = SynchronizerInstanceProvider.getCoraClientFactory();
		return clientFactory.factor(userId, apptoken);
	}

	public int handleWorkorderType(String workOrderType) {
		if (isRemove(workOrderType)) {
			return tryToRemoveFromIndex();
		}
		return tryToAddToIndex();
	}

	private boolean isRemove(String workOrderType) {
		return "removeFromIndex".equals(workOrderType);
	}

	private int tryToRemoveFromIndex() {
		try {
			return removeFromIndex();
		} catch (Exception e) {
			return handleRemoveError(e);
		}
	}

	private int removeFromIndex() {
		logBeforeRemovingFromIndex();
		coraClient.removeFromIndex(recordType, recordId);
		logAfterRemovingFromIndex();
		return HttpServletResponse.SC_OK;
	}

	private void logBeforeRemovingFromIndex() {
		logToInfoAppendTypeAndId("Removing from index.");
	}

	private void logToInfoAppendTypeAndId(String messagePart) {
		log.logInfoUsingMessage(messagePart + typeAndIdLogPart);
	}

	private void logAfterRemovingFromIndex() {
		logToInfoAppendTypeAndId("Finished removing.");
	}

	private int handleRemoveError(Exception e) {
		logToErrorAppendTypeAndIdAndError("Error when removing from index.", e);
		return HttpServletResponse.SC_BAD_REQUEST;
	}

	private void logToErrorAppendTypeAndIdAndError(String message, Exception e) {
		log.logErrorUsingMessage(message + typeAndIdLogPart + ". " + e.getMessage());
	}

	private int tryToAddToIndex() {
		try {
			return addToIndex();
		} catch (CoraClientException cce) {
			return handleClientErrorDuringIndexing(cce);
		} catch (Exception e) {
			return handleIndexError(e);
		}
	}

	private int addToIndex() {
		logBeforeIndexing();
		coraClient.indexData(recordType, recordId);
		logAfterIndexing();
		return HttpServletResponse.SC_OK;
	}

	private void logBeforeIndexing() {
		logToInfoAppendTypeAndId("Indexing record.");
	}

	private void logAfterIndexing() {
		logToInfoAppendTypeAndId("Indexing finished.");
	}

	private int handleClientErrorDuringIndexing(CoraClientException cce) {
		logToErrorAppendTypeAndIdAndError("CoraClient error when indexing record.", cce);
		return HttpServletResponse.SC_UNAUTHORIZED;
	}

	private int handleIndexError(Exception e) {
		logToErrorAppendTypeAndIdAndError("Error when indexing record.", e);
		return HttpServletResponse.SC_BAD_REQUEST;
	}
}
