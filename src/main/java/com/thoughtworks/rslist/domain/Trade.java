package com.thoughtworks.rslist.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Trade {
        //数据库会保存每次热搜购买记录，包含：金额，购买热搜排名，对应热搜事件
    private double amount;
    private int rank;
}
