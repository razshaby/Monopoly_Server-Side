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
public class CheckIn implements Plugin {

	private ExtendedUserDao<String> users;
	private AdvancedElementDao<String> elementDao;
	private AdvancedActionDao actions;
	

	@Autowired
	public CheckIn(ExtendedUserDao<String> users,AdvancedElementDao<String> elementDao,AdvancedActionDao actions) {
		this.users = users;
		this.elementDao = elementDao;
		this.actions = actions;
	}
	
	public ActionEntity execute(ActionEntity action) {
		
		
		String userKey = action.getPlayerSmartspace() + "|" + action.getPlayerEmail();

		UserEntity user = users.readById(userKey).orElseThrow(
				()->new RuntimeException("could not find any user with id: " + userKey));
	
		
		if (!user.getRole().equals(UserRole.PLAYER))
			throw new RuntimeException("user Role need to be PLAYER");
		
		if(user.getPoints()==-1)
		{
			throw new RuntimeException("can't check in,number of points is -1");
		}
		
	String elementKey = action.getElementSmartspace()+"|"+action.getElementId();
	
	
		ElementEntity element = elementDao.readById(elementKey).orElseThrow(
					()->new RuntimeException("could not find any element with id: " + elementKey));
		
		if(!element.getType().equals("city"))
			throw new RuntimeException("can't check in to element with type: " + element.getType());
		
					
		ArrayList<String> playersList = (ArrayList<String>) element.getMoreAttributes().get("visitors");
	    if(playersList == null) {
	    	playersList = new ArrayList<String>();
	    	
	    }
	  
	    for (String string : playersList) {
	    	if(userKey.equals(string))
	    	{
	    		throw new RuntimeException(userKey+" already checked in to: " + element.getName());
	    		
	    	}
		}

	    playersList.add(userKey);
	    element.getMoreAttributes().put("visitors",playersList);
	    element.setMoreAttributes(element.getMoreAttributes());
	    
	    
	    elementDao.update(element);
	    
		Map<String, Object> properties = new HashMap<>();

	    
	    payFine(element,properties,user);
	    
	    
	    
		//create a message in returned action
				properties.put("message", "Player " + user.getUsername() +" check in to " + element.getName());
				action.setMoreAttributes(properties);
				actions.create(action);
				return action;
	}

	private void payFine(ElementEntity element,Map<String, Object> properties,UserEntity user) {
	String ownerKey = (String) element.getMoreAttributes().get("ownerId");
		
		if((ownerKey.isEmpty())||(ownerKey.equals(user.getKey())))
			{
			if(ownerKey.isEmpty())
			properties.put("state","canPlay");
			else
				properties.put("state","isOwner");
			return;
			}

		

		UserEntity owner = users.readById(ownerKey).orElseThrow(
				()->new RuntimeException("could not find any owner with id: " + ownerKey));
		
		//check money and update
		long userMoney = user.getPoints();
		long fineToPay = (long) element.getMoreAttributes().get("fine");
		
		
		
		if(userMoney < fineToPay) {
			fineToPay = userMoney;
			user.setPoints(-1);
			properties.put("state","lose");
		}else {
			user.setPoints(userMoney - fineToPay);
			properties.put("state","payed");
			
		}
		properties.put("message", "Player " + user.getUsername() +" paid fine to " + owner.getUsername());
		properties.put("payedTo",owner.getUsername());
		properties.put("finePaid",fineToPay);
		
		owner.setPoints(owner.getPoints() + fineToPay);
		
		users.updateUserMoney(user);
		users.updateUserMoney(owner);	
	}

}
