package com.example.fawrypaymentrouting.payment.service;

import com.example.fawrypaymentrouting.payment.dto.PaymentTransactionRequestDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentTransactionResponseDto;
import com.example.fawrypaymentrouting.payment.model.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {

    @Mapping(target = "billerId", source = "biller.id")
    @Mapping(target = "gatewayId", source = "gateway.id")
    PaymentTransactionResponseDto toDto(PaymentTransaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "biller", ignore = true)
    @Mapping(target = "gateway", ignore = true)
    @Mapping(target = "commissionApplied", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PaymentTransaction toEntity(PaymentTransactionRequestDto dto);
}
