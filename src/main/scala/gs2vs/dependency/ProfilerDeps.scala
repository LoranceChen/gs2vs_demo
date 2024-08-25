package gs2vs.dependency
import gs2vs.DepsModule
import gs2vs.core.{Config, Dependencies}
import io.pyroscope.http.Format
import io.pyroscope.javaagent.config.{Config => PSConfig}
import io.pyroscope.javaagent.{EventType, PyroscopeAgent}

import java.time.Duration

class ProfilerDeps extends DepsModule {

//  val defaultAppName =
//    "cmk-lobby-service" + conf.common.worldType + "-" + conf.common.worldId + "-" + UUID
//      .randomUUID()
//      .toString
//      .take(3)
  val appName = "gs2vs_local" // podName.getOrElse(defaultAppName)

  private lazy val profiler = {
    val pyroscopeConfig = Dependencies
      .getGsObject(classOf[ConfDeps], classOf[Config])
      .pyroscopeConfig

    val pyroscopeAgent: PyroscopeAgent.Options =
      new PyroscopeAgent.Options.Builder(
        new PSConfig.Builder()
          .setApplicationName(appName)
          .setProfilingInterval(Duration.ofMillis(10L))
          .setProfilingEvent(EventType.ITIMER)
          .setProfilingAlloc("512k")
          .setProfilingLock("10ms")
          .setFormat(Format.JFR)
          .setServerAddress(pyroscopeConfig.serverUrl)
          .setAuthToken(pyroscopeConfig.serverKey)
          .build()
      ).build()

    PyroscopeAgent.start(pyroscopeAgent)
    pyroscopeAgent
  }

  override lazy val defineObjects = List(
    classOf[PyroscopeAgent.Options] -> profiler
  )

}
