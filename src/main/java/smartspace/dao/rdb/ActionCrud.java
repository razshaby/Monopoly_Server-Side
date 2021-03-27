package smartspace.dao.rdb;

import org.springframework.data.repository.PagingAndSortingRepository;

import smartspace.data.ActionEntity;

public interface ActionCrud extends PagingAndSortingRepository<ActionEntity, String>{

}