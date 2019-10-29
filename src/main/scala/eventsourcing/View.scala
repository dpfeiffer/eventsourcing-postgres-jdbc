package eventsourcing

import java.util.concurrent.atomic.AtomicReference

import scala.reflect.ClassTag

abstract class View[E <: Event: ClassTag, R](journal: Journal, stream: EventStream) {

  private val r: AtomicReference[R] = new AtomicReference[R]({
    initialStream
      .map(envelope => decode(envelope.`type`, envelope.payload))
      .foldLeft(initialState)(handleEvent)
  })

  stream.subscribe[E] { event =>
    r.updateAndGet { current =>
      handleEvent(current, event)
    }
  }

  protected def decode(`type`: String, payload: String): E

  protected def handleEvent(result: R, e: E): R

  protected def initialState: R

  protected def initialStream: LazyList[EventEnvelope]

  def get: R = {
    r.get()
  }

}
