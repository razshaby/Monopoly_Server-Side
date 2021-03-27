package smartspace.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smartspace.dao.AdvancedActionDao;
import smartspace.dao.AdvancedElementDao;
import smartspace.dao.AdvancedUserDao;
import smartspace.data.ActionEntity;
import smartspace.data.UserEntity;
import smartspace.data.UserRole;
import smartspace.plugins.Plugin;

@Service
public class ActionsServiceImpl implements ActionsService {

	private AdvancedActionDao actions;
	private AdvancedElementDao<String> elements;
	private AdvancedUserDao<String> users;
	private String smartspace;
	private ApplicationContext ctx;

	@Autowired
	public ActionsServiceImpl(AdvancedActionDao actions, AdvancedUserDao<String> users,
			AdvancedElementDao<String> elements, ApplicationContext ctx) {
		super();
		this.actions = actions;
		this.users = users;
		this.elements = elements;
		this.ctx = ctx; 

	}

	@Value("${smartspace.name}")
	public void setSmartspace(String smartspace) {
		this.smartspace = smartspace;
	}

	@Override
	public List<ActionEntity> getActions(String adminSmartspace, String adminEmail, int size, int page) {
		if (validateAdmin(adminSmartspace, adminEmail))
			return this.actions.readAll("creationTimestamp", size, page);
		else
			throw new RuntimeException("User has to be admin");
	}

	// import actions
	@Transactional
	@Override
	public List<ActionEntity> insertActions(List<ActionEntity> actions, String adminSmartspace, String adminEmail) {
		List<ActionEntity> actionsEntities = new ArrayList<>();
		if (!validateAdmin(adminSmartspace, adminEmail))
			throw new RuntimeException("User has to be admin");

		for (ActionEntity action : actions) {
			
			validateAction(action);
			
				if (!validateDifferentSmartspaces(action.getActionSmartspace()))
					throw new RuntimeException("Invalid Action Smartspace");
				else {
					if (!validateElement(action))
						throw new RuntimeException("Invalid Element " + action.getElementId());
				}

			actionsEntities.add(this.actions.createFromImport(action));
		}
		return actionsEntities;
	}

	private void validateAction(ActionEntity action) {

		if(action==null)
		{
			throw new RuntimeException("action is null");
		}
		
		if(action.getActionId() == null || action.getActionId().trim().isEmpty())
		{
			throw new RuntimeException("Invalid ActionId " + action.getActionId());
		}
		if(action.getActionSmartspace() == null || action.getActionSmartspace().trim().isEmpty())
		{
			throw new RuntimeException("Invalid ActionSmartspace " + action.getActionSmartspace());

		}
	
		if(action.getActionType() == null || action.getActionType().trim().isEmpty())
		{
			throw new RuntimeException("Invalid ActionType "+action.getActionType());

		}
		if(action.getPlayerSmartspace() == null ||
				action.getPlayerSmartspace().trim().isEmpty())
		{
			throw new RuntimeException("Invalid PlayerSmartspace "+action.getPlayerSmartspace());

		}
		if(action.getPlayerEmail() == null ||
				action.getPlayerEmail().trim().isEmpty() )
		{
			throw new RuntimeException("Invalid PlayerEmail "+action.getPlayerEmail());

		}
		if(action.getElementId() == null ||
				action.getElementId().trim().isEmpty())
		{
			throw new RuntimeException("Invalid ElementId "+action.getElementId());
		}
		
		if(action.getElementSmartspace() == null ||
				action.getElementSmartspace().trim().isEmpty())
		{
			throw new RuntimeException("Invalid ElementSmartspace "+action.getElementSmartspace());
		}
		
		if(action.getMoreAttributes()==null)
		{
			throw new RuntimeException("MoreAttributes is null");
		}
		
	}

	private boolean validateDifferentSmartspaces(String adminSmartspace) {
		if (this.smartspace.equals(adminSmartspace))
			return false;
		else
			return true;

	}

	private boolean validateElement(ActionEntity action) {
		return (this.elements.readById(action.getElementSmartspace() + "|" + action.getElementId()).isPresent());
	}

	private boolean validateAdmin(String adminSmartspace, String adminEmail) {
		Optional<UserEntity> user = this.users.readById(adminSmartspace + "|" + adminEmail);

		if (user.isPresent()) {
			if (user.get().getRole().equals(UserRole.ADMIN)) {
//				if (this.smartspace.equals(adminSmartspace))
					return true;
//				else
//					throw new RuntimeException("this.smartspace.equals(adminSmartspace)");

			} else {
				throw new RuntimeException("user.get().getRole().equals(UserRole.ADMIN)");
			}
		} else {
			throw new RuntimeException("user not found");

		}

	}
	
	private boolean validatePlayer(String playerSmartspace, String playerEmail) {
		Optional<UserEntity> user = this.users.readById(playerSmartspace + "|" + playerEmail);

		if (user.isPresent()) {
			if (user.get().getRole().equals(UserRole.PLAYER)) {
				if (this.smartspace.equals(playerSmartspace))
					return true;
				else
					throw new RuntimeException("this.smartspace.equals(playerSmartspace)");

			} else {
				throw new RuntimeException("user.get().getRole().equals(UserRole.PLAYER)");
			}
		} else {
			throw new RuntimeException("user not found");

		}

	}
	
	@Override
	public ActionEntity invokeAction(ActionEntity action) {
		if (!validatePlayer(action.getPlayerSmartspace(), action.getPlayerEmail()))
			throw new RuntimeException("User has to be palyer");
		if (!validateElement(action))
			throw new RuntimeException("Invalid Element " + action.getElementId());
		action.setCreationTimestamp(new Date());
		return invokeActionByType(action);
	}
	
	private ActionEntity invokeActionByType(ActionEntity action) {
		try {
			String operation = action.getActionType();
			if (!operation.trim().isEmpty()) {
				String className = "smartspace.plugins." + operation.toUpperCase().charAt(0) + operation.substring(1, operation.length());
				Class<?> pluginClass = Class.forName(className);
				Plugin plugin = (Plugin) this.ctx.getBean(pluginClass);
				action = plugin.execute(action);				
			}
			return action;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
