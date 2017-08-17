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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;

@SpringBootApplication
public class KitchenSinkApplication {
    static Path downloadedContentDir;
    private Connection c;
    public Connection connection{
    	return c;
    }
    public static void main(String[] args) throws IOException {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
               .getConnection("jdbc:postgres://ec2-54-83-48-188.compute-1.amazonaws.com:5432/dd0o9u061jj8k6",
               "okkpzunfngqddy", "3368af043d89677c824553247b1026010cad749945f4e6a3f03e8d6173e0d92c");
            c.setAutoCommit(false);
        }catch(Exception e){
        }
        downloadedContentDir = Files.createTempDirectory("line-bot");
        SpringApplication.run(KitchenSinkApplication.class, args);
   }
}
