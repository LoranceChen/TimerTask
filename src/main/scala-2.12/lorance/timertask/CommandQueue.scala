package lorance.timertask

import java.util.concurrent.ConcurrentLinkedQueue

/**
  * similar Actor pattern to flatten multiple thread request to a queue
  */
trait CommandQueue[T] {
  val myQueue = new ConcurrentLinkedQueue[T]()
  val lock = new Object()
  object QueueThread extends Thread {
    setDaemon(true)

    override def run = {
      while(true) {
        if (myQueue.size() == 0) {
          lock.synchronized(lock.wait())
        } else {
          val theTask = myQueue.poll()
          ConsoleLog.log(s"poll task cmd queue - $theTask", 100)

          receive(theTask)
        }
      }
    }
  }

  QueueThread.start()

  def tell(cmd: T) = {
    myQueue.add(cmd)
    ConsoleLog.log(s"tell cmd - $cmd - current count - ${myQueue.size()}", 100)
    lock.synchronized(lock.notify())
  }

  //must sync operation
  protected def receive(t: T): Unit
}

