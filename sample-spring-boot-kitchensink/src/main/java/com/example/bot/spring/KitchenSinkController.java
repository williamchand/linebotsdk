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
    public Timer t0;
    public String TokenCallback;
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
        this.replyText(replyToken, "Got followed event");
    }

    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Joined " + event.getSource());
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
            message = message.substring(0, 1000 - 2) + "â€¦â€¦";
        }
        this.reply(replyToken, new TextMessage(message));
    }
    private Timer startTimer(final String value) {
    	   Timer timer = new Timer("Timer" + value);
    	   return timer;
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
        String text = content.getText();
        
        log.info("Got text message from {}: {}", replyToken, text);
        if (text.indexOf("/profile")>=0){
                String userId = event.getSource().getUserId();
                if (userId != null) {
                    lineMessagingClient
                            .getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if (throwable != null) {
                                    this.replyText(replyToken, throwable.getMessage());
                                    return;
                                }

                                this.reply(
                                        replyToken,
                                        Arrays.asList(new TextMessage(
                                                	          "Id: " + userId),
                                        			  new TextMessage(
                                                              "Display name: " + profile.getDisplayName()),
                                                      new TextMessage(
                                                    		  "Status message: " + profile.getStatusMessage()))
                                );
                            });
                } else {
                    this.replyText(replyToken, "Bot can't use profile API without user ID");
                }
                
        }else if (text.indexOf("/leave")>=0){
                Source source = event.getSource();
                if (source instanceof GroupSource) {
                    this.replyText(replyToken, "Leaving group");
                    lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
                } else if (source instanceof RoomSource) {
                    this.replyText(replyToken, "Leaving room");
                    lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
                } else {
                    this.replyText(replyToken, "Bot can't leave from 1:1 chat");
                }
        }else if (text.indexOf("/question")>=0){
                ConfirmTemplate confirmTemplate = new ConfirmTemplate(
                        "Do it?",
                        new MessageAction("Yes", "Yes!"),
                        new MessageAction("No", "No!")
                );
                TemplateMessage templateMessage = new TemplateMessage("Confirm alt text", confirmTemplate);
                this.reply(replyToken, templateMessage);
        }else if (text.indexOf("/buttons")>=0){
                String imageUrl = createUri("/static/buttons/1040.jpg");
                ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                        imageUrl,
                        "My button sample",
                        "Hello, my button",
                        Arrays.asList(
                                new URIAction("Go to line.me",
                                              "https://line.me"),
                                new PostbackAction("Say hello1",
                                                   "hello ã�“ã‚“ã�«ã�¡ã�¯"),
                                new PostbackAction("è¨€ hello2",
                                                   "hello ã�“ã‚“ã�«ã�¡ã�¯",
                                                   "hello ã�“ã‚“ã�«ã�¡ã�¯"),
                                new MessageAction("Say message",
                                                  "Rice=ç±³")
                        ));
                TemplateMessage templateMessage = new TemplateMessage("Button alt text", buttonsTemplate);
                this.reply(replyToken, templateMessage);
        }else if (text.indexOf("/carousel")>=0){
                String imageUrl = createUri("/static/buttons/1040.jpg");
                CarouselTemplate carouselTemplate = new CarouselTemplate(
                        Arrays.asList(
                                new CarouselColumn(imageUrl, "hoge", "fuga", Arrays.asList(
                                        new URIAction("Go to line.me",
                                                      "https://line.me"),
                                        new PostbackAction("Say hello1",
                                                           "hello ã�“ã‚“ã�«ã�¡ã�¯")
                                )),
                                new CarouselColumn(imageUrl, "hoge", "fuga", Arrays.asList(
                                        new PostbackAction("è¨€ hello2",
                                                           "hello ã�“ã‚“ã�«ã�¡ã�¯",
                                                           "hello ã�“ã‚“ã�«ã�¡ã�¯"),
                                        new MessageAction("Say message",
                                                          "Rice=ç±³")
                                ))
                        ));
                TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
                this.reply(replyToken, templateMessage);
        }else if (text.indexOf("/imagemap")>=0){
                this.reply(replyToken, new ImagemapMessage(
                        createUri("/static/rich"),
                        "This is alt text",
                        new ImagemapBaseSize(1040, 1040),
                        Arrays.asList(
                                new URIImagemapAction(
                                        "https://store.line.me/family/manga/en",
                                        new ImagemapArea(
                                                0, 0, 520, 520
                                        )
                                ),
                                new URIImagemapAction(
                                        "https://store.line.me/family/music/en",
                                        new ImagemapArea(
                                                520, 0, 520, 520
                                        )
                                ),
                                new URIImagemapAction(
                                        "https://store.line.me/family/play/en",
                                        new ImagemapArea(
                                                0, 520, 520, 520
                                        )
                                ),
                                new MessageImagemapAction(
                                        "URANAI!",
                                        new ImagemapArea(
                                                520, 520, 520, 520
                                        )
                                )
                        )
                ));
      }else if (text.indexOf("/help")>=0){
        		this.replyText(replyToken,
        			  "feature /help : bantuan\n"+"/imagemap:gambar yang dapat diklik\n"+"/buttons:tombol\n"+
		    		  "/question:pertanyaan\n"+"/carousel:carousel\n"+"/leave:keluar dari grup\n"+"/profile:user ID\n");
	  }else if(text.indexOf("/time")>=0){
		  		try{
		  			Connection connection = getConnection();
		  	        Statement stmt = connection.createStatement();
		  	        stmt.executeUpdate("DROP TABLE IF EXISTS ticks");
		  	        stmt.executeUpdate("CREATE TABLE ticks (tick timestamp)");
		  	        stmt.executeUpdate("INSERT INTO ticks VALUES (now() + INTERVAL '7 HOUR')");
		  	        ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
		  	        while (rs.next()) {
		  	        	this.replyText(replyToken,"Read from DB: " + rs.getTimestamp("tick"));
		  	        }
		  		}catch(SQLException e){
		  			this.replyText(replyToken,e.getMessage());
		  		}catch(URISyntaxException err){
		  				this.replyText(replyToken,err.getMessage());
		  			}
	  }else if(text.indexOf("/delay")>=0){
		  		Source source = event.getSource();
		  		String groupid="";
		  		this.TokenCallback = replyToken;
		  		if (source instanceof GroupSource) {
		  			groupid = ((GroupSource) source).getGroupId();
		  		}
		  		KitchenSinkController.this.t0 = startTimer(groupid);
		  		KitchenSinkController.this.t0.schedule( new TimerTask() {
   	   				@Override
   	   				public void run() {
   	   					try{
   	 		  				Connection connection = KitchenSinkController.getConnection();
   	 		  	        	Statement stmt = connection.createStatement();
   	 		  	        	stmt.executeUpdate("DROP TABLE IF EXISTS ticks");
   	 		  	        	stmt.executeUpdate("CREATE TABLE ticks (tick timestamp)");
   	 		  	        	stmt.executeUpdate("INSERT INTO ticks VALUES (now() + INTERVAL '7 HOUR')");
   	 		  	        	ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
   	 		  	        	while (rs.next()) {
   	 		  	        		KitchenSinkController.this.replyText(KitchenSinkController.this.TokenCallback,"Read from DB: " + rs.getTimestamp("tick"));
   	 		  	        	}
   	 		  			}catch(SQLException e){
   	 		  				KitchenSinkController.this.replyText(KitchenSinkController.this.TokenCallback,e.getMessage());
   	 		  			}catch(URISyntaxException err){
   	 		  				KitchenSinkController.this.replyText(KitchenSinkController.this.TokenCallback,err.getMessage());
   	 		  			}
   	   				}
   	   			}, 5000, 100); // Every second
      }else if(text.indexOf("/cancel")>=0){
    	  		Source source = event.getSource();
		  		String groupid="";
	  			if (source instanceof GroupSource) {
	  				groupid = ((GroupSource) source).getGroupId();
	  			}
	  			KitchenSinkController.this.t0 = startTimer(groupid);
	  			KitchenSinkController.this.t0.cancel();
      }else{
                log.info("Ignore message {}: {}", replyToken, text);
      }
    }
    
    private static Connection getConnection() throws URISyntaxException, SQLException {
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
            message = message.substring(0, 1000 - 2) + "â€¦â€¦";
        }
        this.push(To, new TextMessage(message));
    }
}