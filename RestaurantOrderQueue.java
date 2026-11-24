import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class RestaurantOrderQueue {
    private static final String FILE = "orders.txt";
    private static Scanner sc = new Scanner(System.in);
    private static String[] menu = {
        "1. Place Order",
        "2. Serve Next Order",
        "3. View Pending Orders",
        "4. Cancel Order by ID",
        "5. Exit"
    };
    private static Queue<Order> orders = new LinkedList<>();
    private static int nextId = 101;
    static class Order {
        String id, customer, dish;
        int quantity;
        Order(String id, String customer, String dish, int quantity) {
            this.id = id;
            this.customer = customer;
            this.dish = dish;
            this.quantity = quantity;
        }
        public String toString() {
            return id + " | " + customer + " | " +
                   dish + " x" + quantity;
        }
    }
    public static void main(String[] args) {
        loadOrders();
        while (true) {
            System.out.println("\n===== RESTAURANT ORDER QUEUE =====");
            for (String m : menu) {
                System.out.println(m);
            }
            System.out.print("Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    placeOrder();
                    break;
                case 2:
                    serveOrder();
                    break;
                case 3:
                    viewOrders();
                    break;
                case 4:
                    cancelOrder();
                    break;
                case 5:
                    saveOrders();
                    System.out.println("Data saved. Thank you!");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
    static void placeOrder() {
        System.out.print("Customer Name: ");
        String customer = sc.nextLine().trim();
        System.out.print("Dish Name: ");
        String dish = sc.nextLine().trim();
        System.out.print("Quantity: ");
        int quantity = readInt();
        if (customer.isEmpty() || dish.isEmpty()) {
            System.out.println("Name and dish cannot be empty.");
            return;
        }
        if (quantity < 1) {
            System.out.println(
                "Invalid quantity. Quantity must be at least 1."
            );
            return;
        }
        String id = "O" + nextId++;
        Order order = new Order(
            id, customer, dish, quantity
        );
        orders.offer(order);
        System.out.println("Order placed. ID: " + order +
            " | Position: " + orders.size());
        saveOrders();
    }
    static void serveOrder() {
        Order order = orders.poll();
        if (order == null) {
            System.out.println("No pending orders!");
        } else {
            System.out.println("Order " + order.id +
                " served. Pending orders: " + orders.size());
        }
        saveOrders();
    }
    static void viewOrders() {
        if (orders.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }
        System.out.println("\n--- PENDING ORDERS ---");
        int position = 1;
        for (Order order : orders) {
            System.out.println(position + ". " + order);
            position++;
        }
        System.out.println("Total pending orders: " + orders.size());
    }
    static void cancelOrder() {
        System.out.print("Enter Order ID: ");
        String id = sc.nextLine().trim();
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.id.equalsIgnoreCase(id)) {
                iterator.remove();
                System.out.println(
                    "Order " + order.id + " cancelled."
                );
                saveOrders();
                return;
            }
        }
        System.out.println("Order ID not found.");
    }
    static void loadOrders() {
        Path path = Paths.get(FILE);
        if (!Files.exists(path)) {
            return;
        }
        int highestId = 100;
        try (BufferedReader br = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split("\\|", -1);
                    if (data.length != 4) continue;
                    String id = data[0];
                    String customer = new String(
                        Base64.getDecoder().decode(data[1]),
                        StandardCharsets.UTF_8
                    );
                    String dish = new String(
                        Base64.getDecoder().decode(data[2]),
                        StandardCharsets.UTF_8
                    );
                    int quantity = Integer.parseInt(data[3]);
                    if (quantity < 1) continue;
                    orders.offer(new Order(
                        id, customer, dish, quantity
                    ));
                    if (id.matches("O\\d+")) {
                        highestId = Math.max(
                            highestId,
                            Integer.parseInt(id.substring(1))
                        );
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid file record skipped.");
                }
            }
            nextId = highestId + 1;
            System.out.println(
                "Loaded " + orders.size() + " pending order(s)."
            );
        } catch (IOException e) {
            System.out.println("File loading error: " + e.getMessage());
        }
    }
    static void saveOrders() {
        try (BufferedWriter bw = Files.newBufferedWriter(
                Paths.get(FILE), StandardCharsets.UTF_8)) {
            for (Order order : orders) {
                String customer = Base64.getEncoder()
                    .encodeToString(
                        order.customer.getBytes(StandardCharsets.UTF_8)
                    );
                String dish = Base64.getEncoder()
                    .encodeToString(
                        order.dish.getBytes(StandardCharsets.UTF_8)
                    );
                bw.write(order.id + "|" + customer + "|" +
                    dish + "|" + order.quantity);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("File saving error: " + e.getMessage());
        }
    }
}