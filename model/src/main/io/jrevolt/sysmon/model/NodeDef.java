package io.jrevolt.sysmon.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 * @version $Id$
 */
public class NodeDef {

	String hostname;
	List<URI> provides;
	List<URI> depends;
	List<URI> requires;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public List<URI> getProvides() {
		return provides;
	}

	public void setProvides(List<URI> provides) {
		this.provides = provides;
	}

	public List<URI> getDepends() {
		return depends;
	}

	public void setDepends(List<URI> depends) {
		this.depends = depends;
	}

	public List<URI> getRequires() {
		return requires;
	}

	public void setRequires(List<URI> requires) {
		this.requires = requires;
	}

	///

	public URI fixup(URI uri) {
		return UriBuilder.fromUri(uri)
				.host(uri.getHost().equals("$hostname") ? getHostname() : uri.getHost())
				.build();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}