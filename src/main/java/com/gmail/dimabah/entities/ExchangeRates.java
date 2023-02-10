package com.gmail.dimabah.entities;

import com.gmail.dimabah.Currency;
import com.google.gson.annotations.SerializedName;

import javax.persistence.*;

@Entity
public class ExchangeRates implements FormatObj{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @SerializedName("ccy")
    private Currency currency;
    private  double buy;
    private  double sale;

    public ExchangeRates(Currency currency, double buy, double sale) {
        this.currency = currency;
        this.buy = buy;
        this.sale = sale;
    }

    public ExchangeRates() {
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public double getBuy() {
        return buy;
    }

    public void setBuy(double buy) {
        this.buy = buy;
    }

    public double getSale() {
        return sale;
    }

    public void setSale(double sale) {
        this.sale = sale;
    }

    @Override
    public String toString() {
        return "ExchangeCourse{" +
                "currency=" + currency +
                ", buy=" + buy +
                ", sale=" + sale +
                '}';
    }
    public String getFormattedObject() {
        return String.format("%-10d | %-10s | %-15.4f | %-15.4f",
                id, currency.name(), buy, sale);
    }

    public String getHeader() {
        return String.format("%-10s | %-10s | %-15s | %-15s",
                "id", "currency",  "buy", "sale");
    }
}
