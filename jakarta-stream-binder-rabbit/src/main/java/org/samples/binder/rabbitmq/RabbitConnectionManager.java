package org.samples.binder.rabbitmq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.samples.binder.ChannelConfig;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RabbitConnectionManager {

    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    public synchronized Connection getConnection(ChannelConfig cfg) throws Exception {
        String key = String.format("%s:%d:%s", cfg.getHost(), cfg.getPort(), cfg.getUsername());
        Connection conn = connections.get(key);
        if (conn != null && conn.isOpen()) {
            return conn;
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(cfg.getHost());
        factory.setPort(cfg.getPort());
        if (cfg.getUsername() != null) {
            factory.setUsername(cfg.getUsername());
        }
        if (cfg.getPassword() != null) {
            factory.setPassword(cfg.getPassword());
        }
        factory.setAutomaticRecoveryEnabled(true);
        factory.setTopologyRecoveryEnabled(true);
        Connection newConn = factory.newConnection();
        connections.put(key, newConn);
        return newConn;
    }

    @PreDestroy
    public void closeAll() {
        connections.values().forEach(c -> {
            try {
                if (c != null && c.isOpen())
                    c.close();
            }
            catch (Exception ignored) {
            }
        });
        connections.clear();
    }

}
