package smartspace.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class Logout implements Plugin {

	private ExtendedUserDao<String> users;
	private AdvancedElementDao<String> elements;
	private AdvancedActionDao actions;

	@Autowired
	public Logout(ExtendedUserDao<String> users, AdvancedElementDao<String> elements, AdvancedActionDao actions) {
		this.users = users;
		this.elements = elements;
		this.actions = actions;

	}

	@Override
	public ActionEntity execute(ActionEntity action) {

		String userID = action.getPlayerSmartspace() + "|" + action.getPlayerEmail();
		UserEntity userEntity = users.readById(userID)
				.orElseThrow(() -> new RuntimeException("could not find any user with id: " + userID));

		if (!userEntity.getRole().equals(UserRole.PLAYER))
			throw new RuntimeException("userRole need to be PLAYER");

		Map<String, Object> properties = new HashMap<>();
		properties.put("message", "Player " + userID + " set to -1 points");
		action.setMoreAttributes(properties);

		String elementID = action.getElementSmartspace() + "|" + action.getElementId();

		ElementEntity elementEntity = elements.readById(elementID)
				.orElseThrow(() -> new RuntimeException("could not find any element with id: " + elementID));

		if (!elementEntity.getType().equals("game"))
			throw new RuntimeException("Element type need to be game");

		userEntity.setPoints(-1);
		users.updateUserMoney(userEntity);

		List<ElementEntity> citiesList = elements.readAllOfSpecificTypeAndByExpired("location.x", 100, 0, "city",
				false);

		for (ElementEntity cityEntity : citiesList) {
			if (cityEntity.getMoreAttributes().get("ownerId").equals(userID)) {
				cityEntity.getMoreAttributes().put("ownerId", "");
				cityEntity.getMoreAttributes().put("ownerName", "");
				elements.update(cityEntity);
				
			}
			checkout(cityEntity,userID);
		}

		return actions.create(action);

//	List<ElementEntity> elementList=elementDao.readAllOfSpecificTypeAndByExpired("type", 1, 0, "game", false);
//		
//		if(elementList.size()==0)
//				throw new RuntimeException("Element with type game not exist");
//		
//		if(!elementID.equals(elementList.get(0).getKey()))
//			throw new RuntimeException("Element id not valid");
	}

	private void checkout(ElementEntity cityEntity,String userID) {
		
			Map<String, Object> moreAttributes = cityEntity.getMoreAttributes();
			ArrayList<String> playersList = (ArrayList<String>) moreAttributes.get("visitors");

			if (playersList == null) {
				playersList = new ArrayList<String>();

			} else {
				playersList.remove(userID);
			}
			
			moreAttributes.put("visitors", playersList);
			cityEntity.setMoreAttributes(moreAttributes);
			elements.update(cityEntity);
				
	}
	
	


}
