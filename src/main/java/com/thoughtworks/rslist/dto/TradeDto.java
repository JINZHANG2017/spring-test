package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "trade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDto {
    @Id
    @GeneratedValue
    private Integer id;
    private double amount;
    private int rank;
//    private int rsevent_id;
    @ManyToOne
    private RsEventDto rsEventDto;
}
