package smartspace.logic;

import java.util.List;


import smartspace.data.ActionEntity;


public interface ActionsService {
	public List<ActionEntity> insertActions (List<ActionEntity> actions, String adminSmartspace, String adminEmail);
	public List<ActionEntity> getActions (String adminSmartspace, String adminEmail, int size, int page);
	public ActionEntity invokeAction (ActionEntity action);
	
	
}


