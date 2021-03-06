package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired
  TradeRepository tradeRepository;

  private UserDto userDto;

  @BeforeEach
  void setUp() {
    voteRepository.deleteAll();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
    tradeRepository.deleteAll();
    userDto =
        UserDto.builder()
            .voteNum(10)
            .phone("188888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("idolice")
            .build();
  }

  @Test
  public void shouldGetRsEventList() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").boughtRank(1).user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第二条事件").voteNum(10).user(save).build();
    rsEventRepository.save(rsEventDto);

    mockMvc
        .perform(get("/rs/list"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[0]", not(hasKey("user"))))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldGetOneEvent() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
  }

  @Test
  public void shouldGetErrorWhenIndexInvalid() throws Exception {
    mockMvc
        .perform(get("/rs/4"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid index")));
  }

  @Test
  public void shouldGetRsListBetween() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list?start=1&end=2"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=2&end=3"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=1&end=3"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")))
        .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[2].keyword", is("无分类")));
  }

  @Test
  public void shouldAddRsEventWhenUserExist() throws Exception {

    UserDto save = userRepository.save(userDto);

    String jsonValue =
        "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    List<RsEventDto> all = rsEventRepository.findAll();
    assertNotNull(all);
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getEventName(), "猪肉涨价了");
    assertEquals(all.get(0).getKeyword(), "经济");
    assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
    assertEquals(all.get(0).getUser().getAge(), save.getAge());
  }

  @Test
  public void shouldAddRsEventWhenUserNotExist() throws Exception {
    String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldVoteSuccess() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    String jsonValue =
        String.format(
            "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
            save.getId(), LocalDateTime.now().toString());
    mockMvc
        .perform(
            post("/rs/vote/{id}", rsEventDto.getId())
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    UserDto userDto = userRepository.findById(save.getId()).get();
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(userDto.getVoteNum(), 9);
    assertEquals(newRsEvent.getVoteNum(), 1);
    List<VoteDto> voteDtos =  voteRepository.findAll();
    assertEquals(voteDtos.size(), 1);
    assertEquals(voteDtos.get(0).getNum(), 1);
  }

  @Test
  void shouldBuyRsWhenItHasNotBeenBought() throws Exception {
    UserDto userDto =
            UserDto.builder()
                    .voteNum(5)
                    .phone("18888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("xiaoli")
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("event name")
                    .keyword("keyword")
                    .voteNum(2)
                    .user(userDto)
                    .build();
    userRepository.save(userDto);
    rsEventRepository.save(rsEventDto);
    Trade trade= Trade.builder()
            .amount(10)
            .rank(1).build();
    ObjectMapper mapper=new ObjectMapper();
    String json = mapper.writeValueAsString(trade);
    mockMvc.perform(post("/rs/buy/{id}", rsEventDto.getId())
                          .content(json)
                          .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    List<TradeDto> tradeDtoList = tradeRepository.findAll();
    assertEquals(1,tradeDtoList.size());
    assertEquals(1,tradeDtoList.get(0).getRank());
    assertEquals(10,tradeDtoList.get(0).getAmount());
  }

  @Test
  void shouldDelPreWhenItHasBeenBoughtAndAmountIsMore() throws Exception {
    UserDto userDto =
            UserDto.builder()
                    .voteNum(5)
                    .phone("18888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("xiaoli")
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("event name")
                    .keyword("keyword")
                    .voteNum(2)
                    .user(userDto)
                    .build();
    userRepository.save(userDto);
    rsEventRepository.save(rsEventDto);
    Trade trade= Trade.builder()
            .amount(10)
            .rank(1).build();
    ObjectMapper mapper=new ObjectMapper();
    String json = mapper.writeValueAsString(trade);
    mockMvc.perform(post("/rs/buy/{id}", rsEventDto.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    List<TradeDto> tradeDtoList = tradeRepository.findAll();
    assertEquals(1,tradeDtoList.size());
    assertEquals(1,tradeDtoList.get(0).getRank());
    assertEquals(10,tradeDtoList.get(0).getAmount());

    RsEventDto rsEventDto2 =
            RsEventDto.builder()
                    .eventName("event name2")
                    .keyword("keyword2")
                    .voteNum(3)
                    .user(userDto)
                    .build();
    rsEventRepository.save(rsEventDto2);
    Trade trade2= Trade.builder()
            .amount(20)
            .rank(1).build();
    String json2 = mapper.writeValueAsString(trade2);
    mockMvc.perform(post("/rs/buy/{id}", rsEventDto2.getId())
            .content(json2)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    tradeDtoList = tradeRepository.findAll();
    assertEquals(2,tradeDtoList.size());
    assertEquals(1,tradeDtoList.get(0).getRank());
    assertEquals(10,tradeDtoList.get(0).getAmount());
    assertEquals(1,tradeDtoList.get(1).getRank());
    assertEquals(20,tradeDtoList.get(1).getAmount());
    List<RsEventDto> rsEventDtoList = rsEventRepository.findAll();
    assertEquals(2,rsEventDtoList.size());
    assertEquals("event name",rsEventDtoList.get(0).getEventName());
    assertEquals("keyword",rsEventDtoList.get(0).getKeyword());
    assertEquals(1,rsEventDtoList.get(0).getIsDeleted());
    assertEquals("event name2",rsEventDtoList.get(1).getEventName());
    assertEquals("keyword2",rsEventDtoList.get(1).getKeyword());
    assertEquals(0,rsEventDtoList.get(1).getIsDeleted());
  }

  @Test
  void shouldReturn400WhenAmountIsLess() throws Exception {
    UserDto userDto =
            UserDto.builder()
                    .voteNum(5)
                    .phone("18888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("xiaoli")
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("event name")
                    .keyword("keyword")
                    .voteNum(2)
                    .user(userDto)
                    .build();
    userRepository.save(userDto);
    rsEventRepository.save(rsEventDto);
    Trade trade= Trade.builder()
            .amount(10)
            .rank(1).build();
    ObjectMapper mapper=new ObjectMapper();
    String json = mapper.writeValueAsString(trade);
    mockMvc.perform(post("/rs/buy/{id}", rsEventDto.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    List<TradeDto> tradeDtoList = tradeRepository.findAll();
    assertEquals(1,tradeDtoList.size());
    assertEquals(1,tradeDtoList.get(0).getRank());
    assertEquals(10,tradeDtoList.get(0).getAmount());

    RsEventDto rsEventDto2 =
            RsEventDto.builder()
                    .eventName("event name2")
                    .keyword("keyword2")
                    .voteNum(3)
                    .user(userDto)
                    .build();
    rsEventRepository.save(rsEventDto2);
    Trade trade2= Trade.builder()
            .amount(5)
            .rank(1).build();
    String json2 = mapper.writeValueAsString(trade2);
    mockMvc.perform(post("/rs/buy/{id}", rsEventDto2.getId())
            .content(json2)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", is("amount not enough")));
  }
}
