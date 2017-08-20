package com.example.bot.spring;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.net.URISyntaxException;
import java.net.URI;
import java.sql.*;
import javax.sql.DataSource;;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Override;
import com.heroku.sdk.jdbc.DatabaseUrl;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.google.common.io.ByteStreams;
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
@RestController
public class KitchenSinkController {
    @Autowired
    private LineMessagingClient lineMessagingClient;
    private String TokenCallback1;
    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleVideoMessageEvent(MessageEvent<VideoMessageContent> event) throws IOException {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        log.info("unfollowed this bot: {}", event);
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "bot telah di ikuti info lebih lanjut /help");
    }

    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Bot telah bergabung ke grup info lebih lanjut /help" );
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleOtherEvent(Event event) {
        log.info("Received message(Ignored): {}", event);
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }

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

    public void pushText(@NonNull String To, @NonNull String message) {
        if (To.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2);
        }
        this.push(To, new TextMessage(message));
    }

    
    private String DB1(String userId,String groupId,Connection connection){
    	String Messages="";
    	try{
  	        Statement stmt = connection.createStatement();
  	        ResultSet rs = stmt.executeQuery("SELECT \"UserId\",\"GroupId\" FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"UserId\" = '"+userId+"'");
  	        rs.next();
  	        if ((rs.getString("UserId")==null)){
  	        	stmt.executeUpdate("INSERT INTO \"Tabel Pemain\" (\"UserId\",\"GroupId\") VALUES ('"+userId+"','"+groupId+"')");
  	        	stmt.executeUpdate("INSERT INTO ticks (\"Condition\",\"GroupId\",\"tick\") VALUES (0,'"+groupId+"',now() + INTERVAL '7 HOUR')");  	        	
  	        	Messages = "Insert";
  	        }
  	        else{
  	        	Messages = "Already";
  	        }
  		}catch(SQLException e){
  			Messages = e.getMessage();
  		}
    	return Messages;
    }
    private String DB2(String userId,String groupId,Connection connection){
    	String Messages="";
    	try{
  	        Statement stmt = connection.createStatement();
  	        Statement statement2 = connection.createStatement();
  	        ResultSet rs = stmt.executeQuery("SELECT \"UserId\",\"GroupId\" FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"UserId\" = '"+userId+"'");
  	        ResultSet rs2 = statement2.executeQuery("SELECT COUNT(\"GroupId\") AS \"GroupId\" FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"GroupId\" = '"+groupId+"' GROUPBY \"GroupId\"");
  	        rs.next();
  	        if ((rs.getString("UserId")==userId)){
  	        	Messages = "Already";
  	        }else{
  	        	rs2.next();
  	        	if((rs.getInt("GroupId")>0)){
  	        		stmt.executeUpdate("INSERT INTO \"Tabel Pemain\" (\"UserId\",\"GroupId\") VALUES ('"+userId+"','"+groupId+"')");	        	
  	        		Messages = "Insert";
  	        	}else{
  	        		Messages = "Game Belum";
  	        	}
  	        } 	       
  		}catch(SQLException e){
  			Messages = e.getMessage();
  		}
    	return Messages;
    }
    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
		Connection connection = getConnection();
        String text = content.getText();
        log.info("Got text message from {}: {}", replyToken, text);
        if (text.indexOf("/create")>=0){
	  			Source source = event.getSource();
	  			String groupId="";
	  			if (source instanceof GroupSource) {
	  				groupId = ((GroupSource) source).getGroupId();
	  			}else if (source instanceof RoomSource) {
                    groupId = ((RoomSource) source).getRoomId();
                } 
                String userId = event.getSource().getUserId();
                if (userId != null && groupId != null) {
                	String check =DB1(userId,groupId,connection);
                    if (check=="Insert"){
                    	lineMessagingClient
                            	.getProfile(userId)
                            	.whenComplete((profile, throwable) -> {
                            		if (throwable != null) {
                            			this.replyText(replyToken, throwable.getMessage());
                            			return;
                            		}
                            		this.reply(
                            				replyToken,
                            				Arrays.asList(new TextMessage( profile.getDisplayName()+" Memulai Permainan" )
                                        			      )
                                );
                            });

                    	String imageUrl = createUri("/static/buttons/1040.jpg");
                    	ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                            imageUrl,
                            "Teka Teki Indonesia",
                            "Mari Bermain permainan teka teki indonesia",
                            Arrays.asList(
                                    new MessageAction("Join Game",
                                                      "/join")
                            ));
                    	TemplateMessage templateMessage = new TemplateMessage("Teka Teki Indonesia", buttonsTemplate);
                    	this.reply(replyToken, templateMessage);
                	}else{
                		this.replyText(replyToken,"Tidak bisa membuat permainan "+check);
                	}
                } else {
                    this.replyText(replyToken, "Tolong izinkan Bot mengakses akun");
                }
        }else if (text.indexOf("/join")>=0){
  			Source source = event.getSource();
  			String groupId="";
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            } 
            String userId = event.getSource().getUserId();
            if (userId != null && groupId != null) {
            	String check =DB2(userId,groupId,getConnection());
                if (check=="Insert"){
                	lineMessagingClient
                        	.getProfile(userId)
                        	.whenComplete((profile, throwable) -> {
                        		if (throwable != null) {
                        			this.replyText(replyToken, throwable.getMessage());
                        			return;
                        		}
                        		this.reply(
                        				replyToken,
                        				Arrays.asList(new TextMessage( profile.getDisplayName()+" Bergabung ke Permainan" )
                                    			      )
                            );
                        });
                } else {
                	lineMessagingClient
                	.getProfile(userId)
                	.whenComplete((profile, throwable) -> {
                		if (throwable != null) {
                			this.replyText(replyToken, throwable.getMessage());
                			return;
                		}
                		this.reply(
                				replyToken,
                				Arrays.asList(new TextMessage( profile.getDisplayName()+" tidak bisa bergabung dalam permainan" )
                            			      )
                				);
                	});
                }
            }
        }else if (text.indexOf("/start")>=0){
        	Source source = event.getSource();
  			String groupId="";
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            } 
        	try{
	  	        	Statement stmt = connection.createStatement();
	  	        	ResultSet rs = stmt.executeQuery("SELECT \"GroupId\" FROM ticks WHERE ticks.\"GroupId\" = '"+groupId+"'");
	  	        	rs.next();
	  	        	if (rs.getString("GroupId")==null){
	  	        		this.pushText(rs.getString("GroupId"),"Permainan Dimulai");
	  	        		stmt.executeUpdate("UPDATE ticks SET \"Condition\" = 1 , tick = now() + INTERVAL '7 HOUR'"
	  	        			+ "WHERE ticks.\"Condition\" = 0 AND ticks.\"GroupId\" = '"+groupId+"'");
	  	        	}
        		}catch(SQLException e){
	  				e.getMessage();
	  			}
        }else if (text.indexOf("/stop")>=0){
        	Source source = event.getSource();
  			String groupId="";
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            } 
        	try{
	  	        	Statement stmt = connection.createStatement();
	  	        	stmt.executeUpdate("DELETE ticks WHERE ticks.\"GroupId\" = '"+groupId+"'");		 
	  	        	stmt.executeUpdate("DELETE \"tabel Jawaban\" WHERE \"tabel Jawaban\".\"GroupId\" = '"+groupId+"'");	
	  	        	stmt.executeUpdate("DELETE \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"GroupId\" = '"+groupId+"'");		  	        		
	  	        	this.pushText(groupId,"Permainan Berhenti");
	  	       
	  		}catch(SQLException e){
	  				e.getMessage();
	  		}
        }else if (text.indexOf("/help")>=0){
        		this.replyText(replyToken,
        			  "feature /help : bantuan\n"+"/create : Membuat game\n"+"/join:Memasuki game\n"+
		    		  "/Start:memulai game\n"+"/stop : Menghentikan Game\n"+"/leave:keluar dari grup\n");
	    }else if (text.indexOf("/leave")>=0){
          Source source = event.getSource();
          if (source instanceof GroupSource) {
              this.replyText(replyToken, "Bot meninggalkan grup");
              lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
          } else if (source instanceof RoomSource) {
              this.replyText(replyToken, "Bot meninggalkan ruangan");
              lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
          } else {
              this.replyText(replyToken, "ini room 1:1 tidak bisa menggunakan perintah /leave");
          }
        }else{
                log.info("Ignore message {}: {}", replyToken, text);
        }
    }
    
    public static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

        return DriverManager.getConnection(dbUrl, username, password);
    }
    
    private static String createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .path(path).build()
                                          .toUriString();
    }

    private void system(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            log.info("result: {} =>  {}", Arrays.toString(args), i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private static DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
        log.info("Got content-type: {}", responseBody);

        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(responseBody.getStream(), outputStream);
            log.info("Saved {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    
    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
        Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(
                tempFile,
                createUri("/downloaded/" + tempFile.getFileName()));
    }

    @Value
    public static class DownloadedContent {
        Path path;
        String uri;
    }
    
    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="UserId", defaultValue="") String User,@RequestParam(value="message", defaultValue="") String message) {
       this.pushText(User, message);
       return new Greeting(User,message);
    }

}