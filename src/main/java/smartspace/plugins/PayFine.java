//package smartspace.plugins;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
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
//public class PayFine implements Plugin {
//
//	
//	private ExtendedUserDao<String> users;
//	private AdvancedElementDao<String> elementDao;
//	private AdvancedActionDao actions;
//	
//	@Autowired
//	public PayFine(ExtendedUserDao<String> users,AdvancedElementDao<String> elementDao,AdvancedActionDao actions) {
//		this.users = users;
//		this.elementDao = elementDao;
//		this.actions = actions;
//
//
//	}
//
//	@Override
//	public ActionEntity execute(ActionEntity action) {
//		String elementKey = action.getElementSmartspace()+"|"+action.getElementId();
//		
//		ElementEntity element = elementDao.readById(elementKey).orElseThrow(
//					()->new RuntimeException("could not find any element with id: " + elementKey));
//		
//		
//		if(!element.getType().equals("city"))
//			new RuntimeException("can't pay fine on element with type: " + element.getType());
//		
//		String userKey = action.getPlayerSmartspace() + "|" + action.getPlayerEmail();
//		
//		UserEntity user = users.readById(userKey).orElseThrow(
//				()->new RuntimeException("could not find any user with id: " + userKey));
//	
//		
//		if (!user.getRole().equals(UserRole.PLAYER))
//			throw new RuntimeException("user Role need to be PLAYER");
//		
//		
//		String ownerKey = (String) element.getMoreAttributes().get("ownerId");
//		
//		if(ownerKey.isEmpty())
//			new RuntimeException("could not find any owner to: " + element.getName());
//
//		UserEntity owner = users.readById(ownerKey).orElseThrow(
//				()->new RuntimeException("could not find any owner with id: " + ownerKey));
//		
//		//check money and update
//		long userMoney = user.getPoints();
//		long fineToPay = (long) element.getMoreAttributes().get("fine");
//		
//		if(userMoney < fineToPay) {
//			fineToPay = userMoney;
//			user.setPoints(-1);
//		}else {
//			user.setPoints(userMoney - fineToPay);
//		}
//		
//		owner.setPoints(owner.getPoints() + fineToPay);
//		
//		users.updateUserMoney(user);
//		users.updateUserMoney(owner);
//
//		//create a message in returned action
//		Map<String, Object> properties = new HashMap<>();
//		properties.put("message", "Player " + user.getUsername() +" paid fine to " + owner.getUsername());
//		action.setMoreAttributes(properties);
//		actions.create(action);
//		return action;
//	}
//}