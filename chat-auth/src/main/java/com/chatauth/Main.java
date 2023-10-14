package com.chatauth;

import com.chatauth.codecs.login_codecs.IncorrectPasswordMessageCodec;
import com.chatauth.codecs.login_codecs.LoginRequestCodec;
import com.chatauth.codecs.login_codecs.LoginSuccessCodec;
import com.chatauth.codecs.service_codecs.UserJWTGeneratedCodec;
import com.chatauth.codecs.signup_codecs.AddUserToDatabaseCodec;
import com.chatauth.codecs.signup_codecs.CreateUserCodec;
import com.chatauth.codecs.signup_codecs.CreateUserRequestCodec;
import com.chatauth.codecs.signup_codecs.UserCreatedCodec;
import com.chatauth.codecs.user_check_codecs.CheckUserExistenceRequestCodec;
import com.chatauth.codecs.user_check_codecs.PasswordCheckFailedMessageCodec;
import com.chatauth.codecs.user_check_codecs.UserAlreadyExistsCodec;
import com.chatauth.domain.CreateUser;
import com.chatauth.messages.*;
import com.chatauth.messages.login_messages.IncorrectPasswordMessage;
import com.chatauth.messages.login_messages.LoginRequest;
import com.chatauth.messages.login_messages.LoginSuccess;
import com.chatauth.services.implementation.JwtEncoderServiceImpl;
import com.chatauth.verticles.serviceverticles.AuthorizationVerticle;
import com.chatauth.verticles.httpverticles.HttpServerVerticle;
import com.chatauth.verticles.databaseverticles.RepositoryVerticle;
import com.chatauth.verticles.databaseverticles.UserValidatorVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class Main extends AbstractVerticle {

  public static void main(String[] args) {

    final var vertx = Vertx.vertx();

    final var configStoreOptions = new ConfigStoreOptions().setType("file")
      .setConfig(new JsonObject().put("path", "src/main/resources/config.json"));

    final var options = new ConfigRetrieverOptions()
      .addStore(configStoreOptions);

    ConfigRetriever.create(vertx, options)
      .getConfig()
      .onSuccess(cfg -> {
        final var jdbcClient = JDBCClient.createShared(vertx, cfg);


        jdbcClient.query("SELECT 1", asyncResult -> {
          System.out.println("adwadawdawd");
          if (asyncResult.succeeded()) {
            System.out.println(
              "yle var"
            );
          } else {
            System.out.println("mainc yle var");
          }
        });
        // ping database
        try {
          jdbcClient.getConnection(asyncConnection -> {
            if (asyncConnection.succeeded()) {
              System.out.println("Successfully connected to database");
            } else {
              System.out.println("oeeeeee");
              throw new RuntimeException("Database is dead");
            }
          });
        } catch (Throwable t) {
          t.printStackTrace();
        }

        registerCodecs(vertx);

        // deploy verticles so that they are ready to receive and send messages to each other
        vertx.deployVerticle(new HttpServerVerticle(
          cfg.getInteger("port"),
          cfg.getString("host")
          ));

        vertx.deployVerticle(new RepositoryVerticle(jdbcClient));

        vertx.deployVerticle(new AuthorizationVerticle(new JwtEncoderServiceImpl()));

        vertx.deployVerticle(new UserValidatorVerticle(jdbcClient));

      }).onFailure(System.out::println);
  }

  /**
   * register codecs for sending and receiving messages between verticles
   * codecs are necessary for se/deserializing verticle messages
   */
  private static void registerCodecs(Vertx vertx) {
    vertx.eventBus().registerDefaultCodec(CreateUser.class, new CreateUserCodec());
    vertx.eventBus().registerDefaultCodec(AddUserToDatabase.class,
                                        new AddUserToDatabaseCodec());
    vertx.eventBus().registerDefaultCodec(CheckUserExistenceRequest.class,
                                        new CheckUserExistenceRequestCodec());
    vertx.eventBus().registerDefaultCodec(CreateUserRequest.class,
                                          new CreateUserRequestCodec());
    vertx.eventBus().registerDefaultCodec(UserAlreadyExists.class,
                                          new UserAlreadyExistsCodec());
    vertx.eventBus().registerDefaultCodec(UserCreated.class,
                                          new UserCreatedCodec());
    vertx.eventBus().registerDefaultCodec(UserJWTGenerated.class,
                                          new UserJWTGeneratedCodec());
    vertx.eventBus().registerDefaultCodec(PasswordCheckFailedMessage.class,
                                          new PasswordCheckFailedMessageCodec());
    vertx.eventBus().registerDefaultCodec(LoginRequest.class,
      new LoginRequestCodec());
    vertx.eventBus().registerDefaultCodec(LoginSuccess.class,
      new LoginSuccessCodec());
    vertx.eventBus().registerDefaultCodec(IncorrectPasswordMessage.class,
      new IncorrectPasswordMessageCodec());
  }

}

