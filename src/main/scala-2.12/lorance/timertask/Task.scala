package lorance.timertask

case class TaskKey(id: String, timestamp: Long)

trait Task {
  val taskId: TaskKey //account and custom name
  def execute(): Unit
  def nextTask: Option[Task] //able to execute next time, completed as None
  override def toString = {
    super.toString + s"-$taskId"
  }
}

object Task {
  def delay(ms: Long): Long = System.currentTimeMillis() + ms
}