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
import javax.sql.DataSource;
import java.lang.Override;
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
import org.springframework.scheduling.annotation.Scheduled;
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
@Component
public class KitchenSinkController {
    @Autowired
    private LineMessagingClient lineMessagingClient;
    
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
 	        Statement stmt2 = connection.createStatement();
 	        ResultSet rs = stmt.executeQuery("SELECT \"UserId\",\"GroupId\" FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"GroupId\" = '"+groupId+"'");
 	        ResultSet rs2 = stmt2.executeQuery("SELECT \"UserId\",\"GroupId\" FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"UserId\" = '"+userId+"'");
 	        boolean cek = rs.next();
     		boolean cek2 = rs2.next();
	        if (!cek){
	        	if(!cek2){
	        		stmt.executeUpdate("INSERT INTO \"Tabel Pemain\" (\"UserId\",\"GroupId\") VALUES ('"+userId+"','"+groupId+"')");
		        	stmt.executeUpdate("INSERT INTO ticks (\"Condition\",\"GroupId\",\"tick\") VALUES (0,'"+groupId+"',now() + INTERVAL '7 HOUR')");  	        	
		        	Messages = "Insert";
	        	}
	        	else{
	        		Messages = "Pemain ada digame lain";
  	        	}
  	        }else{
  	        	Messages = "Sudah ada game";
  	        }
	        rs.close();
	        stmt.close();
	        rs2.close();
	        stmt2.close();
  		}catch(SQLException e){
  			Messages = e.getMessage();
  			
  		}
    	return Messages;
    }
    
    private String DB2(String userId,String groupId,Connection connection){
    	String Messages="";
    	try{
  	        Statement stmt = connection.createStatement();
  	        Statement stmt2 = connection.createStatement();
  	        ResultSet rs = stmt.executeQuery("SELECT \"UserId\",\"GroupId\" FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"UserId\" = '"+userId+"'");
  	        ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(\"GroupId\") AS \"GroupId\" FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"GroupId\" = '"+groupId+"' GROUP BY \"GroupId\"");
  	        boolean cek = rs.next();
     		boolean cek2 = rs2.next();
  	        if (!cek){
  	        	if(cek2){
  	        		if((rs2.getInt("GroupId")>0)){
  	        			stmt.executeUpdate("INSERT INTO \"Tabel Pemain\" (\"UserId\",\"GroupId\") VALUES ('"+userId+"','"+groupId+"')");	        	
  	        			Messages = "Insert";
  	        		}else{
  	        			Messages = "Game Belum ada";
  	        		}
  	        	}
  	        }else{
  	        	Messages = "Sudah terdaftar di grup lain";
  	        }
	        rs.close();
	        stmt.close();
	        rs2.close();
	        stmt2.close();
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
                }else{
                	groupId = event.getSource().getUserId();
                } 
                String userId = event.getSource().getUserId();
                if (userId != null && groupId != null) {
                	String check =DB1(userId,groupId,connection);
                    if (check=="Insert"){
        	        	UserProfileResponse profile = lineMessagingClient.getProfile(userId).get();
                    	this.pushText(groupId,profile.getDisplayName()+" Memulai Permainan");
                    	this.push(groupId,new TemplateMessage("Teka Teki Indonesia", 
								 	new ButtonsTemplate(
									createUri("/static/buttons/1040.jpg"),
		                            "Teka Teki Indonesia",
		                            "Mari Bermain permainan teka teki indonesia",
		                            Arrays.asList(
		                                    new MessageAction("Join Game",
		                                                      "/join")
		                            			)
								 			)
                    					)
                    			);
                    }else{
                		this.pushText(groupId,"Tidak bisa membuat permainan karena "+ check);
                	}
                } else {
                    this.replyText(replyToken, "Tolong izinkan Bot mengakses akun / update ke LINE versi baru");
                }
        }else if (text.indexOf("/join")>=0){
  			Source source = event.getSource();
  			String groupId="";
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            }else{
            	groupId = event.getSource().getUserId();
            } 
            String userId = event.getSource().getUserId();
            if (userId != null && groupId != null) {
            	String check = DB2(userId,groupId,getConnection());
                if (check=="Insert"){
    	        	UserProfileResponse profile = lineMessagingClient.getProfile(userId).get();
    	        	this.replyText(replyToken,profile.getDisplayName()+" Bergabung ke Permainan");
                } else {
    	        	UserProfileResponse profile = lineMessagingClient.getProfile(userId).get();
    	        	this.replyText(replyToken,profile.getDisplayName()+" tidak bisa bergabung dalam permainan karena "+check);
                }
            } else {
                this.replyText(replyToken, "Tolong izinkan Bot mengakses akun / update ke LINE versi baru");
            }
        }else if (text.indexOf("/start")>=0){
        	Source source = event.getSource();
  			String groupId="";
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            }else{
            	groupId = event.getSource().getUserId();
            } 
        	try{
	  	        	Statement stmt = connection.createStatement();
	  	        	ResultSet rs = stmt.executeQuery("SELECT \"GroupId\",\"Condition\" FROM ticks WHERE ticks.\"GroupId\" = '"+groupId+"'");
	  	        	if(rs.next()){	
	  	        		if (rs.getInt("Condition")==0){
	  	         	        Statement stmt2 = connection.createStatement();
	  	         	        ResultSet rs2 = stmt2.executeQuery("SELECT \"Id\", \"Pertanyaan\" , \"Jawaban\" FROM \"Tabel Pertanyaan\" ORDER BY random() LIMIT 1");
	  	         	        if(rs2.next()){
	  	         	        	stmt.executeUpdate("INSERT INTO \"tabel Jawaban\" (\"Jawaban\",\"GroupId\") VALUES ('"+rs2.getString("Jawaban")+"','"+groupId+"')");
	  	        				stmt.executeUpdate("UPDATE ticks SET \"Condition\" = 1 , tick = now() + INTERVAL '7 HOUR'"
	  	        					+ "WHERE ticks.\"Condition\" = 0 AND ticks.\"GroupId\" = '"+groupId+"'");
	  	        				this.pushText(groupId,"Permainan Dimulai");
	  	        				this.pushText(groupId,""+ rs2.getString("Pertanyaan"));
	  	        				rs2.close();
	  	        				stmt2.close();
	  	         	        }
	  	        		}else {
	  	        			this.pushText(groupId,"Permainan Sudah Dimulai");
	  	        		}
	  	        	} else{
  	        			this.pushText(groupId,"Tidak ada Permainan");
	  	        	}
	  	        	rs.close();
	  		        stmt.close();
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
            }else{
            	groupId = event.getSource().getUserId();
            }
        	try{	  	        		
	  	        	Statement stmt = connection.createStatement();
	  	        	stmt.executeUpdate("DELETE FROM ticks WHERE \"ticks\".\"GroupId\" = '"+groupId+"'");		 
	  	        	stmt.executeUpdate("DELETE FROM \"tabel Jawaban\" WHERE \"tabel Jawaban\".\"GroupId\" = '"+groupId+"'");	
	  	        	stmt.executeUpdate("DELETE FROM \"Tabel Pemain\" WHERE \"Tabel Pemain\".\"GroupId\" = '"+groupId+"'");
	  	        	stmt.executeUpdate("DELETE FROM \"Tabel Skor\" WHERE \"Tabel Skor\".\"GroupId\" = '"+groupId+"'");
	  		        stmt.close();
	  		}catch(SQLException e){
	  				e.getMessage();
	  		}
  			this.pushText(groupId,"Permainan Dihentikan");
        }else if (text.indexOf("/help")>=0){
        		this.replyText(replyToken,
        			  "feature /help : bantuan\n"+"/create : Membuat game\n"+"/join:Memasuki game\n"+"/skor:melihat skor\n"+"/id:melihat id\n"+
		    		  "/Start:memulai game\n"+"/stop : Menghentikan Game\n"+"/leave:keluar dari grup\n");
	    }else if (text.indexOf("/skor")>=0){
        	Source source = event.getSource();
  			String groupId="";
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            }else{
            	groupId = event.getSource().getUserId();
            }
        	try{	  	        		
	  	        	Statement stmt = connection.createStatement();
	  	        	ResultSet rs = stmt.executeQuery("SELECT \"UserId\",\"Skor\" FROM \"Tabel Skor\" WHERE \"Tabel Skor\".\"GroupId\" = '"+groupId+"'");
	  		        String tabelskor = "Tabel Skor Sebagai Berikut = \n"; 
	  	        	while (rs.next()){
	  	        		UserProfileResponse profile = lineMessagingClient.getProfile(rs.getString("UserId")).get();
	                	tabelskor += profile.getDisplayName();
	  	        		tabelskor += " = " + rs.getInt("Skor")+"\n";	
	  	        	}
	        		this.replyText(replyToken,tabelskor);	
	  	        	rs.close();
	  	        	stmt.close();
	  		}catch(SQLException e){
	  				e.getMessage();
	  		}
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
        }else if (text.indexOf("/id")>=0){
        	Source source = event.getSource();
        	String groupId="";
            String userId = event.getSource().getUserId();
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            }else{
            	groupId = event.getSource().getUserId();
            }
  			if (groupId!=""){
	        	UserProfileResponse profile = lineMessagingClient.getProfile(userId).get();
  				this.replyText(replyToken, "ID : " + groupId +"\nNama : "+ profile.getDisplayName());
  			}else{
  				this.replyText(replyToken, "Tolong izinkan Bot mengakses akun / update ke LINE versi baru");
  			}
    	}else{
    		Source source = event.getSource();
        	String groupId="";
  			if (source instanceof GroupSource) {
  				groupId = ((GroupSource) source).getGroupId();
  			}else if (source instanceof RoomSource) {
                groupId = ((RoomSource) source).getRoomId();
            }else{
            	groupId = event.getSource().getUserId();
            }
            String userId = event.getSource().getUserId();
   			UserProfileResponse profile = lineMessagingClient.getProfile(userId).get();
   			String DisplayName = ""+profile.getDisplayName();
  	       	try{
  	         	Statement stmt = connection.createStatement();
  	         	ResultSet rs = stmt.executeQuery("SELECT \"Jawaban\",\"GroupId\" FROM \"tabel Jawaban\" WHERE \"GroupId\" = '"+groupId+"'");
  	         	        if(rs.next()){
	         	        	if (text.indexOf(rs.getString("Jawaban"))>=0){  			
  	    	         	        Statement stmt2 = connection.createStatement();
  		  	         	        ResultSet rs2 = stmt2.executeQuery("SELECT \"Id\", \"Pertanyaan\" , \"Jawaban\" FROM \"Tabel Pertanyaan\" ORDER BY random() LIMIT 1");
  		  	         	        if (rs2.next()){
  	    	         	        	stmt.executeUpdate("UPDATE ticks SET tick = now() + INTERVAL '7 HOUR' WHERE ticks.\"GroupId\" = '"+groupId+"'");
  	         	        			stmt.executeUpdate("DELETE FROM \"tabel Jawaban\" WHERE \"GroupId\" = '"+groupId+"'");  	    
  	         	        			stmt.executeUpdate("INSERT INTO \"tabel Jawaban\" (\"Jawaban\",\"GroupId\") VALUES ('"+rs2.getString("Jawaban")+"','"+groupId+"')");	
  	    	         	   			this.pushText(groupId,DisplayName+" Berhasil menjawab");
  	  	         	        		this.pushText(groupId,""+ rs2.getString("Pertanyaan"));
	         	        			stmt.executeUpdate("UPDATE \"Tabel Skor\" SET \"Skor\" = (SELECT \"Skor\" FROM \"Tabel Skor\" WHERE \"GroupId\" = '"+groupId+"' AND \"UserId\" = '"+ userId +"')+1  WHERE EXISTS (SELECT * FROM \"Tabel Skor\" WHERE \"GroupId\" = '"+groupId+"' AND \"UserId\" = '"+ userId + "')");	
  	    	         	   			this.pushText(groupId,DisplayName+" Berhasil menjawab");
	         	        			stmt.executeUpdate("INSERT INTO \"Tabel Skor\" (\"UserId\",\"GroupId\",\"Skor\") SELECT '"+userId+"','"+groupId+"',1 FROM \"Tabel Skor\" WHERE NOT EXISTS (SELECT * FROM \"Tabel Skor\" WHERE \"GroupId\" = '"+groupId+"' AND \"UserId\" = '"+ userId +"')");
  		  	         	        }
  	        					rs2.close();
  	        					stmt2.close();
  	         	        	}
  	         	        }
	        	rs.close();
	        	stmt.close();
  	        }catch(SQLException e){
  	        	this.pushText(groupId,""+e.getMessage());
  			}
        }
        connection.close();
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

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="UserId", defaultValue="") String User,@RequestParam(value="message", defaultValue="") String message) {
       this.pushText(User, message);
       return new Greeting(User,message);
    }

    @RequestMapping("/db")
    public Databasing databasing(@RequestParam(value="QuestionId", defaultValue="0") int questionId,@RequestParam(value="Question", defaultValue="") String question,@RequestParam(value="Answer", defaultValue="") String answer) {
    	if (questionId>0){
    		try{
				Connection connection = getConnection();
        		Statement stmt = connection.createStatement();
        		stmt.executeUpdate("DELETE FROM \"Tabel Pertanyaan\" WHERE \"Tabel Pertanyaan\".\"Id\" = "+questionId);
        		stmt.executeUpdate("INSERT INTO \"Tabel Pertanyaan\" (\"Id\",\"Pertanyaan\",\"Jawaban\") VALUES ('"+questionId+"','"+question+"','"+answer+"')");
        		connection.close();
			}catch(SQLException e){
				e.getMessage();
			}catch(URISyntaxException err){
				err.getMessage();
			}
    	}
       return new Databasing(questionId,question,answer);
    }

    @Scheduled(fixedRate = 1000)
    public void GameStart() {
    	try{
			Connection connection = getConnection();
        	Statement stmt = connection.createStatement();
        	ResultSet rs = stmt.executeQuery("SELECT \"Condition\",\"GroupId\" FROM ticks WHERE tick <= now() + INTERVAL '6 HOUR 59 MINUTES'");
        	while (rs.next()) {
            	String groupId = rs.getString("GroupId");
        		if (rs.getInt("Condition")==0){
         	        Statement stmt2 = connection.createStatement();
         	        ResultSet rs2 = stmt2.executeQuery("SELECT \"Id\", \"Pertanyaan\" , \"Jawaban\" FROM \"Tabel Pertanyaan\" ORDER BY random() LIMIT 1");
         	        if(rs2.next()){
         	        	stmt.executeUpdate("INSERT INTO \"tabel Jawaban\" (\"Jawaban\",\"GroupId\") VALUES ('"+rs2.getString("Jawaban")+"','"+groupId+"')");
         	        	stmt.executeUpdate("UPDATE ticks SET \"Condition\" = 1 , tick = now() + INTERVAL '7 HOUR' WHERE ticks.tick <= now() + INTERVAL '6 HOUR 59 MINUTES' AND ticks.\"GroupId\" = '"+groupId+"'"); 
         	        	this.pushText(groupId,"Permainan Dimulai");
         	        	this.pushText(groupId,""+ rs2.getString("Pertanyaan"));
  	        			rs2.close();
        				stmt2.close();
         	        }
        		}else if (rs.getInt("Condition")==1){
        			Statement stmt2 = connection.createStatement();	  	         	       
        			ResultSet rs2 = stmt2.executeQuery("SELECT \"Id\", \"Pertanyaan\" , \"Jawaban\" FROM \"Tabel Pertanyaan\" ORDER BY random() LIMIT 1");
	         	    if(rs2.next()){
	         	    	stmt.executeUpdate("UPDATE ticks SET tick = now() + INTERVAL '7 HOUR' WHERE ticks.tick <= now() + INTERVAL '6 HOUR 59 MINUTES' AND ticks.\"GroupId\" = '"+groupId+"'");
        				stmt.executeUpdate("DELETE FROM \"tabel Jawaban\" WHERE \"GroupId\" = '"+groupId+"'");
        				stmt.executeUpdate("INSERT INTO \"tabel Jawaban\" (\"Jawaban\",\"GroupId\") VALUES ('"+rs2.getString("Jawaban")+"','"+groupId+"')");
        				this.pushText(groupId, "Tidak ada yang berhasil menjawab");
        				this.pushText(groupId,""+ rs2.getString("Pertanyaan"));
        				rs2.close();
        				stmt2.close();
	         	    }
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