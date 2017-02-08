import lorance.timertask.{Task, TaskKey, TaskManager}
import lorance.timertask._
import scala.concurrent.ExecutionContext.Implicits.global

object Test extends App {
  //init task Manager
  val taskManager = new TaskManager()

  //create task
  val simpleTask = new Task {
    override val taskId: TaskKey = TaskKey("task01", Task.delay(2000))

    //account and custom name
    override def execute(): Unit = {
      ConsoleLog.log("start simpleTask ==================")
      val exeTime =  System.currentTimeMillis()
      ConsoleLog.log(s"completed - ${taskId}, want executed at ${taskId.timestamp} , delayed ${exeTime - taskId.timestamp} ms" )
    }

    override def nextTask: Option[Task] = None
  }

  /**
    * the second action can based the former status
    */
  class DoubleTask extends Task {
    override val taskId: TaskKey = TaskKey("task01", Task.delay(4000))

    //account and custom name
    override def execute(): Unit = {
      val exeTime =  System.currentTimeMillis()
      ConsoleLog.log("do Double Task ========================")
      ConsoleLog.log(s"double task completed - ${taskId}, want executed at ${taskId.timestamp} , delayed ${exeTime - taskId.timestamp} ms" )
    }

    override def nextTask: Option[Task] = Some( new Task {
      //delay 1s compare with the former one
      override val taskId: TaskKey = TaskKey("task01",
        1000 + DoubleTask.this.taskId.timestamp)

      //account and custom name
      override def execute(): Unit = {
        val exeTime =  System.currentTimeMillis()
        ConsoleLog.log(s"double task completed - ${taskId}, want executed at ${taskId.timestamp} , delayed ${exeTime - taskId.timestamp} ms" )
      }

      override def nextTask: Option[Task] = None
    })
  }

  //do a tons of task by RepeatTask class
  val repeatTask = new RepeatTask(TaskKey("repeat task", Task.delay(6000)),
    Some((100,100)), //repeat 3 times, wait 1s each other
    (theTaskId, timeAndLoop) => {
      if(timeAndLoop.fold(false)(_._1 == 100)) {
        ConsoleLog.log("start RepearTask ========================")
      }
      ConsoleLog.log(s"${theTaskId.id} completed. delayed ${System.currentTimeMillis() - theTaskId.timestamp} ms - left ${timeAndLoop.map(_._1)} times")
      //stop the task itself when achieve 50 times (actually,the task will execute 50 + 1 times).
      if(timeAndLoop.fold(false)(_._1 == 50)) {
        taskManager.cancelTask("repeat task")
        taskManager.tasksCount.foreach(x => ConsoleLog.log("left task count - " + x))
      }
    })
  // add task
  taskManager.addTask(simpleTask)
  taskManager.addTask(new DoubleTask())
  taskManager.addTask(repeatTask)

  Thread.currentThread().join()
}
