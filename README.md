# Chatting server by Ktor

Using Ktor for developing http server, template server and websocket server

## Overview

The chatting server with chatting feature can be send text, image, video. Overwide, user can login with jwt authentication.
The system using Jwt authenticate, Json serialize, PostgreSQL database and Expose ORM.

## Socket Ktor

  User A ------connecting------>[socket server]<------connecting------User B
  - When user A open message tab with user B, the client will be send a connection to socket server.
  - This socket server will sign user A to connected list
  - Each message user A send to user B, the socket server will be route message to user B
