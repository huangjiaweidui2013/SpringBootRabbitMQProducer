package org.lang.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Slf4j
public class RpcProduceController {
    private final RabbitTemplate rabbitTemplate;

    private final RabbitAdmin rabbitAdmin;

    @GetMapping("/send")
    public String send() {
        String uuid = UUID.randomUUID().toString();
        String msg = "produce: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(uuid);
        //自定义消息头来解决CorrelationId自定义不生效的问题
        properties.setHeader("custom-msg-id", "q-w-e-r");
        Message message = MessageBuilder.withBody(("send message => " + msg).getBytes()).andProperties(properties).build();
        Message receive = rabbitTemplate.sendAndReceive("my.exchange", "queue_1", message);
        if (receive != null) {
            // 获取已发送的消息的 correlationId
            String correlationId = message.getMessageProperties().getCorrelationId();
//            String receiveMsgId = (String) message.getMessageProperties().getHeaders().get("custom-msg-id");
            String receiveMsgId = receive.getMessageProperties().getCorrelationId();
            log.info("correlationId: {}, receiveMsgId: {}", correlationId, receiveMsgId);

            // 获取响应头信息
            HashMap<String, Object> headers = (HashMap<String, Object>) receive.getMessageProperties().getHeaders();

            // 获取 server 返回的消息 id
//            String msgId = (String) headers.get("spring_returned_message_correlation");
//            String msgId = (String) headers.get("custom-msg-id");

//            if (correlationId.equals(receiveMsgId)) {
            if (uuid.equals(receiveMsgId)) {
                String response = new String(receive.getBody());
                log.info("client 收到的 correlationId: {}, receive：{}", correlationId, response);
                return response;
            }
        }

        return "empty string";
    }

    @GetMapping("/send2")
    public String send2() {
        String uuid = UUID.randomUUID().toString();
        String msg = "produce: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        String queueName = "temp_queue";

        QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);
        if (queueInfo == null) {

        }

        //在RabbitMQ中设置高优先级消息主要涉及两个步骤：配置队列以支持优先级，以及在发送消息时指定消息的优先级。
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-max-priority", 10); // 设置队列支持的最大优先级为10
        Queue queue = new Queue(queueName, true, false, false, arguments);
        //声明队列
        rabbitAdmin.declareQueue(queue);
//        rabbitAdmin.deleteQueue(queueName);

        //声明路由
        String exchangeName = "myExchange";
        DirectExchange exchange = new DirectExchange(exchangeName, true, false);
        rabbitAdmin.declareExchange(exchange);
//        rabbitAdmin.deleteExchange(exchangeName);

        //删除队列
//        rabbitAdmin.deleteQueue(queueName);

        //声明绑定
        Binding binding = BindingBuilder.bind(queue).to(exchange).with("test_route_key");
        rabbitAdmin.declareBinding(binding);
//        rabbitAdmin.removeBinding(binding);


        //一旦设置了队列的最大优先级，就无法更改。如果尝试发布超过最大优先级的消息，那么该消息将不会被拒绝，而是按照最大允许的优先级进行处理。
        //使用消息优先级可能会对性能产生影响，因为RabbitMQ需要额外的工作来维护消息的优先级顺序。如果队列中同时存在大量不同优先级的消息，这可能会影响消息吞吐量和延迟
        //。
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(uuid);
        properties.setPriority(10);// 设置消息的优先级为10
        //自定义消息头来解决CorrelationId自定义不生效的问题
        properties.setHeader("custom-msg-id", "q-w-e-r");

        Message message = MessageBuilder.withBody(("send message => " + msg).getBytes()).andProperties(properties).build();
        Message receive = rabbitTemplate.sendAndReceive(exchangeName, "test_route_key", message);
//        Message receive = rabbitTemplate.sendAndReceive("my.exchange", "queue_1", message);
        if (receive != null) {
            // 获取已发送的消息的 correlationId
            String correlationId = message.getMessageProperties().getCorrelationId();
//            String receiveMsgId = (String) message.getMessageProperties().getHeaders().get("custom-msg-id");
            String receiveMsgId = receive.getMessageProperties().getCorrelationId();
            log.info("correlationId: {}, receiveMsgId: {}", correlationId, receiveMsgId);

            // 获取响应头信息
            HashMap<String, Object> headers = (HashMap<String, Object>) receive.getMessageProperties().getHeaders();

            // 获取 server 返回的消息 id
//            String msgId = (String) headers.get("spring_returned_message_correlation");
//            String msgId = (String) headers.get("custom-msg-id");

//            if (correlationId.equals(receiveMsgId)) {
            if (uuid.equals(receiveMsgId)) {
                String response = new String(receive.getBody());
                log.info("client 收到的 correlationId: {}, receive：{}", correlationId, response);
                return response;
            }
        }

        return "empty string";
    }
}
