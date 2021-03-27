package smartspace.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smartspace.dao.AdvancedActionDao;
import smartspace.dao.AdvancedElementDao;
import smartspace.dao.ExtendedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;

@Component
public class Buy implements Plugin {

	private ExtendedUserDao<String> users;
	private AdvancedElementDao<String> elementDao;
	private AdvancedActionDao actions;

	@Autowired
	public Buy(ExtendedUserDao<String> users, AdvancedElementDao<String> elementDao, AdvancedActionDao actions) {
		this.users = users;
		this.elementDao = elementDao;
		this.actions = actions;

	}

	@Override
	public ActionEntity execute(ActionEntity action) {
		String elementKey = action.getElementSmartspace() + "|" + action.getElementId();

		ElementEntity elementToBuy = elementDao.readById(elementKey)
				.orElseThrow(() -> new RuntimeException("could not find any element with id: " + elementKey));

		if (!elementToBuy.getType().equals("city"))
			throw new RuntimeException("can't buy element with type: " + elementToBuy.getType());

		String userKey = action.getPlayerSmartspace() + "|" + action.getPlayerEmail();

		UserEntity user = users.readById(userKey)
				.orElseThrow(() -> new RuntimeException("could not find any user with id: " + userKey));

		long userMoney = user.getPoints();
		long cityPrice = (long) elementToBuy.getMoreAttributes().get("price");

		
		
		// check if user can buy
		boolean canBuy = false;
		if (userMoney < cityPrice)
			throw new RuntimeException(
					"user " + user.getUsername() + " don't have enough money to buy " + elementToBuy.getName());
		if (!elementToBuy.getMoreAttributes().get("ownerId").equals(""))
			throw new RuntimeException(elementToBuy.getName() + " already has an owner");
		ArrayList<String> playersList = (ArrayList<String>) elementToBuy.getMoreAttributes().get("visitors");
		if (playersList != null) {

			for (String string : playersList) {
				if (userKey.equals(string)) {
					canBuy = true;

				}
			}
			if (!canBuy)
				throw new RuntimeException(userKey + " need to checkIn to " + elementToBuy.getName() + " first!");
		}
			user.setPoints(userMoney - cityPrice);

			
			
			users.updateUserMoney(user);

			// update ownerId and ownerName
			Map<String, Object> moreAttributes = elementToBuy.getMoreAttributes();
			moreAttributes.put("ownerId", userKey);
			moreAttributes.put("ownerName", user.getUsername());
			elementToBuy.setMoreAttributes(moreAttributes);
			elementDao.update(elementToBuy);

			// create a message in returned action
			Map<String, Object> properties = new HashMap<>();
			properties.put("message", "Player " + user.getUsername() + " bought " + elementToBuy.getName());
			action.setMoreAttributes(properties);
			actions.create(action);
			return action;

		
	}
}
