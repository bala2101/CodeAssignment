package kry.assignment.servicepoller;

import io.vertx.core.Vertx;

public class KryServicePoller {

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new ServicePollerVerticle());
  }
}
