package eventsourcing

trait Journal {
  def store(events: List[EventEnvelope]): Unit
  def stream(aggregateId: String): LazyList[EventEnvelope]
}
