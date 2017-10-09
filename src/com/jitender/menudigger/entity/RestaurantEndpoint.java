package com.jitender.menudigger.entity;

import com.jitender.menudigger.entity.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "restaurantendpoint", namespace = @ApiNamespace(ownerDomain = "jitender.com", ownerName = "jitender.com", packagePath = "menudigger.entity"))
public class RestaurantEndpoint {

	/** 
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listRestaurant")
	public CollectionResponse<Restaurant> listRestaurant(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit,
			@Nullable @Named("searchItem") String searchItem,
			@Nullable @Named("itemTags") ArrayList<String> itemTags) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Restaurant> execute = null;
		List<MenuItem> items = null;
		ArrayList<String> restroIds = new ArrayList<String>();

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Restaurant.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}
			
			//For filtering Restaurants based on searchItem
			if(searchItem!=null){
				Query queryItem = mgr.newQuery(MenuItem.class);
				queryItem.declareParameters("String item");
				queryItem.setFilter("name==item");
				items=(List<MenuItem>) queryItem.execute(searchItem);
				//For Filtering on itemTags
				if(itemTags!=null){
					for(MenuItem item:items){
						Boolean flag=true;
						for(String tag:itemTags){
							if(!item.getTags().contains(tag)){
								flag=false;
							}
						}
						if(flag){
							restroIds.add(item.restaurantId);
						}
					}
				}
				else{
					for(MenuItem item:items){
						restroIds.add(item.restaurantId);
					}
				}
				//Avoid Null Pointer exception
				if(restroIds.isEmpty())
					restroIds.add("INVALID ID");
				//Set Query Filter
				query.declareParameters("java.util.ArrayList restroIds"); 
				query.setFilter("id==restroIds");
				execute = (List<Restaurant>) query.execute(restroIds);
			}
			else execute = (List<Restaurant>) query.execute();
						
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Restaurant obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Restaurant>builder().setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getRestaurant")
	public Restaurant getRestaurant(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		Restaurant restaurant = null;
		try {
			restaurant = mgr.getObjectById(Restaurant.class, id);
		} finally {
			mgr.close();
		}
		return restaurant;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param restaurant the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertRestaurant")
	public Restaurant insertRestaurant(Restaurant restaurant) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (restaurant.getId() != null) {
				if (containsRestaurant(restaurant)) {
					throw new EntityExistsException("Object already exists");
				}
			}
			mgr.makePersistent(restaurant);
		} finally {
			mgr.close();
		}
		return restaurant;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param restaurant the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateRestaurant")
	public Restaurant updateRestaurant(Restaurant restaurant) {
		PersistenceManager mgr = getPersistenceManager();
		Restaurant existingRestro=null;
		try {
			if (!containsRestaurant(restaurant)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			existingRestro=mgr.getObjectById(Restaurant.class, restaurant.getId());
			if(restaurant.getRestroName()!=null){
				existingRestro.setRestroName(restaurant.getRestroName());
			}
			if(restaurant.getRestAddress()!=null){
				existingRestro.setRestAddress(restaurant.getRestAddress());
			}
			if(restaurant.getLatLong()!=null){
				existingRestro.setLatLong((restaurant.getLatLong()));
			}
			mgr.makePersistent(existingRestro);
		} finally {
			mgr.close();
		}
		return existingRestro;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeRestaurant")
	public void removeRestaurant(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Restaurant restaurant = mgr.getObjectById(Restaurant.class, id);
			mgr.deletePersistent(restaurant);
		} finally {
			mgr.close();
		}
	}

	private boolean containsRestaurant(Restaurant restaurant) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Restaurant.class, restaurant.getId());
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
