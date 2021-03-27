package smartspace.dao;

import java.util.List;

import smartspace.data.ElementEntity;

public interface AdvancedElementDao<ElementKey> extends ElementDao<ElementKey> {
	
	public List<ElementEntity> readAll(
			int size, int page);

	public List<ElementEntity> readAll(
			String sortingAttr,
			int size, int page);
	
	public ElementEntity createFromImport(ElementEntity elementEntity);
	
	public List<ElementEntity> readAllByExpired(
			String sortingAttr,
			int size, int page,
			boolean isExpired);

	public List<ElementEntity> readAllBySpecificName(
			String sortingAttr,
			int size, int page,
			String name);
	
	public List<ElementEntity> readAllBySpecificNameAndByExpired(
			String sortingAttr,
			int size, int page,
			String name,
			boolean isExpired);

	public List<ElementEntity> readAllOfSpecificType(
			String sortingAttr,
			int size, int page,
			String type);
	
	public List<ElementEntity> readAllOfSpecificTypeAndByExpired(
			String sortingAttr,
			int size, int page,
			String type,
			boolean isExpired);
	
	public List<ElementEntity> readAllByLocationAndByExpired(
			String sortingAttr,
			int size, int page,
			double x, double y, double distance,
			boolean isExpired);

	public List<ElementEntity> readAllByLocation(
			String sortingAttr,
			int size, int page,
			double x, double y,	double distance);
	
	

}
