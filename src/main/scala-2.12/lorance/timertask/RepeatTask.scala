package lorance.timertask


class RepeatTask(val taskId: TaskKey,
                 loopAndBreakTimes: Option[(Int, Long)] = None, // None : no next. Some(int < 0)
                 action: (TaskKey, Option[(Int, Long)]) => Unit
               ) extends Task {
  // pre calculate next execute time to avoid deviation after execute
  private val nextTime = loopAndBreakTimes match {
    case Some((times, breakTime)) if times != 0 => //can calculate
      Some(taskId.timestamp + breakTime)
    case _ => None
  }

  //connect http server and do the action cmd
  //when executed, tell Waiter Thread not return current thread
  override def execute(): Unit = {
    action(this.taskId, loopAndBreakTimes)
  }

  /**
    * 1. use nextTime as new Task real execute time
    * 2. ensure loopTime not decrease if it is < 0
    */
  override def nextTask: Option[Task] = {
    nextTime.map(x => new RepeatTask(
      TaskKey(taskId.id, x),
      loopAndBreakTimes.map { case (loopTime, breakTime) =>
        if(loopTime > 0) (loopTime - 1, breakTime)
        else (loopTime, breakTime)
      },
      action
    ))
  }
}
