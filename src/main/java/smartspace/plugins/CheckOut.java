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
public class CheckOut implements Plugin {

	private ExtendedUserDao<String> users;
	private AdvancedElementDao<String> elementDao;
	private AdvancedActionDao actions;

	@Autowired
	public CheckOut(ExtendedUserDao<String> users, AdvancedElementDao<String> elementDao, AdvancedActionDao actions) {
		this.users = users;
		this.elementDao = elementDao;
		this.actions = actions;
	}

	public ActionEntity execute(ActionEntity action) {

		String userKey = action.getPlayerSmartspace() + "|" + action.getPlayerEmail();

		UserEntity user = users.readById(userKey)
				.orElseThrow(() -> new RuntimeException("could not find any user with id: " + userKey));

		if (!user.getRole().equals(UserRole.PLAYER))
			throw new RuntimeException("user Role need to be PLAYER");

		String elementKey = action.getElementSmartspace() + "|" + action.getElementId();

		ElementEntity element = elementDao.readById(elementKey)
				.orElseThrow(() -> new RuntimeException("could not find any element with id: " + elementKey));

		if (!element.getType().equals("city"))
			new RuntimeException("can't checkout element with type: " + element.getType());

		Map<String, Object> moreAttributes = element.getMoreAttributes();
		ArrayList<String> playersList = (ArrayList<String>) moreAttributes.get("visitors");

		if (playersList == null) {
			playersList = new ArrayList<String>();

		} else {
			playersList.remove(userKey);
		}
		
		moreAttributes.put("visitors", playersList);
		element.setMoreAttributes(moreAttributes);
		elementDao.update(element);

		// create a message in returned action
		Map<String, Object> properties = new HashMap<>();
		properties.put("message", "Player " + user.getUsername() + " check out from " + element.getName());
		action.setMoreAttributes(properties);
		actions.create(action);
		return action;
	}

}
