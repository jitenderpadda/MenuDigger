package com.jitender.menudigger.entity;

import com.jitender.menudigger.entity.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
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

@Api(name = "menuitemendpoint", namespace = @ApiNamespace(ownerDomain = "jitender.com", ownerName = "jitender.com", packagePath = "menudigger.entity"))
public class MenuItemEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	@ApiMethod(name = "listMenuItem")
	public ArrayList<MenuItem> listMenuItem(@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit,
			@Nullable @Named("search") String search,
			@Nullable @Named("restroid") String restroid,
			@Nullable @Named("tags") ArrayList<String> tags) {  

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<MenuItem> execute = null;
		HashMap<String,String> parameters = new HashMap<String, String>();
		ArrayList<MenuItem> resultList=new ArrayList<MenuItem>();

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(MenuItem.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}
			
			if (limit != null) {
				query.setRange(0, limit);
			}
								
			//Specify Filters
			if(search !=null && restroid ==null){
				query.declareParameters("String search");
				query.setFilter("name.startsWith(search)");
				execute = (List<MenuItem>) query.execute(search);
			} else if(restroid !=null && search ==null){
				query.declareParameters("String restroid");
				query.setFilter("restaurantId==restroid");
				execute = (List<MenuItem>) query.execute(restroid);
			} else if(search !=null && restroid !=null){
				query.declareParameters("String search,String restroid");
				query.setFilter("name.startsWith(search) && restaurantId == restroid");
				execute = (List<MenuItem>) query.execute(search,restroid);
			} else execute = (List<MenuItem>) query.execute();
			
			if(tags!=null){
				for(MenuItem item:execute){
					Boolean flag=true;
					for(String tag:tags){
						if(!item.getTags().contains(tag)){
							flag=false;
						}
					}
					if(flag){
						resultList.add(item);
					}
				}
			}
			else resultList.addAll(execute);
				
				
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (MenuItem obj : execute)
				;
		} finally { 
			mgr.close();
		}

		//return CollectionResponse.<MenuItem>builder().setItems(execute).setNextPageToken(cursorString).build();
		return resultList;
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getMenuItem")
	public MenuItem getMenuItem(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		MenuItem menuitem = null;
		try {
			menuitem = mgr.getObjectById(MenuItem.class, id);
		} finally {
			mgr.close();
		}
		return menuitem;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param menuitem the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertMenuItem")
	public MenuItem insertMenuItem(MenuItem menuitem) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (menuitem.getId() != null) {
				if (containsMenuItem(menuitem)) {
					throw new EntityExistsException("Object already exists");
				}
			}
			mgr.makePersistent(menuitem);
		} finally {
			mgr.close();
		}
		return menuitem;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param menuitem the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateMenuItem")
	public MenuItem updateMenuItem(MenuItem menuitem) {
		PersistenceManager mgr = getPersistenceManager();
		MenuItem existingItem=null;
		try {
			if (!containsMenuItem(menuitem)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			existingItem=mgr.getObjectById(MenuItem.class, menuitem.getId());
			if(menuitem.getName()!=existingItem.getName()){
				existingItem.setName(menuitem.getName());
			}
			if(menuitem.getPrice()!=existingItem.getPrice()){
				existingItem.setPrice(menuitem.getPrice());
			}
			if(menuitem.getRestaurantId()!=existingItem.getRestaurantId()){
				existingItem.setRestaurantId(menuitem.getRestaurantId());
			}
			if(menuitem.getTags()!=existingItem.getTags()){
				existingItem.setTags(menuitem.getTags());
			}
			mgr.makePersistent(existingItem);
		} finally {
			mgr.close();
		}
		return existingItem;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeMenuItem")
	public void removeMenuItem(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			MenuItem menuitem = mgr.getObjectById(MenuItem.class, id);
			mgr.deletePersistent(menuitem);
		} finally {
			mgr.close();
		}
	}

	private boolean containsMenuItem(MenuItem menuitem) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(MenuItem.class, menuitem.getId());
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
