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
public class EndTurn implements Plugin {

	private ExtendedUserDao<String> users;
	private AdvancedElementDao<String> elementDao;
	private AdvancedActionDao actions;

	@Autowired
	public EndTurn(ExtendedUserDao<String> users, AdvancedElementDao<String> elementDao, AdvancedActionDao actions) {
		this.users = users;
		this.elementDao = elementDao;
		this.actions = actions;
	}

	public ActionEntity execute(ActionEntity action) {
		Map<String, Object> properties = new HashMap<>();
		ArrayList<String> lastOnlineUsers;

		String userKey = action.getPlayerSmartspace() + "|" + action.getPlayerEmail();

		ArrayList<ElementEntity> elements = (ArrayList<ElementEntity>) elementDao
				.readAllOfSpecificTypeAndByExpired("name", 1, 0, "game", false);

		if (elements.isEmpty())
			throw new RuntimeException("could not find any element of type game");

		ElementEntity gameElement = elements.get(0);
		
		if(!gameElement.getType().equalsIgnoreCase("game"))
			throw new RuntimeException("can't end turn on elemenet with type " + gameElement.getType());

		String currentTurnKey = (String) gameElement.getMoreAttributes().get("turn");
		
		
		// check if current turn and action's player are the same user
		if (currentTurnKey != null && currentTurnKey.equals(userKey)) {
			boolean containsLastOnlineUsers = gameElement.getMoreAttributes().containsKey("onlineUsers");

			if (!containsLastOnlineUsers)
				throw new RuntimeException("There is no online users list in game");
			else
				lastOnlineUsers = (ArrayList<String>) gameElement.getMoreAttributes().get("onlineUsers");

			ArrayList<UserEntity> currentOnlineUsers = (ArrayList<UserEntity>) users.readAllByPoints(UserRole.PLAYER);

			ArrayList<String> currentOnlineUsersKeys = new ArrayList<String>();
			for (UserEntity user : currentOnlineUsers)
				currentOnlineUsersKeys.add(user.getKey());

			int indexLastTurn;
			boolean foundNextTurn = false;

			if (currentOnlineUsers.size() >= 2) {

				// check if player is still online

				indexLastTurn = lastOnlineUsers.indexOf(currentTurnKey);
				int nextTurnIndex = -1;

				// if player doesn't online try to find the next player
				while (!foundNextTurn) {

					if (indexLastTurn == lastOnlineUsers.size() - 1)
						nextTurnIndex = 0;
					else {
						nextTurnIndex = indexLastTurn + 1;
					}

					foundNextTurn = currentOnlineUsersKeys.contains(lastOnlineUsers.get(nextTurnIndex));
					indexLastTurn++;

				}

				gameElement.getMoreAttributes().put("turn", lastOnlineUsers.get(nextTurnIndex));
				gameElement.getMoreAttributes().put("onlineUsers", currentOnlineUsersKeys);
				elementDao.update(gameElement);

				properties.put("message",
						"Turn moved from " + currentTurnKey + " to" + lastOnlineUsers.get(nextTurnIndex));
			} else {
				gameElement.getMoreAttributes().remove("turn");
				gameElement.getMoreAttributes().remove("onlineUsers");
				elementDao.update(gameElement);
				properties.put("message", "Game is over");

			}
		} else {

			properties.put("message", "End turn skipped because it's not user turn");

		}
		action.setMoreAttributes(properties);
		actions.create(action);
		return action;
	}

}
