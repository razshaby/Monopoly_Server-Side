//package smartspace.plugins;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import smartspace.dao.AdvancedActionDao;
//import smartspace.dao.AdvancedElementDao;
//import smartspace.dao.ExtendedUserDao;
//import smartspace.data.ActionEntity;
//import smartspace.data.ElementEntity;
//import smartspace.data.UserEntity;
//import smartspace.data.UserRole;
//
//@Component
//public class WhosTurn implements Plugin {
//
//	private ExtendedUserDao<String> users;
//	private AdvancedElementDao<String> elementDao;
//	private AdvancedActionDao actions;
//
//	@Autowired
//	public WhosTurn(ExtendedUserDao<String> users, AdvancedElementDao<String> elementDao, AdvancedActionDao actions) {
//		this.users = users;
//		this.elementDao = elementDao;
//		this.actions = actions;
//	}
//
//	public ActionEntity execute(ActionEntity action) {
//		
//		ArrayList<ElementEntity> elements = (ArrayList<ElementEntity>) elementDao.readAllOfSpecificTypeAndByExpired("name", 1, 0, "game",false);
//		
//		if(elements.isEmpty())
//			throw new RuntimeException("could not find any element of type game");
//		
//		ElementEntity gameElement = elements.get(0);
//		Map<String,Object> moreAttributes =  gameElement.getMoreAttributes();
//		
//		if(moreAttributes == null)
//			gameElement.setMoreAttributes(new HashMap<String, Object>());
//		
//		boolean containsTurn = gameElement.getMoreAttributes().containsKey("turn");
//		String currentTurnKey="";
//		
//		if(containsTurn)
//			currentTurnKey = (String) gameElement.getMoreAttributes().get("turn");
//		
//		
//		if(!containsTurn || currentTurnKey.trim().isEmpty() || currentTurnKey == null) {
//			ArrayList<UserEntity> currentOnlineUsers = (ArrayList<UserEntity>) users.readAllByPoints(UserRole.PLAYER);
//			currentTurnKey = currentOnlineUsers.get(0).getKey();
//			gameElement.getMoreAttributes().put("turn", currentTurnKey);
//			
//			ArrayList<String> currentOnlineUsersKeys = new ArrayList<String>();
//			for(UserEntity user: currentOnlineUsers)
//				currentOnlineUsersKeys.add(user.getKey());
//			
//			gameElement.getMoreAttributes().put("onlineUsers", currentOnlineUsersKeys);
//			
//			elementDao.update(gameElement);
//
//		}
//		
//		Map<String, Object> properties = new HashMap<>();
//		properties.put("turn",currentTurnKey);
//		action.setMoreAttributes(properties);
//		actions.create(action);
//		
//		return action;
//		
//	}
//
//}
