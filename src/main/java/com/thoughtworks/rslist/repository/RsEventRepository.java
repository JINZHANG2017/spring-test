package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RsEventRepository extends CrudRepository<RsEventDto, Integer> {
  List<RsEventDto> findAll();
  List<RsEventDto> findAllByIsDeletedEqualsAndBoughtRankEqualsOrderByVoteNumDesc(int isDeleted,int boughtRank);
  List<RsEventDto> findAllByIsDeletedEqualsAndBoughtRankNot(int isDeleted,int boughtRank);
  @Transactional
  void deleteAllByUserId(int userId);
}
