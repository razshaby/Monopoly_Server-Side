package smartspace.logic;

import java.util.List;

import smartspace.data.UserEntity;


public interface UsersService {
	public List<UserEntity> insertUsers (List<UserEntity> user,String adminSmartspace,String adminEmail);
	public List<UserEntity> getUsers (String adminSmartspace, String adminEmail, int size, int page);
	public UserEntity insertUser (UserEntity user);
	public UserEntity getUser(String userSmartspace, String userEmail);
	public void update(String userSmartspace, String userEmail, UserEntity entity) throws EntityNotFoundException;
	public List<UserEntity> getOnlinePlayers (String playerSmartspace, String playerEmail);
	public UserEntity getNextUserTurn(String playerSmartspace, String playerEmail);



}
