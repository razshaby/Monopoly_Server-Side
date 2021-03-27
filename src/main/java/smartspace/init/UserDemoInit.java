package smartspace.init;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import smartspace.dao.UserDao;
import smartspace.data.UserRole;
import smartspace.data.util.EntityFactory;

@Component
@Profile("production")
public class UserDemoInit implements CommandLineRunner{
	private UserDao<String> userDao;
	private EntityFactory factory;
	private ArrayList<String> avatars;
	private ArrayList<String> names;

	
	@Autowired
	public void setFactory(EntityFactory factory) {
		this.factory = factory;
		avatars = new ArrayList<String>(Arrays.asList("Car","Dog","Ship","Hat","Iron"));
		names = new ArrayList<String>(Arrays.asList("Raz","Tal","Dor","Meytal","player"));
	}
	
	
	@Autowired
	public UserDemoInit(UserDao<String> userDao) {
		this.userDao = userDao;
	}


	@Override
	public void run(String... args) throws Exception {

//		createAdmin();
//		createManager();
//		createPlayer();
//		createPlayers();
	}

	private void createPlayers() {

		for(int i=0 ; i<names.size(); i++) {
			this.userDao
			.create(this.factory.createNewUser(names.get(i).toLowerCase()  + "@test.com", "2019b.meytal",
					names.get(i) , avatars.get(i), UserRole.PLAYER, -1));
		}
		
	}


	private void createAdmin() {
		this.userDao
		.create(this.factory.createNewUser("admin@test.com", 
				"not matter", "admin_userName", "admin_avatar :-)", UserRole.ADMIN, 0));
		
	}
	
	private void createManager() {
		this.userDao
		.create(this.factory.createNewUser("manager@test.com", 
				"not matter", "manager_userName", "manager_avatar :-)", UserRole.MANAGER, 0));
		
	}
	
	private void createPlayer() {
				this.userDao
		.create(this.factory.createNewUser("player@test.com", 
				"not matter", "player_userName", "player_avatar :-)", UserRole.PLAYER, 1000));
	}
}
