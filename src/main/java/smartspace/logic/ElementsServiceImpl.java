package smartspace.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.data.ElementEntity;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;

@Service
public class ElementsServiceImpl implements ElementsService {

	private AdvancedElementDao<String> elements;
	private AdvancedUserDao<String> users;
	private String smartspace;

	@Value("${smartspace.name}")
	public void setSmartspace(String smartspace) {
		this.smartspace = smartspace;
	}

	@Autowired
	public ElementsServiceImpl(AdvancedElementDao<String> elements, AdvancedUserDao<String> users) {
		super();
		this.elements = elements;
		this.users = users;
	}

	@Override
	@Transactional
	public List<ElementEntity> insertElements(List<ElementEntity> elements, String adminSmartspace, String adminEmail) {
		List<ElementEntity> elementEntities = new ArrayList<>();

		if (!isEqualToCurrentSmartspace(adminSmartspace) || !validateAdmin(adminEmail, adminSmartspace))
			throw new RuntimeException("User has to be admin in " + this.smartspace);

		for (ElementEntity element : elements) {
			if (!validateElementFromImport(element) || isEqualToCurrentSmartspace(element.getElementSmartspace()))
				throw new RuntimeException("Invalid element");
			elementEntities.add(this.elements.createFromImport(element));

		}

		return elementEntities;

	}

	private boolean isEqualToCurrentSmartspace(String smartspace) {
		if (this.smartspace.equals(smartspace))
			return true;
		else
			return false;

	}

	private boolean validateAdmin(String adminEmail, String adminSmartspace) {
		Optional<UserEntity> user = this.users.readById(adminSmartspace + "|" + adminEmail);
		if (user.isPresent())
			if (user.get().getRole().equals(UserRole.ADMIN))
				return true;

		return false;

	}

	private boolean validateManager(String managerEmail, String managerSmartspace) {
		Optional<UserEntity> user = this.users.readById(managerSmartspace + "|" + managerEmail);
		if (user.isPresent())
			if (user.get().getRole().equals(UserRole.MANAGER))
				return true;

		return false;

	}

	private boolean validatePlayer(String playerEmail, String playerSmartspace) {
		Optional<UserEntity> user = this.users.readById(playerSmartspace + "|" + playerEmail);
		if (user.isPresent())
			if (user.get().getRole().equals(UserRole.PLAYER))
				return true;

		return false;

	}

	private boolean validateElementFromImport(ElementEntity element) {
		return element != null && element.getLocation() != null && element.getName() != null
				&& !element.getName().trim().isEmpty() && element.getType() != null
				&& !element.getType().trim().isEmpty() && element.getMoreAttributes() != null
				&& element.getCreatorEmail() != null && !element.getCreatorEmail().isEmpty()
				&& element.getCreatorSmartspace() != null && !element.getCreatorSmartspace().isEmpty()
				&& element.getElementSmartspace() != null && !element.getElementSmartspace().isEmpty()
				&& element.getElementId() != null && !element.getElementId().trim().isEmpty();
	}

	private boolean validateElementLocal(ElementEntity element) {
		return element != null && element.getLocation() != null && element.getName() != null
				&& !element.getName().trim().isEmpty() && element.getType() != null
				&& !element.getType().trim().isEmpty() && element.getMoreAttributes() != null
				&& element.getCreatorEmail() != null && !element.getCreatorEmail().isEmpty()
				&& element.getCreatorSmartspace() != null && !element.getCreatorSmartspace().isEmpty();
	}

	@Override
	public List<ElementEntity> getElements(String adminSmartspace, String adminEmail, int size, int page) {
		if (isEqualToCurrentSmartspace(adminSmartspace) && validateAdmin(adminEmail, adminSmartspace))
			return this.elements.readAll("creationTimestamp", size, page);
		else
			throw new RuntimeException("User has to be admin");
	}

