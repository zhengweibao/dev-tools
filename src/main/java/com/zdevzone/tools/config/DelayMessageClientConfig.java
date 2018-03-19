package com.zdevzone.tools.config;

import org.springframework.util.Assert;

/**
 * @author zhengweibao
 */
public class DelayMessageClientConfig {

	public static class Builder {
		private String rabbitHost;

		private Integer rabbitPort;

		private String rabbitVirtualHost;

		private String rabbitUsername;

		private String rabbitPassword;

		/**
		 * 客户端ID 客户端之间消息隔离，一组想要相互订阅相同消息队列的server应该使用相同的client_id
		 */
		private String clientId;

		/**
		 * 节点ID 一组server中，node_id表示其中每个server的唯一标识
		 */
		private String nodeId;

		private Builder(){
		}

		public Builder rabbitHost(String rabbitHost) {
			this.rabbitHost = rabbitHost;
			return this;
		}

		public Builder rabbitPort(Integer rabbitPort) {
			this.rabbitPort = rabbitPort;
			return this;
		}

		public Builder rabbitVirtualHost(String rabbitVirtualHost) {
			this.rabbitVirtualHost = rabbitVirtualHost;
			return this;
		}

		public Builder rabbitUsername(String rabbitUsername) {
			this.rabbitUsername = rabbitUsername;
			return this;
		}

		public Builder rabbitPassword(String rabbitPassword) {
			this.rabbitPassword = rabbitPassword;
			return this;
		}

		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public Builder nodeId(String nodeId) {
			this.nodeId = nodeId;
			return this;
		}

		public DelayMessageClientConfig build(){
			Assert.hasText(rabbitHost, "The rabbitHost cannot be empty!");
			Assert.isTrue(rabbitPort != null && rabbitPort > 0, "The rabbitPort cannot be null or negative!");
			Assert.hasText(rabbitVirtualHost, "The rabbitVirtualHost cannot be empty!");
			Assert.hasText(rabbitUsername, "The rabbitUsername cannot be empty!");
			Assert.hasText(rabbitPassword, "The rabbitPassword cannot be empty!");
			Assert.hasText(clientId, "The clientId cannot be empty!");
			Assert.hasText(nodeId, "The nodeId cannot be empty!");
			
			DelayMessageClientConfig config = new DelayMessageClientConfig();

			config.rabbitHost = this.rabbitHost;
			config.rabbitPort = this.rabbitPort;
			config.rabbitVirtualHost = this.rabbitVirtualHost;
			config.rabbitUsername = this.rabbitUsername;
			config.rabbitPassword = this.rabbitPassword;
			config.clientId = this.clientId;
			config.nodeId = this.nodeId;

			return config;
		}
	}

	private String rabbitHost;

	private Integer rabbitPort;

	private String rabbitVirtualHost;

	private String rabbitUsername;

	private String rabbitPassword;

	private String clientId;

	private String nodeId;

	private DelayMessageClientConfig() {}

	public String getRabbitHost() {
		return rabbitHost;
	}

	public Integer getRabbitPort() {
		return rabbitPort;
	}

	public String getRabbitVirtualHost() {
		return rabbitVirtualHost;
	}

	public String getRabbitUsername() {
		return rabbitUsername;
	}

	public String getRabbitPassword() {
		return rabbitPassword;
	}

	public String getClientId() {
		return clientId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public static Builder builder(){
		return new Builder();
	}
}
