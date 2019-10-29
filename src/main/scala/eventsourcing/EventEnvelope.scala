package eventsourcing

import java.time.Instant

case class EventEnvelope(
    id: Option[Long],
    aggregateId: String,
    version: Long,
    `type`: String,
    payload: String,
    tags: Map[String, String],
    timestamp: Instant
)
