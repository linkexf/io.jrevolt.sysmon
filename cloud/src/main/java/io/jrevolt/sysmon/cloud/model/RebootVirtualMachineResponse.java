package io.jrevolt.sysmon.cloud.model;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 */
public class RebootVirtualMachineResponse extends ApiObject {

	String jobid;
	VirtualMachine virtualmachine;

	public String getJobid() {
		return jobid;
	}

	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	public VirtualMachine getVirtualmachine() {
		return virtualmachine;
	}

	public void setVirtualmachine(VirtualMachine virtualmachine) {
		this.virtualmachine = virtualmachine;
	}
}
