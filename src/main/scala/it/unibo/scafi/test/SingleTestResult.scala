package it.unibo.scafi.test

enum ScafiTestResult:
  case Success(program: String)
  case CompilationFailed(producedCode: String)
  case TestFailed(producedCode: String)
  case GenericFailure(exceptionMessage: String)

final case class SingleTestResult(
    testName: String,
    promptIndex: Int,
    knowledgeFile: String,
    modelUsed: String,
    result: ScafiTestResult,
)
