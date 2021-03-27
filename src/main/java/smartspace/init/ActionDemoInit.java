package smartspace.init;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import smartspace.dao.AdvancedActionDao;
import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.data.ActionEntity;

@Component
@Profile("production")
public class ActionDemoInit implements CommandLineRunner {


	private AdvancedActionDao actionDao;
	private ActionEntity actionEntity;

	
	
	@Autowired
	public ActionDemoInit(AdvancedElementDao<String> elementDao, AdvancedUserDao<String> userDao,
			AdvancedActionDao actionDao) {

		this.actionDao=actionDao;
	}

	@Override
	public void run(String... args) throws Exception {

//		createActions("action1");
//		createActions("action2");
//		createActions("action3");

	}


	private void createActions(String name) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("key1", "hello");
		properties.put("key2", 2);
		properties.put("key3", "[]");
		properties.put("lastKey","Bye");
		
		this.actionEntity=new ActionEntity();
		this.actionEntity.setPlayerEmail("player@test.com");
		this.actionEntity.setPlayerSmartspace("2019b.meytal");		
		
		this.actionEntity.setActionType(name);
		this.actionEntity.setMoreAttributes(properties);
		this.actionEntity=  this.actionDao.create(actionEntity);
		
		
	}
}