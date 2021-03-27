package smartspace.layout;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smartspace.logic.ActionsService;


@RestController
public class ActionController {
	private ActionsService actions;

	@Autowired
	public ActionController(ActionsService actions) {
		super();
		this.actions=actions;
		}
	
	
	@RequestMapping(
			method=RequestMethod.POST,
			path="/smartspace/admin/actions/{adminSmartspace}/{adminEmail}",
			consumes=MediaType.APPLICATION_JSON_VALUE,
			produces=MediaType.APPLICATION_JSON_VALUE)
		public ActionBoundary[] insert(
				@RequestBody ActionBoundary[] actions,
				@PathVariable("adminSmartspace") String adminSmartspace,
				@PathVariable("adminEmail") String adminEmail)
				{			
			return 
					this.actions
					.insertActions(Arrays.asList(actions)
							.stream()
							.map(ActionBoundary::toEntity)
							.collect(Collectors.toList())
							, adminSmartspace, adminEmail)
					.stream()
					.map(ActionBoundary::new)
					.collect(Collectors.toList())
					.toArray(new ActionBoundary[0]);
				}
	
	
	@RequestMapping(
			path="/smartspace/admin/actions/{adminSmartspace}/{adminEmail}",
			method=RequestMethod.GET,
			produces=MediaType.APPLICATION_JSON_VALUE)
	public ActionBoundary[] 
			getActions (
					@PathVariable("adminSmartspace") String adminSmartspace,
					@PathVariable("adminEmail") String adminEmail,
					@RequestParam(name="size", required=false, defaultValue="10") int size, 
					@RequestParam(name="page", required=false, defaultValue="0") int page) {
		return 
			this.actions
			.getActions(adminSmartspace,adminEmail,size, page)
			.stream()
			.map(ActionBoundary::new)
			.collect(Collectors.toList())
			.toArray(new ActionBoundary[0]);

	}
	
	@RequestMapping(
			method=RequestMethod.POST,
			path="/smartspace/actions",
			consumes=MediaType.APPLICATION_JSON_VALUE,
			produces=MediaType.APPLICATION_JSON_VALUE)
		public ActionBoundary invokeAction(
				@RequestBody ActionBoundary action) {		
			
		return new ActionBoundary(
				this.actions
				.invokeAction(action.toEntity()));
					
	}
	

}

