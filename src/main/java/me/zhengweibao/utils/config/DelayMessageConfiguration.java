package me.zhengweibao.utils.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Objects;

/**
 * @author zhengweibao
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EnableRabbit
public class DelayMessageConfiguration {

	public static final String HANDLER_ID_PROPERTY_NAME = "target_handler_id";

	/**
	 * 延时消息时长阈值，当延时消息时长大于该值时，将以接力方式传递
	 */
	public static final Long MAX_DELAY_MESSAGE_TIME = 1000L * 60L * 60L * 24L * 15L;

	/**
	 * 延时消息exchange格式   delay_message.exchange.{client_id}
	 */
	private static final String DELAY_MESSAGE_EXCHANGE_FORMAT = "delay_message.exchange.%s";

	/**
	 * 延时消息同一客户端共享队列  delay_message.share_queue
	 */
	private static final String DELAY_MESSAGE_SHARE_QUEUE = "delay_message.share_queue";

	/**
	 * 延时消息当前节点独立队列格式   delay_message.current_node_queue.{node_id}
	 */
	private static final String DELAY_MESSAGE_CURRENT_NODE_QUEUE_FORMAT = "delay_message.current_node_queue.%s";

	/**
	 * 延时消息当前节点独立队列routing key  delay_message.current_node_queue.*
	 */
	private static final String DELAY_MESSAGE_CURRENT_NODE_QUEUE_ROUTING_KEY = "delay_message.current_node_queue.*";

	private DelayMessageClientConfig delayMessageClientConfig;

	@Autowired
	public void setDelayMessageClientConfig(DelayMessageClientConfig delayMessageClientConfig) {
		this.delayMessageClientConfig = Objects.requireNonNull(delayMessageClientConfig, "delayMessageClientConfig");
	}

	@Bean
	public ConnectionFactory delayMessageRabbitConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(delayMessageClientConfig.getRabbitHost(), delayMessageClientConfig.getRabbitPort());
		connectionFactory.setVirtualHost(delayMessageClientConfig.getRabbitVirtualHost());
		connectionFactory.setUsername(delayMessageClientConfig.getRabbitUsername());
		connectionFactory.setPassword(delayMessageClientConfig.getRabbitPassword());
		connectionFactory.setPublisherConfirms(true);

		return connectionFactory;
	}

	@Bean
	public AmqpAdmin delayMessageRabbitAmqpAdmin() {
		return new RabbitAdmin(delayMessageRabbitConnectionFactory());
	}

	@Bean
	public TopicExchange delayMessageExchange(){
		TopicExchange delayMessageExchange = new TopicExchange(String.format(DELAY_MESSAGE_EXCHANGE_FORMAT, delayMessageClientConfig.getClientId()));
		delayMessageExchange.setAdminsThatShouldDeclare(delayMessageRabbitAmqpAdmin());
		delayMessageExchange.setDelayed(true);

		return delayMessageExchange;
	}

	@Bean
	public Queue delayMessageClientShareQueue(){
		Queue delayMessageClientShareQueue = new Queue(DELAY_MESSAGE_SHARE_QUEUE);
		delayMessageClientShareQueue.setAdminsThatShouldDeclare(delayMessageRabbitAmqpAdmin());

		return delayMessageClientShareQueue;
	}

	@Bean
	public Binding delayMessageClientShareQueueBinding(){
		Binding delayMessageClientShareQueue = BindingBuilder.bind(delayMessageClientShareQueue()).to(delayMessageExchange()).with(DELAY_MESSAGE_SHARE_QUEUE);
		delayMessageClientShareQueue.setAdminsThatShouldDeclare(delayMessageRabbitAmqpAdmin());

		return delayMessageClientShareQueue;
	}

	@Bean
	public Queue delayMessageCurrentNodeQueue(){
		Queue delayMessageClientShareQueue = new Queue(String.format(DELAY_MESSAGE_CURRENT_NODE_QUEUE_FORMAT, delayMessageClientConfig.getNodeId()), true, true, false);
		delayMessageClientShareQueue.setAdminsThatShouldDeclare(delayMessageRabbitAmqpAdmin());

		return delayMessageClientShareQueue;
	}

	@Bean
	public Binding delayMessageCurrentNodeQueueBinding(){
		Binding delayMessageCurrentNodeQueueBinding = BindingBuilder.bind(delayMessageCurrentNodeQueue()).to(delayMessageExchange()).with(DELAY_MESSAGE_CURRENT_NODE_QUEUE_ROUTING_KEY);
		delayMessageCurrentNodeQueueBinding.setAdminsThatShouldDeclare(delayMessageRabbitAmqpAdmin());

		return delayMessageCurrentNodeQueueBinding;
	}

	@Bean
	public SimpleMessageListenerContainer refreshableMessageListener(){
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(delayMessageRabbitConnectionFactory());
		container.setQueues(delayMessageCurrentNodeQueue(), delayMessageClientShareQueue());
		container.setConcurrentConsumers(3);
		container.setMaxConcurrentConsumers(5);

		return container;
	}

	@Bean
	public ObjectMapper delayMessageObjectMapper(){
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

		// 驼峰属性名转下划线属性名
		builder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		// 不序列化null值
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		// 忽略多余的字段
		builder.failOnUnknownProperties(false);

		return builder.build();
	}
}
