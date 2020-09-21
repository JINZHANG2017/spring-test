package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.AmountNotEnoughException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<RsEvent> getList(){
        List<RsEvent> notBoughtList = rsEventRepository.findAllByIsDeletedEqualsAndBoughtRankEqualsOrderByVoteNumDesc(0,0).stream()
                .map(
                        item ->
                                RsEvent.builder()
                                        .eventName(item.getEventName())
                                        .keyword(item.getKeyword())
                                        .userId(item.getId())
                                        .voteNum(item.getVoteNum())
                                        .boughtRank(item.getBoughtRank())
                                        .build())
                .collect(Collectors.toList());
        List<RsEvent> boughtList = rsEventRepository.findAllByIsDeletedEqualsAndBoughtRankNot(0,0).stream()
                .map(
                        item ->
                                RsEvent.builder()
                                        .eventName(item.getEventName())
                                        .keyword(item.getKeyword())
                                        .userId(item.getId())
                                        .voteNum(item.getVoteNum())
                                        .boughtRank(item.getBoughtRank())
                                        .build())
                .collect(Collectors.toList());
        List<RsEvent> allInOneList=new ArrayList<>();
        int sum=notBoughtList.size()+boughtList.size();
        int temp=0;
        for(int i=0;i<sum;i++){
            final Integer finalI=i;
            List<RsEvent> boughtCurrentRankList=boughtList.stream().filter(x->x.getBoughtRank()==(finalI+1)).collect(Collectors.toList());
            if(boughtCurrentRankList.size()>0){
                allInOneList.add(boughtCurrentRankList.get(0));
            }else{
                allInOneList.add(notBoughtList.get(temp++));
            }
        }
        return allInOneList;
    }

    public List<RsEvent> getList(Integer start, Integer end) {
        return getList().subList(start - 1, end);
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
        if (tradeDtoList.size() > 0) {
            TradeDto tradeDtoWithMaxAmout = tradeDtoList.stream().max(Comparator.comparing(TradeDto::getAmount)).get();
            if (tradeDtoWithMaxAmout.getId() != id) {
                if (trade.getAmount() <= tradeDtoWithMaxAmout.getAmount()) {
                    throw new AmountNotEnoughException("amount not enough");
                } else {
//        rsEventRepository.delete(tradeDtoWithMaxAmout.getRsEventDto());
                }
                RsEventDto rsEventDtoToBeDelete = tradeDtoWithMaxAmout.getRsEventDto();
//      rsEventDtoToBeDelete.setUser(null);
//      rsEventRepository.save(rsEventDtoToBeDelete);
//      rsEventRepository.delete(rsEventDtoToBeDelete);;
//      rsEventRepository.deleteById(rsEventDtoToBeDelete.getId());
                rsEventDtoToBeDelete.setIsDeleted(1);
                rsEventRepository.save(rsEventDtoToBeDelete);
            }
        }

        RsEventDto rsEventDto = rsEventRepository.findById(id).get();
        rsEventDto.setBoughtRank(trade.getRank());
        rsEventRepository.save(rsEventDto);
        TradeDto tradeDto = TradeDto.builder()
                .amount(trade.getAmount())
                .rank(trade.getRank())
                .rsEventDto(rsEventDto)
                .build();
        tradeRepository.save(tradeDto);
    }
}
