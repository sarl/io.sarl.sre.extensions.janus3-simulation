/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2018 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sarl.extensions.sre.simulation.tests.units.services.time;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.extensions.sre.simulation.services.time.SimulatedTimeService;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.sre.services.time.TimeListener;
import io.sarl.sre.services.time.TimeService;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class SimulatedTimeServiceTest extends AbstractSreTest {

	@Nullable
	private SimulatedTimeService service;
	
	@Nullable
	private TimeListener listener;

	@Nullable
	private LoggingService logger;

	@Before
	public void setUp() {
		this.logger = mock(LoggingService.class);
		when(this.logger.getKernelLogger()).thenReturn(mock(Logger.class));
		this.listener = mock(TimeListener.class);
		this.service = new MyTimeService();
		this.service.setLoggingService(this.logger);
		this.service.addTimeListener(this.listener);
	}

	@Test
	public void getTime_beforeEvolution() {
		assertEpsilonEquals(0, this.service.getTime(TimeUnit.MINUTES));
		assertEpsilonEquals(0, this.service.getTime(TimeUnit.SECONDS));
		assertEpsilonEquals(0., this.service.getTime(TimeUnit.MILLISECONDS));
		verifyZeroInteractions(this.listener);
	}

	@Test
	public void getTime_afterEvolution() {
		this.service.evolveTimeIfPossible(4);
		assertEpsilonEquals(0.066, this.service.getTime(TimeUnit.MINUTES));
		assertEpsilonEquals(4, this.service.getTime(TimeUnit.SECONDS));
		assertEpsilonEquals(4000., this.service.getTime(TimeUnit.MILLISECONDS));
		ArgumentCaptor<TimeService> serviceCaptor = ArgumentCaptor.forClass(TimeService.class);
		verify(this.listener, times(1)).timeChanged(serviceCaptor.capture());
		assertSame(this.service, serviceCaptor.getValue());
	}

	@Test
	public void getOSTimeFactor_beforeEvolution() {
		assertEquals(1., this.service.getOSTimeFactor());
		verifyZeroInteractions(this.listener);
	}

	@Test
	public void evolveTimeIfPossible() {
		this.service.evolveTimeIfPossible(15);
		
		ArgumentCaptor<TimeService> serviceCaptor = ArgumentCaptor.forClass(TimeService.class);
		verify(this.listener).timeChanged(serviceCaptor.capture());
		assertSame(this.service, serviceCaptor.getValue());
	}

	private static class MyTimeService extends SimulatedTimeService {
		
		private long ostime = 1500;
		
		public long getOSCurrentTime() {
			return this.ostime;
		}

		@Override
		public void evolveTimeIfPossible(double timeDelta) {
			this.ostime = 1654;
			super.evolveTimeIfPossible(timeDelta);
		}
	}

}
