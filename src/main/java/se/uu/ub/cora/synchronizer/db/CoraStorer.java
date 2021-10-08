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

/**
 * CoraStorer takes a record in a xml format, transform it to a format suitable for Cora storage and
 * stores the record in that format.
 */
public interface CoraStorer {

	/**
	 * Stores a xml record in cora storage, using recordType and recordid, which are needed by Cora
	 * to store a record.
	 * 
	 * @param String
	 *            xml, the xml to handle and store
	 * 
	 * @param String
	 *            recordtype, the recordType of the record
	 * 
	 * @param String
	 *            recordId, the recordId of the record
	 * 
	 * 
	 */
	int storeXML(String xml, String recordType, String recordId);

}
