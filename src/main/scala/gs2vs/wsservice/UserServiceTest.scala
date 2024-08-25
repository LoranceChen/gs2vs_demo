package gs2vs.wsservice

import gs2vs.core.db.DataSource
import gs2vs.core.diaptcher.InputEventConsumer
import gs2vs.core.wsservice.Action.{ActionHandler, ActionIndex}
import gs2vs.util.VTExecutor
import org.slf4j.LoggerFactory

import java.util.concurrent.StructuredTaskScope.Subtask
import java.util.concurrent.StructuredTaskScope.Subtask.State
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{
  CompletableFuture,
  ExecutionException,
  Executors,
  StructuredTaskScope
}
import scala.collection.mutable.ArrayBuffer
import scala.util.Using

@Deprecated
class UserServiceTest(dataSource: DataSource) {
  protected val logger =
    LoggerFactory.getLogger(classOf[InputEventConsumer].getName)

  private val vtExecutor = Executors.newVirtualThreadPerTaskExecutor()
  private val osExecutor = Executors.newWorkStealingPool()

  val actions: Map[ActionIndex, ActionHandler] = {
    Map[ActionIndex, ActionHandler](
//      "signUp" -> signUp,
//      "loopTest" -> loopTest,
//      "vtThrowStackTest" -> vtThrowStackTest,
//      "structScopeTest" -> structScopeTest,
//      "testWaitOsExecutor" -> testWaitOsExecutor,
//      "testLockBetweenVT" -> testLockBetweenVT,
//      "testSynchronizedBetweenVT" -> testSynchronizedBetweenVT,
//      "testSynchronizedVtAndOs" -> testSynchronizedVtAndOs,
//      "testSynchronizedWithLock" -> testSynchronizedWithLock,
//      "testDataSource" -> testDataSource,
//      "testCompleteFuture" -> testCompleteFuture
    )
  }

  def signUp(request: Object): Object = {
    logger.info(s"get request: $request")
    Thread.sleep(3000)

    // todo redis/db/etc with blocking style

    val rst = s"handled $request in UserService"
    logger.info(s"response: $rst")
    rst
  }

  def loopTest(request: Object): Object = {
    logger.info(s"get request: $request")

    def simpleOps() = {
      (0 until 1000).foreach(x => {
        Math.log(1000000)
        Math.log(999)
        Math.log(3213213)
        Math.log(1323132132)
      })
    }

    while (true) {
      simpleOps()
    }

    val rst = s"handled $request in UserService"
    logger.info(s"response: $rst")
    rst
  }

  case class Ref[T](var value: T)
  def vtThrowStackTest(_param: Object): Object = {
    var task1Result: Ref[Int] = Ref(0)
    val rst0Future = VTExecutor.instance.submit(
      new Runnable {
        override def run(): Unit = {
          UserServiceTest.this.logger.info("start a fork virtual thread")
          Thread.sleep(3000)
          UserServiceTest.this.logger.info("start a fork virtual thread")
          task1Result.value = 10
          ()
        }
      },
      task1Result
    )

    val rst0 = rst0Future.get()
    logger.info(s"complete result fork virtual thread. rst0: ${rst0}")

    var task2FailResult: Int = 0
    val rst1 = VTExecutor.instance.submit[Int](
      new Runnable {
        override def run(): Unit = {
          UserServiceTest.this.logger.info("start a fork virtual thread2")
          mockFail()
        }
      },
      task2FailResult
    )
    val cannotAchieved = rst1.get()
    logger.info(s"complete result fork virtual thread. rst1: ${rst1}")
    Integer.valueOf(rst0.value + cannotAchieved)
  }

  private def mockFail(): Int = {
    def innerVTFail() = {
      val a = 1
      logger.info(s"hello failed. ${Thread.currentThread().getName}")
      throw new NullPointerException("mock NPE")
    }

    Thread.sleep(1000)
    var task2FailResult: Int = 0
    val rst1 = VTExecutor.instance.submit[Int](
      new Runnable {
        override def run(): Unit = {
          UserServiceTest.this.logger.info("start a fork virtual thread2")
          innerVTFail()
        }
      },
      task2FailResult
    )
    val cannotAchieved = rst1.get()
    cannotAchieved
  }

