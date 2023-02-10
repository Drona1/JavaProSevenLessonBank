package com.gmail.dimabah.entities;

import com.gmail.dimabah.Currency;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Accounts implements FormatObj {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Currency currency;
    private BigDecimal amount;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Clients client;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transactions> transactions = new ArrayList<>();

    public Accounts(Currency currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public Accounts() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Transactions> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transactions> transactions) {
        this.transactions = transactions;
    }

    public Clients getClient() {
        return client;
    }

    public void setClient(Clients client) {
        this.client = client;
    }
    public void addTransaction(Transactions transaction){
        transactions.add(transaction);
        transaction.setAccount(this);
    }

    public String getFormattedObject() {
        return String.format("%-10d | %-10d | %-10s | %-15.2f | %-10d",
                id, client.getId(), currency,  amount, transactions.size());
    }

    public String getHeader() {
        return String.format("%-10s | %-10s | %-10s | %-15s | %-10s",
                "account_id", "client_id","currency",  "amount", "transactions");
    }

}
