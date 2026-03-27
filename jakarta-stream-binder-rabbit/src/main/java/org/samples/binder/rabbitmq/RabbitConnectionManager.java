package org.samples.binder.rabbitmq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.samples.binder.BinderConfigurationException;
import org.samples.binder.ChannelConfig;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RabbitConnectionManager {

    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    public synchronized Connection getConnection(ChannelConfig cfg) throws Exception {
        String host = cfg.getProperty("host", String.class)
            .orElseThrow(() -> new BinderConfigurationException("host", cfg));
        Integer port = cfg.getProperty("port", Integer.class).orElse(5672);
        String username = cfg.getProperty("username", String.class).orElse(null);
        String password = cfg.getProperty("password", String.class).orElse(null);
        String key = String.format("%s:%d:%s", host, port, username);
        Connection conn = connections.get(key);
        if (conn != null && conn.isOpen()) {
            return conn;
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        if (username != null) {
            factory.setUsername(username);
        }
        if (password != null) {
            factory.setPassword(password);
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
