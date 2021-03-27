package smartspace.layout;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import smartspace.logic.ElementsService;
import smartspace.logic.EntityNotFoundException;

@RestController
public class ElementController {
	
	private ElementsService elements;
	
	@Autowired
	public ElementController(ElementsService elementsService) {
		super();
		this.elements = elementsService;
	}
	
	@RequestMapping(
			method=RequestMethod.POST,
			path="/smartspace/admin/elements/{adminSmartspace}/{adminEmail}",
			consumes=MediaType.APPLICATION_JSON_VALUE,
			produces=MediaType.APPLICATION_JSON_VALUE)
		public ElementBoundary[] insert(
				@RequestBody ElementBoundary[] elements,
				@PathVariable("adminSmartspace") String adminSmartspace,
				@PathVariable("adminEmail") String adminEmail) {
		
			return
					this.elements
					.insertElements(Arrays.asList(elements)
							.stream()
							.map(ElementBoundary::toEntity)
							.collect(Collectors.toList()) 
							,adminSmartspace ,adminEmail)
					.stream()
					.map(ElementBoundary::new)
					.collect(Collectors.toList())
					.toArray(new ElementBoundary[0]);
	}
	
	
	@RequestMapping(
			path="/smartspace/admin/elements/{adminSmartspace}/{adminEmail}",
			method=RequestMethod.GET,
			produces=MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[]
			getElements (
					@PathVariable("adminSmartspace") String adminSmartspace,
					@PathVariable("adminEmail") String adminEmail,
					@RequestParam(name="size", required=false, defaultValue="10") int size,
					@RequestParam(name="page", required=false, defaultValue="0") int page) {
		
		return
			this.elements
				.getElements(adminSmartspace, adminEmail, size, page)
				.stream()
				.map(ElementBoundary::new)
				.collect(Collectors.toList())
				.toArray(new ElementBoundary[0]);
	}
	
	@RequestMapping(
			method=RequestMethod.POST,
			path="/smartspace/elements/{managerSmartspace}/{managerEmail}",
			consumes=MediaType.APPLICATION_JSON_VALUE,
			produces=MediaType.APPLICATION_JSON_VALUE)
		public ElementBoundary insert(
				@RequestBody ElementBoundary element,
				@PathVariable("managerSmartspace") String managerSmartspace,
				@PathVariable("managerEmail") String managerEmail) {
		
			return new ElementBoundary(
					this.elements
					.insertOneElement(element.toEntity(), managerSmartspace, managerEmail));
	}
	
	@RequestMapping(
			method=RequestMethod.PUT,
			path="/smartspace/elements/{managerSmartspace}/{managerEmail}/{elementSmartspace}/{elementId}",
			consumes=MediaType.APPLICATION_JSON_VALUE)
		public void update(
			@RequestBody ElementBoundary element,
			@PathVariable("managerSmartspace") String managerSmartspace,
			@PathVariable("managerEmail") String managerEmail,
			@PathVariable("elementSmartspace") String elementSmartspace,
			@PathVariable("elementId") String elementId) {
		
		this.elements
			.updateElement(element.toEntity(), managerSmartspace, managerEmail,elementSmartspace, elementId);
	}
	
	@RequestMapping(
			path="/smartspace/elements/{userSmartspace}/{userEmail}/{elementSmartspace}/{elementId}",
			method=RequestMethod.GET,
			produces=MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary
			getElement (
					@PathVariable("userSmartspace") String userSmartspace,
					@PathVariable("userEmail") String userEmail,
					@PathVariable("elementSmartspace") String elementSmartspace,
					@PathVariable("elementId") String elementId)		 {
	
		return new ElementBoundary(
				this.elements
				.getElement(elementId, elementSmartspace, userSmartspace, userEmail));
	}
	
	@RequestMapping(
			path="/smartspace/elements/{userSmartspace}/{userEmail}",
			method=RequestMethod.GET,
			produces=MediaType.APPLICATION_JSON_VALUE)
	public ElementBoundary[]
			getAllElementsBySearch (
					@PathVariable("userSmartspace") String userSmartspace,
					@PathVariable("userEmail") String userEmail,
					@RequestParam(name="search", required=false) String search,
					@RequestParam(name="x", required=false, defaultValue = "0") double x,
					@RequestParam(name="y", required=false, defaultValue = "0") double y,
					@RequestParam(name="distance", required=false, defaultValue="1.0") double distance,
					@RequestParam(name="value", required=false, defaultValue= "") String value ,
					@RequestParam(name="size", required=false, defaultValue="100") int size,
					@RequestParam(name="page", required=false, defaultValue="0") int page) {
		
		return
			this.elements
				.getAllElementsByAttr(userSmartspace, userEmail, x, y, distance, size, page, search, value)
				.stream()
				.map(ElementBoundary::new)
				.collect(Collectors.toList())
				.toArray(new ElementBoundary[0]);
	}
			
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorMessage handleException (EntityNotFoundException e) {
		String message = e.getMessage();
		
		if (message == null || message.trim().isEmpty()) {
			message = "Could not find element";
		}
		
		return new ErrorMessage(message);
	}

}
