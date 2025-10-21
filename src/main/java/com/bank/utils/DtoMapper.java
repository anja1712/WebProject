package com.bank.utils;

import com.bank.model.Goal;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.model.Wallet;
import com.bank.model.dto.*;


import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DtoMapper {

    @Mapping(target = "role", source = "role")
    UserDto toDto(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "currency", source = "currency.name")
    @Mapping(target = "balance", source = "currentAmount")
    WalletDto toDto(Wallet wallet);


    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "percentReached", expression = "java(goal.calculatePercentReached())")
    GoalDto toDto(Goal goal);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "walletName", source = "wallet.name")
    @Mapping(target = "type", source = "transactionType")
    TransactionFullDto toDto(Transaction transaction);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "type", source = "transactionType")
    @Mapping(target=  "date", source = "transactionDate")
    TransactionViewDto toViewDto(Transaction transaction);
}
