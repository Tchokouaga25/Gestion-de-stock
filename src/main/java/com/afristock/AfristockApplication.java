package com.afristock;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.afristock.repository")
public class AfristockApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AfristockApplication.class);
		app.addListeners(new DatabaseUrlConverter());
		app.run(args);
	}

	/**
	 * Convertit la variable DATABASE_URL fournie par Render
	 * (format postgres[ql]://user:pass@host:port/db)
	 * en URL JDBC compatible (jdbc:postgresql://...).
	 */
	public static class DatabaseUrlConverter
			implements org.springframework.context.ApplicationListener<org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent> {

		@Override
		public void onApplicationEvent(org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent event) {
			ConfigurableEnvironment env = event.getEnvironment();
			String dbUrl = env.getProperty("SPRING_DATASOURCE_URL");
			if (dbUrl == null) {
				dbUrl = env.getProperty("DATABASE_URL");
			}
			if (dbUrl == null || dbUrl.startsWith("jdbc:")) {
				return; // déjà au bon format ou absent (dev local)
			}
			try {
				URI uri = new URI(dbUrl);
				String scheme = uri.getScheme(); // postgres ou postgresql
				if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
					return;
				}
				String userInfo = uri.getUserInfo();
				String username = null;
				String password = null;
				if (userInfo != null) {
					int idx = userInfo.indexOf(':');
					username = idx < 0 ? userInfo : userInfo.substring(0, idx);
					password = idx < 0 ? null : userInfo.substring(idx + 1);
				}
				StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
				if (uri.getPort() != -1) {
					jdbcUrl.append(':').append(uri.getPort());
				}
				jdbcUrl.append(uri.getPath());
				String query = uri.getQuery();
				if (query != null && !query.isEmpty()) {
					jdbcUrl.append('?').append(query);
				} else {
					jdbcUrl.append("?sslmode=require");
				}

				Map<String, Object> props = new HashMap<>();
				props.put("spring.datasource.url", jdbcUrl.toString());
				if (username != null) {
					props.put("spring.datasource.username", username);
				}
				if (password != null) {
					props.put("spring.datasource.password", password);
				}
				props.put("spring.datasource.driverClassName", "org.postgresql.Driver");
				env.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrl", props));
				System.out.println("DATABASE_URL convertie en JDBC : " + jdbcUrl);
			} catch (Exception e) {
				System.err.println("Impossible de convertir DATABASE_URL : " + e.getMessage());
			}
		}
	}
}
