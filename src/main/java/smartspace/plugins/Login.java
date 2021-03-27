package smartspace.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smartspace.dao.AdvancedActionDao;
import smartspace.dao.AdvancedElementDao;
import smartspace.dao.ExtendedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;

@Component
public class Login implements Plugin {

	
	private ExtendedUserDao<String> users;
	private AdvancedElementDao<String> elementDao;
	private AdvancedActionDao actions;
	
	@Autowired
	public Login(ExtendedUserDao<String> users,AdvancedElementDao<String> elementDao,AdvancedActionDao actions) {
		this.users = users;
		this.elementDao = elementDao;
		this.actions = actions;


	}
	
	@Override
	public ActionEntity execute(ActionEntity action) {
		
		String userID=action.getPlayerSmartspace()+"|"+action.getPlayerEmail();
		UserEntity userEntity = users.readById(userID).orElseThrow(
				()->new RuntimeException("could not find any user with id: " +userID));
		
		if (!userEntity.getRole().equals(UserRole.PLAYER))
			throw new RuntimeException("user Role need to be PLAYER");

		
		Map<String, Object> properties = new HashMap<>();
		checkInToFirstCity(action,properties);

		properties.put("message", "Player " +userID +" set to 1000 points");
		action.setMoreAttributes(properties);
		

		
		String elementID = action.getElementSmartspace()+"|"+action.getElementId();
		
		
		ElementEntity elementEntity = elementDao.readById(elementID).orElseThrow(
				()->new RuntimeException("could not find any element with id: " +elementID));
		
				
		if(!elementEntity.getType().equals("game"))
				throw new RuntimeException("Element type need to be game");
		
		
		userEntity.setPoints(1000);
		users.updateUserMoney(userEntity);
	
		
		
		return actions.create(action);
		
//	List<ElementEntity> elementList=elementDao.readAllOfSpecificTypeAndByExpired("type", 1, 0, "game", false);
//		
//		if(elementList.size()==0)
//				throw new RuntimeException("Element with type game not exist");
//		
//		if(!elementID.equals(elementList.get(0).getKey()))
//			throw new RuntimeException("Element id not valid");
	}

	private void checkInToFirstCity(ActionEntity action,Map<String, Object> properties) {
		String userKey = action.getPlayerSmartspace() + "|" + action.getPlayerEmail();

		UserEntity user = users.readById(userKey).orElseThrow(
				()->new RuntimeException("could not find any user with id: " + userKey));
	
		
		if (!user.getRole().equals(UserRole.PLAYER))
			throw new RuntimeException("user Role need to be PLAYER");
		
		
		
		ElementEntity cityEntity = this.elementDao.readAllOfSpecificTypeAndByExpired("location.x", 100, 0, "city", false).get(0);
		
		
	String elementKey = cityEntity.getElementSmartspace()+"|"+cityEntity.getElementId();
	
	
//		ElementEntity element = elementDao.readById(elementKey).orElseThrow(
//					()->new RuntimeException("could not find any element with id: " + elementKey));
		
//		if(!element.getType().equals("city"))
//			throw new RuntimeException("can't check in to element with type: " + element.getType());
		
					
		ArrayList<String> playersList = (ArrayList<String>) cityEntity.getMoreAttributes().get("visitors");
	    if(playersList == null) {
	    	playersList = new ArrayList<String>();	
	    }
	  
	    for (String string : playersList) {
	    	if(userKey.equals(string))
	    	{
	    		throw new RuntimeException(userKey+" already checked in to: " + cityEntity.getName());
	    		
	    	}
		}

	    playersList.add(userKey);
	    cityEntity.getMoreAttributes().put("visitors",playersList);
	    cityEntity.setMoreAttributes(cityEntity.getMoreAttributes());
	    
	    properties.put("checkIn", "Player " +user.getUsername() +" check in to "+ cityEntity.getName());
	    
	    elementDao.update(cityEntity);		
	}
	
	
	
	
	

}
