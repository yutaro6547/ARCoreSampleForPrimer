package com.example.zukkey.arcoresampleforprimer.java;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CloudAnchorFragment extends ArFragment {

  @Override
  protected Config getSessionConfiguration(Session session) {
    Config config = super.getSessionConfiguration(session);
    config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
    return config;
  }
}
