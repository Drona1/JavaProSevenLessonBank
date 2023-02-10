package com.gmail.dimabah.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Clients implements FormatObj{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phone;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Accounts> accounts = new ArrayList<>();

    public Clients(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public Clients() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Accounts> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Accounts> accounts) {
        this.accounts = accounts;
    }
    public void addAccount(Accounts account) {
        accounts.add(account);
        account.setClient(this);
    }


    @Override
    public String toString() {
        return "Clients{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", accounts_amount=" + accounts.size() +
                '}';
    }
    public String getFormattedObject() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < accounts.size(); i++) {
            sb.append("#").append(accounts.get(i).getId());
            sb.append("(").append(accounts.get(i).getCurrency().name()).append(")");
            if (i!=accounts.size()-1){
                sb.append(", ");
            }
        }
        sb.append("]");
        return String.format("%-10d | %-25s | %-15s | %-25s",
                id, name,  phone, sb);
    }

    public String getHeader() {
        return String.format("%-10s | %-25s | %-15s | %-25s",
                "client_id", "name", "phone", "accounts");
    }

}
