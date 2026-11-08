package com.example.storeapp;

import com.example.storeapp.model.Order;
import com.example.storeapp.model.Product;
import com.example.storeapp.model.Review;
import com.example.storeapp.model.Role;
import com.example.storeapp.model.User;
import com.example.storeapp.service.AuthService;
import com.example.storeapp.service.ShopService;
import javafx.scene.Parent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class MainApp extends Application {

    // ═══════════════════════════════════════════════════════════════════════
    //  ТЕМА (изменяемые поля — переключаются в applyTheme())
    // ═══════════════════════════════════════════════════════════════════════
    private static String BG_DARK, BG_CARD, BG_SURFACE, BG_ELEVATED;
    private static String BORDER, BORDER_BRIGHT;
    private static String NEON_GREEN, NEON_BLUE, NEON_ORANGE, NEON_PINK, NEON_PURPLE;
    private static String GRAD_HEADER, GRAD_SIDEBAR;
    private static String TEXT_PRIMARY, TEXT_SECONDARY, TEXT_MUTED;

    private boolean darkTheme = true;

    // ───────────────────────────────────────────────────────────────────────
    private final AuthService authService = new AuthService();
    private final ShopService shopService = new ShopService();

    private Stage stage;
    private User  currentUser;

    private FlowPane productsPane;
    private ComboBox<String> categoryBox;
    private TextField searchField;
    private TextField minPriceField;
    private TextField maxPriceField;
    private CheckBox inStockBox;
    private Label cartNavLabel;
    private Label favoriteNavLabel;
    private VBox cartItemsBox;

    // title bar drag
    private double dragOffsetX, dragOffsetY;

    private enum Screen { AUTH, STORE }
    private Screen currentScreen = Screen.STORE;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.initStyle(StageStyle.UNDECORATED);   // кастомный title bar
        stage.setTitle("PELEGOOD SPORT SHOP");
        applyTheme();
        currentUser = null;
        showStoreScene();
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ТЕМА
    // ═══════════════════════════════════════════════════════════════════════
    private void applyTheme() {
        if (darkTheme) {
            BG_DARK       = "#0a0a0f";
            BG_CARD       = "#12121a";
            BG_SURFACE    = "#1a1a26";
            BG_ELEVATED   = "#22223a";
            BORDER        = "#2a2a40";
            BORDER_BRIGHT = "#3a3a55";
            NEON_GREEN    = "#00ff88";
            NEON_BLUE     = "#00c3ff";
            NEON_ORANGE   = "#ff6b00";
            NEON_PINK     = "#ff0080";
            NEON_PURPLE   = "#bf00ff";
            GRAD_HEADER   = "linear-gradient(to right, #0a0a0f 0%, #0d1b2a 40%, #0a1628 100%)";
            GRAD_SIDEBAR  = "linear-gradient(to bottom, #0f0f1a 0%, #12121e 100%)";
            TEXT_PRIMARY   = "#ffffff";
            TEXT_SECONDARY = "#a0a0c0";
            TEXT_MUTED     = "#606080";
        } else {
            BG_DARK       = "#f4f5fa";
            BG_CARD       = "#ffffff";
            BG_SURFACE    = "#eef0f7";
            BG_ELEVATED   = "#e2e6f0";
            BORDER        = "#d4d8e4";
            BORDER_BRIGHT = "#b8bfd0";
            NEON_GREEN    = "#00a04d";
            NEON_BLUE     = "#0074c8";
            NEON_ORANGE   = "#d95a00";
            NEON_PINK     = "#c40060";
            NEON_PURPLE   = "#8a00c8";
            GRAD_HEADER   = "linear-gradient(to right, #ffffff 0%, #e8f0ff 40%, #e0ecff 100%)";
            GRAD_SIDEBAR  = "linear-gradient(to bottom, #fbfcff 0%, #eef1f8 100%)";
            TEXT_PRIMARY   = "#141628";
            TEXT_SECONDARY = "#4a4f6a";
            TEXT_MUTED     = "#8890a8";
        }
    }

    private void toggleTheme() {
        darkTheme = !darkTheme;
        applyTheme();
        if (currentScreen == Screen.AUTH) showAuthScene();
        else showStoreScene();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TITLE BAR
    // ═══════════════════════════════════════════════════════════════════════
    private HBox buildTitleBar(Stage targetStage, String titleText) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 10, 6, 14));
        bar.setStyle(
                "-fx-background-color: " + BG_ELEVATED + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label icon = new Label("⚡");
        icon.setStyle("-fx-font-size: 16px; -fx-text-fill: " + NEON_GREEN + ";");

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeBtn = windowBtn(darkTheme ? "☀" : "🌙", NEON_ORANGE);
        themeBtn.setOnAction(e -> toggleTheme());
        themeBtn.setTooltip(new Tooltip(darkTheme ? "Светлая тема" : "Тёмная тема"));

        Button minBtn   = windowBtn("—", TEXT_SECONDARY);
        Button maxBtn   = windowBtn("▢", TEXT_SECONDARY);
        Button closeBtn = windowBtn("✕", NEON_PINK);

        minBtn.setOnAction(e -> targetStage.setIconified(true));
        maxBtn.setOnAction(e -> targetStage.setMaximized(!targetStage.isMaximized()));
        closeBtn.setOnAction(e -> targetStage.close());

        // Если это НЕ главное окно (т.е. модальное), скрываем кнопки свернуть/развернуть
        if (targetStage != stage) {
            minBtn.setVisible(false); minBtn.setManaged(false);
            maxBtn.setVisible(false); maxBtn.setManaged(false);
            bar.getChildren().addAll(icon, title, spacer, closeBtn);
        } else {
            bar.getChildren().addAll(icon, title, themeBtn, spacer, minBtn, maxBtn, closeBtn);
        }

        // Перетаскивание окна (теперь работает для любого переданного окна)
        bar.setOnMousePressed(e -> {
            dragOffsetX = e.getScreenX() - targetStage.getX();
            dragOffsetY = e.getScreenY() - targetStage.getY();
        });
        bar.setOnMouseDragged(e -> {
            if (!targetStage.isMaximized()) {
                targetStage.setX(e.getScreenX() - dragOffsetX);
                targetStage.setY(e.getScreenY() - dragOffsetY);
            }
        });
        bar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && targetStage == stage) targetStage.setMaximized(!targetStage.isMaximized());
        });

        return bar;
    }

    private Button windowBtn(String text, String color) {
        Button b = new Button(text);
        b.setPrefSize(34, 26);
        b.setStyle(windowBtnStyle(color, false));
        b.setOnMouseEntered(e -> b.setStyle(windowBtnStyle(color, true)));
        b.setOnMouseExited(e -> b.setStyle(windowBtnStyle(color, false)));
        return b;
    }

    private String windowBtnStyle(String color, boolean hover) {
        return "-fx-background-color: " + (hover ? color + "33" : "transparent") + ";" +
                "-fx-text-fill: " + color + ";" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-background-radius: 4; -fx-cursor: hand;";
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ГЛАВНЫЙ ЭКРАН
    // ═══════════════════════════════════════════════════════════════════════
    private void showStoreScene() {
        currentScreen = Screen.STORE;

        if (currentUser == null) {
            currentUser = new User();
            currentUser.setFullName("Гость");
            currentUser.setRole(Role.USER);
        }

        BorderPane root = new BorderPane();
        root.setTop(new VBox(buildTitleBar(stage, "PELEGOOD SPORT SHOP"), buildHeader()));

        // Убрали root.setLeft(buildSidebar());
        // Убрали root.setRight(buildBrandPanel());

        root.setCenter(buildCatalog());
        root.setBottom(buildFooter());
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        // Уменьшаем размер окна, чтобы оно влезало на любой экран
        Scene scene = new Scene(root, 1280, 720);

        // Подключаем стили для скроллбара
        try {
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("CSS файл не найден, используются стандартные стили.");
        }

        stage.setScene(scene);
        stage.centerOnScreen(); // <--- ВАЖНО: Центрируем окно по экрану

        // Устанавливаем класс для светлой темы (если нужно)
        if (!darkTheme) scene.getRoot().getStyleClass().add("light-theme");

        stage.setScene(scene);

        if (isRealUser()) loadCartAsync();
        else updateCounters(List.of());
        loadCatalogAsync();
    }

    private boolean isRealUser() {
        return currentUser != null && currentUser.getId() != 0;
    }

    private void loadCartAsync() {
        Task<List<ShopService.CartEntry>> t = new Task<>() {
            @Override protected List<ShopService.CartEntry> call() {
                return shopService.getCartEntries(currentUser);
            }
        };
        t.setOnSucceeded(e -> Platform.runLater(() -> updateCounters(t.getValue())));
        t.setOnFailed(e -> t.getException().printStackTrace());
        new Thread(t, "cart-loader").start();
    }

    private void loadCatalogAsync() {
        String search   = searchField   == null ? "" : searchField.getText();
        String category = categoryBox   == null ? "Все" : categoryBox.getValue();
        Double minP     = minPriceField == null ? null : parseDoubleOrNull(minPriceField.getText());
        Double maxP     = maxPriceField == null ? null : parseDoubleOrNull(maxPriceField.getText());
        boolean inStock = inStockBox    != null && inStockBox.isSelected();

        Task<List<Product>> t = new Task<>() {
            @Override protected List<Product> call() {
                return shopService.searchProducts(search, category, minP, maxP, inStock);
            }
        };
        t.setOnSucceeded(e -> Platform.runLater(() -> {
            productsPane.getChildren().clear();
            List<Product> products = t.getValue();
            int i = 0;
            for (Product p : products) {
                Node card = productCard(p);
                card.setOpacity(0);
                productsPane.getChildren().add(card);
                animateCard(card, i++);
            }
            if (products.isEmpty()) {
                VBox box = darkCard("Ничего не найдено",
                        "Попробуй изменить запрос, категорию или диапазон цен.");
                box.setPrefWidth(420);
                productsPane.getChildren().add(box);
            }
        }));
        t.setOnFailed(e -> t.getException().printStackTrace());
        new Thread(t, "catalog-loader").start();
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.replace(',', '.').trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private void animateCard(Node card, int index) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition move = new TranslateTransition(Duration.millis(400), card);
        move.setFromY(20);
        move.setToY(0);
        fade.setDelay(Duration.millis(index * 80));
        move.setDelay(Duration.millis(index * 80));
        fade.play();
        move.play();
    }

    private void refreshCatalog() {
        if (productsPane != null) {
            productsPane.getChildren().clear();
            Label loading = new Label("Загрузка...");
            loading.setStyle("-fx-text-fill: " + NEON_GREEN +
                    "; -fx-font-size: 18px; -fx-font-weight: bold;");
            productsPane.getChildren().add(loading);
        }
        loadCatalogAsync();
    }

    private void refreshCountersAsync() {
        if (!isRealUser()) { updateCounters(List.of()); return; }
        Task<List<ShopService.CartEntry>> t = new Task<>() {
            @Override protected List<ShopService.CartEntry> call() {
                return shopService.getCartEntries(currentUser);
            }
        };
        t.setOnSucceeded(e -> Platform.runLater(() -> updateCounters(t.getValue())));
        new Thread(t, "counter-refresh").start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ШАПКА (с фильтрами по цене и наличию)
    // ═══════════════════════════════════════════════════════════════════════
    private VBox buildHeader() {
        VBox header = new VBox(0);
        header.setStyle(
                "-fx-background-color: " + GRAD_HEADER + ";" +
                        "-fx-border-color: transparent transparent " + NEON_BLUE + "44 transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        HBox accent = new HBox();
        accent.setPrefHeight(3);
        accent.setStyle("-fx-background-color: linear-gradient(to right, " +
                NEON_GREEN + ", " + NEON_BLUE + ", " + NEON_PINK + ");");

        HBox row1 = new HBox(16);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.setPadding(new Insets(14, 28, 10, 28));

        // Загружаем картинку логотипа
        ImageView logoImg = new ImageView(loadImage("/images/logotip2.png"));
        // Устанавливаем высоту 45 пикселей (чтобы влезало в шапку), ширина подстроится сама
        logoImg.setFitHeight(55); // Можно сделать чуть побольше, раз нет фона
        logoImg.setPreserveRatio(true);

        // Добавляем небольшой отступ справа, чтобы поиск не прилипал к логотипу
        HBox.setMargin(logoImg, new Insets(0, 20, 0, 0));
        searchField = new TextField();
        searchField.setPromptText("Найти товар...");
        searchField.setPrefWidth(500); // Ограничиваем ширину
        searchField.setStyle(inputStyle());
        searchField.textProperty().addListener((obs, o, n) -> refreshCatalog());
        HBox.setHgrow(searchField, Priority.ALWAYS); // Поиск занимает свободное место

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Навигационные кнопки (иконка + текст)
        favoriteNavLabel = new Label("Избранное");
        VBox favBox = buildTopNavItem("❤", favoriteNavLabel, NEON_ORANGE, () -> showFavoritesDialog());

        cartNavLabel = new Label("Корзина");
        VBox cartBox = buildTopNavItem("🛒", cartNavLabel, NEON_GREEN, () -> {
            if (!isRealUser()) { alert("Вход", "Войдите, чтобы открыть корзину."); return; }
            showCartDialog();
        });

        VBox ordersBox = buildTopNavItem("📦", new Label("Заказы"), NEON_BLUE, () -> {
            if (!isRealUser()) { alert("Вход", "Войдите, чтобы посмотреть заказы."); return; }
            if (currentUser.getRole() == Role.ADMIN) showAdminOrdersDialog();
            else showOrdersDialog();
        });

        row1.getChildren().addAll(logoImg, spacer, searchField, favBox, cartBox, ordersBox);

        if (currentUser.getFullName().equals("Гость")) {
            Button loginBtn = neonButton("ВОЙТИ", NEON_GREEN);
            loginBtn.setOnAction(e -> showAuthScene());
            row1.getChildren().add(loginBtn);
        } else {
            Label userChip = new Label("  " + currentUser.getFullName() + "  ");
            userChip.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-background-color: " + BG_SURFACE + "; -fx-background-radius: 20; -fx-padding: 5 12 5 12;");
            Button logoutBtn = neonButton("EXIT", NEON_PINK);
            logoutBtn.setOnAction(e -> { currentUser = null; showAuthScene(); });
            row1.getChildren().addAll(userChip, logoutBtn);

            if (currentUser.getRole() == Role.ADMIN) {
                Button adminBtn = neonButton("ADMIN", NEON_PURPLE);
                adminBtn.setOnAction(e -> showAdminPanel());
                row1.getChildren().add(adminBtn);
            }
        }

        // Строка фильтров
        HBox row2 = new HBox(10);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.setPadding(new Insets(0, 28, 14, 28));

        categoryBox = new ComboBox<>();
        categoryBox.getItems().add("Все");
        categoryBox.getSelectionModel().selectFirst();
        categoryBox.setPrefWidth(180);
        categoryBox.setStyle(
                "-fx-background-color: " + BG_SURFACE + ";" +
                        "-fx-background-radius: 30; " +
                        "-fx-border-color: " + BORDER_BRIGHT + "; " +
                        "-fx-border-radius: 30; " +
                        "-fx-font-size: 13px;"
        );
        categoryBox.setOnAction(e -> refreshCatalog());

        // ФИКС: Настраиваем ячейку, которая отображается как выбранный элемент (когда список свернут)
        categoryBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-padding: 0 5 0 15;");
                }
            }
        });

        // ФИКС: Настраиваем ячейки внутри самого выпадающего списка
        categoryBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: " + BG_SURFACE + ";");
                } else {
                    setText(item);
                    setStyle(
                            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                                    "-fx-background-color: " + BG_SURFACE + ";" +
                                    "-fx-padding: 10 15;"
                    );
                }
            }
        });

        Task<List<String>> catTask = new Task<>() {
            @Override protected List<String> call() { return shopService.getCategories(); }
        };
        catTask.setOnSucceeded(e -> Platform.runLater(() -> {
            categoryBox.getItems().setAll(catTask.getValue());
            categoryBox.getSelectionModel().selectFirst();
        }));
        new Thread(catTask, "cat-loader").start();

        minPriceField = new TextField();
        minPriceField.setPromptText("Цена от");
        minPriceField.setPrefWidth(100);
        minPriceField.setStyle(inputStyle());
        minPriceField.textProperty().addListener((obs, o, n) -> refreshCatalog());

        maxPriceField = new TextField();
        maxPriceField.setPromptText("до");
        maxPriceField.setPrefWidth(100);
        maxPriceField.setStyle(inputStyle());
        maxPriceField.textProperty().addListener((obs, o, n) -> refreshCatalog());

        inStockBox = new CheckBox("В наличии");
        inStockBox.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;");
        inStockBox.selectedProperty().addListener((obs, o, n) -> refreshCatalog());

        Button clear = ghostButton("Сбросить фильтры");
        clear.setOnAction(e -> {
            searchField.clear();
            categoryBox.getSelectionModel().selectFirst();
            minPriceField.clear();
            maxPriceField.clear();
            inStockBox.setSelected(false);
        });

        row2.getChildren().addAll(categoryBox, minPriceField, maxPriceField, inStockBox, clear);

        header.getChildren().addAll(accent, row1, row2);
        return header;
    }

    private String inputStyle() {
        return "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-background-radius: 30; -fx-border-radius: 30;" +
                "-fx-padding: 11 20 11 20;" +
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
                "-fx-border-color: " + BORDER_BRIGHT + ";" +
                "-fx-font-size: 14px;";
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  БОКОВАЯ ПАНЕЛЬ
    // ═══════════════════════════════════════════════════════════════════════


    // ═══════════════════════════════════════════════════════════════════════
    //  КАТАЛОГ
    // ═══════════════════════════════════════════════════════════════════════
    private ScrollPane buildCatalog() {
        VBox container = new VBox(22);
        container.setPadding(new Insets(26));
        container.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label bar = new Label("|");
        bar.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 32px; -fx-font-weight: bold;");
        Label title = new Label("КАТАЛОГ ТОВАРОВ");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 26px; -fx-font-weight: bold;");
        titleRow.getChildren().addAll(bar, title);

        productsPane = new FlowPane();
        productsPane.setHgap(20);
        productsPane.setVgap(20);
        productsPane.setPrefWrapLength(1060);

        container.getChildren().addAll(buildBanner(), titleRow, productsPane);
        ScrollPane sp = new ScrollPane(container);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: " + BG_DARK + "; -fx-background: " + BG_DARK + ";");
        return sp;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  КАРТОЧКА ТОВАРА (с рейтингом из БД)
    // ═══════════════════════════════════════════════════════════════════════
    private VBox productCard(Product product) {
        VBox card = new VBox(0);
        card.setPrefWidth(286);
        card.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-effect: dropshadow(gaussian, " + NEON_BLUE + "22, 20, 0, 0, 4);"
        );

        // --- ФИКС КАРТИНКИ (Добавлена обрезка углов) ---
        ImageView image = new ImageView(loadImage(product.getImagePath()));
        image.setFitWidth(286);
        image.setFitHeight(175);
        image.setPreserveRatio(false);

        StackPane imgWrap = new StackPane(image);
        imgWrap.setStyle("-fx-background-color: " + BG_SURFACE + "; -fx-background-radius: 20 20 0 0;");
        imgWrap.setMinHeight(175);
        imgWrap.setMaxHeight(175);

        // Обрезаем картинку по форме карточки (скругляем верхние углы)
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(286, 175);
        clip.setArcWidth(40);  // 20 радиус * 2
        clip.setArcHeight(40);
        imgWrap.setClip(clip);
        // ------------------------------------------------

        Label catBadge = new Label(product.getCategory().toUpperCase());
        catBadge.setStyle(
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #0a0a0f;" +
                        "-fx-background-color: " + NEON_GREEN + "; -fx-background-radius: 4; -fx-padding: 3 8 3 8;"
        );
        StackPane.setAlignment(catBadge, Pos.TOP_LEFT);
        StackPane.setMargin(catBadge, new Insets(10, 0, 0, 10));
        imgWrap.getChildren().add(catBadge);

        VBox content = new VBox(8);
        content.setPadding(new Insets(14, 16, 14, 16));

        Label name = new Label(product.getName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label desc = new Label(product.getDescription());
        desc.setWrapText(true);
        desc.setMaxHeight(36);
        desc.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        boolean inStock = product.getStock() > 0;
        Label stock = new Label(inStock ? "V  В наличии: " + product.getStock() + " шт." : "X  Нет в наличии");
        stock.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + (inStock ? NEON_GREEN : NEON_PINK) + ";"
        );

        content.getChildren().addAll(name, desc, stock);

        List<Review> reviews = shopService.getReviews(product.getId());
        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            Label starLbl = new Label(String.format("★ %.1f  (%d отзывов)", avg, reviews.size()));
            starLbl.setStyle("-fx-text-fill: " + NEON_ORANGE + "; -fx-font-size: 12px;");
            content.getChildren().add(starLbl);
        }

        Label price = new Label(String.format("%,.0f ₽", product.getPrice()));
        price.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #a7c63d;");

        Region divLine = new Region();
        divLine.setPrefHeight(1);
        divLine.setStyle("-fx-background-color: " + BORDER + ";");

        boolean isFav = currentUser.getFavoriteProductIds().contains(product.getId());

        Button addToCart = cardButton("В КОРЗИНУ", NEON_GREEN);
        addToCart.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addToCart, Priority.ALWAYS);
        addToCart.setOnAction(e -> {
            if (!isRealUser()) {
                alert("Требуется вход", "Войдите в аккаунт, чтобы добавить товар в корзину.");
                return;
            }
            shopService.addToCart(currentUser.getId(), product.getId());
            refreshCountersAsync();
            alert("Корзина", product.getName() + " добавлен в корзину.");
        });

        Button favBtn = cardButtonSmall(isFav ? "♥" : "♡", isFav ? NEON_PINK : TEXT_MUTED);
        favBtn.setOnAction(e -> {
            shopService.toggleFavorite(currentUser, product.getId());
            refreshCountersAsync();
            refreshCatalog();
        });

        Button reviewBtn = cardButtonSmall("★", NEON_BLUE);
        reviewBtn.setOnAction(e -> {
            if (!shopService.hasUserBoughtProduct(currentUser, product.getId())) {
                alert("Ошибка", "Оставить отзыв можно только после покупки.");
                return;
            }
            showReviewDialog(product);
        });

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + BG_ELEVATED + ";" +
                        "-fx-border-color: " + NEON_BLUE + ";" +
                        "-fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, " + NEON_BLUE + "55, 25, 0, 0, 8);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-background-radius: 20; -fx-border-radius: 20;"
        ));

        HBox btnRow = new HBox(8, addToCart, favBtn, reviewBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(price, divLine, btnRow);
        card.getChildren().addAll(imgWrap, content);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ФУТЕР
    // ═══════════════════════════════════════════════════════════════════════
    private HBox buildFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10, 24, 10, 24));
        footer.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-border-color: " + BORDER + " transparent transparent transparent;" +
                        "-fx-border-width: 1 0 0 0;"
        );
        Label label = new Label(
                "PELEGOOD SPORT SHOP  ·  Спортивные товары, питание, оборудование, одежда и инвентарь"
        );
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        footer.getChildren().add(label);
        return footer;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  АВТОРИЗАЦИЯ
    // ═══════════════════════════════════════════════════════════════════════
    private void showAuthScene() {
        currentScreen = Screen.AUTH;

        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: " + BG_DARK + ";");

        // Кастомные вкладки (кнопки)
        Button loginTabBtn = new Button("ВХОД");
        Button registerTabBtn = new Button("РЕГИСТРАЦИЯ");

        HBox tabHeader = new HBox(10, loginTabBtn, registerTabBtn);
        tabHeader.setAlignment(Pos.CENTER);

        // Контейнер для форм
        StackPane contentPane = new StackPane();
        contentPane.setPrefSize(480, 520);
        contentPane.setMaxSize(480, 520);
        contentPane.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 12; -fx-border-color: " + BORDER + "; -fx-border-radius: 12;");

        VBox loginPane = buildLoginPane();
        VBox registerPane = buildRegisterPane(loginTabBtn); // Передаем кнопку входа для переключения

        contentPane.getChildren().addAll(loginPane, registerPane);

        // Начальное состояние
        loginPane.setVisible(true);
        registerPane.setVisible(false);
        styleAuthTab(loginTabBtn, true);
        styleAuthTab(registerTabBtn, false);

        // Логика переключения
        loginTabBtn.setOnAction(e -> {
            loginPane.setVisible(true);
            registerPane.setVisible(false);
            styleAuthTab(loginTabBtn, true);
            styleAuthTab(registerTabBtn, false);
        });

        registerTabBtn.setOnAction(e -> {
            loginPane.setVisible(false);
            registerPane.setVisible(true);
            styleAuthTab(loginTabBtn, false);
            styleAuthTab(registerTabBtn, true);
        });

        centerBox.getChildren().addAll(tabHeader, contentPane);

        BorderPane root = new BorderPane();
        root.setTop(buildTitleBar(stage, "Авторизация"));
        root.setCenter(centerBox);
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        stage.setScene(new Scene(root, 1280, 720));
        stage.centerOnScreen();
    }

    // Вспомогательный метод для красивого стиля кнопок-вкладок
    private void styleAuthTab(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: " + NEON_GREEN + "; -fx-text-fill: #0a0a0f; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30; -fx-font-size: 14px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, " + NEON_GREEN + "88, 10, 0, 0, 2);");
        } else {
            btn.setStyle("-fx-background-color: " + BG_SURFACE + "; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: " + BORDER_BRIGHT + "; -fx-border-radius: 8;");
        }
    }

    private VBox buildLoginPane() {
        VBox box = authFormBox();
        Label title = new Label("ДОБРО ПОЖАЛОВАТЬ");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label sub = new Label("PELEGOOD SPORT SHOP");
        sub.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        TextField     username = darkField("Логин");
        PasswordField password = new PasswordField();
        password.setPromptText("Пароль");
        password.setMaxWidth(360);
        styleDarkInput(password);
        Button login = bigNeonButton("ВОЙТИ", NEON_GREEN);
        login.setMaxWidth(360);
        login.setOnAction(e -> {
            Optional<User> user = authService.login(username.getText(), password.getText());
            if (user.isPresent()) { currentUser = user.get(); showStoreScene(); }
            else alert("Ошибка входа", "Неверный логин или пароль.");
        });
        box.getChildren().addAll(title, sub, username, password, login);
        return box;
    }

    // Меняем параметр на Button
    private VBox buildRegisterPane(Button loginTabBtn) {
        VBox box = authFormBox();
        Label title = new Label("НОВЫЙ АККАУНТ");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        TextField     fullName = darkField("ФИО");
        TextField     email    = darkField("Email");
        TextField     username = darkField("Логин");
        PasswordField password = new PasswordField();
        password.setPromptText("Пароль (не менее 6 символов)");
        password.setMaxWidth(360);
        styleDarkInput(password);

        Button register = bigNeonButton("ЗАРЕГИСТРИРОВАТЬСЯ", NEON_BLUE);
        register.setMaxWidth(360);

        register.setOnAction(e -> {
            String error = authService.register(fullName.getText(), email.getText(),
                    username.getText(), password.getText());
            if (error == null) {
                alert("Успех!", "Аккаунт создан. Теперь войди.");
                loginTabBtn.fire(); // <-- ПРОГРАММНО НАЖИМАЕМ НА КНОПКУ ВХОДА
                fullName.clear(); email.clear(); username.clear(); password.clear();
            } else alert("Ошибка", error);
        });

        box.getChildren().addAll(title, fullName, email, username, password, register);
        return box;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ОТЗЫВЫ
    // ═══════════════════════════════════════════════════════════════════════
    private void showReviewDialog(Product product) {
        Stage modal = modalStage("Отзыв");
        VBox root = darkModalRoot();
        root.getChildren().add(sectionTitle("Отзыв: " + product.getName()));

        // существующие отзывы
        List<Review> existing = shopService.getReviews(product.getId());
        if (!existing.isEmpty()) {
            VBox list = new VBox(6);
            list.setPadding(new Insets(4, 0, 4, 0));
            for (int i = 0; i < Math.min(existing.size(), 3); i++) {
                Review r = existing.get(i);
                Label l = new Label("★".repeat(r.getRating()) + "☆".repeat(5 - r.getRating()) + "  " + r.getText());
                l.setWrapText(true);
                l.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");
                list.getChildren().add(l);
            }
            root.getChildren().add(list);
        }

        TextArea text = new TextArea();
        text.setPromptText("Ваш отзыв...");
        text.setPrefRowCount(4);
        text.setStyle(textAreaStyle());

        ComboBox<Integer> rating = new ComboBox<>();
        rating.getItems().addAll(1, 2, 3, 4, 5);
        rating.setValue(5);

        Button send = bigNeonButton("ОТПРАВИТЬ", NEON_BLUE);
        send.setOnAction(e -> {
            if (text.getText().isBlank()) {
                alert("Ошибка", "Напишите текст отзыва.");
                return;
            }
            shopService.addReview(currentUser.getId(), product.getId(), text.getText().trim(), rating.getValue());
            modal.close();
            refreshCatalog();
            alert("Готово!", "Отзыв добавлен.");
        });

        root.getChildren().addAll(text, rating, send);
        setModalScene(modal, root, 460, 400);
        modal.showAndWait();
    }

    private String textAreaStyle() {
        return "-fx-control-inner-background: " + BG_SURFACE + ";" +
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
                "-fx-background-color: " + BG_SURFACE + ";" +
                "-fx-background-radius: 12; -fx-border-radius: 12;" +
                "-fx-border-color: " + BORDER_BRIGHT + ";" +
                "-fx-font-size: 14px;";
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ИЗБРАННОЕ / КОРЗИНА / ЗАКАЗЫ
    // ═══════════════════════════════════════════════════════════════════════
    private void showFavoritesDialog() {
        if (!isRealUser()) { alert("Вход", "Войдите, чтобы открыть избранное."); return; }

        Stage modal = modalStage("Избранное");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");


        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        List<Product> favorites = shopService.getFavoriteProducts(currentUser);
        if (favorites.isEmpty()) {
            content.getChildren().add(darkCard("Пусто", "Добавь товары в избранное из каталога."));
        } else {
            for (Product p : favorites) {
                HBox line = darkRowCard(p.getName(), String.format("%,.0f ₽", p.getPrice()));
                Button remove = dangerButton("Удалить");
                remove.setOnAction(e -> {
                    shopService.toggleFavorite(currentUser, p.getId());
                    modal.close();
                    showFavoritesDialog();
                    refreshCountersAsync();
                });
                line.getChildren().add(remove);
                content.getChildren().add(line);
            }
        }
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);
        // ------------------------------------------------

        setModalScene(modal, root, 620, 520);
        modal.show();
    }

    private void showCartDialog() {
        Stage modal = modalStage("Корзина");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(22));
        root.setStyle("-fx-background-color: " + BG_DARK + ";");


        cartItemsBox = new VBox(10);
        ScrollPane scroll = new ScrollPane(cartItemsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        HBox bottom = new HBox(12);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(16, 0, 0, 0));
        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        Button checkoutBtn = bigNeonButton("ОФОРМИТЬ ЗАКАЗ", NEON_GREEN);
        Button clearBtn    = ghostButton("Очистить");
        clearBtn.setOnAction(e -> {
            shopService.clearCart(currentUser.getId());
            updateCartUI(shopService.getCartEntries(currentUser), totalLabel, checkoutBtn);
        });
        checkoutBtn.setOnAction(e -> { modal.close(); showCheckoutDialog(); });
        bottom.getChildren().addAll(totalLabel, clearBtn, checkoutBtn);
        root.setBottom(bottom);

        Task<List<ShopService.CartEntry>> task = new Task<>() {
            @Override protected List<ShopService.CartEntry> call() {
                return shopService.getCartEntries(currentUser);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() ->
                updateCartUI(task.getValue(), totalLabel, checkoutBtn)));
        new Thread(task, "cart-dialog-loader").start();

        setModalScene(modal, root, 940, 620);
        modal.show();
    }

    private void updateCartUI(List<ShopService.CartEntry> cart, Label totalLabel, Button checkoutBtn) {
        if (cartItemsBox == null) return;
        cartItemsBox.getChildren().clear();
        if (cart.isEmpty()) {
            cartItemsBox.getChildren().add(darkCard("Корзина пустая", "Добавь товары из каталога."));
        } else {
            for (ShopService.CartEntry entry : cart) {
                Label name = new Label(entry.product().getName());
                name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
                Label info = new Label(entry.quantity() + " шт · " +
                        String.format("%,.0f ₽", entry.product().getPrice() * entry.quantity()));
                info.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 13px;");

                Button minus  = qtyBtn("-", NEON_PINK);
                Button plus   = qtyBtn("+", NEON_GREEN);
                Button remove = qtyBtn("X", TEXT_MUTED);

                // Делаем запросы асинхронными (чтобы окно не зависало)
                minus.setOnAction(e -> processCartAction(entry.product().getId(), -1, totalLabel, checkoutBtn));
// в updateCartUI внутри цикла:
                plus.setOnAction(e -> {
                    // мгновенно увеличиваем счётчик в интерфейсе
                    int newQty = entry.quantity() + 1;
                    if (newQty > entry.product().getStock()) {
                        alert("Внимание", "На складе только " + entry.product().getStock() + " шт.");
                        return;
                    }
                    info.setText(newQty + " шт · " +
                            String.format("%,.0f ₽", entry.product().getPrice() * newQty));
                    totalLabel.setText("Итого: " + String.format("%,.0f ₽",
                            cart.stream().mapToDouble(x -> x.product().getPrice() *
                                    (x.product().getId() == entry.product().getId() ? newQty : x.quantity())).sum()));

                    // и параллельно гоним в БД через дебаунс
                    processCartAction(entry.product().getId(), 1, totalLabel, checkoutBtn);
                });                remove.setOnAction(e -> processCartAction(entry.product().getId(), -entry.quantity(), totalLabel, checkoutBtn));

                HBox btns = new HBox(6, minus, plus, remove);
                VBox texts = new VBox(4, name, info);
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                HBox row = new HBox(12, texts, sp, btns);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(14));
                row.setStyle(
                        "-fx-background-color: " + BG_CARD + ";" +
                                "-fx-background-radius: 14; -fx-border-color: " + BORDER + "; -fx-border-radius: 14;"
                );
                cartItemsBox.getChildren().add(row);
            }
        }
        double total = cart.stream().mapToDouble(e -> e.product().getPrice() * e.quantity()).sum();
        totalLabel.setText("Итого: " + String.format("%,.0f ₽", total));
        checkoutBtn.setDisable(cart.isEmpty());
        updateCounters(cart);
    }

    // НОВЫЙ МЕТОД: Изменение кол-ва в БД в фоне + перерисовка UI
    private void processCartAction(int productId, int delta, Label totalLabel, Button checkoutBtn) {
        Task<String> updateTask = new Task<>() {
            @Override protected String call() {
                return shopService.updateCartQuantity(currentUser.getId(), productId, delta);
            }
        };
        updateTask.setOnSucceeded(evt -> {
            String err = updateTask.getValue();
            if (err != null) {
                alert("Внимание", err);
            }
            refreshCartAsync(totalLabel, checkoutBtn);
        });
        new Thread(updateTask).start();
    }

    // НОВЫЙ МЕТОД: Асинхронная загрузка списка корзины
    private void refreshCartAsync(Label totalLabel, Button checkoutBtn) {
        Task<List<ShopService.CartEntry>> task = new Task<>() {
            @Override protected List<ShopService.CartEntry> call() {
                return shopService.getCartEntries(currentUser);
            }
        };
        task.setOnSucceeded(e -> updateCartUI(task.getValue(), totalLabel, checkoutBtn));
        new Thread(task).start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ОФОРМЛЕНИЕ С ПРОМОКОДОМ
    // ═══════════════════════════════════════════════════════════════════════
    private void showCheckoutDialog() {
        Stage modal = modalStage("Оформление заказа");
        VBox root = darkModalRoot();
        root.getChildren().add(sectionTitle("ОФОРМЛЕНИЕ ЗАКАЗА"));

        TextField phone   = darkField("Телефон");
        TextField address = darkField("Адрес доставки");
        TextArea comment  = new TextArea();
        comment.setPromptText("Комментарий");
        comment.setPrefRowCount(3);
        comment.setStyle(textAreaStyle());

        // ─── Промокод ──────────────────────────────────────────────────────
        HBox promoRow = new HBox(10);
        promoRow.setAlignment(Pos.CENTER_LEFT);
        TextField promoField = darkField("Промокод (если есть)");
        promoField.setMaxWidth(220);
        Button applyPromo = ghostButton("Применить");
        promoRow.getChildren().addAll(promoField, applyPromo);

        Label discountLabel = new Label("");
        discountLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        double baseTotal = shopService.getCartTotal(currentUser);
        Label total = new Label("Сумма: " + String.format("%,.0f ₽", baseTotal));
        total.setStyle(
                "-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 22px; -fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, " + NEON_GREEN + "88, 10, 0, 0, 0);"
        );

        final String[] appliedPromo = { null };

        applyPromo.setOnAction(e -> {
            String code = promoField.getText();
            Integer disc = shopService.getDiscountPercent(code);
            if (disc == null) {
                appliedPromo[0] = null;
                discountLabel.setText("Промокод не найден");
                discountLabel.setStyle("-fx-text-fill: " + NEON_PINK + "; -fx-font-size: 12px;");
                total.setText("Сумма: " + String.format("%,.0f ₽", baseTotal));
            } else {
                appliedPromo[0] = code;
                double finalTotal = baseTotal * (100 - disc) / 100.0;
                discountLabel.setText("Скидка " + disc + "%  (-" +
                        String.format("%,.0f ₽", baseTotal - finalTotal) + ")");
                discountLabel.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 12px; -fx-font-weight: bold;");
                total.setText("Сумма: " + String.format("%,.0f ₽", finalTotal));
            }
        });

        Button confirm = bigNeonButton("ПОДТВЕРДИТЬ ЗАКАЗ", NEON_GREEN);
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setOnAction(e -> {
            String phoneStr = phone.getText().trim();

            // Проверка телефона (разрешены цифры, +, пробелы, скобки и дефис. От 10 до 20 символов)
            if (!phoneStr.matches("^\\+?[0-9\\s\\-\\(\\)]{10,20}$")) {
                alert("Ошибка", "Введите корректный номер телефона (пример: +7 999 123-45-67).");
                return;
            }

            // Проверка адреса (чтобы не вводили одну букву)
            if (address.getText().trim().length() < 5) {
                alert("Ошибка", "Введите более подробный адрес доставки.");
                return;
            }

            String err = shopService.checkout(
                    currentUser, phoneStr, address.getText().trim(),
                    comment.getText(), appliedPromo[0]
            );
            if (err == null) {
                modal.close();
                refreshCountersAsync();
                refreshCatalog();
                alert("Заказ оформлен!", "Спасибо! Менеджер свяжется с вами.");
            } else {
                alert("Ошибка", err);
            }
        });

        root.getChildren().addAll(phone, address, comment, promoRow, discountLabel, total, confirm);
        setModalScene(modal,root, 640, 560);
        modal.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ЗАКАЗЫ
    // ═══════════════════════════════════════════════════════════════════════
    private void showOrdersDialog() {
        Stage modal = modalStage("Заказы");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");


        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        List<Order> orders = shopService.getOrders(currentUser.getId());
        if (orders.isEmpty()) {
            content.getChildren().add(darkCard("Заказов нет", "Оформленные заказы появятся здесь."));
        } else {
            for (Order o : orders) {
                VBox orderCard = new VBox(8);
                orderCard.setPadding(new Insets(15));
                orderCard.setStyle(
                        "-fx-background-color: " + BG_CARD + ";" +
                                "-fx-border-color: " + NEON_BLUE + ";" +
                                "-fx-background-radius: 12; -fx-border-radius: 12;"
                );
                Label id = new Label("Заказ #" + o.getId());
                id.setStyle("-fx-text-fill: " + NEON_BLUE + "; -fx-font-weight: bold;");
                Label total = new Label("Сумма: " + String.format("%,.0f ₽", o.getTotal()));
                total.setStyle("-fx-text-fill: #a7c63d; -fx-font-weight: bold;");
                String status = o.getStatus() != null ? o.getStatus() : "В обработке";
                Label statusLabel = new Label(status);
                String color = status.equals("Доставлен") ? NEON_GREEN :
                        status.equals("Отменен") ? NEON_PINK : NEON_BLUE;
                statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-border-color: " + color + "; -fx-border-radius: 4; -fx-padding: 2 6 2 6;");

                HBox headerRow = new HBox(10, id, total, statusLabel);
                headerRow.setAlignment(Pos.CENTER_LEFT);
                orderCard.getChildren().add(headerRow);
                for (var line : o.getLines()) {
                    Label productLine = new Label("• " + line.getProductName() +
                            " — " + line.getQuantity() + " шт. x " +
                            String.format("%,.0f ₽", line.getUnitPrice()));
                    productLine.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 12px;");
                    orderCard.getChildren().add(productLine);
                }
                content.getChildren().add(orderCard);
            }
        }
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);
        // ------------------------------------------------

        setModalScene(modal, root, 620, 520);
        modal.show();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  АДМИНКА
    // ═══════════════════════════════════════════════════════════════════════
    private void showAdminPanel() {
        Stage modal = modalStage("Панель администратора");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        VBox form = darkModalRoot();
        form.getChildren().add(sectionTitle("РЕДАКТОР ТОВАРОВ"));
        TextField idField       = darkField("ID (пусто = новый товар)");
        TextField nameField     = darkField("Название");

        // --- ЗАМЕНА ТЕКСТОВОГО ПОЛЯ НА ВЫПАДАЮЩИЙ СПИСОК ---
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.setEditable(true); // Разрешаем вписывать свои значения
        categoryBox.setMaxWidth(360);
        categoryBox.setPromptText("Выберите или введите категорию");
        categoryBox.setStyle(
                "-fx-background-color: " + BG_SURFACE + ";" +
                        "-fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-border-color: " + BORDER_BRIGHT + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-font-size: 14px;"
        );

        // Загружаем существующие категории из БД
        List<String> existingCategories = shopService.getCategories();
        existingCategories.remove("Все"); // Убираем служебную категорию "Все"
        categoryBox.getItems().addAll(existingCategories);
        // ----------------------------------------------------

        TextField priceField    = darkField("Цена");
        TextField stockField    = darkField("Остаток");

        TextField imageField    = darkField("Путь к картинке");
        imageField.setPromptText("Перетащите картинку сюда");

        imageField.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        });

        imageField.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                String fileName = file.getName();
                try {
                    File dir = new File("images");
                    if (!dir.exists()) dir.mkdirs();
                    File dest = new File(dir, fileName);
                    Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    imageField.setText("images/" + fileName);
                    success = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        TextArea  descArea      = new TextArea();
        descArea.setPromptText("Описание");
        descArea.setPrefRowCount(4);
        descArea.setStyle(textAreaStyle());

        Button save = bigNeonButton("СОХРАНИТЬ", NEON_GREEN);
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            try {
                Product p = new Product();
                String idText = idField.getText().trim();

                if (!idText.isEmpty()) {
                    try {
                        p.setId(Integer.parseInt(idText));
                    } catch (NumberFormatException ex) {
                        alert("Ошибка ввода", "В поле ID должно быть только число.");
                        return;
                    }
                }

                p.setName(nameField.getText().trim());
                // Получаем текст из выпадающего списка (даже если он введен вручную)
                p.setCategory(categoryBox.getEditor().getText().trim());
                p.setDescription(descArea.getText().trim());
                p.setPrice(Double.parseDouble(priceField.getText().trim().replace(",", ".")));
                p.setStock(Integer.parseInt(stockField.getText().trim()));
                p.setImagePath(imageField.getText().isBlank() ? "/images/placeholder.png" : imageField.getText().trim());

                if (p.getName().isEmpty() || p.getCategory().isEmpty()) {
                    alert("Ошибка", "Заполните Название и Категорию.");
                    return;
                }

                save.setDisable(true);
                save.setText("СОХРАНЕНИЕ...");

                Task<Void> task = new Task<>() {
                    @Override protected Void call() {
                        shopService.saveProduct(p);
                        return null;
                    }
                };

                task.setOnSucceeded(evt -> Platform.runLater(() -> {
                    alert("Готово!", "Товар сохранён.");

                    // Очистка формы после сохранения
                    idField.clear();
                    nameField.clear();
                    categoryBox.setValue(null);
                    categoryBox.getEditor().clear();
                    priceField.clear();
                    stockField.clear();
                    imageField.clear();
                    descArea.clear();

                    save.setDisable(false);
                    save.setText("СОХРАНИТЬ");

                    refreshCatalog();
                    refreshCountersAsync();
                    modal.close();
                    showAdminPanel();
                }));

                task.setOnFailed(evt -> Platform.runLater(() -> {
                    save.setDisable(false);
                    save.setText("СОХРАНИТЬ");
                    Throwable ex = task.getException();
                    alert("Ошибка БД", ex.getMessage() != null ? ex.getMessage() : "Не удалось сохранить товар");
                    ex.printStackTrace();
                }));

                new Thread(task).start();

            } catch (Exception ex) {
                alert("Ошибка ввода", "Проверьте правильность заполнения цены и остатка.");
            }
        });

        form.getChildren().addAll(idField, nameField, categoryBox, priceField,
                stockField, imageField, descArea, save);
        root.setLeft(form);

        // --- АСИНХРОННАЯ ЗАГРУЗКА СПИСКА (остается без изменений) ---
        VBox list = new VBox(10);
        list.setPadding(new Insets(0, 0, 0, 20));
        list.getChildren().add(sectionTitle("ТОВАРЫ"));

        Label loadingLabel = new Label("Загрузка товаров...");
        loadingLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + ";");
        list.getChildren().add(loadingLabel);

        ScrollPane listScroll = new ScrollPane(list);
        listScroll.setFitToWidth(true);
        listScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(listScroll);

        setModalScene(modal,root, 1200, 700);
        modal.show();

        Task<List<Product>> loadTask = new Task<>() {
            @Override protected List<Product> call() {
                return shopService.getProducts();
            }
        };

        loadTask.setOnSucceeded(evt -> Platform.runLater(() -> {
            list.getChildren().clear();
            list.getChildren().add(sectionTitle("ТОВАРЫ"));
            List<Product> products = loadTask.getValue();

            if (products == null || products.isEmpty()) {
                list.getChildren().add(new Label("Список товаров пуст."));
                return;
            }

            for (Product p : products) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 14, 12, 14));
                row.setStyle(
                        "-fx-background-color: " + BG_CARD + ";" +
                                "-fx-background-radius: 12; -fx-border-color: " + BORDER + "; -fx-border-radius: 12;"
                );

                Label nameLbl = new Label("#" + p.getId() + "  " + p.getName());
                nameLbl.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px;");
                nameLbl.setMaxWidth(250);

                Label infoLbl = new Label(p.getCategory() + " · " + String.format("%,.0f ₽", p.getPrice()));
                infoLbl.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 13px;");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Button fill = ghostButton("Заполнить");
                fill.setOnAction(e -> {
                    idField.setText(String.valueOf(p.getId()));
                    nameField.setText(p.getName());
                    // Устанавливаем значение в выпадающий список
                    categoryBox.setValue(p.getCategory());
                    categoryBox.getEditor().setText(p.getCategory());
                    priceField.setText(String.valueOf((int) p.getPrice()));
                    stockField.setText(String.valueOf(p.getStock()));
                    imageField.setText(p.getImagePath());
                    descArea.setText(p.getDescription());
                });

                Button del = dangerButton("Удалить");
                del.setOnAction(e -> {
                    del.setDisable(true);
                    Task<Void> t = new Task<>() {
                        @Override protected Void call() {
                            shopService.deleteProduct(p.getId());
                            return null;
                        }
                    };
                    t.setOnSucceeded(ev -> {
                        modal.close();
                        refreshCatalog();
                        refreshCountersAsync();
                        showAdminPanel();
                    });
                    new Thread(t).start();
                });

                row.getChildren().addAll(nameLbl, infoLbl, sp, fill, del);
                list.getChildren().add(row);
            }
        }));

        loadTask.setOnFailed(evt -> Platform.runLater(() -> {
            list.getChildren().clear();
            list.getChildren().add(new Label("Ошибка загрузки списка. Смотрите консоль."));
            loadTask.getException().printStackTrace();
        }));

        new Thread(loadTask).start();
    }
    private void showAdminOrdersDialog() {
        Stage modal = modalStage("Управление заказами");
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        root.setTop(sectionTitle("ВСЕ ЗАКАЗЫ"));

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        Label loading = new Label("Загрузка заказов...");
        loading.setStyle("-fx-text-fill: " + TEXT_MUTED + ";");
        content.getChildren().add(loading);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);

        setModalScene(modal,root, 850, 650);
        modal.show();

        // Загружаем все заказы в фоне
        Task<List<Order>> task = new Task<>() {
            @Override protected List<Order> call() {
                return shopService.getAllOrders();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            content.getChildren().clear();
            List<Order> orders = task.getValue();
            if (orders.isEmpty()) {
                content.getChildren().add(darkCard("Заказов нет", "Пока никто ничего не заказал."));
                return;
            }

            for (Order o : orders) {
                VBox card = new VBox(8);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER + "; -fx-background-radius: 12; -fx-border-radius: 12;");

                // Шапка карточки заказа
                Label id = new Label("Заказ #" + o.getId() + "  |  " + o.getCustomerName());
                id.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 15px;");

                Label phone = new Label(o.getPhone() + "  |  " + o.getAddress());
                phone.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");

                Label total = new Label("Сумма: " + String.format("%,.0f ₽", o.getTotal()));
                total.setStyle("-fx-text-fill: #a7c63d; -fx-font-weight: bold;");

                // Выпадающий список для смены статуса
                ComboBox<String> statusBox = new ComboBox<>();
                statusBox.getItems().addAll("В обработке", "Отправлен", "Доставлен", "Отменен");
                statusBox.setValue(o.getStatus() != null ? o.getStatus() : "В обработке");

                // Стиль самой рамки ComboBox
                statusBox.setStyle(
                        "-fx-background-color: " + BG_SURFACE + ";" +
                                "-fx-border-color: " + NEON_BLUE + ";" +
                                "-fx-border-radius: 6; -fx-background-radius: 6;" +
                                "-fx-font-size: 13px; -fx-padding: 2 5 2 5;"
                );

                // ФИКС: Настраиваем ячейку, которая отображается как выбранный элемент
                statusBox.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item);
                            setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");
                        }
                    }
                });

                // ФИКС: Настраиваем ячейки в выпадающем списке
                statusBox.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("-fx-background-color: " + BG_SURFACE + ";");
                        } else {
                            setText(item);
                            setStyle(
                                    "-fx-background-color: " + BG_SURFACE + ";" +
                                            "-fx-text-fill: " + TEXT_PRIMARY + ";" + // Цвет текста в списке
                                            "-fx-padding: 8;"
                            );
                        }
                    }
                });
                statusBox.setOnAction(evt -> {
                    String newStatus = statusBox.getValue();
                    if (!newStatus.equals(o.getStatus())) {
                        // Сохраняем в фоне, чтобы не фризить окно
                        new Thread(() -> {
                            shopService.updateOrderStatus(o.getId(), newStatus);
                            Platform.runLater(() -> alert("Успех", "Статус заказа #" + o.getId() + " изменен на: " + newStatus));
                        }).start();
                    }
                });

                HBox header = new HBox(15, id, new Region(), statusBox);
                HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
                header.setAlignment(Pos.CENTER_LEFT);

                card.getChildren().addAll(header, phone, total);

                // Товары в заказе
                for (var line : o.getLines()) {
                    Label pLine = new Label("• " + line.getProductName() + " — " + line.getQuantity() + " шт.");
                    pLine.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
                    card.getChildren().add(pLine);
                }
                content.getChildren().add(card);
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            content.getChildren().clear();
            content.getChildren().add(new Label("Ошибка загрузки заказов. Смотрите консоль."));
            task.getException().printStackTrace();
        }));

        new Thread(task).start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ИНФО-ДИАЛОГИ
    // ═══════════════════════════════════════════════════════════════════════
    private void showContactsDialog() {
        alert("Контакты",
                "PELEGOOD SPORT SHOP\nТел: +7 961-053-06-11\nEmail: pelegoodsport@mail.ru\n" +
                        "Адрес: г. Энгельс, ул. Пушкина, 15\nРежим: 09:00 – 21:00");
    }

    private void showAboutDialog() {
        alert("О магазине",
                "PELEGOOD SPORT SHOP — обширный магазин спортивных товаров.\n" +
                        "Здесь вы найдёте товары на любой вкус и цвет!");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  СЧЁТЧИКИ
    // ═══════════════════════════════════════════════════════════════════════
    private void updateCounters(List<ShopService.CartEntry> cart) {
        if (favoriteNavLabel != null) {
            int favCount = currentUser.getFavoriteProductIds().size();
            favoriteNavLabel.setText(favCount > 0 ? "Избранное (" + favCount + ")" : "Избранное");
        }
        if (cartNavLabel != null) {
            int count = cart.stream().mapToInt(ShopService.CartEntry::quantity).sum();
            // Показываем только количество, чтобы не перегружать панель
            cartNavLabel.setText(count > 0 ? "Корзина (" + count + ")" : "Корзина");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UI-ХЕЛПЕРЫ
    // ═══════════════════════════════════════════════════════════════════════
    private VBox authFormBox() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(36, 40, 36, 40));
        box.setStyle("-fx-background-color: " + BG_CARD + ";");
        return box;
    }

    private VBox darkModalRoot() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        box.setStyle("-fx-background-color: " + BG_DARK + ";");
        return box;
    }

    private TextField darkField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setMaxWidth(360);
        styleDarkInput(f);
        return f;
    }

    private void styleDarkInput(Control c) {
        c.setStyle(
                "-fx-background-color: " + BG_SURFACE + ";" +
                        "-fx-background-radius: 12; -fx-border-radius: 12;" +
                        "-fx-padding: 11 16 11 16;" +
                        "-fx-border-color: " + BORDER_BRIGHT + ";" +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-prompt-text-fill: " + TEXT_MUTED + ";"
        );
    }

    private Button neonButton(String text, String color) {
        Button b = new Button(text);
        b.setMinWidth(Region.USE_PREF_SIZE);
        b.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + color + ";" +
                        "-fx-font-weight: bold; -fx-font-size: 12px;" +
                        "-fx-background-radius: 8; -fx-border-color: " + color + "66; -fx-border-radius: 8;" +
                        "-fx-padding: 8 16 8 16;"
        );
        return b;
    }

    private Button bigNeonButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: #0a0a0f;" +
                        "-fx-font-weight: bold; -fx-font-size: 14px;" +
                        "-fx-background-radius: 12; -fx-padding: 12 24 12 24;" +
                        "-fx-effect: dropshadow(gaussian, " + color + "88, 12, 0, 0, 4);"
        );
        return b;
    }

    private Button ghostButton(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-font-size: 13px; -fx-background-radius: 8;" +
                        "-fx-border-color: " + BORDER_BRIGHT + "; -fx-border-radius: 8;" +
                        "-fx-padding: 8 14 8 14;"
        );
        return b;
    }

    private Button cardButton(String text, String color) {
        Button b = new Button(text);

        // Выносим базовые настройки в отдельную строку, чтобы не дублировать их
        String baseStyle = "-fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 10 18 10 18; ";

        // Устанавливаем обычный вид
        b.setStyle(baseStyle + "-fx-background-color: " + color + "; -fx-text-fill: #0a0a0f;");

        // При наведении меняем ТОЛЬКО цвет, оставляя форму и отступы
        b.setOnMouseEntered(e -> b.setStyle(
                baseStyle + "-fx-background-color: #ffffff; -fx-text-fill: #0a0a0f;"
        ));

        // При убирании мыши возвращаем цвет, форма и отступы остаются
        b.setOnMouseExited(e -> b.setStyle(
                baseStyle + "-fx-background-color: " + color + "; -fx-text-fill: #0a0a0f;"
        ));

        return b;
    }

    private Button cardButtonSmall(String text, String color) {
        Button b = new Button(text);
        b.setMinWidth(38);
        b.setPrefWidth(38);
        b.setStyle(
                "-fx-background-color: " + BG_SURFACE + "; -fx-text-fill: " + color + ";" +
                        "-fx-font-size: 15px; -fx-background-radius: 10;" +
                        "-fx-border-color: " + BORDER + "; -fx-border-radius: 10;" +
                        "-fx-padding: 8 6 8 6;"
        );
        return b;
    }

    private Button sideNavButton(String text, String color) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(
                "-fx-background-color: " + color + "15; -fx-text-fill: " + color + ";" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-radius: 12; -fx-border-color: " + color + "33; -fx-border-radius: 12;" +
                        "-fx-padding: 11 16 11 16; -fx-alignment: center-left;"
        );
        return b;
    }

    private Button dangerButton(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: " + NEON_PINK + "22; -fx-text-fill: " + NEON_PINK + ";" +
                        "-fx-font-weight: bold; -fx-background-radius: 8;" +
                        "-fx-border-color: " + NEON_PINK + "44; -fx-border-radius: 8;" +
                        "-fx-padding: 8 14 8 14;"
        );
        return b;
    }

    private Button qtyBtn(String text, String color) {
        Button b = new Button(text);
        b.setPrefWidth(34);
        b.setStyle(
                "-fx-background-color: " + color + "22; -fx-text-fill: " + color + ";" +
                        "-fx-font-weight: bold; -fx-font-size: 16px;" +
                        "-fx-background-radius: 8; -fx-border-color: " + color + "44; -fx-border-radius: 8;" +
                        "-fx-padding: 5 8 5 8;"
        );
        return b;
    }



    private Label sectionTitle(String text) {
        Label l = new Label(text);
        String color = NEON_BLUE;
        if (text.contains("ИЗБРАННОЕ")) color = NEON_ORANGE;
        else if (text.contains("ЗАКАЗЫ")) color = NEON_BLUE;
        l.setStyle(
                "-fx-text-fill: " + color + "; -fx-font-size: 26px; -fx-font-weight: bold;"
        );
        return l;
    }

    private VBox darkCard(String title, String body) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-background-radius: 14; -fx-border-color: " + BORDER + "; -fx-border-radius: 14;"
        );
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label c = new Label(body);
        c.setWrapText(true);
        c.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");
        box.getChildren().addAll(t, c);
        return box;
    }
    private VBox buildTopNavItem(String icon, Label textLabel, String color, Runnable action) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px; -fx-text-fill: " + color + ";");

        textLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + TEXT_PRIMARY + ";");
        textLabel.setMinWidth(Region.USE_PREF_SIZE); // Запрещаем сжимать текст

        VBox box = new VBox(2, iconLbl, textLabel);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(Region.USE_PREF_SIZE);
        box.setStyle("-fx-cursor: hand; -fx-padding: 5 10;");

        // Анимация при наведении
        box.setOnMouseEntered(e -> {
            textLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");
            iconLbl.setScaleX(1.15); iconLbl.setScaleY(1.15);
        });
        box.setOnMouseExited(e -> {
            textLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + TEXT_PRIMARY + ";");
            iconLbl.setScaleX(1.0); iconLbl.setScaleY(1.0);
        });

        box.setOnMouseClicked(e -> action.run());
        return box;
    }
    private HBox darkRowCard(String left, String right) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-background-radius: 12; -fx-border-color: " + BORDER + "; -fx-border-radius: 12;"
        );
        Label l = new Label(left);
        l.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label r = new Label(right);
        r.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 13px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().addAll(l, sp, r);
        return row;
    }

    private Region neonDivider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: linear-gradient(to right, transparent, " +
                NEON_BLUE + "44, transparent);");
        return r;
    }

    private VBox buildBanner() {
        VBox box = new VBox();
        box.setPadding(new Insets(20));

        StackPane banner = new StackPane();

        // ЖЕСТКО ФИКСИРУЕМ ВЫСОТУ БАННЕРА
        banner.setPrefHeight(180);
        banner.setMinHeight(180);
        banner.setMaxHeight(180);

        ImageView bgImage = new ImageView(loadImage("/images/Reebok Is The Best Brand.jpg"));
        bgImage.setPreserveRatio(false);

        // Привязываем размеры картинки к размерам баннера
        bgImage.fitWidthProperty().bind(banner.widthProperty());
        bgImage.fitHeightProperty().bind(banner.heightProperty());

        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: linear-gradient(to right, rgba(10,10,15,0.9) 0%, rgba(10,10,15,0.5) 50%, transparent 100%);");

        // Обрезаем углы
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(banner.widthProperty());
        clip.heightProperty().bind(banner.heightProperty());
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        banner.setClip(clip);

        Label title = new Label("НОВАЯ КОЛЛЕКЦИЯ");
        title.setStyle("-fx-text-fill: " + NEON_GREEN + "; -fx-font-size: 28px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 10, 0.8, 0, 0);");

        Label sub = new Label("Экипировка для твоих побед");
        sub.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 5, 0.8, 0, 0);");

        VBox text = new VBox(8, title, sub);
        text.setAlignment(Pos.CENTER_LEFT);
        text.setPadding(new Insets(30));

        StackPane.setAlignment(text, Pos.CENTER_LEFT);

        banner.getChildren().addAll(bgImage, overlay, text);
        box.getChildren().add(banner);
        return box;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  МОДАЛЬНЫЕ ОКНА / ALERT / IMAGE
    // ═══════════════════════════════════════════════════════════════════════
    private Stage modalStage(String title) {
        Stage modal = new Stage();
        modal.initOwner(stage);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.UNDECORATED);
        modal.setTitle(title);
        return modal;
    }
    private void setModalScene(Stage modal, Parent content, double width, double height) {
        BorderPane wrapper = new BorderPane();
        wrapper.setTop(buildTitleBar(modal, modal.getTitle()));
        wrapper.setCenter(content);
        wrapper.setStyle("-fx-background-color: " + BG_DARK + ";");

        Scene scene = new Scene(wrapper, width, height);

        // Подключаем стили (включая кастомный скроллбар) к модальному окну
        try {
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("CSS файл не найден.");
        }
        if (!darkTheme) scene.getRoot().getStyleClass().add("light-theme");

        modal.setScene(scene);
    }
    private Image loadImage(String path) {
        if (path == null || path.isBlank()) path = "/images/placeholder.png";

        // 1. Ищем в ресурсах (стандартные картинки)
        InputStream s = getClass().getResourceAsStream(path);
        if (s != null) return new Image(s);

        // 2. Ищем в локальной папке (если картинку перетащили)
        try {
            File f = new File(path);
            if (f.exists()) return new Image(f.toURI().toString());
        } catch (Exception e) {}

        // 3. Заглушка, если ничего не нашли
        InputStream placeholder = getClass().getResourceAsStream("/images/placeholder.png");
        return placeholder != null ? new Image(placeholder) : new Image("https://via.placeholder.com/286x175?text=No+Image");
    }

    private void alert(String title, String message) {
        showCustomAlert(message);
    }

    private void showCustomAlert(String text) {
        Stage modal = new Stage();
        modal.initOwner(stage);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(StageStyle.UNDECORATED);
        modal.setTitle("Сообщение");

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        // Убрали синюю рамку, оставили только темный фон
        root.setStyle(
                "-fx-background-color: " + BG_DARK + ";" +
                        "-fx-background-radius: 12;"
        );

        Label label = new Label(text);
        label.setWrapText(true);
        // Увеличили максимальную ширину текста, чтобы он влезал
        label.setMaxWidth(400);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        Button ok = new Button("OK");
        ok.setStyle(
                "-fx-background-color: " + NEON_GREEN + "; -fx-text-fill: #0a0a0f;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;"
        );
        ok.setOnAction(e -> modal.close());

        root.getChildren().addAll(label, ok);

        // Увеличили размер самого окна (ширина 450, высота 200)
        setModalScene(modal, root, 450, 200);

        modal.setResizable(false);
        modal.showAndWait();
    }

    private VBox buildBrandPanel() {
        VBox box = new VBox(20);
        box.setPrefWidth(280);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_CENTER);
        box.setStyle(
                "-fx-background-color: " + BG_DARK + ";" +
                        "-fx-border-color: transparent transparent transparent " + BORDER + ";" +
                        "-fx-border-width: 0 0 0 1;"
        );

        ImageView logo = new ImageView(loadImage("/images/logotip.png"));
        logo.setFitWidth(180);
        logo.setPreserveRatio(true);

        Label title = new Label("СПОРТ. СТИЛЬ. СИЛА.");

        // --- ЗДЕСЬ МЕНЯЕМ ЦВЕТ НА RGB-ГРАДИЕНТ ---
        title.setStyle(
                "-fx-text-fill: linear-gradient(to right, " + NEON_BLUE + ", " + NEON_PURPLE + ", " + NEON_PINK + ", " + NEON_GREEN + ");" +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, " + NEON_PURPLE + "88, 10, 0, 0, 0);" // Легкое неоновое свечение
        );
        // -----------------------------------------

        Label text = new Label("Экипировка и товары\nдля спорта и жизни");
        text.setAlignment(Pos.CENTER);
        text.setWrapText(true);
        text.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");

        box.getChildren().addAll(logo, title, text);
        return box;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  СОВМЕСТИМОСТЬ (старые вызовы)
    // ═══════════════════════════════════════════════════════════════════════
    private VBox infoCard(String t, String b)      { return darkCard(t, b); }
    private HBox rowCard(String l, String r)       { return darkRowCard(l, r); }
    private Button primaryButton(String t)         { return bigNeonButton(t, NEON_BLUE); }
    private Button secondaryButton(String t)       { return ghostButton(t); }
    private TextField textField(String p)          { return darkField(p); }
    private void styleAccent(Button b, String c)   { /* no-op */ }

    public static void main(String[] args) { launch(args); }
}