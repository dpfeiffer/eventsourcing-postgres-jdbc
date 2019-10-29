package eventsourcing

import scala.reflect.ClassTag

class EventStream {

  private var subscribers: List[(Class[_], _ => Unit)] = List.empty

  def publish[E](event: E): Unit = {
    subscribers
      .filter { case (clazz, _) => clazz.isAssignableFrom(event.getClass()) }
      .foreach { case (_, handler) => handler.asInstanceOf[E => Unit](event) }
  }

  def subscribe[E: ClassTag](handler: E => Unit): Unit = {
    subscribers = subscribers :+ ((implicitly[ClassTag[E]].runtimeClass, handler))
  }

}
