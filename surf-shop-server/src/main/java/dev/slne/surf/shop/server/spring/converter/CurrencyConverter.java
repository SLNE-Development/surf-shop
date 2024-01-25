package dev.slne.surf.shop.server.spring.converter;

import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;

@Converter
public class CurrencyConverter implements AttributeConverter<Currency, Long> {

    @Override
    public Long convertToDatabaseColumn(Currency currency) {
        return currency.getId();
    }

    @Override
    public Currency convertToEntityAttribute(Long id) {
        return Objects.requireNonNull(TransactionApi.getCurrencyManager().getCurrencyById(id), "Currency with id " + id + " not found");
    }
}
