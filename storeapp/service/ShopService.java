package com.example.storeapp.service;

import com.example.storeapp.model.Order;
import com.example.storeapp.model.Product;
import com.example.storeapp.model.User;
import com.example.storeapp.model.OrderLine;
import com.example.storeapp.model.Review;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShopService {

    public record CartEntry(Product product, int quantity) {}

    private List<Product> cachedProducts = null;


    private final Map<Integer, List<Review>> reviewsByProduct = new ConcurrentHashMap<>();
    private static final Map<String, Integer> PROMO_CODES = new HashMap<>();
    static {
        PROMO_CODES.put("SPORT10",  10);
        PROMO_CODES.put("PELEGOOD", 15);
        PROMO_CODES.put("WELCOME5",  5);
        PROMO_CODES.put("SUMMER20", 20);
    }

    public ShopService() {
        initReviews();
    }

    // ─── ИНИЦИАЛИЗАЦИЯ ОТЗЫВОВ ─────────────────────────────────────────────
    private void initReviews() {
        try (Connection conn = Database.connect()) {
            var stmt = conn.createStatement();
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS reviews (" +
                            "id SERIAL PRIMARY KEY, " +
                            "user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
                            "product_id INT NOT NULL REFERENCES products(id) ON DELETE CASCADE, " +
                            "text TEXT NOT NULL, " +
                            "rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5), " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            var rs = stmt.executeQuery(
                    "SELECT user_id, product_id, text, rating FROM reviews ORDER BY created_at DESC"
            );
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                reviewsByProduct
                        .computeIfAbsent(productId, k -> new ArrayList<>())
                        .add(new Review(
                                rs.getInt("user_id"),
                                productId,
                                rs.getString("text"),
                                rs.getInt("rating")
                        ));
            }
        } catch (Exception e) {
            System.err.println("Reviews init failed: " + e.getMessage());
        }
    }

    // ─── ОТЗЫВЫ ────────────────────────────────────────────────────────────
    public void addReview(int userId, int productId, String text, int rating) {
        try (Connection conn = Database.connect()) {
            var stmt = conn.prepareStatement(
                    "INSERT INTO reviews(user_id, product_id, text, rating) VALUES (?, ?, ?, ?)"
            );
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setString(3, text);
            stmt.setInt(4, rating);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reviewsByProduct
                .computeIfAbsent(productId, k -> new ArrayList<>())
                .add(0, new Review(userId, productId, text, rating));
    }

    public List<Review> getReviews(int productId) {
        return reviewsByProduct.getOrDefault(productId, List.of());
    }

    // ─── ТОВАРЫ ────────────────────────────────────────────────────────────
    public List<Product> getProducts() {
        List<Product> list = new ArrayList<>();
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(true); // Гарантируем, что читаем самые свежие данные
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT * FROM products ORDER BY id ASC");
            while (rs.next()) list.add(mapProduct(rs));
            System.out.println(">>> ВСЕГО ЗАГРУЖЕНО ТОВАРОВ ИЗ БАЗЫ: " + list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Product mapProduct(java.sql.ResultSet rs) throws Exception {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setStock(rs.getInt("stock"));
        p.setImagePath(rs.getString("image"));
        return p;
    }

    /**
     * Расширенный поиск.
     * @param minPrice   null — без ограничения снизу
     * @param maxPrice   null — без ограничения сверху
     * @param onlyInStock true — только с ненулевым остатком
     */
    public List<Product> searchProducts(String search, String category,
                                        Double minPrice, Double maxPrice,
                                        boolean onlyInStock) {
        return getProducts().stream()
                .filter(p -> search == null || search.isBlank()
                        || p.getName().toLowerCase().contains(search.toLowerCase())
                        || (p.getDescription() != null
                        && p.getDescription().toLowerCase().contains(search.toLowerCase())))
                .filter(p -> category == null || category.equals("Все")
                        || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .filter(p -> !onlyInStock || p.getStock() > 0)
                .toList();
    }

    /** Совместимость со старым вызовом. */
    public List<Product> searchProducts(String search, String category) {
        return searchProducts(search, category, null, null, false);
    }

    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        list.add("Все");
        try (Connection conn = Database.connect()) {
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT DISTINCT category FROM products");
            while (rs.next()) list.add(rs.getString("category"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── КОРЗИНА ───────────────────────────────────────────────────────────
    public String addToCart(int userId, int productId) {
        try (Connection conn = Database.connect()) {
            // 1. Узнаем остаток на складе
            var pStmt = conn.prepareStatement("SELECT stock FROM products WHERE id = ?");
            pStmt.setInt(1, productId);
            var pRs = pStmt.executeQuery();
            if (!pRs.next()) return "Товар не найден";
            int stock = pRs.getInt("stock");

            // 2. Узнаем текущее количество в корзине
            var cStmt = conn.prepareStatement("SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?");
            cStmt.setInt(1, userId);
            cStmt.setInt(2, productId);
            var cRs = cStmt.executeQuery();
            int currentQty = cRs.next() ? cRs.getInt("quantity") : 0;

            if (currentQty + 1 > stock) {
                return "На складе только " + stock + " шт.";
            }

            // 3. Добавляем/Обновляем
            var stmt = conn.prepareStatement(
                    "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, 1) " +
                            "ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart.quantity + 1"
            );
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            return null; // Успех
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка БД";
        }
    }


    public String updateCartQuantity(int userId, int productId, int delta) {
        try (Connection conn = Database.connect()) {
            if (delta > 0) {
                var pStmt = conn.prepareStatement("SELECT stock FROM products WHERE id = ?");
                pStmt.setInt(1, productId);
                var pRs = pStmt.executeQuery();
                int stock = pRs.next() ? pRs.getInt("stock") : 0;

                var cStmt = conn.prepareStatement("SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?");
                cStmt.setInt(1, userId);
                cStmt.setInt(2, productId);
                var cRs = cStmt.executeQuery();
                int currentQty = cRs.next() ? cRs.getInt("quantity") : 0;

                if (currentQty + delta > stock) {
                    return "На складе только " + stock + " шт.";
                }
            }

            var update = conn.prepareStatement(
                    "UPDATE cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?"
            );
            update.setInt(1, delta);
            update.setInt(2, userId);
            update.setInt(3, productId);
            update.executeUpdate();

            conn.prepareStatement("DELETE FROM cart WHERE quantity <= 0").executeUpdate();
            return null; // Успех
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка БД";
        }
    }

    public void clearCart(int userId) {
        try (Connection conn = Database.connect()) {
            var stmt = conn.prepareStatement("DELETE FROM cart WHERE user_id = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CartEntry> getCartEntries(User user) {
        List<CartEntry> list = new ArrayList<>();
        try (Connection conn = Database.connect()) {
            var stmt = conn.prepareStatement(
                    "SELECT p.*, c.quantity FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id = ?"
            );
            stmt.setInt(1, user.getId());
            var rs = stmt.executeQuery();
            while (rs.next()) list.add(new CartEntry(mapProduct(rs), rs.getInt("quantity")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getCartTotal(User user) {
        return getCartTotal(getCartEntries(user));
    }

    public double getCartTotal(List<CartEntry> entries) {
        return entries.stream()
                .mapToDouble(e -> e.product().getPrice() * e.quantity())
                .sum();
    }

    // ─── ПРОМОКОДЫ ─────────────────────────────────────────────────────────

    public Integer getDiscountPercent(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) return null;
        return PROMO_CODES.get(promoCode.trim().toUpperCase());
    }

    public static List<String> getActivePromoCodes() {
        return new ArrayList<>(PROMO_CODES.keySet());
    }

    // ─── ИЗБРАННОЕ ─────────────────────────────────────────────────────────
    public void toggleFavorite(User user, int productId) {
        if (user.getFavoriteProductIds().contains(productId)) {
            user.getFavoriteProductIds().remove(Integer.valueOf(productId));
        } else {
            user.getFavoriteProductIds().add(productId);
        }
    }

    public List<Product> getFavoriteProducts(User user) {
        return getProducts().stream()
                .filter(p -> user.getFavoriteProductIds().contains(p.getId()))
                .toList();
    }

    // ─── ЗАКАЗЫ ────────────────────────────────────────────────────────────
    public String checkout(User user, String phone, String address,
                           String comment, String promoCode) {
        if (user == null || user.getId() == 0) return "Сначала войдите в аккаунт";
        if (phone == null || phone.isBlank()) return "Введите телефон";
        if (address == null || address.isBlank()) return "Введите адрес доставки";

        // Начинаем транзакцию (если что-то упадет, база откатит все изменения)
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false);
            try {
                // 1. Получаем актуальную корзину ИЗ ЭТОГО ЖЕ СОЕДИНЕНИЯ (чтобы избежать конфликтов)
                List<CartEntry> entries = new ArrayList<>();
                var cartStmt = conn.prepareStatement(
                        "SELECT p.*, c.quantity FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id = ? FOR UPDATE"
                );
                cartStmt.setInt(1, user.getId());
                var cartRs = cartStmt.executeQuery();
                while (cartRs.next()) {
                    entries.add(new CartEntry(mapProduct(cartRs), cartRs.getInt("quantity")));
                }

                if (entries.isEmpty()) return "Корзина пустая";

                // 2. Проверяем наличие на складе и списываем остатки
                var updateStockStmt = conn.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE id = ?"
                );

                for (CartEntry entry : entries) {
                    int currentStock = entry.product().getStock();
                    int requestedQty = entry.quantity();

                    if (currentStock < requestedQty) {
                        conn.rollback(); // Отменяем изменения, если товара не хватает
                        return "Недостаточно товара: " + entry.product().getName() +
                                ". В наличии: " + currentStock + " шт.";
                    }

                    updateStockStmt.setInt(1, requestedQty);
                    updateStockStmt.setInt(2, entry.product().getId());
                    updateStockStmt.executeUpdate();
                }

                // 3. Считаем сумму с учетом промокода
                double total = getCartTotal(entries);
                Integer discount = getDiscountPercent(promoCode);
                if (discount != null) {
                    total = total * (100 - discount) / 100.0;
                }

                // 4. Создаем сам заказ
                var orderStmt = conn.prepareStatement(
                        "INSERT INTO orders(user_id, customer_name, phone, address, comment, total, status) " +
                                "VALUES (?, ?, ?, ?, ?, ?, 'В обработке') RETURNING id"
                );
                orderStmt.setInt(1, user.getId());
                orderStmt.setString(2, user.getFullName());
                orderStmt.setString(3, phone.trim());
                orderStmt.setString(4, address.trim());
                orderStmt.setString(5, comment == null ? "" : comment.trim());
                orderStmt.setDouble(6, total);

                var rs = orderStmt.executeQuery();
                if (!rs.next()) throw new RuntimeException("Не удалось создать заказ");
                int orderId = rs.getInt("id");

                // 5. Записываем состав заказа (какие товары купили)
                var lineStmt = conn.prepareStatement(
                        "INSERT INTO order_lines(order_id, product_id, product_name, quantity, unit_price) " +
                                "VALUES (?, ?, ?, ?, ?)"
                );
                for (CartEntry entry : entries) {
                    lineStmt.setInt(1, orderId);
                    lineStmt.setInt(2, entry.product().getId());
                    lineStmt.setString(3, entry.product().getName());
                    lineStmt.setInt(4, entry.quantity());
                    lineStmt.setDouble(5, entry.product().getPrice());
                    lineStmt.addBatch();
                }
                lineStmt.executeBatch();

                // 6. Очищаем корзину
                var clearStmt = conn.prepareStatement("DELETE FROM cart WHERE user_id = ?");
                clearStmt.setInt(1, user.getId());
                clearStmt.executeUpdate();

                // 7. Фиксируем транзакцию (сохраняем всё разом)
                conn.commit();

                // Сбрасываем кэш, чтобы в каталоге сразу обновились остатки
                invalidateCache();
                return null;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return "Ошибка оформления заказа: " + e.getMessage();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка подключения к БД: " + e.getMessage();
        }
    }

    public List<Order> getOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = Database.connect()) {
            var stmt = conn.prepareStatement(
                    "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC"
            );
            stmt.setInt(1, userId);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setPhone(rs.getString("phone"));
                order.setAddress(rs.getString("address"));
                order.setComment(rs.getString("comment"));
                order.setCreatedAt(rs.getString("created_at"));
                order.setTotal(rs.getDouble("total"));
                order.setLines(getOrderLines(conn, order.getId()));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    private List<OrderLine> getOrderLines(Connection conn, int orderId) throws Exception {
        List<OrderLine> lines = new ArrayList<>();
        var stmt = conn.prepareStatement("SELECT * FROM order_lines WHERE order_id = ?");
        stmt.setInt(1, orderId);
        var rs = stmt.executeQuery();
        while (rs.next()) {
            lines.add(new OrderLine(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price")
            ));
        }
        return lines;
    }

    public boolean hasUserBoughtProduct(User user, int productId) {
        if (user == null || user.getId() == 0) return false;
        try (Connection conn = Database.connect()) {
            var stmt = conn.prepareStatement(
                    "SELECT 1 FROM orders o " +
                            "JOIN order_lines ol ON ol.order_id = o.id " +
                            "WHERE o.user_id = ? AND ol.product_id = ? LIMIT 1"
            );
            stmt.setInt(1, user.getId());
            stmt.setInt(2, productId);
            return stmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── АДМИНКА ───────────────────────────────────────────────────────────
    public void saveProduct(Product product) {
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false); // Отключаем автоматическое сохранение

            if (product.getId() == 0) {
                var stmt = conn.prepareStatement(
                        "INSERT INTO products(name, category, description, price, stock, image) VALUES (?, ?, ?, ?, ?, ?) RETURNING id"
                );
                stmt.setString(1, product.getName());
                stmt.setString(2, product.getCategory());
                stmt.setString(3, product.getDescription());
                stmt.setDouble(4, product.getPrice());
                stmt.setInt(5, product.getStock());
                stmt.setString(6, product.getImagePath());

                var rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println(">>> УСПЕХ! БАЗА ВЫДАЛА ID: " + rs.getInt("id"));
                }
            } else {
                var stmt = conn.prepareStatement(
                        "UPDATE products SET name=?, category=?, description=?, price=?, stock=?, image=? WHERE id=?"
                );
                stmt.setString(1, product.getName());
                stmt.setString(2, product.getCategory());
                stmt.setString(3, product.getDescription());
                stmt.setDouble(4, product.getPrice());
                stmt.setInt(5, product.getStock());
                stmt.setString(6, product.getImagePath());
                stmt.setInt(7, product.getId());
                int rows = stmt.executeUpdate();
                System.out.println(">>> ОБНОВЛЕНО СТРОК: " + rows);
            }

            conn.commit(); // ЯВНО ФИКСИРУЕМ ИЗМЕНЕНИЯ В БАЗЕ
            System.out.println(">>> ТРАНЗАКЦИЯ УСПЕШНО ЗАВЕРШЕНА (COMMIT)");
            invalidateCache();

        } catch (Exception e) {
            System.out.println("=========== ОШИБКА БАЗЫ ДАННЫХ ===========");
            e.printStackTrace();
            System.out.println("===========================================");
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public void deleteProduct(int productId) {
        try (Connection conn = Database.connect()) {
            var stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?");
            stmt.setInt(1, productId);
            stmt.executeUpdate();
            invalidateCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void invalidateCache() {
        cachedProducts = null;
    }
    // Для админа: получить ВСЕ заказы всех пользователей
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = Database.connect()) {
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT * FROM orders ORDER BY created_at DESC");
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setPhone(rs.getString("phone"));
                order.setAddress(rs.getString("address"));
                order.setComment(rs.getString("comment"));
                order.setCreatedAt(rs.getString("created_at"));
                order.setTotal(rs.getDouble("total"));
                order.setStatus(rs.getString("status")); // Читаем статус
                order.setLines(getOrderLines(conn, order.getId()));
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    // Изменение статуса заказа
    public void updateOrderStatus(int orderId, String newStatus) {
        try (Connection conn = Database.connect()) {
            var stmt = conn.prepareStatement("UPDATE orders SET status = ? WHERE id = ?");
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}