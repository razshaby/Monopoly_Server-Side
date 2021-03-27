package smartspace.logic;

import java.util.List;

import smartspace.data.ElementEntity;

public interface ElementsService {
	public List<ElementEntity> insertElements (List<ElementEntity> elements, String adminSmartspace, String adminEmail);
	public List<ElementEntity> getElements (String adminSmartspace, String adminEmail, int size, int page);
	public List<ElementEntity> getAllElementsUsingPagination(String userSmartspace, String userEmail,
			int size, int page);
//	public List<ElementEntity> getAllElementsWithSpecificName(String userSmartspace, String userEmail,
//			String name, int size, int page);
//	public List<ElementEntity> getAllElementsNearALocation(String userSmartspace, String userEmail,
//			double x, double y, double distance, int size, int page);
//	public List<ElementEntity> getAllElementsOfSpecificType(String userSmartspace, String userEmail,
//			String type, int size, int page);
	public ElementEntity insertOneElement(ElementEntity element, String managerSmartspace, String managerEmail);
	public ElementEntity getElement(String elementId, String elementSmartspace, String userSmartspace, String userEmail);
	public List<ElementEntity> getAllElementsByAttr(String userSmartspace, String userEmail, double x, double y,
			double distance, int size, int page, String search, String value);
	public void updateElement(ElementEntity element, String managerSmartspace, String managerEmail, String elementSmartspace, String elementId);

}
