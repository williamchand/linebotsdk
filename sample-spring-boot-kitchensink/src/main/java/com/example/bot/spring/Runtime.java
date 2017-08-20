/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring;

import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import java.lang.Override;
import com.heroku.sdk.jdbc.DatabaseUrl;
import java.net.URISyntaxException;
import java.net.URI;
import java.sql.*;
import javax.sql.*;
public class Runtime extends TimerTask
{
	@Override
	public void run() {
		try{
				Connection connection = KitchenSinkController.getConnection();
	        	Statement stmt = connection.createStatement();
	        	ResultSet rs = stmt.executeQuery("SELECT Condition,GroupId FROM 'ticks' WHERE 'ticks'.tick <= now() + INTERVAL '6 HOUR 57 MINUTES'");
	        	while (rs.next()) {   	 
	        		if (rs.getInt("Condition")==0){
	        			KitchenSinkController.this.pushText(rs.getString("GroupId"),"Permainan Dimulai");
	        			stmt.executeUpdate("UPDATE 'ticks' SET Condition = 1 , tick = now() + INTERVAL '7 HOUR'"
	    	        			+ "WHERE 'ticks'.tick <= now() + INTERVAL '6 HOUR 57 MINUTES' AND 'ticks'.GroupId = "+rs.getString("GroupId"));
	        		}
	        	}
	        	
			}catch(SQLException e){
				e.getMessage();
			}catch(URISyntaxException err){
				err.getMessage();
		}
	}
}
 	   					
	   				
  
