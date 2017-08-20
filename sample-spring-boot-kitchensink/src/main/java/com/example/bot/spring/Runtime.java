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
import java.util.concurrent.ExecutionException;
import java.util.Collections;
import java.net.URISyntaxException;
import java.net.URI;
import java.sql.*;
import javax.sql.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
public class Runtime extends TimerTask
{    
	@Autowired
    private LineMessagingClient lineMessagingClient;

    private void push(@NonNull String To, @NonNull Message message) {
        push(To, Collections.singletonList(message));
    }

    private void push(@NonNull String To, @NonNull List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .pushMessage(new PushMessage(To, messages))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void pushText(@NonNull String To, @NonNull String message) {
        if (To.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2);
        }
        this.push(To, new TextMessage(message));
    }

	@Override
	public void run() {
		try{
				Connection connection = KitchenSinkController.getConnection();
	        	Statement stmt = connection.createStatement();
	        	ResultSet rs = stmt.executeQuery("SELECT Condition,GroupId FROM 'ticks' WHERE 'ticks'.tick <= now() + INTERVAL '6 HOUR 57 MINUTES'");
	        	while (rs.next()) {   	 
	        		if (rs.getInt("Condition")==0){
	        			Runtime.pushText(rs.getString("GroupId"),"Permainan Dimulai");
	        			stmt.executeUpdate("UPDATE 'ticks' SET Condition = 1 , tick = now() + INTERVAL '7 HOUR'"
	    	        			+ "WHERE 'ticks'.tick <= now() + INTERVAL '6 HOUR 57 MINUTES' AND 'ticks'.GroupId = "+rs.getString("GroupId"));
	        		}
	        	}
	        	rs.close();
	        	stmt.close();
	        	connection.close();
			}catch(SQLException e){
				e.getMessage();
			}catch(URISyntaxException err){
				err.getMessage();
		}
	}
}
 	   					
	   				
  
