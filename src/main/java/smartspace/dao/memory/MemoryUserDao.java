package smartspace.dao.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import smartspace.dao.UserDao;
import smartspace.data.UserEntity;

//@Repository
public class MemoryUserDao implements UserDao<String> {
	private Map<String, UserEntity> users;
	private AtomicLong nextId;
	
	
	
	public MemoryUserDao() {
		// create a thread safe collection
		this.users = Collections.synchronizedMap(new HashMap<>());
		this.nextId = new AtomicLong(1L);
	}

	@Override
	public UserEntity create(UserEntity userEntity) {
		/*
		userEntity.setKey(this.nextId.getAndIncrement());
		this.users.put(userEntity.getKey(), userEntity);
		return userEntity;
	*/
		userEntity.setKey(userEntity.getUserEmail()+userEntity.getUserSmartspace());
		this.users.put(userEntity.getKey(), userEntity);

		return userEntity;

	}

	@Override
	public Optional<UserEntity> readById(String userKey) {
		UserEntity output = this.users.get(userKey);
		if ( output != null) {
			return Optional.of(output);
		}
		else {
			return Optional.empty();
		}
	}

	@Override
	public List<UserEntity> readAll() {
		return new ArrayList<UserEntity>(this.users.values());
	}

	@Override
	public void update(UserEntity userEntity) {
		boolean dirtyFlag = false; 
		UserEntity existing = 
			this.readById(userEntity.getKey()).orElseThrow(
					()->new RuntimeException("could not find any user with id: " + userEntity.getKey())
			);
			/*
	private long points;
			 */
		if (userEntity.getUsername() != null) {
			existing.setUsername(userEntity.getUsername());
			dirtyFlag = true;
		}
		
		if (userEntity.getAvatar() != null) {
			existing.setAvatar(userEntity.getAvatar());
			dirtyFlag = true;
		}
		
		if (userEntity.getPoints() != existing.getPoints()) {
			existing.setPoints(userEntity.getPoints());
			dirtyFlag = true;
		}
		
		if (dirtyFlag) {
			this.users.put(existing.getKey(), existing);
		}
	}

	@Override
	public void deleteAll() {
		this.users.clear();
	}


}
