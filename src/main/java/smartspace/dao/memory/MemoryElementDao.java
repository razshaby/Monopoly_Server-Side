package smartspace.dao.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import smartspace.dao.ElementDao;
import smartspace.data.ElementEntity;

//@Repository
public class MemoryElementDao implements ElementDao<String>{
	private final String SMARTSPACE_PROPERTY = "smartspace.name";
	private Map<String, ElementEntity> elements;
	private AtomicLong id;
	
	@Autowired
	Environment env;
	


	public MemoryElementDao() {
		this.elements = Collections.synchronizedMap(new HashMap<>());
		this.id = new AtomicLong(1L);
	}
	
	@Override
	public ElementEntity create(ElementEntity elementEntity) {
		elementEntity.setElementId(id.getAndIncrement()+"");
		elementEntity.setElementSmartspace(env.getProperty(SMARTSPACE_PROPERTY));
		elementEntity.setKey(elementEntity.getElementSmartspace()+elementEntity.getElementId());
		elements.put(elementEntity.getKey(), elementEntity);
		
		return elementEntity;
		
	}

	@Override
	public Optional<ElementEntity> readById(String elementKey) {
		ElementEntity element = this.elements.get(elementKey);
		if(element != null) 
			return Optional.of(element);
			
		return Optional.empty();
	}

	@Override
	public List<ElementEntity> readAll() {
		return new ArrayList<ElementEntity>(this.elements.values());
	}

	@Override
	public void update(ElementEntity elementEntity) {
		boolean dirtyFlag = false; 
		ElementEntity existing = 
			readById(elementEntity.getKey()).orElseThrow(
					()->new RuntimeException("could not find any element with id: " + elementEntity.getKey())
			);
					
		if (elementEntity.getLocation() != null) {
			existing.setLocation(elementEntity.getLocation());
			dirtyFlag = true;
		}
		
		if (elementEntity.getName() != null) {
			existing.setName(elementEntity.getName());
			dirtyFlag = true;
		}
				
		if (elementEntity.getType() != null) {
			existing.setType(elementEntity.getType());
			dirtyFlag = true;
		}
		
		if (elementEntity.getMoreAttributes() != null) {
			existing.setMoreAttributes(elementEntity.getMoreAttributes());
			dirtyFlag = true;
		}
		
		if (elementEntity.isExpired() != existing.isExpired()) {
			existing.setExpired(elementEntity.isExpired());
			dirtyFlag = true;
		}
		
	
		if (dirtyFlag) {
			this.elements.put(existing.getKey(), existing);
		}
		
	}

	@Override
	public void deleteByKey(String elementKey) {
		ElementEntity removedElement = elements.remove(elementKey);
		
		if (removedElement == null)
			throw new RuntimeException("Remove Failed : no such element with key " + elementKey);
	}

	@Override
	public void delete(ElementEntity elementEntity) {
		deleteByKey(elementEntity.getKey());
		
	}

	@Override
	public void deleteAll() {
		elements.clear();
		
	}

}
