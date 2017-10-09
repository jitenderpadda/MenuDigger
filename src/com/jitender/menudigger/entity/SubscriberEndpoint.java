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

@Api(name = "subscriberendpoint", namespace = @ApiNamespace(ownerDomain = "jitender.com", ownerName = "jitender.com", packagePath = "menudigger.entity"))
public class SubscriberEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listSubscriber")
	public CollectionResponse<Subscriber> listSubscriber(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Subscriber> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Subscriber.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Subscriber>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Subscriber obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Subscriber>builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getSubscriber")
	public Subscriber getSubscriber(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		Subscriber subscriber = null;
		try {
			subscriber = mgr.getObjectById(Subscriber.class, id);
		} finally {
			mgr.close();
		}
		return subscriber;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param subscriber the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertSubscriber")
	public Subscriber insertSubscriber(Subscriber subscriber) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsSubscriber(subscriber)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(subscriber);
		} finally {
			mgr.close();
		}
		return subscriber;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param subscriber the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateSubscriber")
	public Subscriber updateSubscriber(Subscriber subscriber) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsSubscriber(subscriber)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(subscriber);
		} finally {
			mgr.close();
		}
		return subscriber;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeSubscriber")
	public void removeSubscriber(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Subscriber subscriber = mgr.getObjectById(Subscriber.class, id);
			mgr.deletePersistent(subscriber);
		} finally {
			mgr.close();
		}
	}

	private boolean containsSubscriber(Subscriber subscriber) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Subscriber.class, subscriber.getId());
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
