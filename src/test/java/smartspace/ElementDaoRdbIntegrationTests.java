package smartspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import smartspace.dao.ElementDao;
import smartspace.data.ElementEntity;
import smartspace.data.Location;


@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "spring.profiles.active=default" })
public class ElementDaoRdbIntegrationTests {

	private ElementDao<String> elementDao;
	private String elementSmartspace;
	
	@Value("${smartspace.name}")
	public void setElementSmartspace(String elementSmartspace) {
		this.elementSmartspace = elementSmartspace;
	}
	
	@Autowired
	public void setElementDao(ElementDao<String> elementDao) {
		this.elementDao = elementDao;
	}
	
//	@Before
//	public void setup() {
//		this.elementDao.deleteAll();
//	}

	@After
	public void teardown() {
		this.elementDao.deleteAll();		
	}

	
	@Test
	public void testCreate5elementsAndDeleteAll() throws Exception {
		// GIVEN we have a dao

		// WHEN we create 5 new elements and insert them
		List<ElementEntity> list = IntStream.range(1, 6) // int Stream
				.mapToObj(num -> "name #" + num)// String Stream
				.map(ElementEntity::new) // ElementEntity Stream
				.map(this.elementDao::create) // ElementEntity Stream
				.collect(Collectors.toList()); // List<ElementEntity>

		// THEN the dao contains 5 elements
		// AND the elements created available through the dao
		assertThat(this.elementDao.readAll()).usingElementComparatorOnFields("name")
				.containsExactlyInAnyOrderElementsOf(list);
		
		this.elementDao.deleteAll();
		
		assertThat(this.elementDao.readAll())
		.isEmpty();
	}

	@Test
	public void testCreateElementWithValidKey() throws Exception{
		//GIVEN nothing
		
		//WHEN create an element 
		ElementEntity element = new ElementEntity();
		ElementEntity insertedElement = elementDao.create(element);
		
		//THEN a valid element key is created
		//assertThat(insertedElement.getKey()).matches(Pattern.compile(elementSmartspace+"\\|\\d$"));
		assertThat(insertedElement.getKey()).containsSubsequence(elementSmartspace+"|");
	}
	
//	@Test
//	public void testCreateElementAndGetSmartspaceAndGetId() throws Exception{
//		//GIVEN nothing
//		
//		//WHEN create an element 
//		ElementEntity element = new ElementEntity();
//		ElementEntity insertedElement = elementDao.create(element);
//		
//		//THEN a valid element smartspace is returned
//		assertThat(insertedElement.getElementSmartspace()).isEqualTo(elementSmartspace);
//		assertThat(insertedElement.getElementId()).containsOnlyDigits();
//	}
	
	@Test
	public void testCreateElementWithAttrbitusOfVariousTypes() throws Exception {
		//GIVEN nothing
		
		
		//WHEN create an element with attributes of various types
		ElementEntity element = new ElementEntity();
		
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
		
//		String pk = this.elementDao
//			.create(element)
//			.getKey();

		// THEN the database contains the inserted values of the element
		ObjectMapper jackson = new ObjectMapper();
		Map<String, Object> expectedDetails = 
			jackson.readValue(
				jackson.writeValueAsString(details),
				Map.class);
				
		
		
		
		
//		assertThat(this.elementDao.readById(pk))
//			.isPresent()
//			.get()
//			.extracting("moreAttributes")
//			.usingRecursiveFieldByFieldElementComparator()
//			.containsExactly(expectedDetails);
		
		
//		Map<String, Object> dbElement =elementDao
//				.readById(pk).get().getMoreAttributes();
		Map<String, Object> dbElement =elementDao
				.create(element).getMoreAttributes();
		
		assertThat(dbElement).containsAllEntriesOf(details);
	}
	
	
	
	@Test
	public void testDeleteElmenetByKey() throws Exception{
		//GIVEN nothing
		
		// WHEN we create 2 new elements and insert them to dao
		//AND delete one element by key
		List<ElementEntity> list = IntStream.range(1, 3) // int Stream
				.mapToObj(num -> "element#" + num)// String Stream
				.map(ElementEntity::new) // ElementEntity Stream
				.map(this.elementDao::create) // ElementEntity Stream
				.collect(Collectors.toList()); // List<ElementEntity>
		String elementKey = list.get(1).getKey();
		this.elementDao.deleteByKey(elementKey);
		list.remove(1);
		
		
		// THEN the dao contains 1 elements
		// AND the elements created available through the dao
		assertThat(this.elementDao.readAll()).usingElementComparatorOnFields("name")
				.containsExactlyInAnyOrderElementsOf(list);
		
	}

	
	
	@Test
	public void testDeleteElmenet() throws Exception{
		//GIVEN nothing
		
		// WHEN we create 2 new elements and insert them to dao
		//AND delete one element
		List<ElementEntity> list = IntStream.range(1, 3) // int Stream
				.mapToObj(num -> "element#" + num)// String Stream
				.map(ElementEntity::new) // ElementEntity Stream
				.map(this.elementDao::create) // ElementEntity Stream
				.collect(Collectors.toList()); // List<ElementEntity>
		ElementEntity element = list.get(1);
		this.elementDao.delete(element);
		list.remove(1);
		
		
		// THEN the dao contains 1 elements
		// AND the elements created available through the dao
		assertThat(this.elementDao.readAll()).usingElementComparatorOnFields("name")
				.containsExactlyInAnyOrderElementsOf(list);
	}

	
//	@Test(expected=Exception.class)
//	public void testDeleteElementWithInvalidKey() throws Exception{
//		this.elementDao.deleteByKey("testKey");
//	}
//	
	@Test(expected=Exception.class)
	public void testDeleteElementWithInvalidElement() throws Exception{
		this.elementDao.delete(null);
	}
	
	@Test(expected=Exception.class)
	public void testGetExceptionWhenReadByInvalidId() throws Exception{
		//GIVEN nothing
		
		//WHEN create a user with id and trying to read with invalid id 
		String theName = "Test";
		ElementEntity insertedElement =  this.elementDao.create(new ElementEntity(theName));
		
		
		//THEN throws exception
		insertedElement = this.elementDao.readById("Invalid Key")
				.orElseThrow(()->new RuntimeException("element is not available"));
		
	}
	
	
	
	
}
	

