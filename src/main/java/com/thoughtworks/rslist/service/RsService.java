package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final
  TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public void buy(Trade trade, int id) {
//    如果如果该排名上的热搜没有人购买，那么用户花任意价格即可买到该位热搜 如果该排名上热搜已被购买，用户需要花高于当前价格的钱即可买到该位热搜，原热搜将会被替换掉（删除） 如果出价低于当前排名热搜价格，则购买失败，返回400
//    数据库会保存每次热搜购买记录，包含：金额，购买热搜排名，对应热搜事件
    List<TradeDto> tradeDtoList = tradeRepository.findAllByRank(trade.getRank());
    if(tradeDtoList.size()>0){
      TradeDto tradeDtoWithMaxAmout = tradeDtoList.stream().max(Comparator.comparing(TradeDto::getAmount)).get();
      if(trade.getAmount()<=tradeDtoWithMaxAmout.getAmount()){
        throw new RuntimeException();
      }
    }
    RsEventDto rsEventDto = rsEventRepository.findById(id).get();
    TradeDto tradeDto=TradeDto.builder()
            .amount(trade.getAmount())
            .rank(trade.getRank())
            .rsEventDto(rsEventDto)
            .build();
    tradeRepository.save(tradeDto);
  }
}
