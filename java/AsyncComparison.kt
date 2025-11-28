import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

// 테스트용 데이터 클래스
data class RequestData(val id: String, val content: String)
data class Response(val result: String)

// MongoDB 저장을 시뮬레이션
class MongoRepository {
    fun save(log: String) {
        Thread.sleep(50) // MongoDB 저장 시뮬레이션
    }
}

// 1. 동기 방식
class SyncService(
    private val mongoRepository: MongoRepository = MongoRepository()
) {
    fun process(data: RequestData): Response {
        validate(data)
        val result = mainLogic(data)

        mongoRepository.save(data.toLog()) // 동기 호출

        return Response(result)
    }

    private fun validate(data: RequestData) {
        Thread.sleep(1)
    }

    private fun mainLogic(data: RequestData): String {
        Thread.sleep(5)
        return "processed-${data.id}"
    }

    private fun RequestData.toLog(): String = "log-$id-$content"
}

// 2. Thread 방식
class ThreadAsyncService(
    private val mongoRepository: MongoRepository = MongoRepository()
) {
    fun process(data: RequestData): Response {
        validate(data)
        val result = mainLogic(data)

        Thread {
            mongoRepository.save(data.toLog())
        }.start()

        return Response(result)
    }

    private fun validate(data: RequestData) {
        Thread.sleep(1)
    }

    private fun mainLogic(data: RequestData): String {
        Thread.sleep(5)
        return "processed-${data.id}"
    }

    private fun RequestData.toLog(): String = "log-$id-$content"
}

// 3. ExecutorService 방식
class ExecutorAsyncService(
    private val mongoRepository: MongoRepository = MongoRepository()
) {
    private val executor = Executors.newFixedThreadPool(10)

    fun process(data: RequestData): Response {
        validate(data)
        val result = mainLogic(data)

        executor.submit {
            mongoRepository.save(data.toLog())
        }

        return Response(result)
    }

    private fun validate(data: RequestData) {
        Thread.sleep(1)
    }

    private fun mainLogic(data: RequestData): String {
        Thread.sleep(5)
        return "processed-${data.id}"
    }

    private fun RequestData.toLog(): String = "log-$id-$content"

    fun shutdown() {
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)
    }
}

// Virtual Thread 방식
class VirtualThreadAsyncService(
    private val mongoRepository: MongoRepository = MongoRepository()
) {
    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    fun process(data: RequestData): Response {
        validate(data)
        val result = mainLogic(data)

        executor.submit {
            mongoRepository.save(data.toLog())
        }

        return Response(result)
    }

    private fun validate(data: RequestData) {
        Thread.sleep(1)
    }

    private fun mainLogic(data: RequestData): String {
        Thread.sleep(5)
        return "processed-${data.id}"
    }

    private fun RequestData.toLog(): String = "log-$id-$content"

    fun shutdown() {
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)
    }
}

// Coroutine 방식
class CoroutineAsyncService(
    private val mongoRepository: MongoRepository = MongoRepository()
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun process(data: RequestData): Response {
        validate(data)
        val result = mainLogic(data)

        coroutineScope.launch(Dispatchers.IO) {
            mongoRepository.save(data.toLog())
        }

        return Response(result)
    }

    private fun validate(data: RequestData) {
        Thread.sleep(1)
    }

    private fun mainLogic(data: RequestData): String {
        Thread.sleep(5)
        return "processed-${data.id}"
    }

    private fun RequestData.toLog(): String = "log-$id-$content"

    fun shutdown() {
        runBlocking {
            coroutineScope.cancel()
            delay(100)
        }
    }
}

// 메인 테스트 함수
fun main(): kotlin.Unit = runBlocking {
    val testData = RequestData("test-1", "sample data")

    println("=== 1. 동기 방식 ===")
    val syncService = SyncService()
    val syncStart = System.currentTimeMillis()
    val syncResult = syncService.process(testData)
    val syncDuration = System.currentTimeMillis() - syncStart
    println("응답 시간: ${syncDuration}ms")
    println("결과: $syncResult")
    println()

    println("=== 2. Thread 비동기 방식 ===")
    val threadService = ThreadAsyncService()
    val threadStart = System.currentTimeMillis()
    val threadResult = threadService.process(testData)
    val threadDuration = System.currentTimeMillis() - threadStart
    println("응답 시간: ${threadDuration}ms")
    println("결과: $threadResult")
    Thread.sleep(100)
    println()

    println("=== 3. ExecutorService 비동기 방식 ===")
    val executorService = ExecutorAsyncService()
    val executorStart = System.currentTimeMillis()
    val executorResult = executorService.process(testData)
    val executorDuration = System.currentTimeMillis() - executorStart
    println("응답 시간: ${executorDuration}ms")
    println("결과: $executorResult")
    executorService.shutdown()
    println()

    println("=== 4. Virtual Thread 비동기 방식 ===")
    val virtualService = VirtualThreadAsyncService()
    val virtualStart = System.currentTimeMillis()
    val virtualResult = virtualService.process(testData)
    val virtualDuration = System.currentTimeMillis() - virtualStart
    println("응답 시간: ${virtualDuration}ms")
    println("결과: $virtualResult")
    virtualService.shutdown()
    println()

    println("=== 5. Coroutine 비동기 방식 ===")
    val coroutineService = CoroutineAsyncService()
    val coroutineStart = System.currentTimeMillis()
    val coroutineResult = coroutineService.process(testData)
    val coroutineDuration = System.currentTimeMillis() - coroutineStart
    println("응답 시간: ${coroutineDuration}ms")
    println("결과: $coroutineResult")
    coroutineService.shutdown()
    println()

    println("=== 대량 요청 테스트 (1000건) ===")

    // Thread 방식
    println("\n[Thread 방식]")
    val threadBulkStart = System.currentTimeMillis()
    repeat(1000) {
        threadService.process(RequestData("bulk-$it", "data"))
    }
    Thread.sleep(3000) // 모든 작업 완료 대기
    val threadBulkDuration = System.currentTimeMillis() - threadBulkStart
    println("총 처리 시간: ${threadBulkDuration}ms")

    Thread.sleep(30000)

    // ExecutorService 방식
    println("\n[ExecutorService 방식]")
    val executorBulkService = ExecutorAsyncService()
    val executorBulkStart = System.currentTimeMillis()
    repeat(1000) {
        executorBulkService.process(RequestData("bulk-$it", "data"))
    }
    executorBulkService.shutdown()
    val executorBulkDuration = System.currentTimeMillis() - executorBulkStart
    println("총 처리 시간: ${executorBulkDuration}ms")

    // Virtual Thread 방식
    println("\n[Virtual Thread 방식]")
    val virtualBulkService = VirtualThreadAsyncService()
    val virtualBulkStart = System.currentTimeMillis()
    repeat(1000) {
        virtualBulkService.process(RequestData("bulk-$it", "data"))
    }
    virtualBulkService.shutdown()
    val virtualBulkDuration = System.currentTimeMillis() - virtualBulkStart
    println("총 처리 시간: ${virtualBulkDuration}ms")

    // Coroutine 방식
    println("\n[Coroutine 방식]")
    val coroutineBulkService = CoroutineAsyncService()
    val coroutineBulkStart = System.currentTimeMillis()
    repeat(1000) {
        coroutineBulkService.process(RequestData("bulk-$it", "data"))
    }
    coroutineBulkService.shutdown()
    val coroutineBulkDuration = System.currentTimeMillis() - coroutineBulkStart
    println("총 처리 시간: ${coroutineBulkDuration}ms")
}