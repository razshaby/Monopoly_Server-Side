package smartspace.plugins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import smartspace.dao.AdvancedActionDao;
import smartspace.data.ActionEntity;

@Component
public class Echo implements Plugin{
	private AdvancedActionDao actions;
	
	
	@Autowired
	public Echo(AdvancedActionDao actions) {
		this.actions = actions;
	}
	
	@Override
	public ActionEntity execute(ActionEntity action) {		
		return actions.create(action);
	}

}
