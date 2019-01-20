package com.example.zukkey.arcoresampleforprimer.misc


sealed class AnchorState {
  object None: AnchorState()
  object Hosting: AnchorState()
  object Resolving: AnchorState()
}
