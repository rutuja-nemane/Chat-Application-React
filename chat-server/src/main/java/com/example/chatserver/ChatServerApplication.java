package com.example.chatserver;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class ChatServerApplication extends Application<ChatServerConfiguration> {

    @Override
    public void run(ChatServerConfiguration configuration, Environment environment) {
        System.out.println("Chat Server is starting...");

        // Register CORS headers
        configureCors(environment, configuration.getAllowedOrigins());

        // Register WebSocket servlet at /chat
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        environment.getApplicationContext().setContextPath("/");

        // Add WebSocket servlet to the context
        ServletHolder wsHolder = new ServletHolder("ws-chat", new ChatWebSocketServlet());
        environment.getApplicationContext().addServlet(wsHolder, "/chat/*");
    }

    private void configureCors(Environment environment, String allowedOrigins) {
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
    
        // Ensure the filter is mapped to all URL patterns
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    public static void main(String[] args) throws Exception {
        new ChatServerApplication().run(args);
    }
}
