package example

import eventsourcing._
import example.ShoppingCart._
import play.api.libs.json.Json

object ShoppingCart {

  case class ShoppingCartState(items: Map[String, Int])

  sealed trait ShoppingCartCommand             extends Command
  case class AddItem(id: String, name: String) extends ShoppingCartCommand

  sealed abstract class ShoppingCartEvent(`type`: String) extends Event(`type`)
  case class ItemAdded(name: String)                      extends ShoppingCartEvent("item.added")
}

class ShoppingCart(journal: Journal, eventStream: EventStream) extends Aggregate[ShoppingCartCommand, ShoppingCartEvent, ShoppingCartState](journal, eventStream) {

  private val addedFormat = Json.format[ItemAdded]

  override def decode(`type`: String, payload: String): ShoppingCart.ShoppingCartEvent = `type` match {
    case "item.added" => addedFormat.reads(Json.parse(payload)).get
  }
  override def encode(event: ShoppingCart.ShoppingCartEvent): String = event match {
    case e: ItemAdded => addedFormat.writes(e).toString()
  }

  override def handleCommand(s: ShoppingCartState, c: ShoppingCart.ShoppingCartCommand): (List[ShoppingCart.ShoppingCartEvent], Any) = c match {
    case c: AddItem => (List(ItemAdded(c.name)), ())
  }

  override def handleEvent(s: ShoppingCartState, e: ShoppingCart.ShoppingCartEvent): ShoppingCartState = {
    e match {
      case ItemAdded(name) =>
        val currentAmount = s.items.get(name).getOrElse(0)
        ShoppingCartState(s.items + (name -> (currentAmount + 1)))
    }
  }
  override def initialState: ShoppingCartState = ShoppingCartState(Map())
}
