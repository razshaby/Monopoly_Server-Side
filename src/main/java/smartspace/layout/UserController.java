package smartspace.layout;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import smartspace.data.UserEntity;
import smartspace.logic.EntityNotFoundException;
import smartspace.logic.UsersService;

@RestController
public class UserController {
	private UsersService users;
	
	@Autowired
	public UserController(UsersService users) {
		super();
		this.users = users;
	}
	
	@RequestMapping(
			method = RequestMethod.GET,
			path="/smartspace/admin/users/{adminSmartspace}/{adminEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] getUsers(
		@PathVariable("adminSmartspace") String adminSmartspace,
		@PathVariable("adminEmail") String adminEmail,
		@RequestParam(name="size", required=false, defaultValue="10") int size, 
		@RequestParam(name="page", required=false, defaultValue="0") int page) {
	return
			this.users
			.getUsers(adminSmartspace, adminEmail,size, page)
			.stream()
			.map(UserBoundary::new)
			.collect(Collectors.toList())
			.toArray(new UserBoundary[0]);
	}
	
	
	@RequestMapping(
			method = RequestMethod.POST,
			path="/smartspace/admin/users/{adminSmartspace}/{adminEmail}",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] insert(
			@RequestBody UserBoundary[] users,
			@PathVariable("adminSmartspace") String adminSmartspace,
			@PathVariable("adminEmail") String adminEmail) {
	
		
		return

				this.users
				.insertUsers(Arrays.asList(users)
						.stream()
						.map(UserBoundary::toEntity)
						.collect(Collectors.toList())
						, adminSmartspace,adminEmail)
				.stream()
				.map(UserBoundary::new)
				.collect(Collectors.toList())
				.toArray(new UserBoundary[0]);
				
	}
	
	
	@RequestMapping(
			method = RequestMethod.POST,
			path="/smartspace/users",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary createNewUser(
			@RequestBody NewUserFormBoundary userForm
			) {
	 UserEntity userEntity=this.users.insertUser(userForm.toEntity());
		return new UserBoundary(userEntity);
	}
	
	
	@RequestMapping(
			method = RequestMethod.GET,
			path="/smartspace/users/login/{userSmartspace}/{userEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary getUser(
		@PathVariable("userSmartspace") String userSmartspace,
		@PathVariable("userEmail") String userEmail
		      ) {
	return
			new UserBoundary(this.users
			.getUser(userSmartspace, userEmail));

	}
	
	
	@RequestMapping(
			method=RequestMethod.PUT,
			path="/smartspace/users/login/{userSmartspace}/{userEmail}",
			consumes=MediaType.APPLICATION_JSON_VALUE)
	public void updateUser(
			@PathVariable("userSmartspace") String userSmartspace,
			@PathVariable("userEmail") String userEmail,
			@RequestBody UserBoundary user
			) {
		this.users.update(userSmartspace, userEmail, user.toEntity());
	}
	
	
	
	@RequestMapping(
			method = RequestMethod.GET,
			path="/smartspace/users/getOnlinePlayers/{playerSmartspace}/{playerEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary[] getOnlinePlayers(
		@PathVariable("playerSmartspace") String playerSmartspace,
		@PathVariable("playerEmail") String playerEmail
		      ) {
		return
				this.users
				.getOnlinePlayers(playerSmartspace, playerEmail)
				.stream()
				.map(UserBoundary::new)
				.collect(Collectors.toList())
				.toArray(new UserBoundary[0]);
		
		
	}
	
	@RequestMapping(
			method = RequestMethod.GET,
			path="/smartspace/users/getTurn/{playerSmartspace}/{playerEmail}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundary getNextUserTurn(
		@PathVariable("playerSmartspace") String playerSmartspace,
		@PathVariable("playerEmail") String playerEmail
		      ) {
		return
				new UserBoundary(this.users
						.getNextUserTurn(playerSmartspace, playerEmail));
		
		
	}
	
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorMessage handleException (EntityNotFoundException e) {
		String message = e.getMessage();
		
		if (message == null || message.trim().isEmpty()) {
			message = "Could not find user";
		}
		
		return new ErrorMessage(message);
	}
	
	
	
}
