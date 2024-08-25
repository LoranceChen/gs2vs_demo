package gs2vs.dependency
import gs2vs.DepsModule
import gs2vs.core.db.{DBDriver, DataSource, DataSourceImpl}
import gs2vs.core.utils.ExecutorUtils.DbExecutor
import gs2vs.core.{Config, Dependencies}

class DBDeps extends DepsModule {
  private lazy val dataSourceLive: DataSource = new DataSourceImpl(
    Dependencies.getGsObject(classOf[ConfDeps], classOf[Config]).jdbcConfig
  )
  private lazy val dbDriverLive: DBDriver =
    new DBDriver(
      dataSourceLive,
      Dependencies.getGsObject(classOf[ExecutorDeps], classOf[DbExecutor])
    )

  override lazy val defineObjects = List(
    classOf[DataSource] -> dataSourceLive,
    classOf[DBDriver] -> dbDriverLive
  )

}
