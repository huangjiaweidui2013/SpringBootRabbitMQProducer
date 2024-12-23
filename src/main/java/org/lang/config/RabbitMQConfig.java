package org.lang.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true); // 确保在应用启动时自动声明队列和交换机
        return rabbitAdmin;
    }

//    public static final String RPC_QUEUE1 = "queue_3";
//
//    public static final String RPC_QUEUE2 = "queue_4";
//
//    public static final String RPC_EXCHANGE = "rpc_exchange";
//
//    //-- rpc
//
//    /**
//     * 设置消息发送RPC队列
//     */
//    @Bean
//    Queue msgQueue() {
//        return new Queue(RPC_QUEUE1);
//    }
//
//    /**
//     * 设置返回队列
//     */
//    @Bean
//    Queue replyQueue() {
//        return new Queue(RPC_QUEUE2);
//    }
//
//    /**
//     * 设置交换机
//     */
//    @Bean
//    TopicExchange exchangeRPC() {
//        return new TopicExchange(RPC_EXCHANGE);
//    }
//
//    /**
//     * 请求队列和交换器绑定
//     */
//    @Bean
//    Binding msgBinding() {
//        return BindingBuilder.bind(msgQueue()).to(exchangeRPC()).with(RPC_QUEUE1);
//    }
//
//    /**
//     * 返回队列和交换器绑定
//     */
//    @Bean
//    Binding replyBinding() {
//        return BindingBuilder.bind(replyQueue()).to(exchangeRPC()).with(RPC_QUEUE2);
//    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //设置为true将使用消息提供的关联id，而不是生成请求/应答场景的关联id。关联id必须唯一，适用于所有正在处理的请求，以避免串扰
        template.setUserCorrelationId(true);
        return template;
    }


}
