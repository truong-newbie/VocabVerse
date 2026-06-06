package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.response.AdminSystemHealthResponse;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSystemService {

    private final DataSource dataSource;
    private final StringRedisTemplate stringRedisTemplate;
    private final ConnectionFactory rabbitConnectionFactory;

    public AdminSystemHealthResponse getHealth() {
        return new AdminSystemHealthResponse(
                checkDatabase(),
                checkRedis(),
                checkRabbitMq()
        );
    }

    private String checkDatabase() {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private String checkRedis() {
        try (RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection()) {
            String response = connection.ping();
            return response == null || response.isBlank() ? "DOWN" : "UP";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private String checkRabbitMq() {
        try (org.springframework.amqp.rabbit.connection.Connection connection =
                     rabbitConnectionFactory.createConnection()) {
            return connection.isOpen() ? "UP" : "DOWN";
        } catch (Exception exception) {
            return "DOWN";
        }
    }
}
