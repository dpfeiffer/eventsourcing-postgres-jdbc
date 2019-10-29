package eventsourcing

trait Command {
  def id: String
}
