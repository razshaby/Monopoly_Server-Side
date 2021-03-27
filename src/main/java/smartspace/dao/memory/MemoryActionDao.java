package smartspace.dao.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import smartspace.dao.ActionDao;
import smartspace.data.ActionEntity;
import smartspace.data.ElementEntity;


//@Repository
public class MemoryActionDao implements ActionDao  {
	private Map<String, ActionEntity> actions;
	private AtomicLong id;
	
	public MemoryActionDao() {
		this.actions = Collections.synchronizedMap(new HashMap<>());
		this.id = new AtomicLong(1L);
	}
	
	@Override
	public ActionEntity create(ActionEntity actionEntity) {
		actionEntity.setActionId(id.getAndIncrement()+"");
		actionEntity.setKey(actionEntity.getActionId()+actionEntity.getElementSmartspace());
		actions.put(actionEntity.getKey(), actionEntity);
		return actionEntity;
	}

	@Override
	public List<ActionEntity> readAll() {
		return new ArrayList<ActionEntity>(this.actions.values());
	}
	
	@Override
	public void deleteAll() {
		this.actions.clear();		
	}

}
