/*
 * Licensed under the Academic Free License (AFL 3.0).
 *     http://opensource.org/licenses/AFL-3.0
 *
 *  This code is distributed to CSULB students in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, other than educational.
 *
 *  2018 Alvaro Monge <alvaro.monge@csulb.edu>
 *
 */

package csulb.cecs323.app;

// Import all of the entity classes that we have written for this application.
import csulb.cecs323.model.*;

import java.io.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * A simple application to demonstrate how to persist an object in JPA.
 * <p>
 * This is for demonstration and educational purposes only.
 * </p>
 * <p>
 *     Originally provided by Dr. Alvaro Monge of CSULB, and subsequently modified by Dave Brown.
 * </p>
 * Licensed under the Academic Free License (AFL 3.0).
 *     http://opensource.org/licenses/AFL-3.0
 *
 *  This code is distributed to CSULB students in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, other than educational.
 *
 *  2021 David Brown <david.brown@csulb.edu>
 *
 */
public class CustomerOrders {
   /**
    * You will likely need the entityManager in a great many functions throughout your application.
    * Rather than make this a global variable, we will make it an instance variable within the CustomerOrders
    * class, and create an instance of CustomerOrders in the main.
    */
   private EntityManager entityManager;

   /**
    * The Logger can easily be configured to log to a file, rather than, or in addition to, the console.
    * We use it because it is easy to control how much or how little logging gets done without having to
    * go through the application and comment out/uncomment code and run the risk of introducing a bug.
    * Here also, we want to make sure that the one Logger instance is readily available throughout the
    * application, without resorting to creating a global variable.
    */
   private static final Logger LOGGER = Logger.getLogger(CustomerOrders.class.getName());

   /**
    * The constructor for the CustomerOrders class.  All that it does is stash the provided EntityManager
    * for use later in the application.
    * @param manager    The EntityManager that we will use.
    */
   public CustomerOrders(EntityManager manager) {
      this.entityManager = manager;
   }

   public static void main(String[] args) {

      LOGGER.fine("Creating EntityManagerFactory and EntityManager");
      EntityManagerFactory factory = Persistence.createEntityManagerFactory("CustomerOrders");
      EntityManager manager = factory.createEntityManager();
      // Create an instance of CustomerOrders and store our new EntityManager as an instance variable.
      CustomerOrders customerOrders = new CustomerOrders(manager);


      // Any changes to the database need to be done within a transaction.
      // See: https://en.wikibooks.org/wiki/Java_Persistence/Transactions

      LOGGER.fine("Begin of Transaction");
      EntityTransaction tx = manager.getTransaction();

      tx.begin();
      // List of Products that I want to persist.  I could just as easily done this with the seed-data.sql
      List <Products> products = new ArrayList<Products>();
      // Load up my List with the Entities that I want to persist.  Note, this does not put them
      // into the database.
      products.add(new Products("076174517163", "16 oz. hickory hammer", "Stanely Tools", "1", 9.97, 50));

      //add two more products
      products.add(new Products("076167817162", "20 volt drill driver", "Atomic Tools", "5", 69.99, 10));
      products.add(new Products("076111117166", "10 in adjustable wrench", "Husky Tools", "2", 19.97, 100));

      // Create the list of owners in the database.
      customerOrders.createEntity (products);

      /*
       *  create some customers to be chosen from by user
       */

      List <Customers> customers = new ArrayList<>();
      //load up customers list
      customers.add(new Customers("Smith", "John", "Flower road 1112",
              "90809", "9091254327"));
      customers.add(new Customers("Dol", "Bob", "Lewis lane 333",
              "90812", "9041153367"));
      customers.add(new Customers("Frank", "Franky", "Olive street E 281",
              "91842", "5123448695"));

      //create the list of customers in the database
      customerOrders.createEntity(customers);




      // Commit the changes so that the new data persists and is visible to other users.
      tx.commit();
      LOGGER.fine("End of Transaction");

      prompt(customers, products);

   } // End of the main method

   /**
    * Create and persist a list of objects to the database.
    * @param entities   The list of entities to persist.  These can be any object that has been
    *                   properly annotated in JPA and marked as "persistable."  I specifically
    *                   used a Java generic so that I did not have to write this over and over.
    */
   public <E> void createEntity(List <E> entities) {
      for (E next : entities) {
         LOGGER.info("Persisting: " + next);
         // Use the CustomerOrders entityManager instance variable to get our EntityManager.
         this.entityManager.persist(next);
      }

      // The auto generated ID (if present) is not passed in to the constructor since JPA will
      // generate a value.  So the previous for loop will not show a value for the ID.  But
      // now that the Entity has been persisted, JPA has generated the ID and filled that in.
      for (E next : entities) {
         LOGGER.info("Persisted object after flush (non-null id): " + next);
      }
   } // End of createEntity member method

   /**
    * Think of this as a simple map from a String to an instance of Products that has the
    * same name, as the string that you pass in.  To create a new Cars instance, you need to pass
    * in an instance of Products to satisfy the foreign key constraint, not just a string
    * representing the name of the style.
    * @param UPC        The name of the product that you are looking for.
    * @return           The Products instance corresponding to that UPC.
    */
   public Products getProduct (String UPC) {
      // Run the native query that we defined in the Products entity to find the right style.
      List<Products> products = this.entityManager.createNamedQuery("ReturnProduct",
              Products.class).setParameter(1, UPC).getResultList();
      if (products.size() == 0) {
         // Invalid style name passed in.
         return null;
      } else {
         // Return the style object that they asked for.
         return products.get(0);
      }
   }// End of the getStyle method

   public static void prompt(List<Customers> customers, List<Products> products) {
      boolean picking = true;
      long inId = 0;
      int currentCustomer;
      String inUPC = null;
      String currentProduct;
      Scanner in = new Scanner(System.in);
      System.out.println();
      System.out.print("\n**********************************************");
      System.out.print("************************************************\n");
      while (picking) {
         //show the customers in the database
         for (int x = 0; x < customers.size(); x++) {
            System.out.println(customers.get(x).toString());
         }

         //Ask user which customer he/she is

         System.out.println("Enter your customer ID \n:");
         try {
            inId = in.nextLong();
         }
         catch (InputMismatchException e){
            in.next();
            System.out.println("That's not a number! Try again\n");
            continue;
         }
         if (inId == (customers.get(0).getCustomer_id())) {
            currentCustomer = 0;
         } else if (inId == (customers.get(1).getCustomer_id())) {
            currentCustomer = 1;
         } else if (inId == (customers.get(2).getCustomer_id())) {
            currentCustomer = 2;
         }else{
            System.out.println("Not in the data base.");
            continue;
         }

         //show products then ask user which product they want

         for (int x = 0; x < products.size(); x++) {
            System.out.println(products.get(x).toString());
         }
         System.out.println("Enter your products UPC number \n:");
         try {
            inUPC = in.next();
         }
         catch (InputMismatchException e){
            in.next();
            System.out.println("That's not a UPC, Try again.\n");
            continue;
         }
         if (inUPC.equals(products.get(0).getUPC())) {
            currentProduct = products.get(0).getUPC();
         } else if (inUPC.equals(products.get(1).getUPC())) {
            currentProduct = products.get(1).getUPC();
         } else if (inUPC.equals(products.get(2).getUPC())) {
            currentProduct = products.get(2).getUPC();
         }else{
            System.out.println("Not in the data base. Try again\n");
            continue;
         }
      }
   }
} // End of CustomerOrders class
