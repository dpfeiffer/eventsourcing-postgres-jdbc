package eventsourcing

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

abstract class Aggregate[C <: Command, E <: Event, S](journal: Journal, eventStream: EventStream) {

  private val state: ConcurrentHashMap[String, AggregateState[S]] = new ConcurrentHashMap[String, AggregateState[S]]()

  protected def initialState: S

  protected def encode(event: E): String
  protected def decode(`type`: String, payload: String): E

  protected def handleCommand(s: S, c: C): (List[E], Any)
  protected def handleEvent(s: S, e: E): S

  def processCommand(c: C): Any = {

    val previous = state.computeIfAbsent(c.id, recover)

    val (events, result) = handleCommand(previous.state, c)

    state.computeIfPresent(
      c.id, { (_, s) =>
        val now = Instant.now()

        val envelopes = events.zipWithIndex
          .map { case (event, index) => (event, index + s.version) }
          .map {
            case (event, version) =>
              val payload = encode(event)
              EventEnvelope(
                None,
                c.id,
                version,
                event.`type`,
                payload,
                Map(),
                now
              )
          }

        journal.store(envelopes)
        events.foreach(event => eventStream.publish(event))
        events.foldLeft(s) { (s, e) =>
          val newState = handleEvent(s.state, e)
          AggregateState(s.version + 1, newState)
        }
      }
    )

    result
  }

  private def recover(id: String): AggregateState[S] = {

    val events = journal.stream(id)

    events
      .map(p => decode(p.`type`, p.payload))
      .foldLeft(AggregateState(0, initialState)) { (s, e) =>
        val newState = handleEvent(s.state, e)
        AggregateState(s.version + 1, newState)
      }
  }

}
