package com.example.presentations.routes

import com.example.models.DataResponse
import com.example.models.LoginRequest
import com.example.models.RegisterRequest
import com.example.models.User
import com.example.presentations.dao.UserDAO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val REGISTER_REQUEST = "$USERS/register"
const val LOGIN_REQUEST = "$USERS/login"

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(REGISTER_REQUEST)
class UserRegisterRoute

@KtorExperimentalLocationsAPI
@Location(LOGIN_REQUEST)
class UserLoginRoute

@OptIn(KtorExperimentalLocationsAPI::class, InternalSerializationApi::class)
fun Route.authRoute(
    userDAO: UserDAO,
    generateJwtFunction: (User) -> String,
    hashFunction: (String) -> String
) {

    post<UserRegisterRoute> {
        val bodyRequest = call.receiveText()

        val registerRequest = try {
            Json.decodeFromJsonElement<RegisterRequest>(
                RegisterRequest.serializer(),
                Json.parseToJsonElement(bodyRequest)
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Missing some fields", null))
            return@post
        }

        try {
            // TODO: add user to db
            val eUser = userDAO.findByEmail(registerRequest.email)
            if (eUser == null) {
                val user =
                    User(0, registerRequest.email, hashFunction(registerRequest.password), registerRequest.username)
                userDAO.create(user)
                call.respond(
                    HttpStatusCode.OK,
                    DataResponse(true, "register successfully", generateJwtFunction(user))
                )
            } else {
                call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Exit Email", null))
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Conflict,
                DataResponse(false, e.message ?: "Some problem occurred", null)
            )
        }
    }

    post<UserLoginRoute> {
        val bodyRequest = call.receiveText()

        val loginRequest = try {
            Json.decodeFromJsonElement<LoginRequest>(LoginRequest.serializer(), Json.parseToJsonElement(bodyRequest))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Missing some fields", null))
            return@post
        }

        try {
            // TODO: find user in db
            val user = userDAO.findByEmail(loginRequest.email)
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Wrong Email", null))
            } else {
                if (user.hashPassword == hashFunction(loginRequest.password)) {
                    call.respond(HttpStatusCode.OK, DataResponse(true, "login successfully", generateJwtFunction(user)))
                } else {
                    call.respond(
                        HttpStatusCode.OK, DataResponse(false, "Password is incorrect", null)
                    )
                }
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Conflict,
                DataResponse(false, e.message ?: "Some problem occurred", null)
            )
        }
    }
}