	@Override
	public List<ElementEntity> getAllElementsUsingPagination(String userSmartspace, String userEmail, int size,
			int page) {
		if (validatePlayer(userEmail, userSmartspace)) {
			return this.elements.readAllByExpired("creationTimestamp", size, page, false);

		} else if (validateManager(userEmail, userSmartspace)) {
			return this.elements.readAll("creationTimestamp", size, page);
		} else {

			throw new RuntimeException("User has to be manager or player");
		}
	}

	
	private List<ElementEntity> getAllElementsNearALocation(String userSmartspace, String userEmail, double x, double y,
			double distance, int size, int page) {
		
		if(validatePlayer(userEmail, userSmartspace))
			return this.elements.readAllByLocationAndByExpired("creationTimestamp", size, page, x, y, distance, false);
		else if (validateManager(userEmail, userSmartspace))
			return this.elements.readAllByLocation("creationTimestamp", size, page, x, y, distance);
		else
			throw new RuntimeException("User has to be manager or player");
	}

	
	private List<ElementEntity> getAllElementsWithSpecificName(String userSmartspace, String userEmail, String name,
			int size, int page) {
		if(validatePlayer(userEmail,userSmartspace))
			return this.elements.readAllBySpecificNameAndByExpired("creationTimestamp", size, page, name, false);
		else if (validateManager(userEmail, userSmartspace))
			return this.elements.readAllBySpecificName("creationTimestamp", size, page, name);
		else
			throw new RuntimeException("User has to be manager or player");
	}

	
	private List<ElementEntity> getAllElementsOfSpecificType(String userSmartspace, String userEmail, String type,
			int size, int page) {
		if(validatePlayer(userEmail, userSmartspace))
		{
			if(type.contentEquals("city"))
			return this.elements.readAllOfSpecificTypeAndByExpired("location.x", size, page, type, false);	
			else
			return this.elements.readAllOfSpecificTypeAndByExpired("creationTimestamp", size, page, type, false);
		}
		else if(validateManager(userEmail, userSmartspace))
			return this.elements.readAllOfSpecificType("creationTimestamp", size, page, type);
		else
			throw new RuntimeException("User has to be manager or player");
	}

	@Override
	public ElementEntity insertOneElement(ElementEntity element, String managerSmartspace, String managerEmail) {
		if (validateManager(managerEmail, managerSmartspace)) {
			setElementCreator(element, managerSmartspace, managerEmail);
			if (validateElementLocal(element)) 
				{
				element.setCreationTimestamp(new Date());
				return this.elements.create(element);
				}
			else
				throw new RuntimeException("invalid element");
		} else {
			throw new RuntimeException("User has to be manager");
		}
	}

	private void setElementCreator(ElementEntity element, String managerSmartspace, String managerEmail) {
		element.setCreatorEmail(managerEmail);
		element.setCreatorSmartspace(managerSmartspace);
	}

	@Override
	public ElementEntity getElement(String elementId, String elementSmartspace, String userSmartspace, String userEmail) {
		if(validateManager(userEmail, userSmartspace) || validatePlayer(userEmail, userSmartspace)) {
			Optional<ElementEntity> rv = this.elements.readById(elementSmartspace+"|"+elementId);
			if (rv.isPresent())
				return rv.get();
			else
				throw new EntityNotFoundException("Element with id: " + elementId + " does not exist");
		}else{
			throw new RuntimeException("User has to be manager or player");
		}
	}

	@Override
	public List<ElementEntity> getAllElementsByAttr(String userSmartspace, String userEmail, double x, double y,
			double distance, int size, int page, String search, String value) {
		if (search == null)
			return getAllElementsUsingPagination(userSmartspace, userEmail, size, page);
		
		switch(search) {
		
			case "location":
				return getAllElementsNearALocation(userSmartspace, userEmail, x, y, distance, size, page);
			
			case "name":
				return getAllElementsWithSpecificName(userSmartspace, userEmail, value, size, page);
				
			case "type":
				return getAllElementsOfSpecificType(userSmartspace, userEmail, value, size, page);
				
			default:
				return getAllElementsUsingPagination(userSmartspace, userEmail, size, page);
		}
	}

	@Override
	public void updateElement(ElementEntity element, String managerSmartspace, String managerEmail, String elementSmartspace, String elementId){
			if (!validateElementExsist(elementSmartspace+"|"+elementId)) {
				throw new EntityNotFoundException("Element with id: " + elementId + " does not exist");
			}	
			if (!validateManager(managerEmail, managerSmartspace)) {		
				throw new RuntimeException("User has to be manager");	
			}
			element.setKey(elementSmartspace+"|"+elementId);
			this.elements.update(element);						
	}

	private boolean validateElementExsist(String elementKey) {
		Optional<ElementEntity> rv = this.elements.readById(elementKey);
		if (rv.isPresent())
			return true;
		else
			return false;
	}

}
