package org.jboss.fuse.qa.fafram8.provision.provider;

import org.jboss.fuse.qa.fafram8.cluster.container.Container;

import java.util.List;

/**
 * ProvisionProvider interface.
 * <p/>
 * Created by ecervena on 7.10.15.
 */
public interface ProvisionProvider {

	/**
	 * Creates pool of nodes prepared to be assigned to containers.
	 *
	 * @param containerList list of containers
	 */
	void createServerPool(List<Container> containerList);

	/**
	 * Assigns IP addresses of created nodes to containers. If container is marked as root public IP should be assigned.
	 *
	 * @param containerList list of containers to assign addresses
	 */
	void assignAddresses(List<Container> containerList);

	/**
	 * Release all allocated resources.
	 */
	void releaseResources();

	/**
	 * Loads iptables configuration file on all nodes specified in container list.
	 * <p/>
	 * By default method looks for file specified in iptables.conf.file.path system property and copies this file to all
	 * remote nodes to home folder of user. If FaframConstant.WORKING_DIRECTORY property is set then it tries to copy the
	 * file to all nodes in specified folder(this folder must exists on all nodes!)
	 * <p/>
	 * Method behaves differently for OpenStackProvider. If FaframConstant.OFFLINE property is set to true then the method
	 * looks for default iptables-no-internet file in user's home folder(present in ecervena snapshots). This
	 * configuration turns off internet completely.
	 * <p/>
	 * For static provider it is in experimental stage. Default iptables configuration is saved to newly created file on
	 * all nodes. After the test or when there is a exception in FaFram setup then the iptables are restored back to normal
	 * using the created file.
	 *
	 * @param containerList list of containers
	 */
	void loadIPTables(List<Container> containerList);

	/**
	 * Experimental method for cleaning iptables configuration on all nodes. This method restore iptables back to default
	 * using the "sudo iptables-restore savedFile" command.
	 * <p/>
	 * USE ONLY ON YOUR OWN RISK!!!
	 *
	 * @param containerList list of containers
	 */
	void cleanIpTables(List<Container> containerList);

	/**
	 * Checks if there are already nodes with the same name defined (for Openstack only).
	 *
	 * @param containerList container list
	 */
	void checkNodes(List<Container> containerList);
}
