package example

import eventsourcing._
import eventsourcing.jdbc._

import java.sql.DriverManager

object Main {

  def main(args: Array[String]): Unit = {
    Class.forName("org.postgresql.Driver")
    val connection  = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres")
    val journal     = new JdbcJournal(connection)
    val eventStream = new EventStream()

    val carts = new ShoppingCart(journal, eventStream)
    val icon  = new ShoppingCartIconView("customer-1", journal, eventStream)

    carts.processCommand(ShoppingCart.AddItem("customer-1", "jeans"))
    carts.processCommand(ShoppingCart.AddItem("customer-1", "blue jeans"))

    println(s"${icon.get} items in the shopping cart")
  }
}
