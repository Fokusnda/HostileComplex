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
    public void onClientClick(ActionEvent actionEvent) {
        tableTitle.setText("Клиенты");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> idClientColumn = new TableColumn<>("Номер клиента");
        idClientColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        idClientColumn.setPrefWidth(50);

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

        ObservableList<ClientDB> clients = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Cleint\".\"Id_client\", \"Cleint\".\"Surname\", \"Cleint\".\"Name\", \"Cleint\".\"Second_name\", \"Cleint\".\"Phone_number\" from \"Cleint\"")) {

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

    @FXML
    public void onOrganizationClick(ActionEvent actionEvent) {
        tableTitle.setText("Организации");
        tableView.getColumns().clear();

        TableColumn<Object, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);

        TableColumn<Object, Integer> discountRateColumn = new TableColumn<>("Скидка");
        discountRateColumn.setCellValueFactory(new PropertyValueFactory<>("discountRate"));
        discountRateColumn.setPrefWidth(100);

        tableView.getColumns().setAll(nameColumn, discountRateColumn);

        ObservableList<OrganizationDB> organizations = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Organiztion\".\"Id_organization\", \"Organiztion\".\"Name\", \"Organiztion\".\"Discount_rate\"\n" +
                     "from \"Organiztion\"")) {

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

    @FXML
    public void onContractClick(ActionEvent actionEvent) {
        tableTitle.setText("Контракты");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> idContractColumn = new TableColumn<>("Номер контракта");
        idContractColumn.setCellValueFactory(new PropertyValueFactory<>("idContract"));
        idContractColumn.setPrefWidth(50);

        TableColumn<Object, String> idOrganizationColumn = new TableColumn<>("Организация");
        idOrganizationColumn.setCellValueFactory(new PropertyValueFactory<>("nameOrganization"));
        idOrganizationColumn.setPrefWidth(200);

        TableColumn<Object, LocalDate> startDateColumn = new TableColumn<>("Дата подписания");
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startDateColumn.setPrefWidth(100);

        TableColumn<Object, LocalDate> endDateColumn = new TableColumn<>("Дата окончания");
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endDateColumn.setPrefWidth(100);

        tableView.getColumns().setAll(idContractColumn, idOrganizationColumn, startDateColumn, endDateColumn);

        ObservableList<ContractDB> contracts = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Contract\".\"Id_contract\", \"Organiztion\".\"Name\", \"Contract\".\"Start_date\", \"Contract\".\"End_date\"\n" +
                     "from \"Contract\"\n" +
                     "Join \"Organiztion\" on \"Organiztion\".\"Id_organization\" = \"Contract\".\"Id_organization\"\n" +
                     "order by \"Contract\".\"Id_contract\"")) {

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

    @FXML
    public void onBookingClick(ActionEvent actionEvent) {
        tableTitle.setText("Бронирования");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> idBookingColumn = new TableColumn<>("Номер бронирования");
        idBookingColumn.setCellValueFactory(new PropertyValueFactory<>("idBooking"));
        idBookingColumn.setPrefWidth(50);

        TableColumn<Object, Integer> idContractColumn = new TableColumn<>("Номер контракта");
        idContractColumn.setCellValueFactory(new PropertyValueFactory<>("idContract"));
        idContractColumn.setPrefWidth(100);

        TableColumn<Object, String> hotelClassColumn = new TableColumn<>("Класс отеля");
        hotelClassColumn.setCellValueFactory(new PropertyValueFactory<>("hotelClass"));
        hotelClassColumn.setPrefWidth(200);

        TableColumn<Object, Integer> floorNumberColumn = new TableColumn<>("Номер этажа");
        floorNumberColumn.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        floorNumberColumn.setPrefWidth(100);

        TableColumn<Object, Integer> roomsCountColumn = new TableColumn<>("Количество комнат");
        roomsCountColumn.setCellValueFactory(new PropertyValueFactory<>("roomsCount"));
        roomsCountColumn.setPrefWidth(100);

        TableColumn<Object, Integer> peopleCountColumn = new TableColumn<>("Количество людей");
        peopleCountColumn.setCellValueFactory(new PropertyValueFactory<>("peopleCount"));
        peopleCountColumn.setPrefWidth(50);

        TableColumn<Object, LocalDate> bookingDateColumn = new TableColumn<>("Дата бронирования");
        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        bookingDateColumn.setPrefWidth(200);

        TableColumn<Object, LocalDate> arrivalDateColumn = new TableColumn<>("Дата заселения");
        arrivalDateColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalDate"));
        arrivalDateColumn.setPrefWidth(100);

        tableView.getColumns().setAll(idBookingColumn, idContractColumn, hotelClassColumn, floorNumberColumn,
                roomsCountColumn, peopleCountColumn, bookingDateColumn, arrivalDateColumn);

        ObservableList<BookingDB> bookings = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Booking\".\"Id_booking\", \"Booking\".\"Id_contract\",\n" +
                     "  \"Booking\".\"Hotel_class\", \"Booking\".\"Floor_number\",\n" +
                     "  \"Booking\".\"Rooms_count\", \"Booking\".\"People_count\",\n" +
                     "  \"Booking\".\"Booking_date\", \"Booking\".\"Arrival_date\"\n" +
                     "from \"Booking\"")) {

            while (rs.next()) {
                bookings.add(new BookingDB(
                        rs.getInt("Id_booking"),
                        rs.getString("Hotel_class"),
                        rs.getInt("Floor_number"),
                        rs.getInt("Rooms_count"),
                        rs.getInt("People_count"),
                        rs.getDate("Booking_date").toLocalDate(),
                        rs.getDate("Arrival_date").toLocalDate(),
                        rs.getInt("Id_contract")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(bookings));
    }

    @FXML
    public void onBookingClientClick(ActionEvent actionEvent) {
        tableTitle.setText("Бронь-клиенты");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> idBookingClientColumn = new TableColumn<>("Номер бронь-клиента");
        idBookingClientColumn.setCellValueFactory(new PropertyValueFactory<>("idBookingClient"));
        idBookingClientColumn.setPrefWidth(50);

        TableColumn<Object, Integer> idBookingColumn = new TableColumn<>("Номер бронирования");
        idBookingColumn.setCellValueFactory(new PropertyValueFactory<>("idBooking"));
        idBookingColumn.setPrefWidth(100);

        TableColumn<Object, String> idClientColumn = new TableColumn<>("Фамилия клиента");
        idClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientSurname"));
        idClientColumn.setPrefWidth(200);

        tableView.getColumns().setAll(idBookingClientColumn, idBookingColumn, idClientColumn);

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

    @FXML
    public void onBuildingClick(ActionEvent actionEvent) {
        tableTitle.setText("Здания");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> idBuildingColumn = new TableColumn<>("Номер здания");
        idBuildingColumn.setCellValueFactory(new PropertyValueFactory<>("idBuilding"));
        idBuildingColumn.setPrefWidth(50);

        TableColumn<Object, String> hotelClassColumn = new TableColumn<>("Класс отеля");
        hotelClassColumn.setCellValueFactory(new PropertyValueFactory<>("hotelClass"));
        hotelClassColumn.setPrefWidth(100);

        TableColumn<Object, Integer> floorsCountColumn = new TableColumn<>("Количество этажей");
        floorsCountColumn.setCellValueFactory(new PropertyValueFactory<>("floorsCount"));
        floorsCountColumn.setPrefWidth(50);

        TableColumn<Object, Integer> totalRoomsColumn = new TableColumn<>("Всего комнат");
        totalRoomsColumn.setCellValueFactory(new PropertyValueFactory<>("totalRooms"));
        totalRoomsColumn.setPrefWidth(100);

        TableColumn<Object, Integer> roomsPerFloorColumn = new TableColumn<>("Комнат на этаже");
        roomsPerFloorColumn.setCellValueFactory(new PropertyValueFactory<>("roomsPerFloor"));
        roomsPerFloorColumn.setPrefWidth(50);

        TableColumn<Object, Boolean> hasCleaningColumn = new TableColumn<>("Уборка");
        hasCleaningColumn.setCellValueFactory(new PropertyValueFactory<>("hasCleaning"));
        hasCleaningColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> hasLaundryColumn = new TableColumn<>("Прачечная");
        hasLaundryColumn.setCellValueFactory(new PropertyValueFactory<>("hasLaundry"));
        hasLaundryColumn.setPrefWidth(50);

        TableColumn<Object, Boolean> hasDryCleaningColumn = new TableColumn<>("Химчистка");
        hasDryCleaningColumn.setCellValueFactory(new PropertyValueFactory<>("hasDryCleaning"));
        hasDryCleaningColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> hasFoodClientColumn = new TableColumn<>("Ресторан");
        hasFoodClientColumn.setCellValueFactory(new PropertyValueFactory<>("hasFood"));
        hasFoodClientColumn.setPrefWidth(50);

        TableColumn<Object, Boolean> hasEntertainmentColumn = new TableColumn<>("Развлечения");
        hasEntertainmentColumn.setCellValueFactory(new PropertyValueFactory<>("hasEntertainment"));
        hasEntertainmentColumn.setPrefWidth(100);

        tableView.getColumns().setAll(idBuildingColumn, hotelClassColumn, floorsCountColumn,
                totalRoomsColumn, roomsPerFloorColumn, hasCleaningColumn, hasLaundryColumn,
                hasDryCleaningColumn, hasFoodClientColumn, hasEntertainmentColumn);

        ObservableList<BuildingDB> buildings = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Building\".\"Id_building\", \"Building\".\"Hotel_class\",\n" +
                     "  \"Building\".\"Floors_count\", \"Building\".\"Total_rooms\",\n" +
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

    @FXML
    public void onFloorClick(ActionEvent actionEvent) {
        tableTitle.setText("Этажи");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> idBuildingColumn = new TableColumn<>("Номер здания");
        idBuildingColumn.setCellValueFactory(new PropertyValueFactory<>("idBuilding"));
        idBuildingColumn.setPrefWidth(100);

        TableColumn<Object, Integer> floorNumberColumn = new TableColumn<>("Номер этажа");
        floorNumberColumn.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        floorNumberColumn.setPrefWidth(100);

        tableView.getColumns().setAll(idBuildingColumn, floorNumberColumn);

        ObservableList<FloorDB> floors = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Floor\".\"Id_floor\", \"Floor\".\"Id_building\", \"Floor\".\"Floor_number\"\n" +
                     "from \"Floor\"")) {

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

    @FXML
    public void onRoomClick(ActionEvent actionEvent) {
        tableTitle.setText("Комнаты");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> floorColumn = new TableColumn<>("Номер этажа");
        floorColumn.setCellValueFactory(new PropertyValueFactory<>("floor"));
        floorColumn.setPrefWidth(100);

        TableColumn<Object, Integer> roomColumn = new TableColumn<>("Номер комнаты");
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));
        roomColumn.setPrefWidth(100);

        TableColumn<Object, String> roomTypeColumn = new TableColumn<>("Тип комнаты");
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        roomTypeColumn.setPrefWidth(100);

        TableColumn<Object, BigDecimal> priceColumn = new TableColumn<>("Цена");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(100);

        TableColumn<Object, Boolean> isBusyColumn = new TableColumn<>("Занят");
        isBusyColumn.setCellValueFactory(new PropertyValueFactory<>("isBusy"));
        isBusyColumn.setPrefWidth(100);

        tableView.getColumns().setAll(floorColumn, roomColumn, roomTypeColumn, priceColumn, isBusyColumn);

        ObservableList<RoomDB> rooms = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Room\".\"Id_room\", \"Floor\".\"Floor_number\",\n" +
                     "  \"Room\".\"Room\", \"Room\".\"Room_type\",\n" +
                     "  \"Room\".\"Price\"::numeric, \"Room\".\"Is_busy\"\n" +
                     "from \"Room\"\n" +
                     "join \"Floor\" on \"Floor\".\"Id_floor\" = \"Room\".\"Id_floor\"\n" +
                     "order by \"Room\".\"Id_room\"")) {

            while (rs.next()) {
                rooms.add(new RoomDB(
                        rs.getInt("Id_room"),
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

    @FXML
    public void onStayClick(ActionEvent actionEvent) {
        tableTitle.setText("Занятые комнаты");
        tableView.getColumns().clear();

        TableColumn<Object, String> clientSurnameColumn = new TableColumn<>("Фамилия клиента");
        clientSurnameColumn.setCellValueFactory(new PropertyValueFactory<>("clientSurname"));
        clientSurnameColumn.setPrefWidth(100);

        TableColumn<Object, Integer> numberRoomColumn = new TableColumn<>("Номер комнаты");
        numberRoomColumn.setCellValueFactory(new PropertyValueFactory<>("numberRoom"));
        numberRoomColumn.setPrefWidth(100);

        TableColumn<Object, LocalDate> checkInColumn = new TableColumn<>("Дата заселения");
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkInColumn.setPrefWidth(100);

        TableColumn<Object, LocalDate> checkOutColumn = new TableColumn<>("Дата выселения");
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        checkOutColumn.setPrefWidth(100);

        tableView.getColumns().setAll(clientSurnameColumn, numberRoomColumn, checkInColumn, checkOutColumn);

        ObservableList<StayDB> stays = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Stay\".\"Id_stay\", \"Cleint\".\"Surname\", \"Room\".\"Room\",\n" +
                     "  \"Stay\".\"Check_in\", \"Stay\".\"Check_out\"\n" +
                     "from \"Stay\"\n" +
                     "join \"Cleint\" on \"Cleint\".\"Id_client\" = \"Stay\".\"Id_client\"\n" +
                     "join \"Room\" on \"Room\".\"Id_room\" = \"Stay\".\"Id_room\"")) {

            while (rs.next()) {
                stays.add(new StayDB(
                        rs.getInt("Id_stay"),
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

    @FXML
    public void onStayServiceClick(ActionEvent actionEvent) {
        tableTitle.setText("Услуги в комнатах");
        tableView.getColumns().clear();

        TableColumn<Object, Integer> idStayColumn = new TableColumn<>("Номер занятой комнаты");
        idStayColumn.setCellValueFactory(new PropertyValueFactory<>("idStay"));
        idStayColumn.setPrefWidth(100);

        TableColumn<Object, String> nameServiceColumn = new TableColumn<>("Название услуги");
        nameServiceColumn.setCellValueFactory(new PropertyValueFactory<>("nameService"));
        nameServiceColumn.setPrefWidth(100);

        TableColumn<Object, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(100);

        TableColumn<Object, BigDecimal> totalPriceColumn = new TableColumn<>("Итоговая стоимость");
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalPriceColumn.setPrefWidth(100);

        tableView.getColumns().setAll(idStayColumn, nameServiceColumn, quantityColumn, totalPriceColumn);

        ObservableList<StayServiceDB> stayServices = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select \"Stay_service\".\"Id_stay_service\", \"Room\".\"Room\",\n" +
                     "  \"Service\".\"Name\", \"Stay_service\".\"Quantity\",\n" +
                     "  \"Stay_service\".\"Quantity\"*\"Service\".\"Price\"::numeric as total_price\n" +
                     "from \"Stay_service\"\n" +
                     "join \"Stay\" on \"Stay\".\"Id_stay\" = \"Stay_service\".\"Id_stay\"\n" +
                     "join \"Room\" on \"Room\".\"Id_room\" = \"Stay\".\"Id_room\"\n" +
                     "join \"Service\" on \"Service\".\"Id_service\" = \"Stay_service\".\"Id_service\"")) {

            while (rs.next()) {
                stayServices.add(new StayServiceDB(
                        rs.getInt("Id_stay_service"),
                        rs.getInt("Room"),
                        rs.getString("Name"),
                        rs.getInt("Quantity"),
                        rs.getBigDecimal("total_price")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(stayServices));
    }

    @FXML
    public void onServiceClick(ActionEvent actionEvent) {
        tableTitle.setText("Услуги");
        tableView.getColumns().clear();

        TableColumn<Object, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(100);

        TableColumn<Object, BigDecimal> priceColumn = new TableColumn<>("Стоимость");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(100);

        tableView.getColumns().setAll(nameColumn, priceColumn);

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
}
