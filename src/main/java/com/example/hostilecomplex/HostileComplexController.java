package com.example.hostilecomplex;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HostileComplexController {
    private enum ActiveTable {
        BOOKING, BOOKING_CLIENT, BUILDING, CLIENT,
        CONTRACT, FLOOR, ORGANIZATION, ROOM,
        SERVICE, STAY, STAY_SERVICE;
    }
    private ActiveTable activeTable;



    @FXML private Label             tableTitle;
    @FXML private TableView<Object> tableView;
    @FXML private ComboBox<Object>  combo1;
    @FXML private ComboBox<Object>  combo2;
    @FXML private DatePicker        dateFrom;
    @FXML private DatePicker        dateTo;
    @FXML private Button            freeRoomsBtn;
    @FXML private Button            allRoomsBtn;
    @FXML private Button            clearFilterBtn;
    @FXML private Button            roomInfoBtn;



    private void resetTable() {
        tableView.setRowFactory(null);
        tableView.setOnMouseClicked(null);
        tableView.getItems().clear();
        tableView.getColumns().clear();
        tableView.refresh();
        tableView.getSelectionModel().clearSelection();

        roomInfoBtn.setVisible(false);
        clearFilterBtn.setVisible(false);
        freeRoomsBtn.setVisible(false);
        allRoomsBtn.setVisible(false);
        combo1.setVisible(false);
        combo2.setVisible(false);
        dateFrom.setVisible(false);
        dateTo.setVisible(false);

        combo1.setValue(null);
        combo2.setValue(null);
        combo1.getItems().clear();
        combo2.getItems().clear();
        combo1.setOnAction(null);
        combo2.setOnAction(null);
        combo1.setPromptText("");
        combo2.setPromptText("");
        dateFrom.setValue(null);
        dateTo.setValue(null);
    }



    private ObservableList<BuildingDB> loadBuildings() {
        ObservableList<BuildingDB> buildings = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "select \"Building\".\"Id_building\"\n" +
                             "from \"Building\""
             )) {

            while (rs.next()) {
                buildings.add(new BuildingDB(
                        rs.getInt("Id_building")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildings;
    }
    private ObservableList<FloorDB> loadFloorsByBuildings(int idBuilding) {
        ObservableList<FloorDB> floors = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select \"Floor\".\"Id_floor\", \"Floor\".\"Id_building\",\n" +
                     "  \"Floor\".\"Floor_number\" from \"Floor\"\n" +
                     "where \"Floor\".\"Id_building\" = ?\n" +
                     "order by \"Floor\".\"Floor_number\"")) {

            ps.setInt(1,idBuilding);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                floors.add(new FloorDB(
                        rs.getInt("Id_floor"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number")
                ));
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return floors;
    }
    private ObservableList<RoomDB> loadRoomsByBuilding(int buildingId) {
        ObservableList<RoomDB> rooms = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select r.\"Id_room\", r.\"Room\", f.\"Id_building\",\n" +
                     "  r.\"Room_type\", r.\"Price\"::numeric, f.\"Floor_number\",\n" +
                     "  case\n" +
                     "    when s.\"Id_stay\" is not null then true\n" +
                     "    else false\n" +
                     "  end as Is_busy\n" +
                     "from \"Room\" r\n" +
                     "join \"Floor\" f on f.\"Id_floor\" = r.\"Id_floor\"\n" +
                     "left join \"Stay\" s on s.\"Id_room\" = r.\"Id_room\"\n" +
                     "and CURRENT_DATE between s.\"Check_in\" and s.\"Check_out\"\n" +
                     "order by r.\"Id_room\""
             )) {

            ps.setInt(1, buildingId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rooms.add(new RoomDB(
                        rs.getInt("Id_room"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number"),
                        rs.getInt("Room"),
                        rs.getString("Room_type"),
                        rs.getBigDecimal("Price"),
                        rs.getBoolean("Is_busy")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }
    private void loadOrganizationsIntoCombo() {
        combo1.getItems().clear();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select * from \"Organiztion\"")) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                combo1.getItems().add(new OrganizationDB(
                        rs.getInt("Id_organization"),
                        rs.getString("Name"),
                        rs.getInt("Discount_rate")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadStayWithFilters() {
        ObservableList<StayDB> filtered = FXCollections.observableArrayList();

        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();

        BuildingDB b = (BuildingDB) combo1.getValue();
        FloorDB f = (FloorDB) combo2.getValue();

        for (StayDB stay : loadAllStays()) {
            boolean matches = true;

            if (b != null && stay.getBuilding() != b.getIdBuilding()) {
                matches = false;
            }

            if (f != null && stay.getFloorNumber() != f.getFloorNumber()) {
                matches = false;
            }

            if (from != null && stay.getCheckIn().isBefore(from)) {
                matches = false;
            }
            if (to != null && stay.getCheckOut().isAfter(to)) {
                matches = false;
            }

            if (matches) {
                filtered.add(stay);
            }
        }

        tableView.setItems(FXCollections.observableArrayList(filtered));
    }
    private void loadBookingWithFilters() {
        ObservableList<BookingDB> bookings = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "select b.\"Id_booking\", o.\"Name\", b.\"Hotel_class\", b.\"Floor_number\", " +
                        "b.\"Rooms_count\", b.\"People_count\", b.\"Booking_date\", b.\"Arrival_date\" " +
                        "from \"Booking\" b " +
                        "join \"Contract\" c on c.\"Id_contract\" = b.\"Id_contract\" " +
                        "join \"Organiztion\" o on o.\"Id_organization\" = c.\"Id_organization\" " +
                        "where 1=1 "
        );

        List<Object> params = new ArrayList<>();

        OrganizationDB selectedOrg = (OrganizationDB) combo1.getValue();
        if (selectedOrg != null) {
            sql.append(" and o.\"Id_organization\" = ? ");
            params.add(selectedOrg.getIdOrganization());
        }

        if (dateFrom.getValue() != null) {
            sql.append(" and b.\"Booking_date\" >= ? ");
            params.add(Date.valueOf(dateFrom.getValue()));
        }
        if (dateTo.getValue() != null) {
            sql.append(" and b.\"Booking_date\" <= ? ");
            params.add(Date.valueOf(dateTo.getValue()));
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookings.add(new BookingDB(
                        rs.getInt("Id_booking"),
                        rs.getString("Hotel_class"),
                        rs.getInt("Floor_number"),
                        rs.getInt("Rooms_count"),
                        rs.getInt("People_count"),
                        rs.getDate("Booking_date").toLocalDate(),
                        rs.getDate("Arrival_date").toLocalDate(),
                        rs.getString("Name")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tableView.setItems(FXCollections.observableArrayList(bookings));
    }

    private void loadClientsTable() {
        ObservableList<ClientDB> clients = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Cleint\".\"Id_client\", \"Cleint\".\"Surname\",\n" +
                     "  \"Cleint\".\"Name\", \"Cleint\".\"Second_name\",\n" +
                     "  \"Cleint\".\"Phone_number\"\n" +
                     "from \"Cleint\"")) {

            while (rs.next()) {
                clients.add(new ClientDB(
                        rs.getInt("Id_client"),
                        rs.getString("Surname"),
                        rs.getString("Name"),
                        rs.getString("Second_name"),
                        rs.getString("Phone_number")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tableView.setItems(FXCollections.observableArrayList(clients));
    }
    private void loadOrganizationTable(){
        ObservableList<OrganizationDB> organizations = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Organiztion\".\"Id_organization\", \"Organiztion\".\"Name\", \"Organiztion\".\"Discount_rate\"\n" +
                     "from \"Organiztion\"\n" +
                     "order by \"Organiztion\".\"Id_organization\"")) {

            while (rs.next()) {
                organizations.add(new OrganizationDB(
                        rs.getInt("Id_organization"),
                        rs.getString("Name"),
                        rs.getInt("Discount_rate")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(organizations));
    }
    private void loadContractTable(){
        ObservableList<ContractDB> contracts = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(
                "select \"Contract\".\"Id_contract\", \"Contract\".\"Id_organization\",\n" +
                        "  \"Contract\".\"Start_date\", \"Contract\".\"End_date\",\n" +
                        "  \"Organiztion\".\"Name\" from \"Contract\"\n" +
                        "join \"Organiztion\" on \"Organiztion\".\"Id_organization\" = \"Contract\".\"Id_organization\"\n" +
                        "where 1=1"
        );

        List<Object> params = new ArrayList<>();

        OrganizationDB selectedOrg = (OrganizationDB) combo1.getValue();
        if (selectedOrg != null) {
            sql.append(" and \"Contract\".\"Id_organization\" = ?");
            params.add(selectedOrg.getIdOrganization());
        }

        if (dateFrom.getValue() != null) {
            sql.append(" and \"Contract\".\"Start_date\" >= ?");
            params.add(Date.valueOf(dateFrom.getValue()));
        }

        if (dateTo.getValue() != null) {
            sql.append(" and \"Contract\".\"End_date\" <= ?");
            params.add(Date.valueOf(dateTo.getValue()));
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                contracts.add(new ContractDB(
                        rs.getInt("Id_contract"),
                        rs.getString("Name"),
                        rs.getDate("Start_date").toLocalDate(),
                        rs.getDate("End_date").toLocalDate()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(contracts));
    }
    private void loadBookingTable(){
        ObservableList<BookingDB> bookings = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Booking\".\"Id_booking\", \"Organiztion\".\"Name\",\n" +
                     "  \"Booking\".\"Hotel_class\", \"Booking\".\"Floor_number\",\n" +
                     "  \"Booking\".\"Rooms_count\", \"Booking\".\"People_count\",\n" +
                     "  \"Booking\".\"Booking_date\", \"Booking\".\"Arrival_date\"\n" +
                     "from \"Booking\"\n" +
                     "join \"Contract\" on \"Contract\".\"Id_contract\" = \"Booking\".\"Id_contract\"\n" +
                     "join \"Organiztion\" on \"Organiztion\".\"Id_organization\" = \"Contract\".\"Id_organization\"")) {

            while (rs.next()) {
                bookings.add(new BookingDB(
                        rs.getInt("Id_booking"),
                        rs.getString("Hotel_class"),
                        rs.getInt("Floor_number"),
                        rs.getInt("Rooms_count"),
                        rs.getInt("People_count"),
                        rs.getDate("Booking_date").toLocalDate(),
                        rs.getDate("Arrival_date").toLocalDate(),
                        rs.getString("Name")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(bookings));
    }
    private void loadBookingClientTable(){
        ObservableList<BookingClientDB> bookingClients = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Booking_cleint\".\"Id_booking_client\", \"Booking_cleint\".\"Id_booking\", \"Cleint\".\"Surname\"\n" +
                     "from \"Booking_cleint\"\n" +
                     "join \"Cleint\" on \"Cleint\".\"Id_client\" = \"Booking_cleint\".\"Id_client\"\n" +
                     "order by \"Booking_cleint\".\"Id_booking_client\"")) {

            while (rs.next()) {
                bookingClients.add(new BookingClientDB(
                        rs.getInt("Id_booking_client"),
                        rs.getInt("Id_booking"),
                        rs.getString("Surname")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(bookingClients));
    }
    private void loadBuildingTable(){
        ObservableList<BuildingDB> buildings = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Building\".\"Id_building\", \"Building\".\"Hotel_class\",\n" +
                     "  \"Building\".\"Floors_count\",\n" +
                     "  \"Building\".\"Floors_count\"*\"Building\".\"Rooms_per_floor\" as Total_rooms,\n" +
                     "  \"Building\".\"Rooms_per_floor\", \"Building\".\"Has_cleaning\",\n" +
                     "  \"Building\".\"Has_laundry\", \"Building\".\"Has_dry_cleaning\",\n" +
                     "  \"Building\".\"Has_food\", \"Building\".\"Has_entertainment\"\n" +
                     "from \"Building\"")) {

            while (rs.next()) {
                buildings.add(new BuildingDB(
                        rs.getInt("Id_building"),
                        rs.getString("Hotel_class"),
                        rs.getInt("Floors_count"),
                        rs.getInt("Total_rooms"),
                        rs.getInt("Rooms_per_floor"),
                        rs.getBoolean("Has_cleaning"),
                        rs.getBoolean("Has_laundry"),
                        rs.getBoolean("Has_dry_cleaning"),
                        rs.getBoolean("Has_food"),
                        rs.getBoolean("Has_entertainment")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(buildings));
    }
    private ObservableList<FloorDB> loadAllFloors() {
        ObservableList<FloorDB> floors = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Floor\".\"Id_floor\", \"Floor\".\"Id_building\",\n" +
                     "  \"Floor\".\"Floor_number\" from \"Floor\"\n" +
                     "order by \"Floor\".\"Id_building\"")) {

            while (rs.next()) {
                floors.add(new FloorDB(
                        rs.getInt("Id_floor"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return floors;
    }
    private void loadFloorTable(int idBuilding){
        ObservableList<FloorDB> floors = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select \"Floor\".\"Id_floor\", \"Floor\".\"Id_building\", \"Floor\".\"Floor_number\"\n" +
                     "from \"Floor\" where \"Floor\".\"Id_building\" = ?")) {

            ps.setInt(1, idBuilding);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                floors.add(new FloorDB(
                        rs.getInt("Id_floor"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(floors));
    }
    private ObservableList<RoomDB> loadAllRooms() {
        ObservableList<RoomDB> rooms = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select r.\"Id_room\", r.\"Room\", f.\"Id_building\",\n" +
                     "  r.\"Room_type\", r.\"Price\"::numeric, f.\"Floor_number\",\n" +
                     "  case\n" +
                     "    when s.\"Id_stay\" is not null then true\n" +
                     "    else false\n" +
                     "  end as Is_busy\n" +
                     "from \"Room\" r\n" +
                     "join \"Floor\" f on f.\"Id_floor\" = r.\"Id_floor\"\n" +
                     "left join \"Stay\" s on s.\"Id_room\" = r.\"Id_room\"\n" +
                     "and CURRENT_DATE between s.\"Check_in\" and s.\"Check_out\"\n" +
                     "order by r.\"Id_room\"")) {

            while (rs.next()) {
                rooms.add(new RoomDB(
                        rs.getInt("Id_room"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number"),
                        rs.getInt("Room"),
                        rs.getString("Room_type"),
                        rs.getBigDecimal("Price"),
                        rs.getBoolean("Is_busy")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }
    private void loadRoomTable(int idFloor){
        ObservableList<RoomDB> rooms = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select \"Room\".\"Id_room\", \"Room\".\"Room\", \"Floor\".\"Id_building\", \"Room\".\"Room_type\",\n" +
                     "  \"Room\".\"Price\"::numeric,\n" +
                     "  \"Floor\".\"Floor_number\"," +
                     "case \n" +
                     "        when s.\"Id_stay\" is not null then true\n" +
                     "        else false\n" +
                     "    end as Is_busy\n" +
                     " from \"Room\"\n" +
                     "join \"Floor\" on \"Floor\".\"Id_floor\" = \"Room\".\"Id_floor\"\n" +
                     "left join \"Stay\" s on s.\"Id_room\" = \"Room\".\"Id_room\"\n" +
                     "and CURRENT_DATE between s.\"Check_in\" and s.\"Check_out\"\n" +
                     "where \"Room\".\"Id_floor\" = ?\n" +
                     "order by \"Room\".\"Room\"")) {

            ps.setInt(1, idFloor);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rooms.add(new RoomDB(
                        rs.getInt("Id_room"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number"),
                        rs.getInt("Room"),
                        rs.getString("Room_type"),
                        rs.getBigDecimal("Price"),
                        rs.getBoolean("Is_busy")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(rooms));
    }
    private ObservableList<StayDB> loadAllStays() {
        ObservableList<StayDB> stays = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Stay\".\"Id_stay\", \"Floor\".\"Id_building\", \"Floor\".\"Floor_number\",\n" +
                     "  \"Cleint\".\"Surname\", \"Room\".\"Room\", \"Stay\".\"Check_in\",\n" +
                     "  \"Stay\".\"Check_out\" from \"Stay\"\n" +
                     "join \"Room\" on \"Room\".\"Id_room\" = \"Stay\".\"Id_room\"\n" +
                     "join \"Floor\" on \"Floor\".\"Id_floor\" = \"Room\".\"Id_floor\"\n" +
                     "join \"Cleint\" on \"Cleint\".\"Id_client\" = \"Stay\".\"Id_client\"\n" +
                     "order by \"Floor\".\"Id_building\"")) {

            while (rs.next()) {
                stays.add(new StayDB(
                        rs.getInt("Id_stay"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number"),
                        rs.getString("Surname"),
                        rs.getInt("Room"),
                        rs.getDate("Check_in").toLocalDate(),
                        rs.getDate("Check_out").toLocalDate()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stays;
    }
    private void loadStayTable(int idFloor){
        ObservableList<StayDB> stays = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select \"Stay\".\"Id_stay\", \"Floor\".\"Id_building\", \"Floor\".\"Floor_number\"," +
                     "  \"Cleint\".\"Surname\", \"Room\".\"Room\",\n" +
                     "  \"Stay\".\"Check_in\", \"Stay\".\"Check_out\"\n" +
                     "from \"Stay\"\n" +
                     "join \"Room\" on \"Room\".\"Id_room\" = \"Stay\".\"Id_room\"\n" +
                     "join \"Floor\" on \"Floor\".\"Id_floor\" = \"Room\".\"Id_floor\"\n" +
                     "join \"Cleint\" on \"Cleint\".\"Id_client\" = \"Stay\".\"Id_client\"\n" +
                     "where \"Floor\".\"Id_floor\" = ?\n" +
                     "order by \"Room\".\"Room\"")) {

            ps.setInt(1, idFloor);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                stays.add(new StayDB(
                        rs.getInt("Id_stay"),
                        rs.getInt("Id_building"),
                        rs.getInt("Floor_number"),
                        rs.getString("Surname"),
                        rs.getInt("Room"),
                        rs.getDate("Check_in").toLocalDate(),
                        rs.getDate("Check_out").toLocalDate()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(stays));
    }
    private ObservableList<StayServiceDB> loadAllStayServices() {
        ObservableList<StayServiceDB> stayServices = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Stay_service\".\"Id_stay_service\", \"Floor\".\"Id_building\", \"Room\".\"Room\",\n" +
                     "  \"Service\".\"Name\", \"Stay_service\".\"Quantity\",\n" +
                     "  \"Stay_service\".\"Quantity\"*\"Service\".\"Price\"::numeric as Total_price\n" +
                     "from \"Stay_service\"\n" +
                     "join \"Stay\" on \"Stay\".\"Id_stay\" = \"Stay_service\".\"Id_stay\"\n" +
                     "join \"Room\" on \"Room\".\"Id_room\" = \"Stay\".\"Id_room\"\n" +
                     "join \"Floor\" on \"Floor\".\"Id_floor\" = \"Room\".\"Id_floor\"\n" +
                     "join \"Service\" on \"Service\".\"Id_service\" = \"Stay_service\".\"Id_service\"\n" +
                     "order by \"Floor\".\"Id_building\"")) {

            while (rs.next()) {
                stayServices.add(new StayServiceDB(
                        rs.getInt("Id_stay_service"),
                        rs.getInt("Id_building"),
                        rs.getInt("Room"),
                        rs.getString("Name"),
                        rs.getInt("Quantity"),
                        rs.getBigDecimal("Total_price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stayServices;
    }
    private void loadStayServiceTable(int idRoom){
        ObservableList<StayServiceDB> stayServices = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select \"Stay_service\".\"Id_stay_service\", \"Floor\".\"Id_building\", \"Room\".\"Room\", \"Service\".\"Name\",\n" +
                     "  \"Stay_service\".\"Quantity\",\n" +
                     "  \"Stay_service\".\"Quantity\" * \"Service\".\"Price\"::numeric as Total_price\n" +
                     "from \"Stay_service\"\n" +
                     "join \"Stay\" on \"Stay\".\"Id_stay\" = \"Stay_service\".\"Id_stay\"\n" +
                     "join \"Room\" on \"Room\".\"Id_room\" = \"Stay\".\"Id_room\"\n" +
                     "join \"Floor\" on \"Floor\".\"Id_floor\" = \"Room\".\"Id_floor\"\n" +
                     "join \"Service\" on \"Service\".\"Id_service\" = \"Stay_service\".\"Id_service\"\n" +
                     "where \"Room\".\"Id_room\" = ?")) {

            ps.setInt(1, idRoom);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                stayServices.add(new StayServiceDB(
                        rs.getInt("Id_stay_service"),
                        rs.getInt("Id_building"),
                        rs.getInt("Room"),
                        rs.getString("Name"),
                        rs.getInt("Quantity"),
                        rs.getBigDecimal("Total_price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(stayServices));
    }
    private void loadServiceTable() {
        ObservableList<ServiceDB> services = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Service\".\"Id_service\", \"Service\".\"Name\",\n" +
                     "  \"Service\".\"Price\"::numeric\n" +
                     "from \"Service\"")) {

            while (rs.next()) {
                services.add(new ServiceDB(
                        rs.getInt("Id_service"),
                        rs.getString("Name"),
                        rs.getBigDecimal("Price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(services));
    }



    @FXML public void onClientClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.CLIENT;
        resetTable();
        tableTitle.setText("Клиенты");

        TableColumn<Object, Integer> idClientColumn = new TableColumn<>("Номер клиента");
        idClientColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        idClientColumn.setPrefWidth(200);

        TableColumn<Object, String> surnameColumn = new TableColumn<>("Фамилия");
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        surnameColumn.setPrefWidth(200);

        TableColumn<Object, String> nameColumn = new TableColumn<>("Имя");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        TableColumn<Object, String> secondNameColumn = new TableColumn<>("Отчество");
        secondNameColumn.setCellValueFactory(new PropertyValueFactory<>("secondName"));
        secondNameColumn.setPrefWidth(200);

        TableColumn<Object, String> phoneNumberColumn = new TableColumn<>("Номер телефона");
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneNumberColumn.setPrefWidth(200);

        tableView.getColumns().setAll(idClientColumn, surnameColumn, nameColumn, secondNameColumn, phoneNumberColumn);

        tableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ClientDB client = (ClientDB) row.getItem();
                    showClientInfo(client);
                }
            });
            return row;
        });

        loadClientsTable();
    }
    @FXML public void onOrganizationClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.ORGANIZATION;
        resetTable();
        tableTitle.setText("Организации");

        TableColumn<Object, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        TableColumn<Object, Integer> discountRateColumn = new TableColumn<>("Скидка");
        discountRateColumn.setCellValueFactory(new PropertyValueFactory<>("discountRate"));
        discountRateColumn.setPrefWidth(200);

        tableView.getColumns().setAll(nameColumn, discountRateColumn);

        loadOrganizationTable();
    }
    @FXML public void onContractClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.CONTRACT;
        resetTable();
        tableTitle.setText("Контракты");

        clearFilterBtn.setVisible(true);

        combo1.setPromptText("Введите название");

        combo1.setVisible(true);
        dateFrom.setVisible(true);
        dateTo.setVisible(true);

        loadOrganizationsIntoCombo();

        combo1.valueProperty().addListener((obs, oldV, newV) -> loadContractTable());
        dateFrom.valueProperty().addListener((obs, oldV, newV) -> loadContractTable());
        dateTo.valueProperty().addListener((obs, oldV, newV) -> loadContractTable());


        TableColumn<Object, String> idOrganizationColumn = new TableColumn<>("Организация");
        idOrganizationColumn.setCellValueFactory(new PropertyValueFactory<>("nameOrganization"));
        idOrganizationColumn.setPrefWidth(200);

        TableColumn<Object, LocalDate> startDateColumn = new TableColumn<>("Дата подписания");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setPrefWidth(200);

        TableColumn<Object, LocalDate> endDateColumn = new TableColumn<>("Дата окончания");
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateColumn.setPrefWidth(200);

        tableView.getColumns().setAll(idOrganizationColumn, startDateColumn, endDateColumn);

        loadContractTable();
    }
    @FXML public void onBookingClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.BOOKING;
        resetTable();
        tableTitle.setText("Бронирования");

        clearFilterBtn.setVisible(true);
        combo1.setVisible(true);
        dateFrom.setVisible(true);
        dateTo.setVisible(true);

        combo1.setPromptText("Выберите организацию");

        loadOrganizationsIntoCombo();

        combo1.valueProperty().addListener((obs, oldV, newV) -> loadBookingWithFilters());
        dateFrom.valueProperty().addListener((obs, oldV, newV) -> loadBookingWithFilters());
        dateTo.valueProperty().addListener((obs, oldV, newV) -> loadBookingWithFilters());

        TableColumn<Object, Integer> idContractColumn = new TableColumn<>("Организация");
        idContractColumn.setCellValueFactory(new PropertyValueFactory<>("orgName"));
        idContractColumn.setPrefWidth(150);

        TableColumn<Object, String> hotelClassColumn = new TableColumn<>("Класс отеля");
        hotelClassColumn.setCellValueFactory(new PropertyValueFactory<>("hotelClass"));
        hotelClassColumn.setPrefWidth(150);

        TableColumn<Object, Integer> floorNumberColumn = new TableColumn<>("Номер этажа");
        floorNumberColumn.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        floorNumberColumn.setPrefWidth(150);

        TableColumn<Object, Integer> roomsCountColumn = new TableColumn<>("Количество комнат");
        roomsCountColumn.setCellValueFactory(new PropertyValueFactory<>("roomsCount"));
        roomsCountColumn.setPrefWidth(150);

        TableColumn<Object, Integer> peopleCountColumn = new TableColumn<>("Количество людей");
        peopleCountColumn.setCellValueFactory(new PropertyValueFactory<>("peopleCount"));
        peopleCountColumn.setPrefWidth(150);

        TableColumn<Object, LocalDate> bookingDateColumn = new TableColumn<>("Дата бронирования");
        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        bookingDateColumn.setPrefWidth(150);

        TableColumn<Object, LocalDate> arrivalDateColumn = new TableColumn<>("Дата заселения");
        arrivalDateColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalDate"));
        arrivalDateColumn.setPrefWidth(150);

        tableView.getColumns().setAll(idContractColumn, hotelClassColumn, floorNumberColumn,
                roomsCountColumn, peopleCountColumn, bookingDateColumn, arrivalDateColumn);

        loadBookingTable();
    }
    @FXML public void onBookingClientClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.BOOKING_CLIENT;
        resetTable();
        tableTitle.setText("Бронь-клиенты");

        TableColumn<Object, Integer> idBookingClientColumn = new TableColumn<>("Номер бронь-клиента");
        idBookingClientColumn.setCellValueFactory(new PropertyValueFactory<>("idBookingClient"));
        idBookingClientColumn.setPrefWidth(200);

        TableColumn<Object, Integer> idBookingColumn = new TableColumn<>("Номер бронирования");
        idBookingColumn.setCellValueFactory(new PropertyValueFactory<>("idBooking"));
        idBookingColumn.setPrefWidth(200);

        TableColumn<Object, String> idClientColumn = new TableColumn<>("Фамилия клиента");
        idClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientSurname"));
        idClientColumn.setPrefWidth(200);

        tableView.getColumns().setAll(idBookingClientColumn, idBookingColumn, idClientColumn);

        loadBookingClientTable();
    }
    @FXML public void onBuildingClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.BUILDING;
        resetTable();
        tableTitle.setText("Здания");

        TableColumn<Object, Integer> idBuildingColumn = new TableColumn<>("Номер здания");
        idBuildingColumn.setCellValueFactory(new PropertyValueFactory<>("idBuilding"));
        idBuildingColumn.setPrefWidth(100);

        TableColumn<Object, String> hotelClassColumn = new TableColumn<>("Класс отеля");
        hotelClassColumn.setCellValueFactory(new PropertyValueFactory<>("hotelClass"));
        hotelClassColumn.setPrefWidth(100);

        TableColumn<Object, Integer> floorsCountColumn = new TableColumn<>("Количество этажей");
        floorsCountColumn.setCellValueFactory(new PropertyValueFactory<>("floorsCount"));
        floorsCountColumn.setPrefWidth(100);

        TableColumn<Object, Integer> totalRoomsColumn = new TableColumn<>("Всего комнат");
        totalRoomsColumn.setCellValueFactory(new PropertyValueFactory<>("totalRooms"));
        totalRoomsColumn.setPrefWidth(100);

        TableColumn<Object, Integer> roomsPerFloorColumn = new TableColumn<>("Комнат на этаже");
        roomsPerFloorColumn.setCellValueFactory(new PropertyValueFactory<>("roomsPerFloor"));
        roomsPerFloorColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> hasCleaningColumn = new TableColumn<>("Уборка");
        hasCleaningColumn.setCellValueFactory(new PropertyValueFactory<>("hasCleaning"));
        hasCleaningColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> hasLaundryColumn = new TableColumn<>("Прачечная");
        hasLaundryColumn.setCellValueFactory(new PropertyValueFactory<>("hasLaundry"));
        hasLaundryColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> hasDryCleaningColumn = new TableColumn<>("Химчистка");
        hasDryCleaningColumn.setCellValueFactory(new PropertyValueFactory<>("hasDryCleaning"));
        hasDryCleaningColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> hasFoodClientColumn = new TableColumn<>("Ресторан");
        hasFoodClientColumn.setCellValueFactory(new PropertyValueFactory<>("hasFood"));
        hasFoodClientColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> hasEntertainmentColumn = new TableColumn<>("Развлечения");
        hasEntertainmentColumn.setCellValueFactory(new PropertyValueFactory<>("hasEntertainment"));
        hasEntertainmentColumn.setPrefWidth(100);

        tableView.getColumns().setAll(idBuildingColumn, hotelClassColumn, floorsCountColumn,
                totalRoomsColumn, roomsPerFloorColumn, hasCleaningColumn, hasLaundryColumn,
                hasDryCleaningColumn, hasFoodClientColumn, hasEntertainmentColumn);

        loadBuildingTable();
    }
    @FXML public void onFloorClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.FLOOR;
        resetTable();
        tableTitle.setText("Этажи");

        clearFilterBtn.setVisible(true);
        combo1.setVisible(true);
        combo2.setVisible(false);

        combo1.setPromptText("Выберите здание");
        tableView.setItems(FXCollections.observableArrayList(loadAllFloors()));
        combo1.setItems((ObservableList<Object>)(ObservableList<?>) loadBuildings());

        combo1.setOnAction(e -> {
            BuildingDB b = (BuildingDB) combo1.getValue();
            if (b != null) {
                loadFloorTable(b.getIdBuilding());

                ObservableList<FloorDB> filtered = FXCollections.observableArrayList();
                for (FloorDB floor : loadAllFloors()) {
                    if (floor.getIdBuilding() == b.getIdBuilding()) {
                        filtered.add(floor);
                    }
                }
                tableView.setItems(FXCollections.observableArrayList(filtered));
            }
            else {
                tableView.setItems(FXCollections.observableArrayList(loadAllFloors()));
            }
        });

        TableColumn<Object, Integer> idBuildingColumn = new TableColumn<>("Номер здания");
        idBuildingColumn.setCellValueFactory(new PropertyValueFactory<>("idBuilding"));
        idBuildingColumn.setPrefWidth(200);

        TableColumn<Object, Integer> floorNumberColumn = new TableColumn<>("Номер этажа");
        floorNumberColumn.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        floorNumberColumn.setPrefWidth(200);

        tableView.getColumns().setAll(idBuildingColumn, floorNumberColumn);
    }
    @FXML public void onRoomClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.ROOM;
        resetTable();
        tableTitle.setText("Комнаты");

        clearFilterBtn.setVisible(true);
        freeRoomsBtn.setVisible(true);
        allRoomsBtn.setVisible(true);
        combo1.setVisible(true);
        combo2.setVisible(true);

        combo1.setPromptText("Введите здание");
        combo2.setPromptText("Введите этаж");

        tableView.setItems(FXCollections.observableArrayList(loadAllRooms()));

        combo1.setItems((ObservableList<Object>)(ObservableList<?>) loadBuildings());

        combo1.setOnAction(e-> {
            BuildingDB b = (BuildingDB) combo1.getValue();
            if (b != null) {
                combo2.setItems((ObservableList<Object>)(ObservableList<?>) loadFloorsByBuildings(b.getIdBuilding()));

                ObservableList<RoomDB> filtered = FXCollections.observableArrayList();
                for (RoomDB room : loadAllRooms()) {
                    if (room.getBuilding() == b.getIdBuilding()) {
                        filtered.add(room);
                    }
                }
                tableView.setItems(FXCollections.observableArrayList(filtered));
            }
            else {
                tableView.setItems(FXCollections.observableArrayList(loadAllRooms()));
            }
        });

        combo2.setOnAction(e-> {
            FloorDB f = (FloorDB) combo2.getValue();
            if (f != null) loadRoomTable(f.getIdFloor());
        });

        TableColumn<Object, Integer> buildingColumn = new TableColumn<>("Номер здания");
        buildingColumn.setCellValueFactory(new PropertyValueFactory<>("building"));
        buildingColumn.setPrefWidth(100);

        TableColumn<Object, Integer> floorColumn = new TableColumn<>("Номер этажа");
        floorColumn.setCellValueFactory(new PropertyValueFactory<>("floor"));
        floorColumn.setPrefWidth(100);

        TableColumn<Object, Integer> roomColumn = new TableColumn<>("Номер комнаты");
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));
        roomColumn.setPrefWidth(200);

        TableColumn<Object, String> roomTypeColumn = new TableColumn<>("Тип комнаты");
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        roomTypeColumn.setPrefWidth(200);

        TableColumn<Object, BigDecimal> priceColumn = new TableColumn<>("Цена");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(200);

        TableColumn<Object, Boolean> isBusyColumn = new TableColumn<>("Занят");
        isBusyColumn.setCellValueFactory(new PropertyValueFactory<>("isBusy"));
        isBusyColumn.setPrefWidth(200);

        tableView.getColumns().setAll(buildingColumn, floorColumn, roomColumn, roomTypeColumn, priceColumn, isBusyColumn);

        tableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    roomInfoBtn.setVisible(true);
                }
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    RoomDB room = (RoomDB) row.getItem();
                    showRoomStatus(room);
                }
            });
            return row;
        });
    }
    @FXML public void onStayClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.STAY;
        resetTable();
        tableTitle.setText("Занятые комнаты");

        clearFilterBtn.setVisible(true);
        combo1.setVisible(true);
        combo2.setVisible(true);
        dateFrom.setVisible(true);
        dateTo.setVisible(true);

        dateFrom.valueProperty().addListener((obs, oldV, newV) -> loadStayWithFilters());
        dateTo.valueProperty().addListener((obs, oldV, newV) -> loadStayWithFilters());

        combo1.setPromptText("Выберите здание");
        combo2.setPromptText("Выберите этаж");

        tableView.setItems(FXCollections.observableArrayList(loadAllStays()));

        combo1.setItems((ObservableList<Object>)(ObservableList<?>) loadBuildings());

        combo1.setOnAction(e-> {
            BuildingDB b = (BuildingDB) combo1.getValue();
            if (b != null) {
                combo2.setItems((ObservableList<Object>)(ObservableList<?>) loadFloorsByBuildings(b.getIdBuilding()));

                ObservableList<StayDB> filtered = FXCollections.observableArrayList();
                for (StayDB stay : loadAllStays()) {
                    if (stay.getBuilding() == b.getIdBuilding()) {
                        filtered.add(stay);
                    }
                }
                tableView.setItems(FXCollections.observableArrayList(filtered));
            }
            else {
                tableView.setItems(FXCollections.observableArrayList(loadAllRooms()));
            }
        });

        combo2.setOnAction(e-> {
            FloorDB f = (FloorDB) combo2.getValue();
            if (f != null) loadStayTable(f.getIdFloor());
        });

        TableColumn<Object, String> buildingColumn = new TableColumn<>("Номер здания");
        buildingColumn.setCellValueFactory(new PropertyValueFactory<>("building"));
        buildingColumn.setPrefWidth(200);

        TableColumn<Object, String> floorNumberColumn = new TableColumn<>("Этаж");
        floorNumberColumn.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        floorNumberColumn.setPrefWidth(200);

        TableColumn<Object, String> clientSurnameColumn = new TableColumn<>("Фамилия клиента");
        clientSurnameColumn.setCellValueFactory(new PropertyValueFactory<>("clientSurname"));
        clientSurnameColumn.setPrefWidth(200);

        TableColumn<Object, Integer> numberRoomColumn = new TableColumn<>("Номер комнаты");
        numberRoomColumn.setCellValueFactory(new PropertyValueFactory<>("numberRoom"));
        numberRoomColumn.setPrefWidth(200);

        TableColumn<Object, LocalDate> checkInColumn = new TableColumn<>("Дата заселения");
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkInColumn.setPrefWidth(200);

        TableColumn<Object, LocalDate> checkOutColumn = new TableColumn<>("Дата выселения");
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        checkOutColumn.setPrefWidth(200);

        tableView.getColumns().setAll(buildingColumn, floorNumberColumn, clientSurnameColumn, numberRoomColumn, checkInColumn, checkOutColumn);

        tableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    StayDB stay = (StayDB) row.getItem();
                    showStayService(stay);
                }
            });
            return row;
        });
    }
    @FXML public void onStayServiceClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.STAY_SERVICE;
        resetTable();
        tableTitle.setText("Услуги в комнатах");

        clearFilterBtn.setVisible(true);
        combo1.setVisible(true);
        combo2.setVisible(true);

        combo1.setPromptText("Выберите здание");
        combo2.setPromptText("Выберите комнату");

        tableView.setItems(FXCollections.observableArrayList(loadAllStayServices()));

        combo1.setItems((ObservableList<Object>)(ObservableList<?>) loadBuildings());

        combo1.setOnAction(e -> {
            BuildingDB b = (BuildingDB) combo1.getValue();
            if (b != null) {
                combo2.setItems((ObservableList<Object>)(ObservableList<?>) loadRoomsByBuilding(b.getIdBuilding()));

                ObservableList<StayServiceDB> filtered = FXCollections.observableArrayList();
                for (StayServiceDB stayServices : loadAllStayServices()) {
                    if (stayServices.getBuilding() == b.getIdBuilding()) {
                        filtered.add(stayServices);
                    }
                }
                tableView.setItems(FXCollections.observableArrayList(filtered));
            }
            else {
                tableView.setItems(FXCollections.observableArrayList(loadAllRooms()));
            }
        });

        combo2.setOnAction(e -> {
            RoomDB r = (RoomDB) combo2.getValue();
            if (r != null) { loadStayServiceTable(r.getIdRoom()); }
        });

        TableColumn<Object, Integer> buildingColumn = new TableColumn<>("Номер здания");
        buildingColumn.setCellValueFactory(new PropertyValueFactory<>("building"));
        buildingColumn.setPrefWidth(200);

        TableColumn<Object, Integer> idStayColumn = new TableColumn<>("Номер занятой комнаты");
        idStayColumn.setCellValueFactory(new PropertyValueFactory<>("idStay"));
        idStayColumn.setPrefWidth(200);

        TableColumn<Object, String> nameServiceColumn = new TableColumn<>("Название услуги");
        nameServiceColumn.setCellValueFactory(new PropertyValueFactory<>("nameService"));
        nameServiceColumn.setPrefWidth(200);

        TableColumn<Object, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(200);

        TableColumn<Object, BigDecimal> totalPriceColumn = new TableColumn<>("Итоговая стоимость");
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalPriceColumn.setPrefWidth(200);

        tableView.getColumns().setAll(buildingColumn, idStayColumn, nameServiceColumn, quantityColumn, totalPriceColumn);
    }
    @FXML public void onServiceClick(ActionEvent actionEvent) {
        activeTable = ActiveTable.SERVICE;
        resetTable();
        tableTitle.setText("Услуги");

        TableColumn<Object, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        TableColumn<Object, BigDecimal> priceColumn = new TableColumn<>("Стоимость");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(200);

        tableView.getColumns().setAll(nameColumn, priceColumn);

        loadServiceTable();
    }

    @FXML public void onFreeRoomsClick(ActionEvent actionEvent) {
        ObservableList<Object> allRooms = tableView.getItems();
        ObservableList<Object> freeRooms = FXCollections.observableArrayList();

        for (Object item : allRooms) {
            if (item instanceof RoomDB room && !room.getIsBusy()) {
                freeRooms.add(room);
            }
        }

        tableView.setItems(freeRooms);
    }
    @FXML public void onAllRoomsClick(ActionEvent actionEvent) {
        onRoomClick(actionEvent);
    }
    @FXML public void onRoomInfoClick(ActionEvent actionEvent) {
        RoomDB selectedRoom = (RoomDB) tableView.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            new Alert(Alert.AlertType.WARNING, "Сначала выберите комнату!").showAndWait();
            return;
        }

        StringBuilder info = new StringBuilder("История занятости комнаты №" + selectedRoom.getRoom() + ":\n\n");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select c.\"Surname\", s.\"Check_in\", s.\"Check_out\"\n" +
                     "from \"Stay\" s\n" +
                     "join \"Cleint\" c on c.\"Id_client\" = s.\"Id_client\"\n" +
                     "where s.\"Id_room\" = ?\n" +
                     "order by s.\"Check_in\" asc")) {
            ps.setInt(1, selectedRoom.getIdRoom());
            ResultSet rs = ps.executeQuery();

            boolean hasRecords = false;
            while(rs.next()) {
                hasRecords = true;
                String surname = rs.getString("Surname");
                LocalDate checkIn = rs.getDate("Check_in").toLocalDate();
                LocalDate checkOut = rs.getDate("Check_out").toLocalDate();

                info.append("- ").append(surname)
                        .append(": с ").append(checkIn)
                        .append(" по ").append(checkOut)
                        .append("\n");
            }

            if (!hasRecords) {
                info.append("Комната никогда не была занята");
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки данных: " + e.getMessage()).showAndWait();
            return;
        }
        new Alert(Alert.AlertType.INFORMATION, info.toString()).showAndWait();
    }
    @FXML public void onClearClick(ActionEvent actionEvent) {
        switch (activeTable) {
            case CLIENT -> onClientClick(actionEvent);
            case ORGANIZATION -> onOrganizationClick(actionEvent);
            case CONTRACT -> onContractClick(actionEvent);
            case BOOKING -> onBookingClick(actionEvent);
            case BOOKING_CLIENT -> onBookingClientClick(actionEvent);
            case BUILDING -> onBuildingClick(actionEvent);
            case FLOOR -> onFloorClick(actionEvent);
            case ROOM -> onRoomClick(actionEvent);
            case STAY -> onStayClick(actionEvent);
            case STAY_SERVICE -> onStayServiceClick(actionEvent);
            case SERVICE -> onServiceClick(actionEvent);
        }
    }
    @FXML public void onAddClick(ActionEvent actionEvent) {
        switch (activeTable) {
            case CLIENT -> addClient();
            case ORGANIZATION -> addOrganization();
            case CONTRACT -> addContract();
            case BOOKING -> addBooking();
            case BOOKING_CLIENT -> addBookingClient();
            case BUILDING -> addBuilding();
            case FLOOR -> addFloor();
            case ROOM -> addRoom();
            case STAY -> addStay();
            case STAY_SERVICE -> addStayService();
            case SERVICE -> addService();
        }
    }
    @FXML public void onDeleteClick(ActionEvent actionEvent) {
        switch (activeTable) {
            case CLIENT -> deleteClient();
            case ORGANIZATION -> deleteOrganization();
            case CONTRACT -> deleteContract();
            case BOOKING -> deleteBooking();
            case BOOKING_CLIENT -> deleteBookingClient();
            case BUILDING -> deleteBuilding();
            case FLOOR -> deleteFloor();
            case ROOM -> deleteRoom();
            case STAY -> deleteStay();
            case STAY_SERVICE -> deleteStayService();
            case SERVICE -> deleteService();
        }
    }
    @FXML private void onVisitsClick(ActionEvent actionEvent) {
        int totalRooms = 0;
        int busyRooms = 0;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select count(*) as total from \"Room\"")) {
            if (rs.next()) {
                totalRooms = rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select count(distinct \"Id_room\") as busy " +
                     "from \"Stay\" " +
                     "where current_date between \"Check_in\" and \"Check_out\"")) {
            if (rs.next()) {
                busyRooms = rs.getInt("busy");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int freeRooms = totalRooms - busyRooms;

        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Занятые", busyRooms),
                        new PieChart.Data("Свободные", freeRooms)
                );

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Соотношение занятых и свободных комнат");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);

        Stage stage = new Stage();
        stage.setTitle("Посещения");
        stage.initModality(Modality.APPLICATION_MODAL); // модальное окно
        stage.setScene(new Scene(new StackPane(chart), 400, 300));
        stage.showAndWait();
    }
    @FXML  private void onTopClientsClick(ActionEvent actionEvent) {
        StringBuilder info = new StringBuilder("Топ клиентов:\n\n");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "select c.\"Surname\", c.\"Name\", count(s.\"Id_stay\") as visits " +
                             "from \"Cleint\" c " +
                             "join \"Stay\" s on c.\"Id_client\" = s.\"Id_client\" " +
                             "group by c.\"Surname\", c.\"Name\" " +
                             "order by visits desc " +
                             "limit 10"
             )) {
            ResultSet rs = ps.executeQuery();

            int rank = 1;
            while (rs.next()) {
                String surname = rs.getString("Surname");
                String name = rs.getString("Name");
                int visits = rs.getInt("visits");

                info.append(rank).append(") ")
                        .append(surname).append(" ").append(name)
                        .append(" — ").append(visits).append(" посещений\n");
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки топа клиентов: " + e.getMessage()).showAndWait();
            return;
        }

        new Alert(Alert.AlertType.INFORMATION, info.toString()).showAndWait();
    }




    private void addClient() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Добавить клиента");
        dlg.setContentText("Введите ФИО и телефон через запятую:");
        Optional<String> result = dlg.showAndWait();

        result.ifPresent(input -> {
            try {
                String[] parts =    input.split(",");
                String surname =    parts[0].trim();
                String name =       parts[1].trim();
                String secondName = parts[2].trim();
                String phone =      parts[3].trim();

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement saveIndex = conn.prepareStatement(
                             "select setval('\"Cleint_Id_client_seq\"', (select coalesce(max(\"Id_client\"),0) from \"Cleint\"))");
                     PreparedStatement ps = conn.prepareStatement("insert into \"Cleint\" \n" +
                             "  (\"Surname\", \"Name\", \"Second_name\", \"Phone_number\")\n" +
                             "  values (?, ?, ?, ?)")) {
                    saveIndex.execute();
                    ps.setString(1, surname);
                    ps.setString(2, name);
                    ps.setString(3, secondName);
                    ps.setString(4, phone);
                    ps.executeUpdate();
                }
                loadClientsTable();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addOrganization() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Добавить организацию");
        dlg.setContentText("Введите название организации и скидку через запятую:");
        Optional<String> result = dlg.showAndWait();

        result.ifPresent(input -> {
            try {
                String[] parts =    input.split(",");
                String name = parts[0].trim();
                int discountRate = Integer.parseInt(parts[1].trim());

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement saveIndex = conn.prepareStatement(
                             "select setval('\"Organiztion_Id_organization_seq\"', (select coalesce(max(\"Id_organization\"),0) from \"Organiztion\"))");
                     PreparedStatement ps = conn.prepareStatement("insert into \"Organiztion\" (\"Name\", \"Discount_rate\") values(?, ?)")) {
                    saveIndex.execute();
                    ps.setString(1, name);
                    ps.setInt(2, discountRate);
                    ps.executeUpdate();
                }
                loadOrganizationTable();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addContract() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Добавить контракт");
        dlg.setContentText("Введите название организации, дату начала и дату окончания запятую:");
        Optional<String> result = dlg.showAndWait();

        result.ifPresent(input -> {
            try {
                String[] parts =    input.split(",");
                String name = parts[0].trim();
                LocalDate startDate = LocalDate.parse(parts[1].trim());
                LocalDate endDate = LocalDate.parse(parts[2].trim());

                try(Connection conn = DBConnection.getConnection()) {
                    int idOrganization = -1;
                    try(PreparedStatement psOrg = conn.prepareStatement("select \"Organiztion\".\"Id_organization\" from \"Organiztion\"\n" +
                            "where \"Organiztion\".\"Name\" = ?")) {
                        psOrg.setString(1, name);
                        ResultSet rs = psOrg.executeQuery();
                        if (rs.next()) {
                            idOrganization = rs.getInt("Id_organization");
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Организация не найдена!");
                            alert.show();
                            return;
                        }
                    }

                    try (PreparedStatement saveIndex = conn.prepareStatement(
                            "select setval('\"Contract_Id_contract_seq\"', (select coalesce(max(\"Id_contract\"),0) from \"Contract\"))");
                         PreparedStatement ps = conn.prepareStatement("insert into \"Contract\" (\"Id_organization\",\"Start_date\", \"End_date\") values (?, ?, ?)")) {
                        saveIndex.execute();
                        ps.setInt(1, idOrganization);
                        ps.setDate(2, Date.valueOf(startDate));
                        ps.setDate(3, Date.valueOf(endDate));
                        ps.executeUpdate();
                    }

                }
                loadContractTable();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addBooking() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Добавить бронирование");
        dlg.setContentText("Введите номер контракта, класс отеля, номер этажа, количество комнат, количество людей, дату бронирования и дату заселения через запятую:");
        Optional<String> result = dlg.showAndWait();

        result.ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                int idContract = Integer.parseInt(parts[0].trim());
                String hotelClass = parts[1].trim();
                int floorNumber = Integer.parseInt(parts[2].trim());
                int roomsCount = Integer.parseInt(parts[3].trim());
                int peopleCount = Integer.parseInt(parts[4].trim());
                LocalDate bookingDate = LocalDate.parse(parts[5].trim());
                LocalDate arrivalDate = LocalDate.parse(parts[6].trim());

                try (Connection conn = DBConnection.getConnection()) {
                    try (PreparedStatement saveIndex = conn.prepareStatement(
                            "select setval('\"Booking_Id_booking_seq\"', (select coalesce(max(\"Id_booking\"),0) from \"Booking\"))")) {
                        saveIndex.execute();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "insert into \"Booking\" (\"Id_contract\", \"Hotel_class\", \"Floor_number\", \"Rooms_count\", \"People_count\", \"Booking_date\", \"Arrival_date\") " +
                                    "values (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, idContract);
                        ps.setString(2, hotelClass);
                        ps.setInt(3, floorNumber);
                        ps.setInt(4, roomsCount);
                        ps.setInt(5, peopleCount);
                        ps.setDate(6, Date.valueOf(bookingDate));
                        ps.setDate(7, Date.valueOf(arrivalDate));
                        ps.executeUpdate();
                    }
                }
                loadBookingTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addBookingClient() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Добавить бронь-клиента");
        dlg.setContentText("Введите номер бронирования и фамилию клиента через запятую:");
        Optional<String> result = dlg.showAndWait();

        result.ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                int idBooking = Integer.parseInt(parts[0].trim());
                String clientSurname = parts[1].trim();

                try (Connection conn = DBConnection.getConnection()) {
                    // 1. Находим Id_client по фамилии
                    int idClient = -1;
                    try (PreparedStatement psClient = conn.prepareStatement(
                            "select \"Id_client\" from \"Cleint\" where \"Surname\" = ?")) {
                        psClient.setString(1, clientSurname);
                        ResultSet rs = psClient.executeQuery();
                        if (rs.next()) {
                            idClient = rs.getInt("Id_client");
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Клиент с такой фамилией не найден!");
                            alert.show();
                            return;
                        }
                    }

                    // 2. Синхронизируем sequence
                    try (PreparedStatement pS = conn.prepareStatement(
                            "select setval('\"Booking_cleint_Id_booking_client_seq\"', (select coalesce(max(\"Id_booking_client\"),0) from \"Booking_cleint\"))")) {
                        pS.execute();
                    }

                    // 3. Добавляем запись
                    try (PreparedStatement ps = conn.prepareStatement(
                            "insert into \"Booking_cleint\" (\"Id_booking\", \"Id_client\") values (?, ?)")) {
                        ps.setInt(1, idBooking);
                        ps.setInt(2, idClient);
                        ps.executeUpdate();
                    }
                }

                // 4. Обновляем таблицу
                loadBookingClientTable();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addBuilding() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Добавить здание");
        dlg.setContentText("Введите класс отеля, количество этажей, количество комнат на этаже, уборка(true/false), прачечная(true/false), химчистка(true/false), ресторан(true/false), развлечения(true/false) через запятую:");
        Optional<String> result = dlg.showAndWait();

        result.ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                String hotelClass = parts[0].trim();
                int floorsCount = Integer.parseInt(parts[1].trim());
                int roomsPerFloor = Integer.parseInt(parts[2].trim());
                boolean hasCleaning = Boolean.parseBoolean(parts[3].trim());
                boolean hasLaundry = Boolean.parseBoolean(parts[4].trim());
                boolean hasDryCleaning = Boolean.parseBoolean(parts[5].trim());
                boolean hasFood = Boolean.parseBoolean(parts[6].trim());
                boolean hasEntertainment = Boolean.parseBoolean(parts[7].trim());

                try (Connection conn = DBConnection.getConnection()) {
                    try (PreparedStatement pS = conn.prepareStatement(
                            "select setval('\"Building_Id_building_seq\"', (select coalesce(max(\"Id_building\"),0) from \"Building\"))")) {
                        pS.execute();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "insert into \"Building\" (\"Hotel_class\", \"Floors_count\", \"Rooms_per_floor\", \"Has_cleaning\", \"Has_laundry\", \"Has_dry_cleaning\", \"Has_food\", \"Has_entertainment\") " +
                                    "values (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, hotelClass);
                        ps.setInt(2, floorsCount);
                        ps.setInt(3, roomsPerFloor);
                        ps.setBoolean(4, hasCleaning);
                        ps.setBoolean(5, hasLaundry);
                        ps.setBoolean(6, hasDryCleaning);
                        ps.setBoolean(7, hasFood);
                        ps.setBoolean(8, hasEntertainment);
                        ps.executeUpdate();
                    }
                }

                loadBuildingTable();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void addFloor() {
        BuildingDB selectedBuilding = (BuildingDB) combo1.getValue();
        int idBuilding;

        if (selectedBuilding != null) {
            idBuilding = selectedBuilding.getIdBuilding();
        } else {
            TextInputDialog dlgBuilding = new TextInputDialog();
            dlgBuilding.setHeaderText("Добавить этаж");
            dlgBuilding.setContentText("Введите Id здания:");
            Optional<String> buildingResult = dlgBuilding.showAndWait();

            if (buildingResult.isEmpty()) {
                return;
            }

            try {
                idBuilding = Integer.parseInt(buildingResult.get().trim());
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Неверный формат Id здания!");
                alert.show();
                return;
            }
        }

        TextInputDialog dlgFloor = new TextInputDialog();
        dlgFloor.setHeaderText("Добавить этаж");
        dlgFloor.setContentText("Введите номер этажа:");
        Optional<String> floorResult = dlgFloor.showAndWait();

        if (floorResult.isEmpty()) {
            return;
        }

        try {
            int floorNumber = Integer.parseInt(floorResult.get().trim());

            try (Connection conn = DBConnection.getConnection()) {
                try (PreparedStatement pS = conn.prepareStatement(
                        "select setval('\"Floor_Id_floor_seq\"', (select coalesce(max(\"Id_floor\"),0) from \"Floor\"))")) {
                    pS.execute();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "insert into \"Floor\" (\"Id_building\", \"Floor_number\") values (?, ?)")) {
                    ps.setInt(1, idBuilding);
                    ps.setInt(2, floorNumber);
                    ps.executeUpdate();
                }
            }

            tableView.setItems(FXCollections.observableArrayList(loadAllFloors()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addRoom() {
        BuildingDB selectedBuilding = (BuildingDB) combo1.getValue();
        int idBuilding;

        if (selectedBuilding != null) {
            idBuilding = selectedBuilding.getIdBuilding();
        } else {
            TextInputDialog dlgBuilding = new TextInputDialog();
            dlgBuilding.setHeaderText("Добавить комнату");
            dlgBuilding.setContentText("Введите Id здания:");
            Optional<String> buildingResult = dlgBuilding.showAndWait();

            if (buildingResult.isEmpty()) return;

            try {
                idBuilding = Integer.parseInt(buildingResult.get().trim());
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Неверный формат Id здания!").show();
                return;
            }
        }

        int idFloor = -1;
        int floorNumber;

        FloorDB selectedFloor = (FloorDB) combo2.getValue();
        if (selectedFloor != null) {
            idFloor = selectedFloor.getIdFloor();
            floorNumber = selectedFloor.getFloorNumber();
        } else {
            TextInputDialog dlgFloor = new TextInputDialog();
            dlgFloor.setHeaderText("Добавить комнату");
            dlgFloor.setContentText("Введите номер этажа:");
            Optional<String> floorResult = dlgFloor.showAndWait();

            if (floorResult.isEmpty()) return;

            try {
                floorNumber = Integer.parseInt(floorResult.get().trim());
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Неверный формат номера этажа!").show();
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psFloor = conn.prepareStatement(
                         "select \"Id_floor\" from \"Floor\" where \"Floor_number\" = ? and \"Id_building\" = ?")) {
                psFloor.setInt(1, floorNumber);
                psFloor.setInt(2, idBuilding);
                ResultSet rs = psFloor.executeQuery();
                if (rs.next()) {
                    idFloor = rs.getInt("Id_floor");
                } else {
                    new Alert(Alert.AlertType.ERROR, "Этаж не найден в указанном здании!").show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        TextInputDialog dlgRoom = new TextInputDialog();
        dlgRoom.setHeaderText("Добавить комнату");
        dlgRoom.setContentText("Введите номер комнаты, тип и цену через запятую:");
        Optional<String> roomResult = dlgRoom.showAndWait();

        if (roomResult.isEmpty()) return;

        try {
            String[] parts = roomResult.get().split(",");
            int roomNumber = Integer.parseInt(parts[0].trim());
            String roomType = parts[1].trim();
            BigDecimal price = new BigDecimal(parts[2].trim());

            try (Connection conn = DBConnection.getConnection()) {
                try (PreparedStatement pS = conn.prepareStatement(
                        "select setval('\"Room_Id_room_seq\"', (select coalesce(max(\"Id_room\"),0) from \"Room\"))")) {
                    pS.execute();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "insert into \"Room\" (\"Room\", \"Room_type\", \"Price\", \"Is_busy\", \"Id_floor\") values (?, ?, ?, false, ?)")) {
                    ps.setInt(1, roomNumber);
                    ps.setString(2, roomType);
                    ps.setBigDecimal(3, price);
                    ps.setInt(4, idFloor);
                    ps.executeUpdate();
                }
            }

            tableView.setItems(FXCollections.observableArrayList(loadAllRooms()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addStay() {
        BuildingDB selectedBuilding = (BuildingDB) combo1.getValue();
        int idBuilding;

        if (selectedBuilding != null) {
            idBuilding = selectedBuilding.getIdBuilding();
        } else {
            TextInputDialog dlgBuilding = new TextInputDialog();
            dlgBuilding.setHeaderText("Добавить проживание");
            dlgBuilding.setContentText("Введите Id здания:");
            Optional<String> buildingResult = dlgBuilding.showAndWait();

            if (buildingResult.isEmpty()) return;

            try {
                idBuilding = Integer.parseInt(buildingResult.get().trim());
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Неверный формат Id здания!").show();
                return;
            }
        }

        int idFloor = -1;
        int floorNumber;

        FloorDB selectedFloor = (FloorDB) combo2.getValue();
        if (selectedFloor != null) {
            idFloor = selectedFloor.getIdFloor();
            floorNumber = selectedFloor.getFloorNumber();
        } else {
            TextInputDialog dlgFloor = new TextInputDialog();
            dlgFloor.setHeaderText("Добавить проживание");
            dlgFloor.setContentText("Введите номер этажа:");
            Optional<String> floorResult = dlgFloor.showAndWait();

            if (floorResult.isEmpty()) return;

            try {
                floorNumber = Integer.parseInt(floorResult.get().trim());
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Неверный формат номера этажа!").show();
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psFloor = conn.prepareStatement(
                         "select \"Id_floor\" from \"Floor\" where \"Floor_number\" = ? and \"Id_building\" = ?")) {
                psFloor.setInt(1, floorNumber);
                psFloor.setInt(2, idBuilding);
                ResultSet rs = psFloor.executeQuery();
                if (rs.next()) {
                    idFloor = rs.getInt("Id_floor");
                } else {
                    new Alert(Alert.AlertType.ERROR, "Этаж не найден в указанном здании!").show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        TextInputDialog dlgRoom = new TextInputDialog();
        dlgRoom.setHeaderText("Добавить проживание");
        dlgRoom.setContentText("Введите номер комнаты:");
        Optional<String> roomResult = dlgRoom.showAndWait();

        if (roomResult.isEmpty()) return;

        int idRoom = -1;
        int roomNumber;
        try {
            roomNumber = Integer.parseInt(roomResult.get().trim());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Неверный формат номера комнаты!").show();
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psRoom = conn.prepareStatement(
                     "select \"Id_room\" from \"Room\" where \"Room\" = ? and \"Id_floor\" = ?")) {
            psRoom.setInt(1, roomNumber);
            psRoom.setInt(2, idFloor);
            ResultSet rs = psRoom.executeQuery();
            if (rs.next()) {
                idRoom = rs.getInt("Id_room");
            } else {
                new Alert(Alert.AlertType.ERROR, "Комната не найдена на указанном этаже!").show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        TextInputDialog dlgClient = new TextInputDialog();
        dlgClient.setHeaderText("Добавить проживание");
        dlgClient.setContentText("Введите фамилию клиента:");
        Optional<String> clientResult = dlgClient.showAndWait();

        if (clientResult.isEmpty()) return;

        String clientSurname = clientResult.get().trim();
        int idClient = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psClient = conn.prepareStatement(
                     "select \"Id_client\" from \"Cleint\" where \"Surname\" = ?")) {
            psClient.setString(1, clientSurname);
            ResultSet rs = psClient.executeQuery();
            if (rs.next()) {
                idClient = rs.getInt("Id_client");
            } else {
                new Alert(Alert.AlertType.ERROR, "Клиент с такой фамилией не найден!").show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        TextInputDialog dlgDates = new TextInputDialog();
        dlgDates.setHeaderText("Добавить проживание");
        dlgDates.setContentText("Введите дату заселения и дату выезда через запятую (YYYY-MM-DD):");
        Optional<String> datesResult = dlgDates.showAndWait();

        if (datesResult.isEmpty()) return;

        try {
            String[] parts = datesResult.get().split(",");
            LocalDate arrivalDate = LocalDate.parse(parts[0].trim());
            LocalDate departureDate = LocalDate.parse(parts[1].trim());

            try (Connection conn = DBConnection.getConnection()) {
                try (PreparedStatement pS = conn.prepareStatement(
                        "select setval('\"Stay_Id_stay_seq\"', (select coalesce(max(\"Id_stay\"),0) from \"Stay\"))")) {
                    pS.execute();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "insert into \"Stay\" (\"Id_client\", \"Id_room\", \"Check_in\", \"Check_out\") values (?, ?, ?, ?)")) {
                    ps.setInt(1, idClient);
                    ps.setInt(2, idRoom);
                    ps.setDate(3, java.sql.Date.valueOf(arrivalDate));
                    ps.setDate(4, java.sql.Date.valueOf(departureDate));
                    ps.executeUpdate();
                }
            }

            tableView.setItems(FXCollections.observableArrayList(loadAllStays()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addStayService() {
        BuildingDB selectedBuilding = (BuildingDB) combo1.getValue();
        int idBuilding;
        if (selectedBuilding != null) {
            idBuilding = selectedBuilding.getIdBuilding();
        } else {
            TextInputDialog dlgBuilding = new TextInputDialog();
            dlgBuilding.setHeaderText("Добавить услугу проживания");
            dlgBuilding.setContentText("Введите Id здания:");
            Optional<String> buildingResult = dlgBuilding.showAndWait();
            if (buildingResult.isEmpty()) return;
            try {
                idBuilding = Integer.parseInt(buildingResult.get().trim());
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Неверный формат Id здания!").show();
                return;
            }
        }

        RoomDB selectedRoom = (RoomDB) combo2.getValue();
        int idRoom = -1;
        int roomNumber;
        if (selectedRoom != null) {
            idRoom = selectedRoom.getIdRoom();
            roomNumber = selectedRoom.getRoom();
        } else {
            TextInputDialog dlgRoom = new TextInputDialog();
            dlgRoom.setHeaderText("Добавить услугу проживания");
            dlgRoom.setContentText("Введите номер комнаты:");
            Optional<String> roomResult = dlgRoom.showAndWait();
            if (roomResult.isEmpty()) return;
            try {
                roomNumber = Integer.parseInt(roomResult.get().trim());
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Неверный формат номера комнаты!").show();
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psRoom = conn.prepareStatement(
                         "select r.\"Id_room\" " +
                                 "from \"Room\" r join \"Floor\" f on r.\"Id_floor\" = f.\"Id_floor\" " +
                                 "where r.\"Room\" = ? and f.\"Id_building\" = ?")) {
                psRoom.setInt(1, roomNumber);
                psRoom.setInt(2, idBuilding);
                ResultSet rs = psRoom.executeQuery();
                if (rs.next()) {
                    idRoom = rs.getInt("Id_room");
                } else {
                    new Alert(Alert.AlertType.ERROR, "Комната не найдена в указанном здании!").show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        int idStay = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psStay = conn.prepareStatement(
                     "select \"Id_stay\" from \"Stay\" where \"Id_room\" = ?")) {
            psStay.setInt(1, idRoom);
            ResultSet rs = psStay.executeQuery();
            if (rs.next()) {
                idStay = rs.getInt("Id_stay");
            } else {
                new Alert(Alert.AlertType.ERROR, "Проживание для этой комнаты не найдено!").show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        TextInputDialog dlgService = new TextInputDialog();
        dlgService.setHeaderText("Добавить услугу проживания");
        dlgService.setContentText("Введите название услуги:");
        Optional<String> serviceResult = dlgService.showAndWait();
        if (serviceResult.isEmpty()) return;

        String serviceName = serviceResult.get().trim();
        int idService = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psService = conn.prepareStatement(
                     "select \"Id_service\" from \"Service\" where \"Name\" = ?")) {
            psService.setString(1, serviceName);
            ResultSet rs = psService.executeQuery();
            if (rs.next()) {
                idService = rs.getInt("Id_service");
            } else {
                new Alert(Alert.AlertType.ERROR, "Услуга с таким названием не найдена!").show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        TextInputDialog dlgQty = new TextInputDialog("1");
        dlgQty.setHeaderText("Добавить услугу проживания");
        dlgQty.setContentText("Введите количество услуги (целое число):");
        Optional<String> qtyResult = dlgQty.showAndWait();
        if (qtyResult.isEmpty()) return;

        int quantity;
        try {
            quantity = Integer.parseInt(qtyResult.get().trim());
            if (quantity <= 0) {
                new Alert(Alert.AlertType.ERROR, "Количество должно быть положительным!").show();
                return;
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Неверный формат количества!").show();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement pS = conn.prepareStatement(
                    "select setval('\"Stay_service_Id_stay_service_seq\"', (select coalesce(max(\"Id_stay_service\"),0) from \"Stay_service\"))")) {
                pS.execute();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "insert into \"Stay_service\" (\"Id_stay\", \"Id_service\", \"Quantity\") values (?, ?, ?)")) {
                ps.setInt(1, idStay);
                ps.setInt(2, idService);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        tableView.setItems(FXCollections.observableArrayList(loadAllStayServices()));
    }
    private void addService() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Добавить услугу");
        dlg.setContentText("Введите название услуги и цену через запятую:");
        Optional<String> result = dlg.showAndWait();

        result.ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                String name = parts[0].trim();
                BigDecimal price = new BigDecimal(parts[1].trim());

                try (Connection conn = DBConnection.getConnection()) {
                    try (PreparedStatement pS = conn.prepareStatement(
                            "select setval('\"Service_Id_service_seq\"', (select coalesce(max(\"Id_service\"),0) from \"Service\"))")) {
                        pS.execute();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "insert into \"Service\" (\"Name\", \"Price\") values (?, ?)")) {
                        ps.setString(1, name);
                        ps.setBigDecimal(2, price);
                        ps.executeUpdate();
                    }
                }

                loadServiceTable();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    private void deleteClient() {
        ClientDB selected = (ClientDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Cleint\" where \"Id_client\" = ?")) {
            ps.setInt(1, selected.getIdClient());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadClientsTable();
    }
    private void deleteOrganization() {
        OrganizationDB selected = (OrganizationDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Organiztion\" where \"Id_organization\" = ?")) {
            ps.setInt(1, selected.getIdOrganization());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка удаления: " + e.getMessage()).show();
        }
        loadOrganizationTable();
    }
    private void deleteContract() {
        ContractDB selected = (ContractDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Contract\" where \"Id_contract\" = ?")) {
            ps.setInt(1, selected.getIdContract());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadContractTable();
    }
    private void deleteBooking() {
        BookingDB selected = (BookingDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Booking\" where \"Id_booking\" = ?")) {
            ps.setInt(1, selected.getIdBooking());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadBookingTable();
    }
    private void deleteBookingClient() {
        BookingClientDB selected = (BookingClientDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Booking_cleint\" where \"Id_booking_client\" = ?")) {
            ps.setInt(1, selected.getIdBookingClient());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadBookingClientTable();;
    }
    private void deleteBuilding() {
        BuildingDB selected = (BuildingDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Building\" where \"Id_building\" = ?")) {
            ps.setInt(1, selected.getIdBuilding());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadBuildings();
    }
    private void deleteFloor() {
        FloorDB selected = (FloorDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Floor\" where \"Id_floor\" = ?")) {
            ps.setInt(1, selected.getIdFloor());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(loadAllFloors()));
    }
    private void deleteRoom() {
        RoomDB selected = (RoomDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Room\" where \"Id_room\" = ?")) {
            ps.setInt(1, selected.getIdRoom());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(loadAllRooms()));
    }
    private void deleteStay() {
        StayDB selected = (StayDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Stay\" where \"Id_stay\" = ?")) {
            ps.setInt(1, selected.getIdStay());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(loadAllStays()));
    }
    private void deleteStayService() {
        StayServiceDB selected = (StayServiceDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Stay_service\" where \"Id_stay_service\" = ?")) {
            ps.setInt(1, selected.getIdStayService());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(loadAllStayServices()));
    }
    private void deleteService() {
        ServiceDB selected = (ServiceDB) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("delete from \"Service\" where \"Id_service\" = ?")) {
            ps.setInt(1, selected.getIdService());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadServiceTable();
    }



    private void showRoomStatus(RoomDB room) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select \"Stay\".\"Check_in\", \"Stay\".\"Check_out\"\n" +
                     "from \"Stay\"\n" +
                     "where \"Stay\".\"Id_room\" = ?\n" +
                     "order by \"Stay\".\"Check_in\" asc")) {
            ps.setInt(1, room.getIdRoom());
            ResultSet rs = ps.executeQuery();

            LocalDate now = LocalDate.now();
            LocalDate occupiedUntil = null;
            LocalDate nextStay = null;

            while (rs.next()) {
                LocalDate checkIn = rs.getDate("Check_in").toLocalDate();
                LocalDate checkOut = rs.getDate("Check_out").toLocalDate();
                if ((now.isEqual(checkIn) || now.isAfter(checkIn)) && now.isBefore(checkOut.plusDays(1))) {
                    occupiedUntil = checkOut;
                    break;
                }

                if (checkIn.isAfter(now)) {
                    nextStay = checkIn;
                    break;
                }
            }

            String message;
            if (occupiedUntil != null) {
                message = "Комната занята до " + occupiedUntil;
            } else if (nextStay != null) {
                message = "Комната свободна до " + nextStay;
            } else {
                message = "Комната свободна бессрочно";
            }

            new Alert(Alert.AlertType.INFORMATION, message).showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showClientInfo(ClientDB client) {
        StringBuilder info = new StringBuilder("Информация о клиенте " + client.getSurname() + ":\n\n");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select r.\"Room\", s.\"Check_in\", s.\"Check_out\"\n" +
                     "from \"Stay\" s\n" +
                     "join \"Room\" r on r.\"Id_room\" = s.\"Id_room\"\n" +
                     "where s.\"Id_client\" = ?\n" +
                     "order by s.\"Check_in\" asc")) {
            ps.setInt(1, client.getIdClient());
            ResultSet rs = ps.executeQuery();

            int visits = 0;
            while (rs.next()) {
                visits++;
                int roomNumber = rs.getInt("Room");
                LocalDate checkIn = rs.getDate("Check_in").toLocalDate();
                LocalDate checkOut = rs.getDate("Check_out").toLocalDate();

                info.append(visits).append(") Комната №").append(roomNumber)
                        .append(": с ").append(checkIn)
                        .append(" по ").append(checkOut)
                        .append("\n");
            }

            if (visits == 0) {
                info.append("Клиент ещё ни разу не посещал гостиницу.");
            } else {
                info.insert(0, "Количество посещений: " + visits + "\n\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки данных: " + e.getMessage()).showAndWait();
            return;
        }
        new Alert(Alert.AlertType.INFORMATION, info.toString()).showAndWait();
    }
    private void showStayService(StayDB stay) {
        StringBuilder info = new StringBuilder("Дополнительные услуги клиента:\n\n");
        BigDecimal total = BigDecimal.ZERO;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("select s.\"Name\", s.\"Price\"::numeric,\n" +
                     "  ss.\"Quantity\"\n" +
                     "from \"Stay_service\" ss\n" +
                     "join \"Service\" s on s.\"Id_service\" = ss.\"Id_service\"\n" +
                     "where ss.\"Id_stay\" = ?")) {
            ps.setInt(1, stay.getIdStay());
            ResultSet rs = ps.executeQuery();

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                String serviceName = rs.getString("Name");
                BigDecimal price = rs.getBigDecimal("Price");
                int quantity = rs.getInt("Quantity");

                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                total = total.add(subtotal);

                info.append("- ").append(serviceName)
                        .append(" (").append(quantity).append(" шт.)")
                        .append(" → ").append(subtotal).append("₽")
                        .append("\n");
            }

            if (!hasRecords) {
                info.append("Клиент не пользовался дополнительными услугами.");
            } else {
                info.append("\nОбщая сумма: ").append(total).append("₽");
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Ошибка загрузки услуг: " + e.getMessage()).showAndWait();
            return;
        }

        new Alert(Alert.AlertType.INFORMATION, info.toString()).showAndWait();
    }

}
