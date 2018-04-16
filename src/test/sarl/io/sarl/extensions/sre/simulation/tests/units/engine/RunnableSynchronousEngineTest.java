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

package io.sarl.extensions.sre.simulation.tests.units.engine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.extensions.sre.simulation.boot.factories.TimeFactory;
import io.sarl.extensions.sre.simulation.engine.AgentScheduler;
import io.sarl.extensions.sre.simulation.engine.RunnableSynchronousEngine;
import io.sarl.extensions.sre.simulation.services.lifecycle.SimulationLifecycleService;
import io.sarl.lang.core.Agent;
import io.sarl.sre.services.lifecycle.LifecycleService;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.sre.services.time.TimeService;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class RunnableSynchronousEngineTest extends AbstractSreTest {

	@Nullable
	private RunnableSynchronousEngine engine;

	@Nullable
	private TimeFactory timeFactory;

	@Nullable
	private AgentScheduler agentScheduler;

	@Nullable
	private SimulationLifecycleService lifecycleService;

	@Nullable
	private TimeService timeService;

	@Nullable
	private Iterable<Agent> agents;

	@Before
	public void setUp() {
		this.agentScheduler = mock(AgentScheduler.class);
		when(this.agentScheduler.schedule(any(Iterable.class))).thenAnswer((it) -> {
			return ((Iterable<? extends Agent>) it.getArgument(0)).iterator();
		});
		
		Iterator<Agent> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(false);
		this.agents = mock(Iterable.class);
		when(this.agents.iterator()).thenReturn(iterator);

		this.timeFactory = mock(TimeFactory.class);
		when(this.timeFactory.getStartTime()).thenReturn(12.34);
		when(this.timeFactory.getTimeStep()).thenReturn(90.12);

		this.lifecycleService = mock(SimulationLifecycleService.class);
		final AtomicBoolean bool = new AtomicBoolean(true);
		when(this.lifecycleService.hasAgent()).thenAnswer((it) -> bool.getAndSet(false));
		when(this.lifecycleService.getAgents()).thenReturn(this.agents);

		this.timeService = mock(TimeService.class);

		this.engine = new RunnableSynchronousEngine();
		this.engine.setTimeConfiguration(this.timeFactory);
		this.engine.setLifecycleService(this.lifecycleService);
		this.engine.setTimeManager(this.timeService);
		this.engine.setAgentScheduler(this.agentScheduler);
	}

	protected void applyDelay() {
		when(this.timeFactory.getSimulationLoopDelay()).thenReturn(1l);
	}

	protected void applyNoDelay() {
		when(this.timeFactory.getSimulationLoopDelay()).thenReturn(0l);
	}

	@Test
	public void run_delay() {
		applyDelay();

		this.engine.run();

		ArgumentCaptor<Double> doubleArg = ArgumentCaptor.forClass(Double.class);
		verify(this.timeService, times(1)).setTimeIfPossible(doubleArg.capture());
		assertEpsilonEquals(12.34, doubleArg.getValue());

		verify(this.lifecycleService, times(1)).synchronizeAgentList();

		verify(this.timeService, times(1)).evolveTimeIfPossible(doubleArg.capture());
		assertEpsilonEquals(90.12, doubleArg.getValue());
	}

	@Test
	public void run_noDelay() {
		applyNoDelay();

		this.engine.run();

		ArgumentCaptor<Double> doubleArg = ArgumentCaptor.forClass(Double.class);
		verify(this.timeService, times(1)).setTimeIfPossible(doubleArg.capture());
		assertEpsilonEquals(12.34, doubleArg.getValue());

		verify(this.lifecycleService, times(1)).synchronizeAgentList();

		verify(this.timeService, times(1)).evolveTimeIfPossible(doubleArg.capture());
		assertEpsilonEquals(90.12, doubleArg.getValue());
	}

}