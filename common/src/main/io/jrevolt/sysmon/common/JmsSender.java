package io.jrevolt.sysmon.common;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 * @version $Id$
 */
@Component
public class JmsSender implements InvocationHandler {

	static private final Logger LOG = LoggerFactory.getLogger(JmsSender.class);

	@Autowired
	ConnectionFactory cf;

	@Autowired
	JmsCfg jmscfg;

	@Autowired
	JmsTemplate jmsTemplate;

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		String destination = jmscfg.getDestinationName(method);
		JMS jms = method.getAnnotation(JMS.class);
		JmsTemplate template = new JmsTemplate(cf);
		template.setPubSubDomain(jms.topic());
		template.setExplicitQosEnabled(true);
		template.setTimeToLive(jms.timeToLive());
		template.setMessageConverter(jmscfg.getMessageConverter());
		template.send(destination, session -> jmscfg.getMessageConverter().toMessage(args, session));
		if (LOG.isDebugEnabled()) {
			Map<String, Object> map = new HashMap<>();
			Parameter[] params = method.getParameters();
			for (int i=0; i<params.length; i++) {
				map.put(params[i].getName(), args[i]);
			}
			LOG.debug("Message sent: {} {}", destination, map);
		}
		return null;
	}
}