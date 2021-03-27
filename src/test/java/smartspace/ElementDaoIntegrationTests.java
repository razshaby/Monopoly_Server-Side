//package smartspace;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.ArrayList;
//import java.util.Date;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import smartspace.dao.memory.MemoryElementDao;
//import smartspace.data.ElementEntity;
//import smartspace.data.Location;
//
//import smartspace.data.util.EntityFactoryImpl;
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class ElementDaoIntegrationTests {
//	MemoryElementDao elementDao; 
//	
//	@Autowired
//	public ElementDaoIntegrationTests() {
//		elementDao =  new MemoryElementDao();
//		
//	}
//	
//	@Before
//	public void setup() {
//		elementDao.deleteAll();
//	}
//	
//	@After
//	public void teardown() {
//		elementDao.deleteAll();
//	}
//	
//	@Test
//	public void create5elements() throws Exception {
//		// GIVEN we have a dao
//		
//		//WHEN we create 5 elements
//		ArrayList<ElementEntity>list = new ArrayList<ElementEntity>();
//		EntityFactoryImpl entityFactory = new EntityFactoryImpl();
//		for(int i=1 ; i<6 ; i++) {
//			ElementEntity element = entityFactory.createNewElement("", "", new Location(), new Date(), "", "", false, null);
//			elementDao.create(element);
//			list.add(element);
//		}
//		//THEN the dao contains those exactly 5 elements
//		assertThat(this.elementDao.readAll())
//		.usingElementComparatorOnFields("key")
//		.containsExactlyInAnyOrderElementsOf(list);
//	}
//
//	@Test
//	public void testCreateElementAndUpdateAndDeleteAll() throws Exception{
//		//GIVEN Element Dao
//		
//		//when create element
//		//and update element
//		//and delete all elements
//		//and read elements
//		EntityFactoryImpl entityFactory = new EntityFactoryImpl();
//		String email = "example@gmail.com";
//		String smartspace = "Monopoly";
//		String name = "Moshe";
//		String type = "House";
//
//		
//		ElementEntity elementEntity = entityFactory.createNewElement(name,type, new Location(), new Date(), smartspace, email, false, null);
//		ElementEntity insertedEntity = elementDao.create(elementEntity);
//		
//		
//		insertedEntity.setExpired(true);
//		insertedEntity.setKey(elementEntity.getKey());
//		elementDao.update(insertedEntity);
//		
//		insertedEntity = this.elementDao.readById(insertedEntity.getKey())
//				.orElseThrow(()->new RuntimeException("message is not available after update"));
//
//		elementDao.deleteAll();
//		
//		// THEN The DAO is Empty 
//		assertThat(this.elementDao.readAll())
//		.isEmpty();
//		
//		// AND The generated message has an ID which is not available in the DAO
//		assertThat(insertedEntity)
//		.extracting("key")
//		.isNotNull();
//	
//	}
//	
//	@Test
//	public void testReadElementById()throws Exception {
//		
//		//GIVEN Element Dao
//		
//		//when create element
//		//and read element by id
//		EntityFactoryImpl entityFactory = new EntityFactoryImpl();
//		String email = "example@gmail.com";
//		String smartspace = "Monopoly";
//		String name = "Moshe";
//		String type = "House";
//
//		
//		ElementEntity elementEntity = entityFactory.createNewElement(name,type, new Location(), new Date(), smartspace, email, false, null);
//		ElementEntity insertedEntity = elementDao.create(elementEntity);
//		
//		//THEN the dao returns the element 
//		assertThat(elementDao.readById(insertedEntity.getKey())).containsSame(insertedEntity);
//		
//	}
//	
//	@Test
//	public void testDeleteByKey()throws Exception {
//		
//		//GIVEN Element Dao
//		
//		//when create two elements
//		//and delete first element
//		EntityFactoryImpl entityFactory = new EntityFactoryImpl();
//		String email = "example@gmail.com";
//		String smartspace = "Monopoly";
//		String name1 = "Moshe";
//		String name2 = "Rachel";
//		String type = "House";
//
//		
//		ElementEntity elementEntity1 = entityFactory.createNewElement(name1,type, new Location(), new Date(), smartspace, email, false, null);
//		ElementEntity elementEntity2 = entityFactory.createNewElement(name2,type, new Location(), new Date(), smartspace, email, false, null);
//
//		ElementEntity insertedEntity1 = elementDao.create(elementEntity1);
//		ElementEntity insertedEntity2 = elementDao.create(elementEntity2);
//		
//		elementDao.deleteByKey(insertedEntity1.getKey());
//		//THEN the dao returns only the second element
//		assertThat(elementDao.readAll()).containsExactly(insertedEntity2);
//		
//	}
//}
