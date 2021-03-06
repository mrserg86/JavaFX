package org.shoppingCart.persistence;

import org.shoppingCart.checks.Check;
import org.shoppingCart.home.IPersistentHandler;
import org.shoppingCart.home.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PersistentHandler implements IPersistentHandler {
    private static PersistentHandler instance;
    private String url = "localhost";
    private int port = 5432;
    private String databaseName = "blogg_db";
    private String username = "postgres";
    private String password = "l275";
    private Connection connection = null;

    private PersistentHandler() {
        initializePostgresqlDatabase();
    }

    public static PersistentHandler getInstance() {
        if (instance == null) {
            instance = new PersistentHandler();
        }
        return instance;
    }

    private void initializePostgresqlDatabase() {
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            connection = DriverManager.getConnection("jdbc:postgresql://" + url + ":" + port + "/" + databaseName + "?useUnicode=yes&characterEncoding=UTF-8", username, password);
            System.out.println("DB connected");
        } catch (SQLException | IllegalArgumentException ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (connection == null) {
                System.exit(-1);
            }
        }
    }

    @Override
    public List<Product> getProducts(String filter) {
        filter = filter == null ? null : filter.trim().toLowerCase();
        List<Product> returnValues = new ArrayList<>();
        try {
            String query = "SELECT * FROM cashtest.goods";

            if (filter!= null && !filter.isEmpty()) {
                query += " WHERE LOWER(\"productName\") LIKE ?";
            }
            System.out.println(query);
            PreparedStatement stmt = connection.prepareStatement(query);
            if (filter!= null && !filter.isEmpty()) {
                stmt.setString(1, filter + "%");
            }
            ResultSet sqlReturnValues = stmt.executeQuery();

            while (sqlReturnValues.next()) {
                returnValues.add(new Product(sqlReturnValues.getInt(1), sqlReturnValues.getString(2), sqlReturnValues.getInt(3)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnValues;
    }

    @Override
    public boolean createCheck(Check check) {
        try {
            PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO cashtest.checks (date, time, \"Summ\") VALUES (?,?,?)");

            System.out.println("Summ: " + check.getSumm());
            insertStatement.setDate(1, Date.valueOf(check.getDate()));
            insertStatement.setTime(2, Time.valueOf(check.getTime()));
            insertStatement.setInt(3, check.getSumm());

            insertStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
