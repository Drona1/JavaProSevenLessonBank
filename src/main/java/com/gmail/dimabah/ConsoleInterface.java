package com.gmail.dimabah;

import com.gmail.dimabah.entities.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import javax.persistence.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

public class ConsoleInterface {
    private EntityManager em;
    private Scanner sc = new Scanner(System.in);

    public ConsoleInterface() {

    }

    private void loadExchangeRates(String urlAddress) {
        try {
            URL url = new URL(urlAddress);
            Gson gson = new Gson();
            JsonReader jsonReader = new JsonReader(new InputStreamReader(url.openStream()));
            ExchangeRates[] exchangeRates = gson.fromJson(jsonReader, ExchangeRates[].class);
            performTransaction(() -> {
                em.persist(new ExchangeRates(Currency.UAH, 1, 1));
                for (var i : exchangeRates) {
                    em.persist(i);
                }
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void showInterface() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Bank");
        em = emf.createEntityManager();
        try {
            loadExchangeRates("https://api.privatbank.ua/p24api/pubinfo?exchange&json&coursid=11");
            do {
                System.out.println("*".repeat(50));
                System.out.print("""
                        Enter item number:
                          1: add client
                          2: add bank account
                          3: replenish bank account
                          4: make transaction;
                          5: show the client's total balance
                          6: show clients;
                          7: show accounts
                          8: show transactions;
                          9: show exchange rates;
                          0: generate random data in tables
                          e: exit
                        ->\s""");
            } while (processInputDataFromMainInterface());
        } finally {
            sc.close();
            em.close();
            emf.close();
        }
    }

    private boolean processInputDataFromMainInterface() {
        String choice = sc.nextLine();
        boolean result = true;
        try {
            switch (choice) {
                case "1" -> addClint();
                case "2" -> addAccount();
                case "3" -> replenishAccount();
                case "4" -> makeTransfer();
                case "5" -> showBalance();
                case "6" -> viewTable(Clients.class);
                case "7" -> viewTable(Accounts.class);
                case "8" -> viewTable(Transactions.class);
                case "9" -> viewTable(ExchangeRates.class);
                case "0" -> generateTables();
                case "e" -> {
                    return false;
                }
                default -> result = false;
            }
        } catch (IllegalArgumentException ex) {
            result = false;
        }
        if (!result) {
            System.out.println("Incorrect entered data, try again");
        }
        return true;
    }


    private <T> T performTransaction(Callable<T> action) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            T result = action.call();
            transaction.commit();

            return result;
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            ex.printStackTrace();
            System.out.println("Error, problem with database");
            return null;
        }
    }

    private <T> T findPosition(String message, Class<T> clazz) {
        System.out.print(message);
        String name = clazz.getSimpleName();
        long id = Long.parseLong(sc.nextLine());
        T position = em.find(clazz, id);
        if (position == null) {
            System.out.println(name.substring(0, name.length() - 1) + " not found!");
        }
        return position;
    }

    private void addClint() {
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter phone: ");
        String phone = sc.nextLine();
        Clients client = new Clients(name, phone);
        performTransaction(() -> {
            em.persist(client);
            return null;
        });
        System.out.println("Client added");
    }

    private void addAccount() {
        Clients client = findPosition("Enter the id of the client: ", Clients.class);
        if (client == null) {
            return;
        }
        System.out.print("Enter currency name (UAH, USD, EUR): ");
        Currency currency = Currency.valueOf(sc.nextLine().toUpperCase());
        System.out.print("Enter amount (" + currency.name() + "): ");
        BigDecimal amount = new BigDecimal(sc.nextLine());
        Accounts account = new Accounts(currency, amount);
        client.addAccount(account);
        performTransaction(() -> {
            em.persist(account);
            return null;
        });
        System.out.println("Account added");
    }

    private void replenishAccount() {
        Accounts account = findPosition("Enter the id of the bank account: ", Accounts.class);
        if (account == null) {
            return;
        }
        System.out.print("Enter the name of the currency (UAH, USD, EUR) you are depositing: ");
        Currency currency = Currency.valueOf(sc.nextLine().toUpperCase());
        System.out.print(" Enter amount (" + currency.name() + "): ");
        BigDecimal amount = new BigDecimal(sc.nextLine());
        makeReplenishmentTransaction(account, currency, amount);
    }
    private void makeReplenishmentTransaction(Accounts account, Currency currency, BigDecimal amount){
        performTransaction(() -> {
            makeTransaction(account, currency, amount);
            em.refresh(account);
            Transactions transaction = new Transactions(Calendar.getInstance(), amount, currency,
                    "Replenishment of the account");
            account.addTransaction(transaction);
            em.persist(transaction);
            return null;
        });
    }

    private void makeTransaction(Accounts account, Currency currency, BigDecimal amount) {
        String queryString = """
                UPDATE Accounts a 
                SET a.amount = (SELECT a.amount + :amount * s.buy / b.sale 
                FROM ExchangeRates s, ExchangeRates b 
                WHERE (a.id = :id AND s.currency = :currency AND b.currency = a.currency)) 
                WHERE a.id = :id
                """;
        Query query = em.createQuery(queryString);

        query.setParameter("id", account.getId());
        query.setParameter("amount", amount.doubleValue());
        query.setParameter("currency", currency);
        query.executeUpdate();
    }

    private void makeTransfer() {
        Accounts accountFrom = findPosition("Enter the id of the bank account" +
                " from which the transfer will be made: ", Accounts.class);
        if (accountFrom == null) return;
        Accounts accountTo = findPosition("Enter the id of the bank account" +
                " to which the transfer will be made: ", Accounts.class);
        if (accountTo == null) return;
        System.out.print("Enter amount of the transfer (" + accountFrom.getCurrency().name() + "): ");
        BigDecimal amount = new BigDecimal(sc.nextLine());
        if (accountFrom.getAmount().compareTo(amount) < 0) {
            System.out.println("Not enough money");
            return;
        }
        makeTransferTransaction(accountFrom, accountTo, amount);
    }
    private void makeTransferTransaction(Accounts accountFrom, Accounts accountTo, BigDecimal amount){
        performTransaction(() -> {
            makeTransaction(accountFrom, accountFrom.getCurrency(), amount.negate());
            em.refresh(accountFrom);
            Transactions transaction = new Transactions(Calendar.getInstance(), amount.negate(),
                    accountFrom.getCurrency(), "Transfer to account_id:   " + accountTo.getId());
            accountFrom.addTransaction(transaction);
            em.persist(transaction);

            makeTransaction(accountTo, accountFrom.getCurrency(), amount);
            em.refresh(accountTo);
            transaction = new Transactions(Calendar.getInstance(), amount, accountFrom.getCurrency(),
                    "Transfer from account_id: " + accountFrom.getId());
            accountTo.addTransaction(transaction);
            em.persist(transaction);
            return null;
        });
    }

    private void showBalance() {
        Clients client = findPosition("Enter the id of the client: ", Clients.class);
        if (client == null) {
            return;
        }
        String queryString = """
                SELECT SUM(a.amount * ex.buy) 
                FROM Accounts a, ExchangeRates ex 
                WHERE a.client.id = :id AND a.currency=ex.currency
                """;
        TypedQuery<Double> queryBalance = em.createQuery(queryString, Double.class);
        queryBalance.setParameter("id", client.getId());

        System.out.println(String.format("Total balance: %.2f UAH",
                queryBalance.getSingleResult()));
    }
    private void generateTables(){
        Random random = new Random();
        String[] names = {"Dmytro", "Oleksandr", "Vsevolod", "Vadym", "OLena", "Maria"};
        Clients[] clients = new Clients[10];
        List<Accounts> accounts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String name = names[random.nextInt(names.length)];
            String phone = "+38050" + (random.nextInt(8999999) + 1000000);
            clients[i] = new Clients(name,phone);
            for (int j = 0; j <=random.nextInt(4); j++) {
                Accounts account = new Accounts(Currency.values()[random.nextInt(3)],
                        BigDecimal.valueOf(random.nextDouble(100000)));
                accounts.add(account);
                clients[i].addAccount(account);
            }
        }
        performTransaction(()->{
            for (var client: clients) {
                em.persist(client);
            }
            return null;
        });
        for (int i = 0; i < 5; i++) {
            Accounts account = accounts.get(random.nextInt(accounts.size()));
            makeTransferTransaction(account,
                    accounts.get(random.nextInt(accounts.size())),
                    BigDecimal.valueOf(Math.ceil(random.nextDouble(account.getAmount().doubleValue()/10))));
            account = accounts.get(random.nextInt(accounts.size()));
            makeReplenishmentTransaction(account, Currency.values()[random.nextInt(3)],
                    BigDecimal.valueOf(Math.ceil(random.nextDouble(10000))));
        }
    }


    private void viewTable(Class<?> clazz) {
        String name = clazz.getSimpleName();
        String whereParameter = "";
        if (name.equals("Accounts")) {
            System.out.print("Enter id of the client or empty to show all accounts:");
            if (!(whereParameter = sc.nextLine()).isEmpty()) {
                whereParameter = "WHERE o.client.id=" + whereParameter;
            }
        } else if (name.equals("Transactions")) {
            System.out.print("Enter id of the account or empty to show all transactions:");
            if (!(whereParameter = sc.nextLine()).isEmpty()) {
                whereParameter = "WHERE o.account.id=" + whereParameter;
            }
        }
        TypedQuery<FormatObj> query = em.createQuery("SELECT o FROM " + name + " o " + whereParameter,
                FormatObj.class);
        viewTable(query);
    }

    private void viewTable(TypedQuery<FormatObj> query) {
        List<FormatObj> list = query.getResultList();
        if (list.size() == 0) {
            System.out.println("There are no such data in the database");
        } else {
            String border = "-".repeat(150);
            System.out.println(border);
            System.out.println(list.get(0).getHeader());
            System.out.println(border);
            for (var a : list) {
                System.out.println(a.getFormattedObject());
            }
            System.out.println(border);
        }
    }
}
