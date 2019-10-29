package eventsourcing.jdbc

import eventsourcing._
import org.postgresql.util._

import java.sql.Connection

class JdbcJournal(connection: Connection) extends Journal {

  def store(events: List[EventEnvelope]): Unit = transactional {

    val s = connection.prepareStatement(
      """
      |insert into journal(aggregate_id, version, type, payload, tags, timestamp )
      |values(?,?,?,?,?,?);
      |""".stripMargin
    )

    events.foreach { e =>
      s.setString(1, e.aggregateId)
      s.setLong(2, e.version)
      s.setString(3, e.`type`)
      val payload = new PGobject()
      payload.setType("jsonb")
      payload.setValue(e.payload)
      s.setObject(4, payload)

      val tags = new PGobject()
      tags.setType("jsonb")
      tags.setValue("{}")
      s.setObject(5, tags)
      s.setTimestamp(6, new java.sql.Timestamp(e.timestamp.toEpochMilli()))
      s.executeUpdate()
    }
  }

  override def stream(aggregateId: String): LazyList[EventEnvelope] = {
    val statement = connection.prepareStatement("select id, aggregate_id, version, type, payload, timestamp from journal where aggregate_id = ?;")
    statement.setString(1, aggregateId)
    val resultSet = statement.executeQuery()

    val elements = LazyList.unfold(resultSet) { rs =>
      if (rs.next()) {
        val envelope = EventEnvelope(
          Some(rs.getLong("id")),
          rs.getString("aggregate_id"),
          rs.getLong("version"),
          rs.getString("type"),
          rs.getObject("payload").asInstanceOf[PGobject].getValue(),
          Map(),
          rs.getTimestamp("timestamp").toInstant()
        )
        Some((envelope, rs))
      } else {
        None
      }
    }

    elements
  }

  private def transactional[A](f: => A): A = {
    try {
      connection.setAutoCommit(false)
      val result = f
      connection.commit()
      result
    } finally {
      connection.setAutoCommit(true)
    }
  }

}
