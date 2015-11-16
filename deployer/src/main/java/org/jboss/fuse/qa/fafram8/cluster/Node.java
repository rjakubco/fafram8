package org.jboss.fuse.qa.fafram8.cluster;

import lombok.Getter;
import lombok.Setter;

/**
 * Class representing node
 * Created by mmelko on 09/10/15.
 */
public class Node implements Cloneable {
	@Getter
	@Setter
	private String nodeId;

	@Getter
	@Setter
	private boolean live;

	@Getter
	@Setter
	private String host;

	@Getter
	@Setter
	private int port;

	@Getter
	@Setter
	private String username;

	@Getter
	@Setter
	private String password;

	@Getter
	@Setter
	private String privateKey;

	@Getter
	@Setter
	private String passPhrase;

	/**
	 * Constructor.
	 *
	 * @param hostNode node which will be cloned
	 */
	public Node(Node hostNode) {
		this.nodeId = hostNode.getNodeId();
		this.host = hostNode.getHost();
		this.port = hostNode.getPort();
		this.username = hostNode.getUsername();
		this.password = hostNode.getPassword();
		this.privateKey = hostNode.getPrivateKey();
		this.passPhrase = hostNode.getPassPhrase();
	}

	/**
	 * Constructor.
	 */
	public Node() {
		super();
	}

	@Override
	public String toString(){
		return nodeId+" | "+host+" | "+username+":"+password;
	}
}
