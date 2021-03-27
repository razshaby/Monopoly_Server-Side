package smartspace.dao.rdb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import smartspace.dao.AdvancedActionDao;
import smartspace.data.ActionEntity;

@Repository
public class RdbActionDao implements AdvancedActionDao {
	private ActionCrud actionCrud;

	private String actionSmartspace;

	@Value("${smartspace.name}")
	public void setActionSmartspace(String actionSmartspace) {
		this.actionSmartspace = actionSmartspace;
	}

	// private AtomicLong nextId;
	private IdentitySeedCrud identitySeedCrud;

	@Autowired
	public RdbActionDao(ActionCrud actionCrud, IdentitySeedCrud identitySeedCrud) {
		super();
		this.actionCrud = actionCrud;

		// this.nextId = new AtomicLong(1L);
		this.identitySeedCrud = identitySeedCrud;

	}

	@Override
	@Transactional
	public ActionEntity create(ActionEntity actionEntity) {
		IdentitySeed seed = this.identitySeedCrud.save(new IdentitySeed());

		actionEntity.setKey(actionSmartspace+"|"+seed.getId());


		this.identitySeedCrud.delete(seed);

		// SQL INSERT
		if (!this.actionCrud.existsById(actionEntity.getKey())) {
			ActionEntity rv = this.actionCrud.save(actionEntity);
			return rv;
		} else {
			throw new RuntimeException("Action already exists with id: " + actionEntity.getKey());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActionEntity> readAll() {
		List<ActionEntity> list = new ArrayList<>();
		// SQL SELECT
		this.actionCrud.findAll().forEach(list::add);
		return list;
	}

	@Override
	@Transactional
	public void deleteAll() {
		// SQL DELETE
		this.actionCrud.deleteAll();
	}

	@Override
	@Transactional(readOnly=true)
	public List<ActionEntity> readAll(int size, int page) {
	return this.actionCrud
			.findAll(PageRequest.of(page, size)).getContent();
	}

	@Override
	@Transactional(readOnly=true)
	public List<ActionEntity> readAll(String sortingAttr, int size, int page) {

		return
				this.actionCrud
				.findAll(
						PageRequest.of(page, size, Direction.ASC,sortingAttr))
				.getContent();
				}

	@Override
	public ActionEntity createFromImport(ActionEntity actionEntity) {
		// SQL INSERT
				return this.actionCrud.save(actionEntity);
	}
	
	

}
