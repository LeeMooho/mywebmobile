package com.example.myweb.api

import com.example.myweb.data.RemoteAuthRepository
import com.example.myweb.data.RemoteBoardRepository
import com.example.myweb.data.SessionStore
import com.example.myweb.data.UserSession
import com.example.myweb.model.LoginRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.Proxy
import java.util.concurrent.TimeUnit

class ApiClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: ApiClient
    private lateinit var transport: OkHttpClient
    private val sessions = SessionStore()
    private lateinit var auth: RemoteAuthRepository

    @Before fun setup() {
        val cert = HeldCertificate.Builder().commonName("localhost").addSubjectAlternativeName("localhost").build()
        val serverTls = HandshakeCertificates.Builder().heldCertificate(cert).build()
        val clientTls = HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
        server = MockWebServer()
        server.useHttps(serverTls.sslSocketFactory(), false)
        server.start()
        transport = OkHttpClient.Builder().proxy(Proxy.NO_PROXY)
            .sslSocketFactory(clientTls.sslSocketFactory(), clientTls.trustManager).build()
        client = ApiClient(sessions, server.url("/").toString(), transport)
        auth = RemoteAuthRepository(client.auth, sessions, client.url.isHttps)
    }

    @After fun teardown() { server.shutdown() }
    private fun respond(body: String, status: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body))
    }
    private fun session() = UserSession("test", "Test", "test-token")

    @Test fun loginUsesServerContractAndBoardIncludesBearerToken() = runTest {
        sessions.signIn(UserSession("old", "Old", "old-token"))
        respond("""{"statusCode":200,"errorCode":0,"item":{"userId":"test","userName":"Test","token":"test-token"}}""")
        assertTrue(auth.login("test", "password") is ApiResult.Success)
        val login = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("POST", login.method)
        assertEquals("/api/user/login", login.path)
        val payload = com.google.gson.JsonParser().parse(login.body.readUtf8()).asJsonObject
        assertEquals(setOf("userId", "userPw"), payload.keySet())
        assertEquals("password", payload["userPw"].asString)
        assertNull(login.getHeader("Authorization"))
        respond("""{"statusCode":200,"errorCode":0,"pageItems":{"content":[{"boardNo":1,"boardTitle":"Hello","boardContent":"Body","boardWriter":"Test","boardRegdate":"2026-10-22"}]}}""")
        val boards = RemoteBoardRepository(client.boards).loadBoards() as ApiResult.Success
        assertEquals("Hello", boards.value.single().boardTitle)
        val request = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("/api/board/board-list?searchCondition=all&searchKeyword=", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    @Test fun boardDetailUsesIdentifierPathAndReturnsFullPost() = runTest {
        sessions.signIn(session())
        respond(
            """{"statusCode":200,"errorCode":0,"item":{"boardNo":42,"boardTitle":"Hello","boardContent":"Full body","boardWriter":"Test","boardRegdate":"2026-10-22"}}"""
        )
        val result = RemoteBoardRepository(client.boards).loadBoard(42) as ApiResult.Success
        assertEquals("Full body", result.value.boardContent)
        val request = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("GET", request.method)
        assertEquals("/api/board/board/42", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    @Test fun boardDetailWithoutItemIsInvalidResponse() = runTest {
        respond("""{"statusCode":200,"errorCode":0}""")
        assertEquals(
            ApiError.INVALID_RESPONSE,
            (RemoteBoardRepository(client.boards).loadBoard(42) as ApiResult.Failure).error
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun boardDetailRejectsNonPositiveIdentifierBeforeRequest() = runTest {
        RemoteBoardRepository(client.boards).loadBoard(0)
    }

    @Test fun invalidCredentialsAreMappedWithoutLeakingServerMessage() = runTest {
        respond("""{"statusCode":400,"errorCode":201,"errorMessage":"wrong password"}""", 400)
        assertEquals(ApiError.INVALID_CREDENTIALS, (auth.login("test", "wrong") as ApiResult.Failure).error)
        assertNull(sessions.session.value)
    }

    @Test fun successWithoutTokenIsRejected() = runTest {
        respond("""{"statusCode":200,"errorCode":0,"item":{"userId":"test"}}""")
        assertEquals(ApiError.INVALID_RESPONSE, (auth.login("test", "password") as ApiResult.Failure).error)
        assertNull(sessions.session.value)
    }

    @Test fun unauthorizedAuthenticatedRequestClearsSession() = runTest {
        sessions.signIn(session())
        respond("{}", 401)
        assertEquals(ApiError.UNAUTHORIZED, (RemoteBoardRepository(client.boards).loadBoards() as ApiResult.Failure).error)
        assertNull(sessions.session.value)
    }

    @Test fun failedLogoutStillClearsLocalSession() = runTest {
        sessions.signIn(session())
        respond("{}", 503)
        assertEquals(ApiError.SERVER, (auth.logout() as ApiResult.Failure).error)
        assertNull(sessions.session.value)
        val request = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("/api/user/logout", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    @Test fun cleartextLoginIsRejectedBeforeSendingCredentials() = runTest {
        val cleartext = RemoteAuthRepository(client.auth, sessions, secureTransport = false)
        assertEquals(ApiError.SECURE_CONNECTION_REQUIRED, (cleartext.login("test", "secret") as ApiResult.Failure).error)
        assertEquals(0, server.requestCount)
    }

    @Test fun malformedJsonIsReportedAsInvalidResponse() = runTest {
        respond("not json")
        assertEquals(ApiError.INVALID_RESPONSE, (RemoteBoardRepository(client.boards).loadBoards() as ApiResult.Failure).error)
    }

    @Test fun emptySuccessfulResponseIsReportedAsInvalidResponse() = runTest {
        respond("")
        assertEquals(ApiError.INVALID_RESPONSE, (RemoteBoardRepository(client.boards).loadBoards() as ApiResult.Failure).error)
    }

    @Test fun missingPageIsReportedAsInvalidResponseInsteadOfEmptyList() = runTest {
        respond("""{"statusCode":200}""")
        assertEquals(ApiError.INVALID_RESPONSE, (RemoteBoardRepository(client.boards).loadBoards() as ApiResult.Failure).error)
    }

    @Test fun envelopeFailureIsNotTreatedAsHttpSuccess() = runTest {
        respond("""{"statusCode":400,"errorCode":201}""")
        assertEquals(ApiError.INVALID_CREDENTIALS, (auth.login("test", "wrong") as ApiResult.Failure).error)
    }

    @Test fun redirectsAreNotFollowed() = runTest {
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", server.url("/redirected")))
        // No second response is enqueued: following the redirect would make this test fail.
        assertEquals(ApiError.SERVER, (auth.login("test", "password") as ApiResult.Failure).error)
        assertEquals(1, server.requestCount)
    }

    @Test fun cancellationIsPropagated() = runTest {
        try {
            apiCall<Any> { throw CancellationException("cancelled") }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    @Test fun networkFailureIsReportedWithoutThrowing() = runTest {
        val result = apiCall<Any> { throw java.io.IOException("offline") }
        assertEquals(ApiError.NETWORK, (result as ApiResult.Failure).error)
    }

    @Test fun cleartextBoardRequestDoesNotSendStoredToken() = runTest {
        MockWebServer().use { http ->
            http.start()
            http.enqueue(MockResponse().setBody("""{"statusCode":200,"errorCode":0,"pageItems":{"content":[]}}"""))
            sessions.signIn(session())
            val cleartext = ApiClient(sessions, http.url("/").toString(),
                OkHttpClient.Builder().proxy(Proxy.NO_PROXY).build())
            assertTrue(RemoteBoardRepository(cleartext.boards).loadBoards() is ApiResult.Success)
            assertNull(http.takeRequest(2, TimeUnit.SECONDS)!!.getHeader("Authorization"))
        }
    }

    @Test fun staleUnauthorizedResponseCannotClearANewerSession() {
        sessions.signIn(UserSession("other", "Other", "new-token"))
        sessions.clearIfTokenMatches("old-token")
        assertEquals("new-token", sessions.session.value?.token)
    }

    @Test fun otherOriginDoesNotReceiveTokenOrClearSessionOnUnauthorizedResponse() {
        sessions.signIn(session())
        val anotherOrigin = server.url("/").newBuilder().host("other.example").build()
        val isolated = transport.newBuilder().addInterceptor(SessionInterceptor(anotherOrigin, sessions)).build()
        respond("{}", 401)
        isolated.newCall(okhttp3.Request.Builder().url(server.url("/resource")).build()).execute().use {
            assertEquals(401, it.code)
        }
        assertNull(server.takeRequest(2, TimeUnit.SECONDS)!!.getHeader("Authorization"))
        assertEquals("test-token", sessions.session.value?.token)
    }

    @Test fun sensitiveObjectsRedactTheirStringRepresentation() {
        assertFalse(LoginRequest("test", "password").toString().contains("password"))
        assertFalse(session().toString().contains("test-token"))
    }
}
