package it.unibo.scafi.test

import it.unibo.scafi.test.FunctionalTestIncarnation.Network

enum ScafiTestResult:
  case Success(program: String)
  case CompilationFailed(producedCode: String)
  case TestFailed(producedCode: String, producedNetwork: Network)
  case GenericFailure(exception: Throwable)

final case class SingleTestResult(
    testName: String,
    promptIndex: Int,
    knowledgeFile: String,
    modelUsed: String,
    result: ScafiTestResult,
)
