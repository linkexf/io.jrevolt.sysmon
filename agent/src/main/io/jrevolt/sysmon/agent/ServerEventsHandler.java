package io.jrevolt.sysmon.agent;

import io.jrevolt.sysmon.common.ServerEvents;
import io.jrevolt.sysmon.common.AgentEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ForkJoinPool;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 * @version $Id$
 */
@Component
public class ServerEventsHandler implements ServerEvents {

	@Autowired
	ConfigurableApplicationContext ctx;

	@Autowired
	AgentEvents events;

	@Override
	public void ping() {
		events.status("pong()");
	}

	@Override
	public void restart() {
		System.out.println("ServerEvents.restart()");
		// restart (special exit code handled by launcher script)
		ForkJoinPool.commonPool().submit(() -> {
			System.out.println("EXIT");
			System.exit(7);
		});
	}
}