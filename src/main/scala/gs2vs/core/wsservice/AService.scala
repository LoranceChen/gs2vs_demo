package gs2vs.core.wsservice

import gs2vs.core.wsservice.Action.{ActionHandler, ActionIndex}

trait AService {
  val actions: Map[ActionIndex, ActionHandler]

}

object AService {
  type ServiceIndex = Short
}
