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

@OptIn(KtorExperimentalLocationsAPI::class)
fun Route.userRoutes(
    generateJwtFunction: (String) -> String,
    hashFunction: (String) -> String
) {
    val userDAO = UserDAO()
    post<UserRegisterRoute> {
        val registerRequest = try {
            call.receive<RegisterRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Missing some fields"))
            return@post
        }

        try {
            // TODO: add user to db
            val eUser = userDAO.findByEmail(registerRequest.email)
            if (eUser == null) {
                val user =
                    User(0, registerRequest.email, hashFunction(registerRequest.password), registerRequest.username)
                userDAO.addNew(user)
                call.respond(HttpStatusCode.Created, DataResponse(true, generateJwtFunction(user.email)))
            } else {
                call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Exit Email"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, DataResponse(false, e.message ?: "Some problem occurred"))
        }
    }

    post<UserLoginRoute> {
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Missing some fields"))
            return@post
        }

        try {
            // TODO: find user in db
//            val user = User(0, "email", "password", "username")
            val user = userDAO.findByEmail(loginRequest.email)
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Wrong Email"))
            } else {
                if (user.hashPassword == hashFunction(loginRequest.password)) {
                    call.respond(HttpStatusCode.OK, DataResponse(true, generateJwtFunction(user.email)))
                } else {
                    call.respond(HttpStatusCode.BadRequest, DataResponse(false, "Password is incorrect"))
                }
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, DataResponse(false, e.message ?: "Some problem occurred"))
        }
    }
}