package com.jitender.menudigger.entity;

import com.jitender.menudigger.entity.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "usercommentendpoint", namespace = @ApiNamespace(ownerDomain = "jitender.com", ownerName = "jitender.com", packagePath = "menudigger.entity"))
public class UserCommentEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listUserComment")
	public CollectionResponse<UserComment> listUserComment(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit,
			@Nullable @Named("searchItemId") Long searchItemId) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<UserComment> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(UserComment.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}
			
			if (limit != null) {
				query.setRange(0, limit);
			}
			
			//For filtering Comments based on searchItemId
			if(searchItemId!=null){
				query.declareParameters("Long searchItemId");
				query.setFilter("itemId==searchItemId");
				execute=(List<UserComment>) query.execute(searchItemId);
			}	
			else execute = (List<UserComment>) query.execute();
			
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (UserComment obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<UserComment>builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getUserComment")
	public UserComment getUserComment(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		UserComment usercomment = null;
		try {
			usercomment = mgr.getObjectById(UserComment.class, id);
		} finally {
			mgr.close();
		}
		return usercomment;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param usercomment the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertUserComment")
	public UserComment insertUserComment(UserComment usercomment) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (usercomment.getId() != null) {
				if (containsUserComment(usercomment)) {
					throw new EntityExistsException("Object already exists");
				}
			}
			mgr.makePersistent(usercomment);
		} finally {
			mgr.close();
		}
		return usercomment;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param usercomment the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateUserComment")
	public UserComment updateUserComment(UserComment usercomment) {
		PersistenceManager mgr = getPersistenceManager();
		UserComment existingUserComment=null;
		try {
			if (!containsUserComment(usercomment)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			existingUserComment=mgr.getObjectById(UserComment.class, usercomment.getId());
			if(usercomment.getUserId()!=null){
				existingUserComment.setUserId(usercomment.getUserId());
			}
			if(usercomment.getItemId()!=null){
				existingUserComment.setItemId(usercomment.getItemId());
			}
			if(usercomment.getComment()!=null){
				existingUserComment.setComment((usercomment.getComment()));
			}
			if(usercomment.getRating()!=0){
				existingUserComment.setRating((usercomment.getRating()));
			}
			mgr.makePersistent(existingUserComment);
		} finally {
			mgr.close();
		}
		return existingUserComment;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeUserComment")
	public void removeUserComment(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			UserComment usercomment = mgr.getObjectById(UserComment.class, id);
			mgr.deletePersistent(usercomment);
		} finally {
			mgr.close();
		}
	}

	private boolean containsUserComment(UserComment usercomment) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(UserComment.class, usercomment.getId());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		} 
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
