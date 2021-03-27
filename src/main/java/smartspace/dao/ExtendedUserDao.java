package smartspace.dao;

import java.util.List;

import smartspace.data.UserEntity;
import smartspace.data.UserRole;

public interface ExtendedUserDao<UserKey> extends AdvancedUserDao<UserKey> {
	
	public void updateUserMoney(UserEntity userEntity);
	public List<UserEntity> readAllByPoints(UserRole role);

}
