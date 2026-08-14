package com.smartbilling;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Dashboard extends JFrame {
    private final JPanel content = new JPanel(new BorderLayout());
    private final JLabel products = cardValue(), customers = cardValue(), sales = cardValue(), stock = cardValue();

    public Dashboard() {
        setTitle("Smart Billing & Inventory Management System");
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(210,0));
        side.setBackground(new Color(27,38,59));
        side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("<html><center>SMART<br>BILLING</center></html>",SwingConstants.CENTER);
        logo.setForeground(Color.WHITE); logo.setFont(new Font("SansSerif",Font.BOLD,24));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(Box.createVerticalStrut(25)); side.add(logo); side.add(Box.createVerticalStrut(30));

        addMenu(side,"Dashboard",e->showDashboard());
        addMenu(side,"Products",e->showProducts());
        addMenu(side,"Customers",e->showCustomers());
        addMenu(side,"Create Bill",e->showBilling());
        addMenu(side,"Sales History",e->showSales());
        addMenu(side,"Logout",e->{dispose();new Login().setVisible(true);});

        add(side,BorderLayout.WEST);
        add(content,BorderLayout.CENTER);
        showDashboard();
    }

    private void addMenu(JPanel side,String text,java.awt.event.ActionListener a){
        JButton b=new JButton(text); b.setMaximumSize(new Dimension(190,45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT); b.setForeground(Color.WHITE);
        b.setBackground(new Color(40,55,80)); b.setFocusPainted(false); b.addActionListener(a);
        side.add(b); side.add(Box.createVerticalStrut(7));
    }
    private JLabel cardValue(){ JLabel l=new JLabel("0");l.setFont(new Font("SansSerif",Font.BOLD,30));l.setHorizontalAlignment(SwingConstants.CENTER);return l; }
    private JPanel stat(String title,JLabel value){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)),BorderFactory.createEmptyBorder(15,15,15,15)));
        JLabel t=new JLabel(title,SwingConstants.CENTER);t.setForeground(Color.GRAY);p.add(t,BorderLayout.NORTH);p.add(value,BorderLayout.CENTER);return p;
    }
    private void showDashboard(){
        content.removeAll();
        JPanel top=new JPanel(new GridLayout(1,4,15,15));top.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        top.add(stat("TOTAL PRODUCTS",products));top.add(stat("CUSTOMERS",customers));top.add(stat("TODAY'S SALES",sales));top.add(stat("LOW STOCK",stock));
        content.add(top,BorderLayout.NORTH);
        JTextArea welcome=new JTextArea("\n\nWelcome to Smart Billing System\n\nUse the menu to manage products, customers, billing and sales.\n\nTip: Keep stock quantities updated for accurate inventory.");
        welcome.setEditable(false);welcome.setFont(new Font("SansSerif",Font.PLAIN,20));welcome.setBackground(new Color(245,247,250));
        content.add(welcome,BorderLayout.CENTER);loadStats();refresh();
    }
    private void loadStats(){
        try(Connection c=DBConnection.getConnection()){
            products.setText(one(c,"SELECT COUNT(*) FROM products"));
            customers.setText(one(c,"SELECT COUNT(*) FROM customers"));
            sales.setText("₹ "+one(c,"SELECT COALESCE(SUM(total),0) FROM sales WHERE DATE(sale_date)=CURDATE()"));
            stock.setText(one(c,"SELECT COUNT(*) FROM products WHERE quantity<=5"));
        }catch(Exception e){}
    }
    private String one(Connection c,String q)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){r.next();return r.getString(1);}}
    private void refresh(){content.revalidate();content.repaint();}

    private void showProducts(){
        content.removeAll(); content.add(new ProductPanel(),BorderLayout.CENTER); refresh();
    }
    private void showCustomers(){
        content.removeAll(); content.add(new CustomerPanel(),BorderLayout.CENTER); refresh();
    }
    private void showBilling(){
        content.removeAll(); content.add(new BillingPanel(),BorderLayout.CENTER); refresh();
    }
    private void showSales(){
        content.removeAll(); content.add(new SalesPanel(),BorderLayout.CENTER); refresh();
    }

    static class ProductPanel extends JPanel {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Name", "Category", "Price", "Quantity"}, 0);

        JTable t = new JTable(m);

        JTextField name = new JTextField();
        JTextField cat = new JTextField();
        JTextField price = new JTextField();
        JTextField qty = new JTextField();
        JTextField id = new JTextField();

        ProductPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JPanel f = new JPanel(new GridLayout(2, 6, 8, 8));

            f.add(new JLabel("ID"));
            f.add(new JLabel("Name"));
            f.add(new JLabel("Category"));
            f.add(new JLabel("Price"));
            f.add(new JLabel("Quantity"));
            f.add(new JLabel(""));

            f.add(id);
            f.add(name);
            f.add(cat);
            f.add(price);
            f.add(qty);

            JButton add = new JButton("Add Product");
            f.add(add);
            add.addActionListener(e -> save());

            JButton del = new JButton("Delete");
            f.add(del);
            del.addActionListener(e -> delete());

            add(f, BorderLayout.NORTH);
            add(new JScrollPane(t), BorderLayout.CENTER);

            load();

            t.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && t.getSelectedRow() >= 0) {
                    int r = t.getSelectedRow();

                    id.setText(String.valueOf(m.getValueAt(r, 0)));
                    name.setText(String.valueOf(m.getValueAt(r, 1)));
                    cat.setText(String.valueOf(m.getValueAt(r, 2)));
                    price.setText(String.valueOf(m.getValueAt(r, 3)));
                    qty.setText(String.valueOf(m.getValueAt(r, 4)));
                }
            });
        }

        void load() {
            m.setRowCount(0);

            try (Connection c = DBConnection.getConnection();
                 Statement s = c.createStatement();
                 ResultSet r = s.executeQuery(
                         "SELECT id, name, category, price, quantity FROM products")) {

                while (r.next()) {
                    m.addRow(new Object[]{
                            r.getInt("id"),
                            r.getString("name"),
                            r.getString("category"),
                            r.getDouble("price"),
                            r.getInt("quantity")
                    });
                }

            } catch (Exception e) {
                msg(e);
            }
        }

        void save() {
            try {
                String productName = name.getText().trim();
                String category = cat.getText().trim();
                String priceText = price.getText().trim();
                String qtyText = qty.getText().trim();
                String idText = id.getText().trim();

                if (productName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter Product Name");
                    return;
                }

                if (category.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter Category");
                    return;
                }

                // PRICE
                double productPrice;

                try {
                    
                    priceText = priceText.replace("₹", "")
                                         .replace(",", "")
                                         .trim();

                    productPrice = Double.parseDouble(priceText);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Price must be a number.\nExample: 100 or 99.50"
                    );
                    return;
                }

                if (productPrice < 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Price cannot be negative"
                    );
                    return;
                }

                // QUANTITY
                int quantity;

                try {
                    quantity = Integer.parseInt(qtyText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Quantity must be a whole number.\nExample: 10"
                    );
                    return;
                }

                if (quantity < 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Quantity cannot be negative"
                    );
                    return;
                }

                try (Connection c = DBConnection.getConnection()) {

                    String sql;

                    if (idText.isEmpty()) {

                        sql = "INSERT INTO products " +
                              "(name, category, price, quantity) " +
                              "VALUES (?, ?, ?, ?)";

                        PreparedStatement p = c.prepareStatement(sql);

                        p.setString(1, productName);
                        p.setString(2, category);
                        p.setDouble(3, productPrice);
                        p.setInt(4, quantity);

                        p.executeUpdate();

                    } else {

                        int productId;

                        try {
                            productId = Integer.parseInt(idText);
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Product ID must be a number."
                            );
                            return;
                        }

                        sql = "UPDATE products SET " +
                              "name=?, category=?, price=?, quantity=? " +
                              "WHERE id=?";

                        PreparedStatement p = c.prepareStatement(sql);

                        p.setString(1, productName);
                        p.setString(2, category);
                        p.setDouble(3, productPrice);
                        p.setInt(4, quantity);
                        p.setInt(5, productId);

                        p.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(
                            this,
                            "Product saved successfully!"
                    );

                    clear();
                    load();

                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error: " + e.getMessage()
                );
            }
        }

        void delete() {

            String idText = id.getText().trim();

            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please select a product to delete.");
                return;
            }

            try {

                int productId;

                try {
                    productId = Integer.parseInt(idText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Product ID must be a number.");
                    return;
                }

                try (Connection c = DBConnection.getConnection();
                     PreparedStatement p = c.prepareStatement(
                             "DELETE FROM products WHERE id=?")) {

                    p.setInt(1, productId);
                    p.executeUpdate();

                    JOptionPane.showMessageDialog(this,
                            "Product deleted successfully!");

                    clear();
                    load();
                }

            } catch (Exception e) {
                msg(e);
            }
        }

        void clear() {
            id.setText("");
            name.setText("");
            cat.setText("");
            price.setText("");
            qty.setText("");
        }

        void msg(Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage());
        }
    }

    static class CustomerPanel extends JPanel{
        DefaultTableModel m=new DefaultTableModel(new String[]{"ID","Name","Mobile","Address"},0); JTable t=new JTable(m);
        JTextField id=new JTextField(),name=new JTextField(),mobile=new JTextField(),address=new JTextField();
        CustomerPanel(){setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
            JPanel f=new JPanel(new GridLayout(2,5,8,8));f.add(new JLabel("ID"));f.add(new JLabel("Name"));f.add(new JLabel("Mobile"));f.add(new JLabel("Address"));f.add(new JLabel(""));
            f.add(id);f.add(name);f.add(mobile);f.add(address);JButton b=new JButton("Save");f.add(b);b.addActionListener(e->save());add(f,BorderLayout.NORTH);add(new JScrollPane(t),BorderLayout.CENTER);load();
            t.getSelectionModel().addListSelectionListener(e->{if(t.getSelectedRow()>=0){int r=t.getSelectedRow();id.setText(""+m.getValueAt(r,0));name.setText(""+m.getValueAt(r,1));mobile.setText(""+m.getValueAt(r,2));address.setText(""+m.getValueAt(r,3));}});
        }
        void load(){m.setRowCount(0);try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM customers")){while(r.next())m.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getString(4)});}catch(Exception e){}}
        void save() {
            try {
                Connection c = DBConnection.getConnection();

                if (name.getText().isBlank() ||
                    mobile.getText().isBlank() ||
                    address.getText().isBlank()) {

                    JOptionPane.showMessageDialog(this, "Please enter all details");
                    return;
                }

                if (id.getText().isBlank()) {

                    String q = "INSERT INTO customers(name, mobile, address) VALUES (?, ?, ?)";

                    PreparedStatement p = c.prepareStatement(q);
                    p.setString(1, name.getText().trim());
                    p.setString(2, mobile.getText().trim());
                    p.setString(3, address.getText().trim());

                    p.executeUpdate();

                } else {

                    String q = "UPDATE customers SET name=?, mobile=?, address=? WHERE id=?";

                    PreparedStatement p = c.prepareStatement(q);
                    p.setString(1, name.getText().trim());
                    p.setString(2, mobile.getText().trim());
                    p.setString(3, address.getText().trim());
                    p.setInt(4, Integer.parseInt(id.getText().trim()));

                    p.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Customer Saved Successfully!");

                id.setText("");
                name.setText("");
                mobile.setText("");
                address.setText("");

                load();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    static class BillingPanel extends JPanel{
        JComboBox<String> customer=new JComboBox<>(),product=new JComboBox<>();JTextField qty=new JTextField("1");JTextArea bill=new JTextArea();
        BillingPanel(){setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            JPanel f=new JPanel(new GridLayout(4,2,10,10));f.add(new JLabel("Customer"));f.add(customer);f.add(new JLabel("Product"));f.add(product);f.add(new JLabel("Quantity"));f.add(qty);
            JButton refresh=new JButton("Load Products");f.add(new JLabel(""));f.add(refresh);add(f,BorderLayout.NORTH);add(new JScrollPane(bill),BorderLayout.CENTER);
            JButton generate=new JButton("GENERATE BILL");add(generate,BorderLayout.SOUTH);load();refresh.addActionListener(e->load());generate.addActionListener(e->generate());
        }
        void load(){customer.removeAllItems();product.removeAllItems();try(Connection c=DBConnection.getConnection()){ResultSet r=c.createStatement().executeQuery("SELECT id,name FROM customers");while(r.next())customer.addItem(r.getInt(1)+" - "+r.getString(2));r=c.createStatement().executeQuery("SELECT id,name,price,quantity FROM products WHERE quantity>0");while(r.next())product.addItem(r.getInt(1)+" - "+r.getString(2)+" - ₹"+r.getDouble(3)+" (Stock "+r.getInt(4)+")");}catch(Exception e){}}
        void generate(){if(product.getSelectedItem()==null)return;try(Connection c=DBConnection.getConnection()){String ps=product.getSelectedItem().toString();int pid=Integer.parseInt(ps.split(" - ")[0]);int q=Integer.parseInt(qty.getText());ResultSet r=c.createStatement().executeQuery("SELECT name,price,quantity FROM products WHERE id="+pid);r.next();String pn=r.getString(1);double price=r.getDouble(2);int stock=r.getInt(3);if(q>stock){JOptionPane.showMessageDialog(this,"Insufficient stock");return;}double total=price*q;Integer cid=null;if(customer.getSelectedItem()!=null)cid=Integer.parseInt(customer.getSelectedItem().toString().split(" - ")[0]);PreparedStatement p=c.prepareStatement("INSERT INTO sales(customer_id,product_id,quantity,total) VALUES(?,?,?,?)");if(cid==null)p.setNull(1,Types.INTEGER);else p.setInt(1,cid);p.setInt(2,pid);p.setInt(3,q);p.setDouble(4,total);p.executeUpdate();PreparedStatement u=c.prepareStatement("UPDATE products SET quantity=quantity-? WHERE id=?");u.setInt(1,q);u.setInt(2,pid);u.executeUpdate();bill.setText("              SMART BILLING SYSTEM\n"+"------------------------------------------\nProduct : "+pn+"\nQuantity: "+q+"\nPrice   : ₹"+price+"\n------------------------------------------\nTOTAL   : ₹"+total+"\n------------------------------------------\n        Thank you for shopping!");load();}catch(Exception e){JOptionPane.showMessageDialog(this,e.getMessage());}}
    }

    static class SalesPanel extends JPanel{
        DefaultTableModel m=new DefaultTableModel(new String[]{"Bill ID","Customer ID","Product ID","Qty","Total","Date"},0);
        SalesPanel(){setLayout(new BorderLayout());setBorder(BorderFactory.createEmptyBorder(15,15,15,15));add(new JScrollPane(new JTable(m)),BorderLayout.CENTER);load();}
        void load(){try(Connection c=DBConnection.getConnection();ResultSet r=c.createStatement().executeQuery("SELECT * FROM sales ORDER BY sale_date DESC")){while(r.next())m.addRow(new Object[]{r.getInt(1),r.getObject(2),r.getObject(3),r.getInt(4),r.getDouble(5),r.getTimestamp(6)});}catch(Exception e){}}
    }
}
