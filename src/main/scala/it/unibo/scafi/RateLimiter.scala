package it.unibo.scafi

import java.util.concurrent.{ ScheduledThreadPoolExecutor, Semaphore, TimeUnit }
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.FiniteDuration

class RateLimiter(maxRequests: Int, every: FiniteDuration)(using ec: ExecutionContext):
  private val semaphore = new Semaphore(maxRequests)
  private val scheduler = new ScheduledThreadPoolExecutor(1)

  scheduler.scheduleAtFixedRate(
    () =>
      val permitsToAdd = maxRequests - semaphore.availablePermits()
      if permitsToAdd > 0 then semaphore.release(permitsToAdd)
    ,
    every.toMillis,
    every.toMillis,
    TimeUnit.MILLISECONDS,
  )

  def acquire(): Future[Unit] =
    Future(semaphore.acquire())

end RateLimiter
