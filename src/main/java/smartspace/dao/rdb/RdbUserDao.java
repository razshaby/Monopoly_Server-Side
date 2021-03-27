package smartspace.dao.rdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import smartspace.dao.ExtendedUserDao;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;

@Repository
public class RdbUserDao implements ExtendedUserDao<String>{
	private UserCrud userCrud;
	private String userSmartspace;


	
	 @Value("${smartspace.name}")
	 public void setUserSmartspace(String userSmartSpace) {
		this.userSmartspace = userSmartSpace;
	}
	
	@Autowired
	public RdbUserDao(UserCrud userCrud) {
		super();
		this.userCrud = userCrud;
		

	}

	@Override
	@Transactional
	public UserEntity create(UserEntity user) {
		user.setKey(userSmartspace+"|"+user.getUserEmail());
		user.setPoints(-1);
		
		// SQL INSERT
		if (!this.userCrud.existsById(user.getKey())) {
			UserEntity rv = this.userCrud.save(user);
			return rv;
		}else {
			throw new RuntimeException("user already exists with id: " + user.getKey());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public Optional<UserEntity> readById(String userKey) {
		// SQL SELECT 
		return this.userCrud.findById(userKey);
	}

	
	@Override
	@Transactional(readOnly=true)
	public List<UserEntity> readAll() {
		List<UserEntity> list = new ArrayList<>();
		// SQL SELECT
		this.userCrud.findAll()
			.forEach(list::add);
		return list;
	}

	@Override
	@Transactional
	public void update(UserEntity userEntity) {
		UserEntity existing = 
			this.readById(userEntity.getKey()).orElseThrow(
					()->new RuntimeException("could not find any user with id: " + userEntity.getKey())
			);
		
		if (userEntity.getUsername() != null) {
			existing.setUsername(userEntity.getUsername());
		}
		
		if (userEntity.getAvatar() != null) {
			existing.setAvatar(userEntity.getAvatar());
		}
		
		if(userEntity.getRole() !=null)
		{
			existing.setRole(userEntity.getRole());
		}
		

		// SQL UPDATE
		this.userCrud.save(existing);
	}



	@Override
	@Transactional
	public void deleteAll() {
		// SQL DELETE 
		this.userCrud.deleteAll();		
	}

	@Override
	@Transactional(readOnly=true)
	public List<UserEntity> readAll(int size, int page) {
		return 
				this.userCrud
				.findAll(PageRequest.of(page, size))
				.getContent();
	}

	@Override
	@Transactional(readOnly=true)
	public List<UserEntity> readAll(String sortingAttr, int size, int page) {
	return
			this.userCrud
			.findAll(
				PageRequest.of(
						page, size, 
						Direction.ASC, sortingAttr))
			.getContent();
	}
	
	@Override
	@Transactional
	public UserEntity createFromImport(UserEntity userEntity) {
	
		UserEntity rv = this.userCrud.save(userEntity);
		return rv;
		}

	@Override
	@Transactional
	public void updateUserMoney(UserEntity userEntity) {
		UserEntity existing = 
				this.readById(userEntity.getKey()).orElseThrow(
						()->new RuntimeException("could not find any user with id: " + userEntity.getKey())
				);
		
		existing.setPoints(userEntity.getPoints());
		this.userCrud.save(existing);
	}

	@Override
	@Transactional(readOnly=true)
	public List<UserEntity> readAllByPoints(UserRole role) {
	
		return this.userCrud.findAllByPointsGreaterThanEqualAndRoleOrderByUsername(0,role);
	}


}
