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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CoraIndexerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int status = handleRequestUsingIndexer(request);
		response.setStatus(status);
	}

	private int handleRequestUsingIndexer(HttpServletRequest request) {
		CoraIndexer indexer = createIndexerFromRequest(request);
		String workOrderType = request.getParameter("workOrderType");
		return indexer.handleWorkorderType(workOrderType);
	}

	private CoraIndexer createIndexerFromRequest(HttpServletRequest request) {
		String recordType = request.getParameter("recordType");
		String recordId = request.getParameter("recordId");
		return new CoraIndexer(recordType, recordId);
	}

}
