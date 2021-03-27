package smartspace.dao.rdb;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import smartspace.data.UserEntity;
import smartspace.data.UserRole;

public interface UserCrud extends PagingAndSortingRepository<UserEntity, String>
//CrudRepository<UserEntity, String>
{

	List<UserEntity> findAllByPointsGreaterThanEqualAndRoleOrderByUsername(long point, UserRole role);
	
}
