package smartspace.plugins;

import smartspace.data.ActionEntity;

public interface Plugin {
		
	public ActionEntity execute(ActionEntity action);
}
