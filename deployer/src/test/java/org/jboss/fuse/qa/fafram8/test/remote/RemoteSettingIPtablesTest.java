package org.jboss.fuse.qa.fafram8.test.remote;

import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.cluster.node.Node;
import org.jboss.fuse.qa.fafram8.exceptions.KarafSessionDownException;
import org.jboss.fuse.qa.fafram8.exceptions.SSHClientException;
import org.jboss.fuse.qa.fafram8.exceptions.VerifyFalseException;
import org.jboss.fuse.qa.fafram8.property.FaframConstant;
import org.jboss.fuse.qa.fafram8.property.FaframProvider;
import org.jboss.fuse.qa.fafram8.resource.Fafram;
import org.jboss.fuse.qa.fafram8.test.base.FaframTestBase;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for loading custom iptables configuration file from local to remote machine.
 *
 * @author : Roman Jakubco (rjakubco@redhat.com)
 */
public class RemoteSettingIPtablesTest {
	private Container root = RootContainer.builder().name("build-load-ip-root").build();
	private Container ssh = SshContainer.builder().name("build-load-ip-ssh").parentName("build-load-ip-root").build();

	@Rule
	public Fafram fafram = new Fafram().withFabric().provider(FaframProvider.OPENSTACK).containers(root, ssh)
			.loadIPtablesConfigurationFile("target/test-classes/iptables-test").suppressStart();

	@BeforeClass
	public static void before() {
		System.setProperty(FaframConstant.FUSE_ZIP, FaframTestBase.CURRENT_LOCAL_URL);
	}

	@After
	public void tearDown() {
		System.clearProperty(FaframConstant.FUSE_ZIP);
	}

	@Test
	public void testLoadingIptables() throws VerifyFalseException, SSHClientException, KarafSessionDownException {
		String response = root.getNode().getExecutor().executeCommand("sudo iptables -L -n");
		assertTrue(response.contains("ACCEPT     icmp --  0.0.0.0/0") && response.contains("state NEW tcp dpt:22"));

		response = ssh.executeNodeCommand("sudo iptables -L -n");
		assertTrue(response.contains("ACCEPT     icmp --  0.0.0.0/0") && response.contains("state NEW tcp dpt:22"));
	}
}
