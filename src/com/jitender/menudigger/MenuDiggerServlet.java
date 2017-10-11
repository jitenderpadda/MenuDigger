package com.jitender.menudigger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.jitender.menudigger.entity.MenuItem;
import com.jitender.menudigger.entity.PMF;
import com.jitender.menudigger.entity.Subscriber;
import com.jitender.menudigger.entity.UserComment;

import googleSendgridJava.Sendgrid;

@SuppressWarnings("serial")
public class MenuDiggerServlet extends HttpServlet {
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PersistenceManager mgr = PMF.get().getPersistenceManager();
		Query queryComments = mgr.newQuery(UserComment.class);
		List<UserComment> userCommentList=(List<UserComment>)queryComments.execute();
		Query queryItems = mgr.newQuery(MenuItem.class);
		List<MenuItem> menuItemList=(List<MenuItem>)queryItems.execute();
		
		HashMap<Long, MenuItem> menuItemId_MenuItem_Map=new HashMap<Long, MenuItem>();
		HashMap<MenuItem, List<UserComment>> menuItem_userCommentList_Map=new HashMap<MenuItem, List<UserComment>>();
		
		for(MenuItem item: menuItemList){
			menuItemId_MenuItem_Map.put(item.getId(), item);
		}
		Logger logger = Logger.getLogger(MenuDiggerServlet.class.getName());
		logger.info("Cron Job has been executed");
		logger.info("menuItemId_MenuItem_Map---" + menuItemId_MenuItem_Map);
		//Map of MenuItem and UserComments
		for (UserComment comment : userCommentList) {
			logger.info("comment---" + comment);
			if (menuItem_userCommentList_Map.containsKey(menuItemId_MenuItem_Map.get(comment.getItemId()))) {
				menuItem_userCommentList_Map.get(menuItemId_MenuItem_Map.get(comment.getItemId())).add(comment);
			} else
				menuItem_userCommentList_Map.put(menuItemId_MenuItem_Map.get(comment.getItemId()),new ArrayList<UserComment>(Arrays.asList(comment)));
		}
		String html="";
		for(MenuItem item:menuItem_userCommentList_Map.keySet()){
			int ratingCount=0;
			int ratingSum=0;
			for(UserComment comment:menuItem_userCommentList_Map.get(item)){
				if(comment.getRating()!=0){
					ratingSum=ratingSum+comment.getRating();
					ratingCount++;
				}
			}
			int ratingAverage=ratingSum/ratingCount;
			item.setRating(ratingAverage);
			html=html+"MenuItem - "+item.getName()+", Rating - "+ratingAverage+"<br/>";
		}
		
		/*** Send Email ***/
		Sendgrid mail = new Sendgrid("XXXXX", "XXXXX");
		// set email data
		mail.setTo("test@sharklasers.com").setFrom("me@bar.com").setSubject("Subject goes here").setText("Hello World!")
				.setHtml("<strong>" + html + "</strong>");
		//Add Subscribers
		Query querySubscribers = mgr.newQuery(Subscriber.class);
		for(Subscriber subscr: (List<Subscriber>)querySubscribers.execute()){
			mail.addTo(subscr.getEmail());
		}
		// send your message
		resp.setContentType("text/plain");
		try {
			mail.send();
			resp.getWriter().println("Success---"+html);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			resp.getWriter().println("Error----"+e.getMessage());
			e.printStackTrace();
		}
	}
}
