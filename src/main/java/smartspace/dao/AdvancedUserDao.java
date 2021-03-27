package smartspace.dao;

import java.util.List;

import smartspace.data.UserEntity;

public interface AdvancedUserDao<UserKey> extends UserDao<UserKey> {

	public List<UserEntity> readAll(
			int size, int page);
	
	public List<UserEntity> readAll(
			String sortingAttr,int size, int page);
	
	public UserEntity createFromImport(UserEntity userEntity);
}
