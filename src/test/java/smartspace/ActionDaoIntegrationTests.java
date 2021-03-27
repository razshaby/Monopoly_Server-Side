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
//import smartspace.dao.memory.MemoryActionDao;
//import smartspace.data.ActionEntity;
//import smartspace.data.util.EntityFactoryImpl;
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//public class ActionDaoIntegrationTests {
//		private MemoryActionDao actionDao; 
//			
//			@Autowired
//			public ActionDaoIntegrationTests() {
//				actionDao =  new MemoryActionDao();
//				
//			}
//			
//			@Before
//			public void setup() {
//				actionDao.deleteAll();
//			}
//			
//			@After
//			public void teardown() {
//				actionDao.deleteAll();
//			}
//			
//			@Test
//			public void create5actionsAndDeleteAll() throws Exception {
//				// GIVEN we have a dao
//				
//				//WHEN we create 5 actions
//				ArrayList<ActionEntity>list = new ArrayList<ActionEntity>();
//				EntityFactoryImpl entityFactory = new EntityFactoryImpl();
//				for(int i=1 ; i<6 ; i++) {
//					ActionEntity action = entityFactory.createNewAction(i+"", "Monopoly", "buy", new Date(), i+"@mail.com", "", null);
//					actionDao.create(action);
//					list.add(action);
//				}
//				//THEN the dao contains those exactly 5 actions
//				assertThat(this.actionDao.readAll())
//				.usingElementComparatorOnFields("key")
//				.containsExactlyInAnyOrderElementsOf(list);
//				
//				actionDao.deleteAll();
//				
//				// THEN The DAO is Empty 
//				assertThat(this.actionDao.readAll())
//				.isEmpty();
//			}
//
//			
//
//}
