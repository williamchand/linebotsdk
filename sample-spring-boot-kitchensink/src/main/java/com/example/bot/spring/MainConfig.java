package com.example.bot.spring;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgreSQLJDBC {
    public void dataSource() {
    	Connection c = null;
    	try{
    		Class.forName("org.postgresql.Driver");
    		c = DriverManager.getConnection("jdbc:postgresql://ec2-54-83-48-188.compute-1.amazonaws.com:5432/dd0o9u061jj8k6","okkpzunfngqddy",
    			"3368af043d89677c824553247b1026010cad749945f4e6a3f03e8d6173e0d92c");
    	} catch (Exception e){
    	}
    }
}