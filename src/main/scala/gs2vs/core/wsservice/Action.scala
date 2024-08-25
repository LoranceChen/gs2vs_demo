package gs2vs.core.wsservice
import com.google.protobuf.GeneratedMessageV3

object Action {
  type ActionIndex = Byte
  type ActionHandler =
    Array[Byte] => GeneratedMessageV3 // alias for request => response
}