  def structScopeTest(param: Object): Object = {
    println("structScopeTest begin")
    val rst = Using(new StructuredTaskScope.ShutdownOnFailure()) { scope =>
      val a = scope.fork(() => this.mockFuncCallSuccess())
      val b = scope.fork(() => this.mockFuncCallFail())
      scope.join().throwIfFailed()

      a.get() -> b.get()
    }
    rst.failed.get.printStackTrace()
    //    print("result: " + rst.failed.get.printStackTrace())
    new Object
  }

  private def mockFuncCallSuccess(): Int = {
    mockSuccessWithoutVT()
  }

  private def mockFuncCallFail(): Int = {
    mockFailWithoutVT()
  }

  private def mockSuccessWithoutVT(): Int = {
    val a = 1
    logger.info(s"hello failed. ${Thread.currentThread().getName}")
    a
  }

  private def mockFailWithoutVT(): Int = {
    val a = 1
    logger.info(s"hello failed. ${Thread.currentThread().getName}")
    throw new NullPointerException("mockFailWithout virtual thread NPE")
  }

  private def get[T](task: Subtask[T]): T = {
    if (task.state eq State.FAILED) throw new ExecutionException(task.exception)
    task.get
  }

  def testWaitOsExecutor(prams: Object): Object = {
    var rst = Ref("default")
    val rstFuture = osExecutor.submit(
      new Runnable {
        override def run(): Unit = {
          println("inner threadName: " + Thread.currentThread().getName)
          println("inner threadId: " + Thread.currentThread().threadId())
          osExecutorCalc()
          mockFail()
          //          Thread.sleep(1000)
          rst.value = "rst from os executor thread"
        }
      },
      rst
    )
    val response = rstFuture.get()
    println("vtExecutorCalc begin")
    vtExecutorCalc()
    println("testWaitOsExecutor response: " + response)
    s"testWaitOsExecutor response: $response"
  }

  private def osExecutorCalc(): Int = {
    val a = 1
    Thread.sleep(15000)
    a
  }
  private def vtExecutorCalc(): Int = {
    val a = 1
    Thread.sleep(15000)
    a
  }

  /** println: completed testLockBetweenVT. a: 0, b: 1
    *
    * jcmd result:
    *
    * #69 "" virtual
    * java.base/java.lang.VirtualThread.park(VirtualThread.java:582)
    * java.base/java.lang.System$2.parkVirtualThread(System.java:2643)
    * java.base/jdk.internal.misc.VirtualThreads.park(VirtualThreads.java:54)
    * java.base/java.util.concurrent.locks.LockSupport.park(LockSupport.java:219)
    * java.base/java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:754)
    * java.base/java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:990)
    * java.base/java.util.concurrent.locks.ReentrantLock$Sync.lock(ReentrantLock.java:153)
    * java.base/java.util.concurrent.locks.ReentrantLock.lock(ReentrantLock.java:322)
    * gs2vs.wsservice.UserService$$anon$5.run(UserService.scala:200)
    * java.base/java.lang.VirtualThread.run(VirtualThread.java:309)
    *
    * #55 "wsinput-" virtual
    * java.base/java.lang.VirtualThread.parkNanos(VirtualThread.java:621)
    * java.base/java.lang.VirtualThread.sleepNanos(VirtualThread.java:791)
    * java.base/java.lang.Thread.sleep(Thread.java:507)
    * gs2vs.wsservice.UserService.testLockBetweenVT(UserService.scala:212)
    * gs2vs.wsservice.UserService.$anonfun$actions$6(UserService.scala:25)
    * gs2vs.core.wsserver.VThreadWorker.routing(VThreadWorker.scala:22)
    * gs2vs.core.diaptcher.InputEventConsumer$$anon$1.run(InputEventConsumer.scala:46)
    * java.base/java.lang.VirtualThread.run(VirtualThread.java:309)
    */
  def testLockBetweenVT(param: Object): Object = {
    val lock = new ReentrantLock()
    var a = 0
    var b = 0
    Thread
      .ofVirtual()
      .name("testLockBetweenVT-inner")
      .start(new Runnable {
        override def run(): Unit = {
          try {
            lock.lock()
            a = 1
            Thread.sleep(10000)
          } finally {
            lock.unlock()
          }
        }
      })

    try {
      lock.lock()
      b = 1
      Thread.sleep(10000)
    } finally {
      lock.unlock()
    }

    println(s"completed testLockBetweenVT. a: ${a}, b: ${b}")
    ""
  }

