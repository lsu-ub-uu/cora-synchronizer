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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.synchronizer.CoraClientFactorySpy;

public class SynchronizerInstanceProviderTest {

	private LoggerFactorySpy loggerFactorySpy;

	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<SynchronizerInstanceProvider> constructor = SynchronizerInstanceProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<SynchronizerInstanceProvider> constructor = SynchronizerInstanceProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testClientFactory() {
		CoraClientFactory gatekeeperTokenProvider = new CoraClientFactorySpy();
		SynchronizerInstanceProvider.setClientFactory(gatekeeperTokenProvider);
		assertEquals(SynchronizerInstanceProvider.getClientFactory(), gatekeeperTokenProvider);
	}

	@Test
	public void testSetInitInfo() throws Exception {
		Map<String, String> initInfo = new HashMap<>();
		SynchronizerInstanceProvider.setInitInfo(initInfo);
		assertEquals(SynchronizerInstanceProvider.getInitInfo(), initInfo);
	}
}
