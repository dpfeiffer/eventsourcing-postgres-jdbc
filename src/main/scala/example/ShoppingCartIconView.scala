package example

import eventsourcing._
import example.ShoppingCart._
import example.ShoppingCartIconView._
import play.api.libs.json._

object ShoppingCartIconView {
  private val addedFormat = Json.format[ItemAdded]
}

class ShoppingCartIconView(customer: String, journal: Journal, stream: EventStream) extends View[ShoppingCart.ShoppingCartEvent, Int](journal, stream) {

  override def decode(`type`: String, payload: String): ShoppingCart.ShoppingCartEvent = {
    `type` match {
      case "item.added" =>
        val json   = Json.parse(payload)
        val result = addedFormat.reads(json)
        result.get
    }
  }

  override def handleEvent(result: Int, e: ShoppingCart.ShoppingCartEvent): Int = {
    e match {
      case ItemAdded(_) => result + 1
    }
  }

  override def initialState: Int = 0

  override def initialStream: LazyList[EventEnvelope] = journal.stream(customer)
}
