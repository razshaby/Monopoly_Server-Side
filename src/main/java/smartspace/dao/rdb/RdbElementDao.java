package smartspace.dao.rdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import smartspace.dao.AdvancedElementDao;
import smartspace.data.ElementEntity;

@Repository
public class RdbElementDao implements AdvancedElementDao<String>{
	private ElementCrud elementCrud;
	
	 private String elementSmartspace;

	 @Value("${smartspace.name}")
	 public void setElementSmartspace(String elementSmartspace) {
		this.elementSmartspace = elementSmartspace;
	}
	 

	//private AtomicLong nextId;
	private IdentitySeedCrud identitySeedCrud;  

	
	@Autowired
	public RdbElementDao(ElementCrud elementCrud,IdentitySeedCrud identitySeedCrud) {
		super();
		this.elementCrud = elementCrud;
		
		//this.nextId = new AtomicLong(1L);
		this.identitySeedCrud = identitySeedCrud;

	}

	@Override
	@Transactional
	public ElementEntity create(ElementEntity elementEntity) {
		//elementEntity.setElementId(nextId.getAndIncrement()+"");
		IdentitySeed seed = this.identitySeedCrud.save(new IdentitySeed());

		//elementEntity.setElementSmartspace(elementSmartspace);
		elementEntity.setKey(elementSmartspace+"|"+seed.getId());
		this.identitySeedCrud.delete(seed);

				
		// SQL INSERT
		if (!this.elementCrud.existsById(elementEntity.getKey())) {
			ElementEntity rv = this.elementCrud.save(elementEntity);
			return rv;
		}else {
			throw new RuntimeException("Element entity already exists with id: " + elementEntity.getKey());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public Optional<ElementEntity> readById(String elementKey) {
		return this.elementCrud.findById(elementKey);
	}





	@Override
	@Transactional
	public void deleteByKey(String elementKey) {
		
		this.elementCrud.deleteById(elementKey);
		
	}

	@Override
	@Transactional
	public void delete(ElementEntity elementEntity) {
		
		this.elementCrud.delete(elementEntity);
		
	}

	
	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAll() {
		List<ElementEntity> list = new ArrayList<>();
		// SQL SELECT
		this.elementCrud.findAll()
			.forEach(list::add);
		return list;
	}
	
	
	@Override
	@Transactional
	public void update(ElementEntity elementEntity) {
		 
		ElementEntity existing = 
			readById(elementEntity.getKey()).orElseThrow(
					()->new RuntimeException("could not find any element with id: " + elementEntity.getKey())
			);
					
		if (elementEntity.getLocation() != null) {
			existing.setLocation(elementEntity.getLocation());
		}
		
		if (elementEntity.getName() != null) {
			existing.setName(elementEntity.getName());
		}
				
		if (elementEntity.getType() != null) {
			existing.setType(elementEntity.getType());
		}
		
		if (elementEntity.getMoreAttributes() != null) {
			existing.setMoreAttributes(elementEntity.getMoreAttributes());
		}
		
		
		existing.setExpired(elementEntity.isExpired());
		// SQL UPDATE
		this.elementCrud.save(existing);
		
	}
	
	@Override
	@Transactional
	public void deleteAll() {
		// SQL DELETE 
		this.elementCrud.deleteAll();
		
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAll(int size, int page) {
		return
				this.elementCrud
				.findAll(PageRequest.of(page, size))
				.getContent();
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAll(String sortingAttr, int size, int page) {
		return
				this.elementCrud
				.findAll(
					PageRequest.of(
							page, size, 
							Direction.ASC, sortingAttr))
				.getContent();
	}

	@Override
	@Transactional
	public ElementEntity createFromImport(ElementEntity elementEntity) {
		
		return this.elementCrud.save(elementEntity);
		
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAllByExpired(String sortingAttr, int size, int page, boolean isExpired) {
		return
				this.elementCrud.findAllByExpired(isExpired,PageRequest.of(page, size, 
						Direction.ASC, sortingAttr));
				
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAllBySpecificName(String sortingAttr, int size, int page, String name) {
		return
				this.elementCrud.findAllByName(name,PageRequest.of(page, size, 
						Direction.ASC, sortingAttr));
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAllOfSpecificType(String sortingAttr, int size, int page, String type) {
		return
				this.elementCrud.findAllByType(type ,PageRequest.of(page, size, 
						Direction.ASC, sortingAttr));
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAllByLocationAndByExpired(String sortingAttr, int size, int page, double x, double y,
			double distance, boolean isExpired) {
		return
		this.elementCrud.findAllByLocation_XBetweenAndLocation_YBetweenAndExpired(x - distance, x + distance, y - distance, y + distance
				,isExpired ,PageRequest.of(page, size, Direction.ASC, sortingAttr));
		
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAllByLocation(String sortingAttr, int size, int page, double x, double y,
			double distance) {
		return
				this.elementCrud.findAllByLocation_XBetweenAndLocation_YBetween(x - distance, x + distance, y - distance, y + distance
						 ,PageRequest.of(page, size, Direction.ASC, sortingAttr));
		
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAllBySpecificNameAndByExpired(String sortingAttr, int size, int page, String name,
			boolean isExpired) {
		return
				this.elementCrud.findAllByNameAndExpired(name,isExpired,PageRequest.of(page, size, 
						Direction.ASC, sortingAttr));
	}

	@Override
	@Transactional(readOnly=true)
	public List<ElementEntity> readAllOfSpecificTypeAndByExpired(String sortingAttr, int size, int page, String type,
			boolean isExpired) {
		return
				this.elementCrud.findAllByTypeAndExpired(type, isExpired ,PageRequest.of(page, size, 
						Direction.ASC, sortingAttr));
	}



	
	

}
