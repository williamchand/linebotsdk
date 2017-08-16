package com.example.bot.spring;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;

@ConfigurationProperties(prefix="spring")
@Configuration
@ComponentScan(basePackages = "com.example.bot.spring")
public class MainConfig {
	private String dbUrl;

	@Autowired
	private DataSource dataSource;

	@RequestMapping("/db")
	String db(Map<String, Object> model) {
	    try (Connection connection = dataSource.getConnection()) {
	      Statement stmt = connection.createStatement();
	      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
	      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
	      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

	      ArrayList<String> output = new ArrayList<String>();
	      while (rs.next()) {
	        output.add("Read from DB: " + rs.getTimestamp("tick"));
	      }

	      model.put("records", output);
	      return "db";
	    } catch (Exception e) {
	      model.put("message", e.getMessage());
	      return "error";
	    }
	}
}