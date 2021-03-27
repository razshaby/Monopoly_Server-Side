package smartspace.dao.rdb;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import smartspace.data.ElementEntity;
import smartspace.data.Location;


public interface ElementCrud extends PagingAndSortingRepository<ElementEntity, String>{
//CrudRepository<ElementEntity, String> 
	
	
	public List<ElementEntity> findAllByExpired(@Param("isExpired") boolean isExpired,
			Pageable pageable);

	public List<ElementEntity> findAllByName(@Param("name") String name,
			Pageable pageable);

	public List<ElementEntity> findAllByType(@Param("type") String type,
			Pageable pageable);
	
	
	public List<ElementEntity> findAllByLocation_XBetweenAndLocation_YBetweenAndExpired(double minX , double maxX,
			double minY, double maxY, boolean isExpired, Pageable pageable);
	
	public List<ElementEntity> findAllByLocation_XBetweenAndLocation_YBetween(double minX , double maxX,
			double minY, double maxY, Pageable pageable);

	public List<ElementEntity> findAllByTypeAndExpired(String type, boolean isExpired, PageRequest pageable);

	public List<ElementEntity> findAllByNameAndExpired(String name, boolean isExpired, PageRequest pageable);
	

}
