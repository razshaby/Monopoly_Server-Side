package smartspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import smartspace.dao.ActionDao;
import smartspace.data.ActionEntity;
import smartspace.data.Location;


@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "spring.profiles.active=default" })
public class ActionDaoRdbIntegrationTests {

	private ActionDao actionDao;
	private String actionSmartspace;

	@Value("${smartspace.name}")
	public void setActionSmartspace(String actionSmartspace) {
		this.actionSmartspace = actionSmartspace;
	}
	
	@Autowired
	public void setActionDao(ActionDao actionDao) {
		this.actionDao = actionDao;
	}

//	@Before
//	public void setup() {
//		this.actionDao.deleteAll();
//	}

	@After
	public void teardown() {
		this.actionDao.deleteAll();
	}

	@Test
	public void testCreate5actions() throws Exception {
		// GIVEN we have a dao

		// WHEN we create 5 new actions and insert them
		List<ActionEntity> list = IntStream.range(1, 6) // int Stream
				.mapToObj(num -> "action type #" + num)// String Stream
				.map(ActionEntity::new) // ActionEntity Stream
				.map(this.actionDao::create) // ActionEntity Stream
				.collect(Collectors.toList()); // List<ActionEntity>

		// THEN the dao contains 5 actions
		// AND the actions created available through the dao
		assertThat(this.actionDao.readAll()).usingElementComparatorOnFields("actionType")
				.containsExactlyInAnyOrderElementsOf(list);
		
		this.actionDao.deleteAll();
		
		assertThat(this.actionDao.readAll())
		.isEmpty();
		
	}
	

	@Test
	public void testCreateActionWithValidKey() throws Exception{
		//GIVEN nothing
		
		//WHEN create an action 
		ActionEntity element = new ActionEntity();
		ActionEntity insertedElement = actionDao.create(element);
		
		//THEN a valid action key is created
		assertThat(insertedElement.getKey()).contains(actionSmartspace);
	}
	
//	@Test
//	public void testCreateActionAndGetSmartspaceAndGetId() throws Exception{
//		//GIVEN nothing
//		
//		//WHEN create an action 
//		ActionEntity element = new ActionEntity();
//		ActionEntity insertedElement = actionDao.create(element);
//		
//		//THEN a valid action key is created
//		assertThat(insertedElement.getActionSmartspace()).isEqualTo(actionSmartspace);
//		assertThat(insertedElement.getActionId()).containsOnlyDigits();
//	}
	
	@Test
	public void testCreateActionWithAttrbitusOfVariousTypes() throws Exception {
		//GIVEN nothing
		
		
		//WHEN create an action with attributes of various types
		ActionEntity element = new ActionEntity();
		
		Map<String, String> houseMap = new HashMap<>();
		houseMap.put("main", "house");
		houseMap.put("2nd", "2nd house");
		houseMap.put("3rd", "Hotel");
		
		Map<String, Object> details = new HashMap<>();
		details.put("x", "y");
		details.put("y", new Location(10.0, 20.0));
		details.put("dice", Arrays.asList(1,2,3,4,5,6));
		details.put("genresMap", houseMap);
		element.setMoreAttributes(details);
		
		

		// THEN the database contains the inserted values of the action
		ObjectMapper jackson = new ObjectMapper();
		Map<String, Object> expectedDetails = 
			jackson.readValue(
				jackson.writeValueAsString(details),
				Map.class);
		
		Map<String, Object> dbElement =actionDao
		.create(element).getMoreAttributes();
		
				
		
		assertThat(dbElement).containsAllEntriesOf(details);
//			//.extracting("moreAttributes")
//			.usingRecursiveFieldByFieldElementComparator()
//			.containsExactly();
	}
	

	
	
	

}
