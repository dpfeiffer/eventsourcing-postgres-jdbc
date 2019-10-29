package eventsourcing

case class AggregateState[S](version: Long, state: S)
