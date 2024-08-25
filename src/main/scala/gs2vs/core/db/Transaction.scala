package gs2vs.core.db
import gs2vs.core.utils.ExecutorUtils.GS2Executor

class Transaction(dataSource: DataSource, gs2Executor: GS2Executor) {

  def begin(): Unit = {
    gs2Executor.runAndWait(dataSource.getConnection())
  }

  def commit(): Unit = {}

}
