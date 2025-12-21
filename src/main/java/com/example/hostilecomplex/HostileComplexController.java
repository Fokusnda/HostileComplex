package com.example.hostilecomplex;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class HostileComplexController {
    @FXML
    private Label tableTitle;

    @FXML
    private TableView<Object> tableView;

    @FXML
    public void onGuestClick(ActionEvent actionEvent) {
        tableTitle.setText("Гости");
        tableView.getColumns().clear();

        TableColumn<Object, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idGuest"));
        idColumn.setPrefWidth(50);

        TableColumn<Object, String> fioColumn = new TableColumn<>("ФИО");
        fioColumn.setCellValueFactory(new PropertyValueFactory<>("fio"));
        fioColumn.setPrefWidth(200);

        TableColumn<Object, String> phoneColumn = new TableColumn<>("Номер телефона");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneColumn.setPrefWidth(200);

        tableView.getColumns().setAll(idColumn, fioColumn, phoneColumn);

        ObservableList<GuestDB> guests = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT \"Guest\".\"Id_guest\", \"Guest\".\"FIO\", \"Guest\".\"Number_phone\"FROM \"Guest\"")) {

            while (rs.next()) {
                guests.add(new GuestDB(
                        rs.getLong("Id_guest"),
                        rs.getString("FIO"),
                        rs.getString("Number_phone")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(guests));
    }

    @FXML
    public void onBookingClick(ActionEvent actionEvent) {
        tableView.getColumns().clear();

        TableColumn<Object, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idBooking"));
        idColumn.setPrefWidth(50);

        TableColumn<Object, String> guestColumn = new TableColumn<>("ФИО");
        guestColumn.setCellValueFactory(new PropertyValueFactory<>("idGuest"));
        idColumn.setPrefWidth(200);

        TableColumn<Object, Integer> numberRoomColumn = new TableColumn<>("Номер комнаты");
        numberRoomColumn.setCellValueFactory(new PropertyValueFactory<>("idRoom"));
        numberRoomColumn.setPrefWidth(100);

        TableColumn<Object, LocalDate> checkInDateColumn = new TableColumn<>("Дата заезда");
        checkInDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkInDateColumn.setPrefWidth(100);

        TableColumn<Object, LocalDate> checkOutDateColumn = new TableColumn<>("Дата выезда");
        checkOutDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        checkOutDateColumn.setPrefWidth(100);

        TableColumn<Object, BigDecimal> totalAmountColumn = new TableColumn<>("Итоговая сумма");
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalAmountColumn.setPrefWidth(100);

        tableView.getColumns().setAll(idColumn, guestColumn, numberRoomColumn, checkOutDateColumn, totalAmountColumn);

        ObservableList<BookingDB> bookings = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Booking\".\"Id_booking\", \"Guest\".\"FIO\", \"Room\".\"Room_number\", \"Booking\".\"Check_in_date\", \"Booking\".\"Check_out_date\", \"Booking\".\"Total_amount\"::numeric\n" +
                     "From \"Booking\"\n" +
                     "Join \"Guest\" on \"Guest\".\"Id_guest\" = \"Booking\".\"Id_guest\"\n" +
                     "Join\"Room\" on \"Room\".\"Id_room\" = \"Booking\".\"Id_room\";")) {

            while (rs.next()) {
                bookings.add(new BookingDB(
                        rs.getLong("Id_booking"),
                        rs.getString("FIO"),
                        rs.getInt("Room_number"),
                        rs.getDate("Check_in_date").toLocalDate(),
                        rs.getDate("Check_out_date").toLocalDate(),
                        rs.getBigDecimal("Total_amount")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(bookings));
    }

    @FXML
    public void onBookingAdditionalServicesClick(ActionEvent actionEvent) {

    }

    @FXML
    public void onAdditionalServicesClick(ActionEvent actionEvent) {

    }

    @FXML
    public void onHotelClassClick(ActionEvent actionEvent) {

    }

    @FXML
    public void onBuildingClick(ActionEvent actionEvent) {

    }

    @FXML
    public void onRoomClick(ActionEvent actionEvent) {

    }

    @FXML
    public void onRoomTypeClick(ActionEvent actionEvent) {

    }

    @FXML
    public void onHouseholdServicesClick(ActionEvent actionEvent) {

    }
}
