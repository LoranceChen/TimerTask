# Timer Task
Register all tasks, which related time, to one managed thread.

## Feature
- less threads to manage massive tasks  
:: a `TaskManager` class use two thread.the one is for save tasks queue another is for task dispatch.
- NO loop  
:: use `wait` and `notify` to manager task and dispatch
- flexible ways to define and manage `Task`
 
## Which stage should NOT use
- care for 10ms time delay.  
:: Because of some times dispatch action, the time not very accurate. 

## Dependency
- ReactiveX
- Future

## Example
```
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
    Some((10,100)), //repeat 3 times, wait 1s each other
    (theTaskId, timeAndLoop) => {
      if(timeAndLoop.fold(false)(_._1 == 10)) {
        ConsoleLog.log("start RepearTask ========================")
      }
      ConsoleLog.log(s"${theTaskId.id} completed. delayed ${System.currentTimeMillis() - theTaskId.timestamp} ms - left ${timeAndLoop.map(_._1)} times")
      //stop the task itself when achieve 50 times (actually,the task will execute 50 + 1 times).
      if(timeAndLoop.fold(false)(_._1 == 5)) {
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
```

Output: 

```
Thread-Waiter:1486568645252 - start simpleTask ==================
Thread-Waiter:1486568645258 - completed - TaskKey(task01,1486568645245), want executed at 1486568645245 , delayed 12 ms
Thread-Waiter:1486568647265 - do Double Task ========================
Thread-Waiter:1486568647266 - double task completed - TaskKey(task01,1486568647264), want executed at 1486568647264 , delayed 1 ms
Thread-Waiter:1486568648269 - double task completed - TaskKey(task01,1486568648264), want executed at 1486568648264 , delayed 5 ms
Thread-Waiter:1486568649251 - start RepearTask ========================
Thread-Waiter:1486568649252 - repeat task completed. delayed 5 ms - left Some(10) times
Thread-Waiter:1486568649349 - repeat task completed. delayed 3 ms - left Some(9) times
Thread-Waiter:1486568649449 - repeat task completed. delayed 3 ms - left Some(8) times
Thread-Waiter:1486568649549 - repeat task completed. delayed 3 ms - left Some(7) times
Thread-Waiter:1486568649649 - repeat task completed. delayed 3 ms - left Some(6) times
Thread-Waiter:1486568649749 - repeat task completed. delayed 3 ms - left Some(5) times
scala-execution-context-global-15:1486568649766 - left task count - 0
```