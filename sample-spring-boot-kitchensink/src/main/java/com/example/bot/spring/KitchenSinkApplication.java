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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class KitchenSinkApplication {
    @Autowired
    private DataSource dataSource;

    static Path downloadedContentDir;
    
    public static void main(String[] args) throws IOException {
        downloadedContentDir = Files.createTempDirectory("line-bot");
        SpringApplication.run(KitchenSinkApplication.class, args);
   }

    @RequestMapping("/db")
    public void myRealMainMethod() throws SQLException {
        Statement stmt = dataSource.getConnection().createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS ticks");
        stmt.executeUpdate("CREATE TABLE ticks (tick timestamp)");
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
        ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
        while (rs.next()) {
            System.out.println("Read from DB: " + rs.getTimestamp("tick"));
        }
    }
    

}
