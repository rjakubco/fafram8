package org.jboss.fuse.qa.fafram8.cluster.xml.toplevel;

import org.jboss.fuse.qa.fafram8.cluster.container.ChildContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.Container;
import org.jboss.fuse.qa.fafram8.cluster.container.RootContainer;
import org.jboss.fuse.qa.fafram8.cluster.container.SshContainer;
import org.jboss.fuse.qa.fafram8.cluster.resolver.Resolver;
import org.jboss.fuse.qa.fafram8.cluster.xml.container.XmlChildContainerModel;
import org.jboss.fuse.qa.fafram8.cluster.xml.container.XmlRootContainerModel;
import org.jboss.fuse.qa.fafram8.cluster.xml.container.XmlSshContainerModel;
import org.jboss.fuse.qa.fafram8.cluster.xml.util.UserModel;
import org.jboss.fuse.qa.fafram8.exception.FaframException;
import org.jboss.fuse.qa.fafram8.manager.ContainerManager;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import java.lang.reflect.Field;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * ContainerModel class represents XML to object model mapping.
 * Containers in XML configuration are mapped to this class.
 * <p/>
 * Created by ecervena on 1/11/16.
 */
@XmlRootElement(name = "containers")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@ToString
@Slf4j
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class ContainersModel {
	@XmlElements({
			@XmlElement(name = "root", type = XmlRootContainerModel.class),
			@XmlElement(name = "child", type = XmlChildContainerModel.class),
			@XmlElement(name = "ssh", type = XmlSshContainerModel.class)
	})
	private List<Object> containerModelList;

	/**
	 * Builds all containers and adds them into the container list.
	 */
	public void buildContainers() {
		Container c;
		for (Object obj : containerModelList) {
			for (int i = 1; i <= getObjFieldValue("instances", obj, Integer.class); i++) {
				if (!getObjFieldValue("template", obj, Boolean.class)) {
					if (obj instanceof XmlRootContainerModel) {
						final XmlRootContainerModel root = (XmlRootContainerModel) obj;
						c = buildRootContainer(root);
						if (root.getInstances() > 1) {
							c.setName(c.getName() + i);
						}
					} else if (obj instanceof XmlChildContainerModel) {
						final XmlChildContainerModel child = (XmlChildContainerModel) obj;
						c = buildChildContainer(child);
						if (child.getInstances() > 1) {
							c.setName(c.getName() + i);
						}
					} else {
						final XmlSshContainerModel ssh = (XmlSshContainerModel) obj;
						c = buildSshContainer(ssh);
						if (ssh.getInstances() > 1) {
							c.setName(c.getName() + i);
						}
					}
					log.trace("Parsed container: " + c.toString());
					ContainerManager.getContainerList().add(c);
				}
			}
		}
	}

	/**
	 * Builds the root container from the model.
	 *
	 * @param root root model.
	 * @return root container
	 */
	public Container buildRootContainer(XmlRootContainerModel root) {
		RootContainer.RootBuilder builder;

		if (root.getRef() != null) {
			builder = RootContainer.builder(buildRootContainer(getModel(root.getRef(), XmlRootContainerModel.class)));
		} else {
			builder = RootContainer.builder();
		}

		if (root.getName() == null) {
			throw new FaframException("Root container name is not set!");
		}
		builder.name(root.getName());
		if (root.getJvmOpts() != null) {
			builder.jvmOpts(root.getJvmOpts());
		}
		if (root.getCommandsModel() != null) {
			builder.commands(root.getCommandsModel().getCommands().toArray(new String[root.getCommandsModel().getCommands().size()]));
		}
		if (root.getProfilesModel() != null) {
			builder.profiles(root.getProfilesModel().getProfiles().toArray(new String[root.getProfilesModel().getProfiles().size()]));
		}
		if (root.isFabric()) {
			builder.withFabric();
		}
		if (root.getJvmMemoryOpts() != null) {
			builder.jvmMemoryOpts(root.getJvmMemoryOpts().getXms(), root.getJvmMemoryOpts().getXmx(), root.getJvmMemoryOpts().getPermMem(), root.getJvmMemoryOpts().getMaxPermMem());
		}
		if (root.getNode() != null) {
			builder.node(root.getNode().createNode());
		}
		if (root.getWorkingDir() != null) {
			builder.directory(root.getWorkingDir());
		}
		if (root.getUsersModel() != null) {
			for (UserModel user : root.getUsersModel().getUsers()) {
				builder.addUser(user.getName(), user.getPassword(), user.getRoles());
			}
		}
		if (root.getBundlesModel() != null) {
			builder.bundles(root.getBundlesModel().getBundles().toArray(new String[root.getBundlesModel().getBundles().size()]));
		}

		return builder.build();
	}

	/**
	 * Builds the child container from the model.
	 *
	 * @param child child model
	 * @return child container
	 */
	public Container buildChildContainer(XmlChildContainerModel child) {
		ChildContainer.ChildBuilder builder;

		if (child.getRef() != null) {
			builder = ChildContainer.builder(buildChildContainer(getModel(child.getRef(), XmlChildContainerModel.class)));
		} else {
			builder = ChildContainer.builder();
		}

		if (child.getName() == null && !child.isTemplate() && builder.getContainer().getName() == null) {
			throw new FaframException("Child container name is not set!");
		}
		// If we are overriding template in the container
		if (child.getName() != null) {
			builder.name(child.getName());
		}
		if (child.getParentName() == null && !child.isTemplate() && builder.getContainer().getParentName() == null) {
			throw new FaframException("Child container parent name is not set!");
		}
		// If we are overriding template in the container
		if (child.getParentName() != null) {
			builder.parentName(child.getParentName());
		}
		if (child.getVersion() != null) {
			builder.version(child.getVersion());
		}
		if (child.getJvmOpts() != null) {
			builder.jvmOpts(child.getJvmOpts());
		}
		if (child.getJmxUser() != null) {
			builder.jmxUser(child.getJmxUser());
		}
		if (child.getJmxPassword() != null) {
			builder.jmxPassword(child.getJmxPassword());
		}
		if (child.getResolver() != null) {
			builder.resolver(Resolver.valueOf(child.getResolver().toUpperCase()));
		}
		if (child.getManualIp() != null) {
			builder.manualIp(child.getManualIp());
		}
		if (child.getCommandsModel() != null) {
			builder.commands(child.getCommandsModel().getCommands().toArray(new String[child.getCommandsModel().getCommands().size()]));
		}
		if (child.getProfilesModel() != null) {
			builder.profiles(child.getProfilesModel().getProfiles().toArray(new String[child.getProfilesModel().getProfiles().size()]));
		}
		if (child.getSameNodeAs() != null) {
			builder.sameNodeAs(child.getSameNodeAs());
		}
		return builder.build();
	}

	/**
	 * Builds the ssh container from the model.
	 *
	 * @param ssh ssh model
	 * @return ssh container
	 */
	public Container buildSshContainer(XmlSshContainerModel ssh) {
		SshContainer.SshBuilder builder;

		if (ssh.getRef() != null) {
			builder = SshContainer.builder(buildSshContainer(getModel(ssh.getRef(), XmlSshContainerModel.class)));
		} else {
			builder = SshContainer.builder();
		}

		if (ssh.getName() == null && !ssh.isTemplate() && builder.getContainer().getName() == null) {
			throw new FaframException("Ssh container name is not set!");
		}
		// If we are overriding template in the container
		if (ssh.getName() != null) {
			builder.name(ssh.getName());
		}

		if (ssh.getParentName() == null && !ssh.isTemplate() && builder.getContainer().getParentName() == null) {
			throw new FaframException("Ssh container parent name is not set!");
		}
		// If we are overriding template in the container
		if (ssh.getParentName() != null) {
			builder.parentName(ssh.getParentName());
		}

		if (ssh.getNode() != null && !ssh.isTemplate()) {
			builder.node(ssh.getNode().createNode());
		}

		if (ssh.getVersion() != null) {
			builder.version(ssh.getVersion());
		}
		if (ssh.getJvmOpts() != null) {
			builder.jvmOpts(ssh.getJvmOpts());
		}
		if (ssh.getResolver() != null) {
			builder.resolver(Resolver.valueOf(ssh.getResolver().toUpperCase()));
		}
		if (ssh.getManualIp() != null) {
			builder.manualIp(ssh.getManualIp());
		}
		if (ssh.getCommandsModel() != null) {
			builder.commands(ssh.getCommandsModel().getCommands().toArray(new String[ssh.getCommandsModel().getCommands().size()]));
		}
		if (ssh.getProfilesModel() != null) {
			builder.profiles(ssh.getProfilesModel().getProfiles().toArray(new String[ssh.getProfilesModel().getProfiles().size()]));
		}
		if (ssh.getEnv() != null) {
			builder.env(ssh.getEnv());
		}
		if (ssh.getWorkingDir() != null) {
			builder.directory(ssh.getWorkingDir());
		}
		if (ssh.getZookeeperPassword() != null) {
			builder.zookeeperPassword(ssh.getZookeeperPassword());
		}
		if (ssh.getPrivateKey() != null) {
			builder.privateKey(ssh.getPrivateKey());
		}
		if (ssh.getPassPhrase() != null) {
			builder.passPhrase(ssh.getPassPhrase());
		}
		if (ssh.getProxyUri() != null) {
			builder.proxyUri(ssh.getProxyUri());
		}
		if (ssh.getMinPort() > 0) {
			builder.minPort(ssh.getMinPort());
		}
		if (ssh.getMaxPort() > 0) {
			builder.maxPort(ssh.getMaxPort());
		}
		if (ssh.getSameNodeAs() != null) {
			builder.sameNodeAs(ssh.getSameNodeAs());
		}
		return builder.build();
	}

	/**
	 * Gets the model by its id.
	 *
	 * @param id XML element id
	 * @param type type to return
	 * @param <T> class to return
	 * @return container model representation of the container with specified id
	 */
	public <T> T getModel(String id, Class<T> type) {
		for (Object obj : containerModelList) {
			if (id.equals(getObjFieldValue("id", obj, String.class))) {
				return type.cast(obj);
			}
		}
		throw new FaframException("Ref " + id + " not found");
	}

	/**
	 * Gets the value of the field from the object and returns the type based on the parameter.
	 * @param field field
	 * @param obj object instance
	 * @param type type to return
	 * @param <T> type to return
	 * @return value of the field
	 */
	public <T> T getObjFieldValue(String field, Object obj, Class<T> type) {
		try {
			final Field f = obj.getClass().getDeclaredField(field);
			f.setAccessible(true);
			return type.cast(f.get(obj));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
			throw new FaframException("Get object field value failed with: " + e);
		}
	}
}


