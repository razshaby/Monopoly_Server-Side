package smartspace.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.dao.ExtendedUserDao;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;


@Service
public class UsersServiceImpl implements UsersService{
	private ExtendedUserDao<String> users;
	private AdvancedElementDao<String> elements;
	private String smartspace;


	@Value("${smartspace.name}")
	public void setSmartspace(String smartspace) {
		this.smartspace = smartspace;
	}
	
	@Autowired
	public UsersServiceImpl(ExtendedUserDao<String> users, AdvancedElementDao<String> elements) {
		super();
		this.users = users;
		this.elements = elements;
	}

	@Override
	@Transactional
	public List<UserEntity> insertUsers(List<UserEntity> users,String adminSmartspace, String adminEmail) {
		List<UserEntity> userEntities = new ArrayList<UserEntity>();
		
		if (!validateAdmin(adminSmartspace, adminEmail))
			throw new RuntimeException("User has to be admin");
		
		for(UserEntity user : users) {
			validate(user);		
			if (!validateSmartspace(user.getUserSmartspace())) {
				throw new RuntimeException("invalid user smartspace");
			}
			
			userEntities.add(this.users.createFromImport(user));
		}
		
		return userEntities;
	}
	
	private boolean validateAdmin(String adminSmartspace, String adminEmail) {
		Optional<UserEntity> user = this.users.readById(adminSmartspace + "|" + adminEmail);
		if (user.isPresent())
			if (user.get().getRole().equals(UserRole.ADMIN))
				return true;

		return false;

	}
	
	
	private boolean validatePlayer(String playerSmartspace, String playerEmail) {
		Optional<UserEntity> user = this.users.readById(playerSmartspace + "|" + playerEmail);
		if (user.isPresent() )
			if (user.get().getRole().equals(UserRole.PLAYER))
				return true;

		return false;

	}
	
	private boolean validateSmartspace(String smartspace) {
		if (this.smartspace.equals(smartspace))
			return false;
		else
			return true;

	}
	
	private void validate(UserEntity user) {
		
		if(user==null)
		{
			throw new RuntimeException("user is null");
		}
		
		if(user.getAvatar() == null || user.getAvatar().trim().isEmpty())
			throw new RuntimeException("Invalid Avatar");

		
		
		if(user.getUsername() == null || user.getUsername().trim().isEmpty())
		{
			throw new RuntimeException("Invalid Username");

		}
		
		if(user.getRole() == null)
		{
			throw new RuntimeException("Invalid Role");

		}
		
		
		if(user.getUserSmartspace()==null || user.getUserSmartspace().trim().isEmpty())
		{
			throw new RuntimeException("Invalid UserSmartspace");
		}
		
		if(	user.getUserEmail()==null || 	user.getUserEmail().trim().isEmpty())
		{
			throw new RuntimeException("Invalid UserEmail: "+user.getUserEmail());
		}
		
	}
	
	
	

	@Override
	public List<UserEntity> getUsers(String adminSmartspace, String adminEmail,int size, int page) {
		
		if (validateAdmin(adminSmartspace, adminEmail))
			return this.users.readAll("username", size, page);
		else
			throw new RuntimeException("User has to be admin");
	}
	
	
	
	
	private void validateNewUser(UserEntity user) {
		
		if(user==null)
		{
			throw new RuntimeException("user is null");
		}
		
		if(user.getAvatar() == null || user.getAvatar().trim().isEmpty())
			throw new RuntimeException("Invalid Avatar");

		
		
		if(user.getUsername() == null || user.getUsername().trim().isEmpty())
		{
			throw new RuntimeException("Invalid Username");

		}
		
		if(user.getRole() == null)
		{
			throw new RuntimeException("Invalid Role");

		}

		
		if(	user.getUserEmail()==null || 	user.getUserEmail().trim().isEmpty())
		{
			throw new RuntimeException("Invalid UserEmail: "+user.getUserEmail());
		}
		
	}

	@Override
	@Transactional
	public UserEntity insertUser(UserEntity user) {
		validateNewUser(user);
//		if(user.getRole().equals(UserRole.PLAYER))
//			user.setPoints(1000);		
		return this.users.create(user);
	}

	@Override
	public UserEntity getUser(String userSmartspace, String userEmail) {
		return this.users.readById(userSmartspace+"|"+userEmail).orElseThrow(()->new RuntimeException("user Not Found"));
	}

	@Override
	public void update(String userSmartspace, String userEmail, UserEntity entity) throws EntityNotFoundException {
		entity.setKey(userSmartspace+"|"+userEmail);		
		this.users.update(entity);
	}

	@Override
	public List<UserEntity> getOnlinePlayers(String playerSmartspace, String playerEmail) {
		if (validatePlayer(playerSmartspace, playerEmail))
			return this.users.readAllByPoints(UserRole.PLAYER);
		else
			throw new RuntimeException("User has to be player");
	}

	@Override
	public UserEntity getNextUserTurn(String playerSmartspace, String playerEmail) {
		ArrayList<ElementEntity> gameElements = (ArrayList<ElementEntity>) elements.readAllOfSpecificTypeAndByExpired("name", 1, 0, "game",false);
		
		if(gameElements.isEmpty())
			throw new RuntimeException("could not find any element of type game");
		
		ElementEntity gameElement = gameElements.get(0);
		Map<String,Object> moreAttributes =  gameElement.getMoreAttributes();
		
		if(moreAttributes == null)
			gameElement.setMoreAttributes(new HashMap<String, Object>());
		
		boolean containsTurn = gameElement.getMoreAttributes().containsKey("turn");
		String currentTurnKey="";
		
		if(containsTurn)
			currentTurnKey = (String) gameElement.getMoreAttributes().get("turn");
		
	
		if(!containsTurn || currentTurnKey.trim().isEmpty() || currentTurnKey == null) {
			ArrayList<UserEntity> currentOnlineUsers = (ArrayList<UserEntity>) users.readAllByPoints(UserRole.PLAYER);
		
			if(currentOnlineUsers.isEmpty()) {
				throw new EntityNotFoundException("game is over");
			}
			currentTurnKey = currentOnlineUsers.get(0).getKey();
			gameElement.getMoreAttributes().put("turn", currentTurnKey);
			
			ArrayList<String> currentOnlineUsersKeys = new ArrayList<String>();
			for(UserEntity user: currentOnlineUsers)
				currentOnlineUsersKeys.add(user.getKey());
			
			gameElement.getMoreAttributes().put("onlineUsers", currentOnlineUsersKeys);
			
			elements.update(gameElement);

		}
		

		UserEntity user = users.readById(currentTurnKey).orElseThrow(
				()->new RuntimeException("could not find next user turn"));
		
		return user;
	}

}
