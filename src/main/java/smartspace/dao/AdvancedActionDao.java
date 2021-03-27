package smartspace.dao;

import java.util.List;

import smartspace.data.ActionEntity;

public interface AdvancedActionDao  extends ActionDao{
	public List<ActionEntity> readAll(
			int size, int page);

	public List<ActionEntity> readAll(
			String sortingAttr,
			int size, int page);
	
	public ActionEntity createFromImport(ActionEntity actionEntity);

}
