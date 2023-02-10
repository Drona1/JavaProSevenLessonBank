package com.gmail.dimabah.entities;

import com.gmail.dimabah.Currency;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Entity
public class Transactions implements FormatObj {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Calendar date;
    private BigDecimal amount;
    @Column(nullable = false)
    private Currency currency;
    private String description;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Accounts account;


    public Transactions(Calendar date, BigDecimal amount, Currency currency, String description) {
        this.date = date;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public Transactions() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Accounts getAccount() {
        return account;
    }

    public void setAccount(Accounts account) {
        this.account = account;
    }

    public String getFormattedObject() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ms");
        String dateTime = simpleDateFormat.format(date.getTime());
        return String.format("%-15d | %-10d | %-10s | %-15.2f | %-25s | %-20s",
                id, account.getId(), currency.name(), amount, dateTime, description);
    }

    public String getHeader() {
        return String.format("%-15s | %-10s | %-10s | %-15s | %-25s | %-20s",
                "transaction_id", "account_id", "currency", "amount", "date", "description");
    }

}
