package org.jrevolt.sysmon.agent;

import org.jrevolt.sysmon.common.AgentEvents;
import org.jrevolt.sysmon.common.ServerEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

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
		ctx.close();
		System.exit(1); // restart
	}
}
