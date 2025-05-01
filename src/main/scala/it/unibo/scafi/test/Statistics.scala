package it.unibo.scafi.test

final case class StatisticResultPerTest(
    testName: String,
    succeeded: Int,
    nonCompiling: Int,
    failed: Int,
    genericErrors: Int,
)

type TestName = String
type ModelName = String
type StatisticsPerModel = Map[TestName, Map[ModelName, StatisticResultPerTest]]
type StatisticsPerTest = Map[TestName, StatisticResultPerTest]

extension (results: List[SingleTestResult])
  private def calculateStatistics(res: Seq[SingleTestResult], testName: String): StatisticResultPerTest =
    StatisticResultPerTest(
      testName = testName,
      succeeded = res.count(_.result.isInstanceOf[ScafiTestResult.Success]),
      nonCompiling = res.count(_.result.isInstanceOf[ScafiTestResult.CompilationFailed]),
      failed = res.count(_.result.isInstanceOf[ScafiTestResult.TestFailed]),
      genericErrors = res.count(_.result.isInstanceOf[ScafiTestResult.GenericFailure]),
    )

  def toResultsPerModelAndKnowledge: Map[(ModelName, String), List[StatisticResultPerTest]] =
    results
      .groupBy(r => (r.modelUsed, r.knowledgeFile))
      .map { case ((modelUsed, knowledgeFile), results) =>
        (modelUsed, knowledgeFile) -> results
          .groupBy(_.testName)
          .map { case (testName, results) =>
            calculateStatistics(results, testName)
          }
          .toList
      }
end extension

//extension (results: Seq[SingleTestResult])
//  private def calculateStatistics(res: Seq[SingleTestResult], testName: String): StatisticResultPerTest =
//    StatisticResultPerTest(
//      testName = testName,
//      succeeded = res.count(_.result.isInstanceOf[ScafiTestResult.Success]),
//      nonCompiling = res.count(_.result.isInstanceOf[ScafiTestResult.CompilationFailed]),
//      failed = res.count(_.result.isInstanceOf[ScafiTestResult.TestFailed]),
//      genericErrors = res.count(_.result.isInstanceOf[ScafiTestResult.GenericFailure]),
//    )
//
//  def toStatisticsPerModel: StatisticsPerModel =
//    results
//      .groupBy(_.testName)
//      .map { case (testName, results) =>
//        testName -> results
//          .groupBy(_.modelUsed)
//          .map { case (modelUsed, results) =>
//            modelUsed -> calculateStatistics(results, testName)
//          }
//      }
//
//  def toStatisticsPerTest: StatisticsPerTest =
//    results
//      .groupBy(_.testName)
//      .map { case (testName, results) =>
//        testName -> calculateStatistics(results, testName)
//      }
//end extension