  /** Thread[#74,ForkJoinPool-1-worker-9,10,CarrierThreads]
    * java.base/java.lang.VirtualThread$VThreadContinuation.onPinned(VirtualThread.java:183)
    * java.base/jdk.internal.vm.Continuation.onPinned0(Continuation.java:393)
    * java.base/java.lang.VirtualThread.parkNanos(VirtualThread.java:621)
    * java.base/java.lang.VirtualThread.sleepNanos(VirtualThread.java:791)
    * java.base/java.lang.Thread.sleep(Thread.java:507)
    * gs2vs.wsservice.UserService.testSynchronizedBetweenVT(UserService.scala:238)
    * <== monitors:1
    * gs2vs.wsservice.UserService.$anonfun$actions$7(UserService.scala:26)
    * gs2vs.core.wsserver.VThreadWorker.routing(VThreadWorker.scala:22)
    * gs2vs.core.diaptcher.InputEventConsumer$$anon$1.run(InputEventConsumer.scala:46)
    * java.base/java.lang.VirtualThread.run(VirtualThread.java:309)
    */
  def testSynchronizedBetweenVT(param: Object): Object = {
    val lock = new Object
    val lock2 = new ReentrantLock()
    var a = 0
    Thread
      .ofVirtual()
      .start(new Runnable {
        override def run(): Unit = {
          try {
            lock2.lock()
            a = 1
            Thread.sleep(10000)
          } finally {
            lock2.unlock()
          }
        }
      })
    lock.synchronized {
      try {
        lock2.lock()
        a = 2
        Thread.sleep(10000)
      } finally {
        lock2.unlock()
      }
    }
    ""
  }

  def testSynchronizedVtAndOs(param: Object): Object = {
    Thread
      .ofPlatform()
      .start(new Runnable {
        override def run(): Unit = {
          this.synchronized {
            Thread.sleep(10000)
          }
        }
      })
    this.synchronized {
      Thread.sleep(10000)
    }
    ""
  }

  def testSynchronizedWithLock(param: Object): Object = {
    val lock = new ReentrantLock()

    Thread
      .ofPlatform()
      .start(new Runnable {
        override def run(): Unit = {
          this.synchronized {
            Thread.sleep(10000)
          }
        }
      })

    try {
      lock.lock()
      Thread.sleep(10000)
    } finally {
      lock.unlock()
    }
    ""
  }

  def testDataSource(param: Object): Object = {
    //    var connection: Connection = null
    //    var pst: PreparedStatement = null
    //    var rs: ResultSet = null
    //    try {
    //      connection = DataSource.getConnection()
    //      pst = connection.prepareStatement("select * from emp")
    //      rs = pst.executeQuery
    //    } catch {
    //      case a => println("testDataSource fail.")
    //    } finally {
    //      pst.close()
    //      connection.close()
    //    }
    val rst = Ref[ArrayBuffer[(Int, String)]](null)
    val fur = osExecutor.submit(
      () => {
        // NOTICE: 这段代码必须在OS thread中执行，否则不会打印任何报错
        val connection = dataSource.getConnection()
        val pst = connection.prepareStatement("select * from Users")
        val rs = pst.executeQuery
        val dbRst = scala.collection.mutable.ArrayBuffer[(Int, String)]()
        while (rs.next) {
          dbRst.addOne(rs.getInt("id"), rs.getString("name"))
        }
        rs.close()
        pst.close()
        connection.close()
        rst.value = dbRst
      },
      rst
    )

    fur.get().value.toList.toString()
  }

  // no pinned
  def testCompleteFuture(prams: Object): Object = {
    //    var rst = Ref("default")
    val completableFuture = new CompletableFuture[String]()
    osExecutor.execute(new Runnable {
      override def run(): Unit = {
        println("inner threadName: " + Thread.currentThread().getName)
        println("inner threadId: " + Thread.currentThread().threadId())
        //        osExecutorCalc()
        //        mockFail()

        Thread.sleep(3000)
        val value = "rst from os executor thread's inner callback"
        vtExecutor.submit(() => {
          Thread.sleep(3000)
          completableFuture.complete(value)

        })
        //        completableFuture.complete(value)

      }
    })
    //    ,
    //      rst
    //    )
    val response = completableFuture.get()
    println("vtExecutorCalc begin. get response from thread pool:" + response)
    vtExecutorCalc()
    println("testWaitOsExecutor response: " + response)
    s"testWaitOsExecutor response: $response"
  }

}
